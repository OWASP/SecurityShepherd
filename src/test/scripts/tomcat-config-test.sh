#!/usr/bin/env bash
# Checks the Tomcat image built by `docker compose build`: the Dockerfile must have substituted
# every placeholder in docker/tomcat/serverxml.patch, so conf/server.xml carries the configured
# HTTPS port and keystore settings rather than literal HTTPS_PORT / TLS_KEYSTORE_* / ALIAS.
# Run from the project root after `docker compose build`. Reads IMAGE_TOMCAT and HTTPS_PORT from
# the environment, falling back to .env.
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
https_port=$(env_value HTTPS_PORT)
server_xml=$(docker run --rm --entrypoint cat "$image" /usr/local/tomcat/conf/server.xml)

fail() {
  echo "FAIL: $1"
  exit 1
}

if grep -nE 'HTTPS_PORT|TLS_KEYSTORE_FILE|TLS_KEYSTORE_PASS|"ALIAS"' <<<"$server_xml"; then
  fail "unsubstituted placeholder(s) in $image conf/server.xml"
fi
grep -q "redirectPort=\"${https_port}\"" <<<"$server_xml" ||
  fail "HTTP connector does not redirect to HTTPS_PORT=${https_port}"
grep -q 'certificateKeystoreFile="conf/' <<<"$server_xml" ||
  fail "HTTPS connector has no certificateKeystoreFile"

echo "PASS: $image conf/server.xml redirects to port ${https_port} and has no placeholders left"
