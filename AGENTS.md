# AGENTS.md

Instructions for AI coding agents working on this repository.

## Project overview

OWASP Security Shepherd is a web and mobile application security training platform. The web app is a Java servlet application built with Maven, deployed as a WAR on Tomcat. The mobile challenges are independent Android apps under `src/MobileShepherd/`.

## Intentionally vulnerable code — DO NOT FIX

The following paths contain **deliberately insecure code** used for security training. Do not fix, refactor, or flag security vulnerabilities in these files:

- `src/main/java/servlets/module/lesson/` — lesson servlets with intentional vulnerabilities
- `src/main/java/servlets/module/challenge/` — challenge servlets with intentional vulnerabilities
- `src/MobileShepherd/` — mobile challenge apps with intentional vulnerabilities

Security improvements should only be made to the **platform infrastructure** (authentication, session management, admin functions, DB layer, etc.), never to the training content.

## Code style

All Java code MUST be formatted with [Google Java Format](https://github.com/google/google-java-format). CI enforces this via `axel-op/googlejavaformat-action` and will reject unformatted code.

Before committing Java changes, run:

```bash
google-java-format --replace <changed-files>
```

## Java version

The project targets **Java 8**. All code must compile against Java 8, even though CI uses Java 24 for linting. Do not use Java 9+ APIs or language features.

## Build and test

```bash
mvn -Pdocker clean install -DskipTests -B   # build WAR with Docker profile (required before docker compose)
docker compose build                          # build Docker images
mvn test -B                                   # unit tests
mvn verify -DskipUTs=true -DmongoDocker -B   # integration tests (requires MySQL + MongoDB)
```

The `-Pdocker` profile must run before `docker compose build` — it generates SQL scripts, MongoDB init scripts, and TLS keystores.

### Running tests locally

**Always run tests locally before pushing to remote.**

Integration tests require a running database. For quick local test runs, start a plain MariaDB container:

```bash
docker run -d --name secshep_test_db \
  -e MYSQL_ROOT_PASSWORD=CowSaysMoo \
  -e MYSQL_DATABASE=core \
  -p 3306:3306 \
  mariadb:10.6.11
```

The docker-compose `db` service is **not suitable for quick test runs** — it requires `mvn -Pdocker validate` first to generate SQL init scripts, and those scripts run on first container startup to create all challenge schemas.

### Test credentials

Tests read DB connection details from `.env` via dotenv. The key values:

- `TEST_MYSQL_HOST` — default `127.0.0.1`
- `TEST_MYSQL_PORT` — default `3306`
- `TEST_MYSQL_PASSWORD` — must match the password on the running database

The `.env` file ships with `TEST_MYSQL_PASSWORD=password` (used in CI where a separate MySQL service is configured with that password). When running locally against the container above (password `CowSaysMoo`), either:

```bash
# Option 1: override at test time
TEST_MYSQL_PASSWORD=CowSaysMoo mvn test -B

# Option 2: update .env temporarily
```

Do not commit `.env` changes that break CI.

## Git workflow

- Never commit directly to `master` or `dev`
- Branch naming: `dev#{issueNumber}` (e.g. `dev#536`)
- PRs always target `dev`, not `master`

## Project structure

- `src/main/java/` — web app source (servlets, DB layer, utilities)
- `src/main/resources/` — config and challenge properties files
- `src/test/java/` — unit tests
- `src/it/java/` — integration tests (require running DB containers)
- `src/MobileShepherd/` — independent Android apps (Gradle-based, not part of Maven build)

## Database

- MySQL/MariaDB for the core app and SQL-based challenges
- MongoDB for NoSQL challenges
- Each challenge uses isolated DB credentials scoped to its own schema — do not consolidate challenge DB users
- Encoding must be `utf8mb4` (not `utf8`) for full Unicode support
- Password hashing uses Argon2 (requires `libargon2` native library)

## Agent behaviors

### Always run tests before pushing

Run `mvn test -B` locally before pushing any code changes. If integration tests are relevant to your changes, run those too. Do not rely solely on CI to catch failures.

### Bug fixes must include tests

When fixing a bug, always write a test that reproduces the bug first, then apply the fix. The test should fail without the fix and pass with it.

### Keep documentation up to date

When making significant changes (new features, architectural changes, build/config changes, new dependencies), check whether these files need updating:

- `AGENTS.md` (this file)
- `CONTRIBUTING.md`
- `README.md`
- Files in `docs/`

Do not scan these files on every change — only review and update them when your changes would make their content inaccurate.
