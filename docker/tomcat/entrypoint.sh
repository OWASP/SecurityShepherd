#!/bin/sh
# Entrypoint of the Security Shepherd Tomcat image.
#
# The image ships no TLS keystore, so a published image never carries a private key (#991). On the
# first start, when conf/$TLS_KEYSTORE_FILE doesn't exist yet, this generates a self-signed
# certificate there. conf/ is a volume in docker-compose.yml, so the keystore is generated once and
# kept. To use your own certificate, put a PKCS12 keystore at conf/$TLS_KEYSTORE_FILE and set
# TLS_KEYSTORE_PASS and ALIAS to match it.
#
# server.xml reads TLS_KEYSTORE_FILE, TLS_KEYSTORE_PASS, ALIAS and HTTPS_PORT from the environment.
# Without TLS_KEYSTORE_PASS, a random password is generated with the keystore and kept next to it
# in conf/.tls-keystore-pass.
set -eu

# docker-compose.yml passes these through from .env, so a missing .env entry arrives as an empty
# value rather than leaving the image's default in place
TLS_KEYSTORE_FILE=${TLS_KEYSTORE_FILE:-shepherdKeystore.p12}
ALIAS=${ALIAS:-tomcat}
HTTPS_PORT=${HTTPS_PORT:-443}
DNAME=${DNAME:-CN=localhost,OU=SecurityShepherd,O=OWASP}
export TLS_KEYSTORE_FILE ALIAS HTTPS_PORT DNAME

conf="${CATALINA_HOME:-/usr/local/tomcat}/conf"
keystore="$conf/$TLS_KEYSTORE_FILE"
pass_file="$conf/.tls-keystore-pass"

if [ -z "${TLS_KEYSTORE_PASS:-}" ] && [ -f "$pass_file" ]; then
  TLS_KEYSTORE_PASS=$(cat "$pass_file")
fi

if [ ! -f "$keystore" ]; then
  if [ ! -w "$conf" ]; then
    echo "ERROR: $keystore doesn't exist, and $conf isn't writable by $(id -un), so no keystore can be" \
      "generated there. Make $conf writable, or provide a keystore at $keystore." >&2
    exit 1
  fi
  if [ -z "${TLS_KEYSTORE_PASS:-}" ]; then
    TLS_KEYSTORE_PASS=$(head -c 32 /dev/urandom | base64 | tr -dc 'A-Za-z0-9')
    (umask 077 && printf '%s' "$TLS_KEYSTORE_PASS" >"$pass_file")
  fi
  export TLS_KEYSTORE_PASS
  # Written under a temporary name first, so an interrupted start doesn't leave half a keystore
  tmp="$keystore.tmp.$$"
  rm -f "$tmp"
  (umask 077 && keytool -genkeypair -noprompt \
    -alias "$ALIAS" -keyalg RSA -keysize 3072 -validity 825 \
    -dname "$DNAME" -ext "SAN=dns:localhost,ip:127.0.0.1" \
    -storetype PKCS12 -keystore "$tmp" -storepass:env TLS_KEYSTORE_PASS)
  mv "$tmp" "$keystore"
  echo "Generated a self-signed TLS certificate for localhost in $keystore" >&2
elif [ -z "${TLS_KEYSTORE_PASS:-}" ]; then
  echo "WARNING: $keystore exists but TLS_KEYSTORE_PASS is not set. Set it to the keystore's password," \
    "unless conf/server.xml has the password written into it." >&2
fi

if [ -n "${TLS_KEYSTORE_PASS:-}" ]; then
  export TLS_KEYSTORE_PASS
fi

exec "$@"
