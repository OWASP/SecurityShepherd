#!/usr/bin/env bash
# Runs Security Shepherd from the Tomcat image, against the MariaDB and Mongo images, and checks it
# works end to end (#1039):
# - Tomcat deploys the WAR, serves HTTPS and redirects HTTP to HTTPS, and a fresh install
#   redirects to setup.jsp;
# - setup configures the databases, as AGENTS.md's curl steps do;
# - the seeded admin logs in and changes the temporary password, which verifies and creates Argon2
#   hashes in native code (argon2-jvm loads its bundled libargon2 through JNA, which needs a libc
#   and a temp directory it can load a library from);
# - a lesson loads through getModule, and the SQL injection lesson queries its own schema with its
#   own database user;
# - Tomcat logs no SEVERE errors.
#
# Run from the project root after `docker compose build`. Tests the images that docker-compose.yml
# names (IMAGE_TOMCAT, IMAGE_MARIADB and IMAGE_MONGO, from the environment or .env), and never
# builds or pulls one. Docker Compose reads .env itself, and setup uses the root password the
# MariaDB container was started with, so this script parses nothing from .env. The stack runs as its own
# compose project, with its own container names, volumes and host ports (SMOKE_HTTP_PORT,
# SMOKE_HTTPS_PORT, SMOKE_DB_PORT and SMOKE_MONGO_PORT), so it leaves a running secshep_* stack
# alone. Everything it starts is removed on exit.
set -euo pipefail

# Overrides what docker-compose.yml reads from .env, so nothing clashes with the default stack
project=shepherd-smoke-$$
export CONTAINER_TOMCAT=$project-web CONTAINER_MARIADB=$project-db CONTAINER_MONGO=$project-mongo
export HTTP_PORT=${SMOKE_HTTP_PORT:-18080} HTTPS_PORT=${SMOKE_HTTPS_PORT:-18443}
export DB_PORT_MAPPED_HOST=${SMOKE_DB_PORT:-13307} MONGO_PORT_MAPPED_HOST=${SMOKE_MONGO_PORT:-27018}
web=$CONTAINER_TOMCAT
base=https://localhost:$HTTPS_PORT

work=$(mktemp -d)
jar=$work/cookies
compose() {
  docker compose -p "$project" "$@"
}

# Tomcat's own SEVERE lines. Exceptions thrown by servlets go to logs/localhost.<date>.log, not to
# the console, so this reads the log files as well as the container's output.
tomcat_severe() {
  {
    docker logs "$web" 2>&1
    docker exec "$web" sh -c 'cat /usr/local/tomcat/logs/*.log' 2>/dev/null
  } | grep -E -A8 '^[0-9]{2}-[A-Za-z]{3}-[0-9]{4} [0-9:.]+ SEVERE ' || true
}

diagnostics() {
  echo "---- $web output ----"
  docker logs "$web" 2>&1 | tail -60 || true
  echo "---- Tomcat SEVERE log lines ----"
  tomcat_severe | tail -60
}

reported=false
fail() {
  echo "FAIL: $1"
  diagnostics
  reported=true
  exit 1
}

# A command that fails outside fail(), such as a curl request that times out, ends the script
# through set -e. Report it with the diagnostics too, before the containers and their logs go.
failed_line=
trap 'failed_line=$LINENO' ERR
cleanup() {
  local status=$?
  if [ "$status" -ne 0 ] && ! $reported; then
    echo "FAIL: exited with status $status${failed_line:+ at line $failed_line}"
    diagnostics
  fi
  compose down -v --remove-orphans >/dev/null 2>&1 || true
  rm -rf "$work"
}
trap cleanup EXIT

# curl against the stack. The certificate is the container's own self-signed one.
request() {
  curl -k -sS --max-time 60 -b "$jar" -c "$jar" "$@"
}

# "<HTTP status> <redirect location>"
status_of() {
  request -o /dev/null -w '%{http_code} %{redirect_url}' "$@"
}

# The CSRF token the login sets as a cookie
token() {
  awk '$6 == "token" { print $7 }' "$jar"
}

login() {
  rm -f "$jar"
  status_of --data-urlencode "login=$1" --data-urlencode "pwd=$2" "$base/login"
}

echo "Starting compose project $project"
compose up -d --no-build --pull never db mongo web >"$work/up.log" 2>&1 || {
  cat "$work/up.log"
  fail "docker compose up failed"
}
tomcat_image=$(docker inspect -f '{{.Config.Image}}' "$web")
for container in "$web" "$CONTAINER_MARIADB" "$CONTAINER_MONGO"; do
  echo "  $container: $(docker inspect -f '{{.Config.Image}} ({{.Image}})' "$container")"
done

# 1. Tomcat is up and the app deployed
code=000
for _ in $(seq 1 90); do
  code=$(curl -k -s -o /dev/null -w '%{http_code}' --max-time 5 "$base/login.jsp" || true)
  if [ "$code" != 000 ]; then
    break
  fi
  if [ "$(docker inspect -f '{{.State.Running}}' "$web")" != "true" ]; then
    fail "$web stopped"
  fi
  sleep 2
