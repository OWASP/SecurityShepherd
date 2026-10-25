#!/usr/bin/env bash
# Checks the Tomcat image built by `docker compose build`:
# - the image ships no TLS keystore, so a published image never carries a private key (#991);
# - conf/server.xml takes the HTTPS port and keystore settings from the environment at runtime;
# - on first start the container generates its own keystore in conf/, a different one for every
#   new container, keeps it across restarts, and Tomcat's HTTPS connector starts with it. This is
#   checked with TLS_KEYSTORE_PASS from .env, and without it, when the container generates and
#   stores a password itself and empty settings fall back to the image's defaults.
# Run from the project root after `docker compose build`. Reads IMAGE_TOMCAT and TLS_KEYSTORE_FILE
# from the environment, falling back to .env.
set -euo pipefail

env_value() {
  local name=$1
  if [ -n "${!name:-}" ]; then
    echo "${!name}"
  else
    sed -n "s/^${name}=//p" .env | tail -1
  fi
}
image=$(env_value IMAGE_TOMCAT)
keystore_file=$(env_value TLS_KEYSTORE_FILE)
conf=/usr/local/tomcat/conf

fail() {
  echo "FAIL: $1"
  exit 1
}

containers=()
cleanup() {
  if [ ${#containers[@]} -gt 0 ]; then
    docker rm -f "${containers[@]}" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

# The image itself
keystores=$(docker run --rm --entrypoint sh "$image" -c \
  "find $conf -maxdepth 1 \\( -name '*.p12' -o -name '*.jks' -o -name '*.keystore' -o -name '.tls-keystore-pass' \\)")
[ -z "$keystores" ] || fail "$image ships a keystore or keystore password: $keystores"

server_xml=$(docker run --rm --entrypoint cat "$image" "$conf/server.xml")
# Literal ${...} placeholders, which Tomcat fills in from the environment
# shellcheck disable=SC2016
for placeholder in 'redirectPort="${HTTPS_PORT}"' 'certificateKeystoreFile="conf/${TLS_KEYSTORE_FILE}"' \
  'certificateKeystorePassword="${TLS_KEYSTORE_PASS}"' 'certificateKeyAlias="${ALIAS}"'; do
  grep -qF "$placeholder" <<<"$server_xml" || fail "conf/server.xml has no $placeholder"
done
docker run --rm --entrypoint cat "$image" "$conf/catalina.properties" |
  grep -qx 'org.apache.tomcat.util.digester.PROPERTY_SOURCE=org.apache.tomcat.util.digester.EnvironmentPropertySource' ||
  fail "conf/catalina.properties doesn't enable EnvironmentPropertySource"

# Starting containers
# Waits until the container's HTTPS connector has started for the nth time (default 1), so a check
# after `docker restart` isn't satisfied by the first start's log line
https_started() {
  local container=$1 times=${2:-1} started
  for _ in $(seq 1 90); do
    started=$(docker logs "$container" 2>&1 | grep -cE 'Starting ProtocolHandler \["https-(jsse|openssl)-nio-8443"\]' || true)
    if [ "$started" -ge "$times" ]; then
      return 0
    fi
    if [ "$(docker inspect -f '{{.State.Running}}' "$container")" != "true" ]; then
      break
    fi
    sleep 2
  done
  docker logs "$container" 2>&1 | tail -40
  fail "$container: the HTTPS connector didn't start"
}

keystore_hash() {
  docker exec "$1" sha256sum "$conf/$keystore_file" | cut -d' ' -f1
}

start() {
  local name="shepherd-tomcat-test-$1-$$"
  shift
  containers+=("$name")
  docker run -d --name "$name" "$@" "$image" >/dev/null
  echo "$name"
}

# With TLS_KEYSTORE_PASS, as docker compose passes it from .env
a=$(start envpass --env-file .env)
https_started "$a"
hash_a=$(keystore_hash "$a")
b=$(start envpass2 --env-file .env)
https_started "$b"
[ "$hash_a" != "$(keystore_hash "$b")" ] || fail "two new containers generated the same keystore"
docker restart "$a" >/dev/null
https_started "$a" 2
[ "$hash_a" = "$(keystore_hash "$a")" ] || fail "$a generated a new keystore on restart"

# Without TLS_KEYSTORE_PASS: the container generates a password and stores it next to the keystore.
# Empty values, as docker compose passes a variable missing from .env, fall back to the defaults.
c=$(start nopass -e TLS_KEYSTORE_PASS= -e TLS_KEYSTORE_FILE= -e ALIAS= -e HTTPS_PORT=)
https_started "$c"
mode=$(docker exec "$c" stat -c '%a' "$conf/.tls-keystore-pass")
[ "$mode" = "600" ] || fail "$c: the generated keystore password file has mode $mode, not 600"
hash_c=$(keystore_hash "$c")
docker restart "$c" >/dev/null
https_started "$c" 2
[ "$hash_c" = "$(keystore_hash "$c")" ] || fail "$c generated a new keystore on restart"

echo "PASS: $image ships no keystore, and generates its own on first start, with and without TLS_KEYSTORE_PASS"
