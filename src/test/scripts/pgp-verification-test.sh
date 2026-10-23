#!/usr/bin/env bash
# Checks that the build's PGP verification (pgpverify-maven-plugin, .mvn/pgp-keys.map)
# rejects a tampered OpenSAML artifact and an OpenSAML artifact signed by a key other than
# the pinned Shibboleth release key. Uses a private local repository, so it never touches
# ~/.m2. Run from the project root: bash src/test/scripts/pgp-verification-test.sh
set -euo pipefail

work=$(mktemp -d)
trap 'rm -rf "$work"' EXIT
repo="$work/repository"
mvn_validate() {
  mvn -B validate -Dmaven.repo.local="$repo" "$@" >"$work/build.log" 2>&1
}
fail() {
  echo "FAIL: $1"
  tail -40 "$work/build.log"
  exit 1
}

echo "1. Untampered dependencies verify"
mvn_validate || fail "verification failed on genuine artifacts"

jar=$(find "$repo/org/opensaml/opensaml-core-api" -name 'opensaml-core-api-*.jar' | head -1)
[ -n "$jar" ] || fail "opensaml-core-api jar not found in $repo"

echo "2. OpenSAML pinned to a different key is rejected"
sed 's/^org\.opensaml = .*/org.opensaml = 0x0000000000000000000000000000000000000001/' \
  .mvn/pgp-keys.map >"$work/wrong-key.map"
if mvn_validate -o -Dpgp.keysMap="$work/wrong-key.map"; then
  fail "build passed although OpenSAML is not signed by the pinned key"
fi
grep -E "^\[ERROR\] Not allowed artifact org\.opensaml" "$work/build.log" | head -1 ||
  fail "failure was not the OpenSAML key check"

echo "3. A tampered OpenSAML jar is rejected"
cp "$jar" "$work/original.jar"
printf 'tampered' >>"$jar"
if mvn_validate -o; then
  fail "build passed with a tampered $(basename "$jar")"
fi
grep -E "^\[ERROR\].*opensaml-core-api" "$work/build.log" | head -1 ||
  fail "failure was not the opensaml-core-api signature check"

echo "PASS: PGP verification rejects tampered and wrongly-signed OpenSAML artifacts"