done
[ "$code" != 000 ] || fail "$base didn't answer"
status=$(status_of "$base/login.jsp")
[ "$status" = "302 $base/setup.jsp" ] || fail "a fresh install answered '$status' to login.jsp, not a redirect to setup.jsp"
status=$(curl -sS -o /dev/null -w '%{http_code} %{redirect_url}' --max-time 30 "http://localhost:$HTTP_PORT/login.jsp")
[ "$status" = "302 $base/login.jsp" ] || fail "HTTP answered '$status', not a redirect to $base/login.jsp"
echo "ok: HTTPS serves the app, HTTP redirects to HTTPS, and a fresh install redirects to setup.jsp"

# 2. Setup. The MariaDB image creates its schemas on first start, with networking off until that
# is done, so it accepts connections from Tomcat only once it is ready.
for target in "$CONTAINER_MARIADB:3306" "$CONTAINER_MONGO:27017"; do
  ready=false
  for _ in $(seq 1 90); do
    if docker exec "$web" bash -c "exec 3<>/dev/tcp/${target%:*}/${target#*:}" 2>/dev/null; then
      ready=true
      break
    fi
    sleep 2
  done
  $ready || fail "$web can't reach $target"
done
auth=$(docker exec "$web" cat /usr/local/tomcat/conf/SecurityShepherd.auth) ||
  fail "there's no setup auth token in conf/SecurityShepherd.auth"
# docker-compose.yml starts MariaDB with MYSQL_ROOT_PASSWORD=${DB_PASS}
db_pass=$(docker exec "$CONTAINER_MARIADB" printenv MYSQL_ROOT_PASSWORD) ||
  fail "$CONTAINER_MARIADB has no MYSQL_ROOT_PASSWORD"
# Sends every field the setup form sends
setup=$(request --data-urlencode "dbhost=$CONTAINER_MARIADB" --data-urlencode "dbport=3306" \
  --data-urlencode "dbuser=root" --data-urlencode "dbpass=$db_pass" \
  --data-urlencode "dboverride=override" --data-urlencode "dbauth=$auth" \
  --data-urlencode "mhost=$CONTAINER_MONGO" --data-urlencode "mport=27017" \
  --data-urlencode "enableMongoChallenge=enable" --data-urlencode "unsafeLevels=disable" \
  "$base/setup")
if ! grep -q "Database Configuration Complete" <<<"$setup" || ! grep -q "Database was overwritten" <<<"$setup"; then
  fail "setup failed: $setup"
fi
echo "ok: setup configured MariaDB and Mongo"

# 3. The login page
page=$(request "$base/login.jsp")
if ! grep -q 'name="login"' <<<"$page" || ! grep -q 'name="pwd"' <<<"$page"; then
  fail "login.jsp has no login form"
fi
echo "ok: login.jsp shows the login form"

# 4. and 5. Logins, which check the password against its Argon2 hash, and a password change, which
# hashes the new password. The seeded admin's password is temporary.
status=$(login admin password)
[ "$status" = "302 $base/index.jsp" ] || fail "admin couldn't log in with the seeded password: '$status'"
[ -n "$(token)" ] || fail "the login set no CSRF token cookie"
new_password=Smoke-$RANDOM-$RANDOM-$RANDOM
request -o /dev/null --data-urlencode "csrfToken=$(token)" --data-urlencode "currentPassword=password" \
  --data-urlencode "newPassword=$new_password" --data-urlencode "passwordConfirmation=$new_password" \
  "$base/passwordChange"
request -o /dev/null "$base/logout?csrfToken=$(token)"
status=$(login admin password)
[ "$status" = "302 $base/login.jsp" ] || fail "the old password got '$status' after the password change, not a redirect to login.jsp"
status=$(login admin "$new_password")
[ "$status" = "302 $base/index.jsp" ] || fail "admin couldn't log in with the new password: '$status'"
echo "ok: admin logs in, and changes the temporary password"

# 6. Module pages: the SQL injection lesson
lesson_id=408610f220b4f71f7261207a17055acbffb8a747
lesson_hash=e881086d4d8eb2604d8093d93ae60986af8119c4f643894775433dbfb6faa594
address=$(request --data-urlencode "csrfToken=$(token)" --data-urlencode "moduleId=$lesson_id" "$base/getModule")
[ "$address" = "lessons/$lesson_hash.jsp" ] || fail "getModule returned '$address' for the SQL injection lesson"
page=$(request "$base/$address")
grep -q 'aUserName' <<<"$page" || fail "$address has no lesson form"
result=$(request --data-urlencode "aUserName='or'1'='1" "$base/lessons/$lesson_hash")
grep -q "Lesson Completed" <<<"$result" || fail "the SQL injection lesson didn't return its result: $result"
echo "ok: the SQL injection lesson loads and queries its database"

# 7. Tomcat's log
[ -z "$(tomcat_severe)" ] || fail "Tomcat logged SEVERE errors"
echo "ok: Tomcat logged no SEVERE errors"

echo "PASS: $tomcat_image sets up, logs in, changes a password and serves a database-backed lesson"
