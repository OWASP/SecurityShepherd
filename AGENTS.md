# AGENTS.md

Instructions for AI coding agents working on this repository.

## Project overview

OWASP Security Shepherd is a web and mobile application security training platform. The web app is a Java servlet application built with Maven, deployed as a WAR on Tomcat. The mobile challenges are independent Android apps under `mobile/MobileShepherd/`.

## Intentionally vulnerable code — DO NOT FIX

The following paths contain **deliberately insecure code** used for security training. Do not fix, refactor, or flag security vulnerabilities in these files:

- `src/main/java/servlets/module/lesson/` — lesson servlets with intentional vulnerabilities
- `src/main/java/servlets/module/challenge/` — challenge servlets with intentional vulnerabilities
- `mobile/MobileShepherd/` — mobile challenge apps with intentional vulnerabilities

Security improvements should only be made to the **platform infrastructure** (authentication, session management, admin functions, DB layer, etc.), never to the training content.

## Code style

All Java code MUST be formatted with [Google Java Format](https://github.com/google/google-java-format), bound to the Maven build via [Spotless](https://github.com/diffplug/spotless) and pinned in `pom.xml`. CI enforces this with `mvn spotless:check` (the `lint-java` job) and will reject unformatted code.

Before committing Java changes, run:

```bash
mvn spotless:apply
```

## Java version and platform

The project targets **Java 17** (`<release>17</release>` in `pom.xml`). Java 17 APIs and language features are fine; do not use anything newer.

The web app runs on **Jakarta EE 11**: Servlet 6.1 on **Tomcat 11** (`TOMCAT_DOCKER_VERSION` in `.env`).

- Use `jakarta.servlet.*`, `jakarta.mail.*`, etc. Never add `javax.servlet` or other Java EE `javax.*` dependencies; they will not run on Tomcat 11. JDK packages that keep the `javax` name (`javax.crypto`, `javax.xml.parsers`, `javax.sql`) are fine.
- JSTL taglibs use the `jakarta.tags.*` URIs (declared once in `src/main/webapp/translation.jsp`).
- Spring 7 is used in two places:
  - **Runtime:** pac4j-saml pulls in Spring core, beans, JDBC, TX and ORM at compile scope, and `utils/SamlSso.java` uses spring-core's `ByteArrayResource`. Don't remove or test-scope these, or SSO breaks in the WAR.
  - **Tests:** `spring-test`'s `MockHttpServletRequest` / `MockHttpServletResponse` / `MockServletConfig`. `spring-web` and `spring-context` are test-scoped and only support those mocks. Never declare `spring-core` test-scoped: a test-scoped direct declaration overrides pac4j's compile-scope copy and drops it from the WAR.
  - The version is set once by the `spring.version` property, which also drives the `spring-framework-bom` import that pins every Spring module, direct or transitive.

## Build and test

```bash
mvn -Pdocker clean install -DskipTests -B   # build WAR with Docker profile (required before docker compose)
docker compose build --pull --no-cache web    # build the Tomcat image with the latest base image and Ubuntu updates
docker compose build                          # build the other Docker images
mvn test -B                                   # unit tests
mvn verify -DskipUTs=true -DmongoDocker -B   # integration tests (requires MySQL + MongoDB)
```

The `-Pdocker` profile must run before `docker compose build` — it generates SQL scripts and MongoDB init scripts. The Tomcat image has no TLS keystore: `docker/tomcat/entrypoint.sh` generates one in the `conf` volume on the container's first start, and `server.xml` reads the TLS settings from the environment (#991).

The Tomcat image runs `apt-get upgrade` over its base image (#1038). Docker caches both that layer and the base image, so a plain `docker compose build` can silently produce an image without the latest Ubuntu security updates. Build it with `docker compose build --pull --no-cache web` whenever the image is tested or scanned. CI builds without a layer cache, so it always gets the updates.

### Running tests locally

**Always run tests locally before pushing to remote.**

Integration tests require a running database. For quick local test runs, start a plain MariaDB container:

```bash
docker run -d --name secshep_test_db \
  -e MYSQL_ROOT_PASSWORD=CowSaysMoo \
  -e MYSQL_DATABASE=core \
  -p 3306:3306 \
  mariadb:12.3
```

The docker-compose `db` service is **not suitable for quick test runs** — it requires `mvn -Pdocker generate-sources` (or the full `mvn -Pdocker clean install -DskipTests` build) first to generate SQL init scripts, and those scripts run on first container startup to create all challenge schemas. `mvn -Pdocker validate` is not enough: it only copies the SQL files, and the `DELIMITER` lines are uncommented in `generate-sources`.

### Running the full CI pipeline locally

Before pushing, run every step of `.github/workflows/test.yml` in the same order, not just the unit tests. Running only `mvn test` misses compile failures on other branches, Docker image problems and integration-test failures, and those then show up in CI instead. In a stack of PRs, run them on each branch whose content changes (at least the bottom branch you changed and the top of the stack).

Use JDK 17, with `JAVA_HOME` pointing at it. Spotless fails on newer JDKs.

Run the pipeline in a throwaway worktree of the exact commit you will push, for example `git worktree add --detach ../shepherd-ci HEAD`, then `cd` into it and remove it afterwards with `git worktree remove --force ../shepherd-ci`. Running Maven in your main checkout while the pipeline runs rebuilds `target/` under it and breaks the run. The worktree also keeps the `moduleSchemas.sql` edit below out of your checkout.

```bash
mvn spotless:check -B                                # lint-java
mvn clean install -Pdocker -DskipTests -B            # build
bash src/test/scripts/pgp-verification-test.sh       # build
docker compose build --pull --no-cache web           # build (CI has no layer cache)
docker compose build                                 # build
bash src/test/scripts/tomcat-config-test.sh          # build
bash src/test/scripts/image-smoke-test.sh            # build
TEST_MYSQL_PASSWORD=CowSaysMoo mvn test -B -e        # unit-tests
```

When a change touches `docs/` or the site configuration, also run the `Docs` workflow's build (`.github/workflows/docs.yml`):

```bash
uv sync --locked --no-build
uv run --locked --no-sync python .github/scripts/generate-level-catalogue.py --check
uv run --locked --no-sync zensical build --clean --strict
uv run --locked --no-sync python .github/scripts/check-docs-site.py
```

For the integration tests, start the `secshep_test_db` container above, then repeat what CI does before running them:

```bash
# CI rewrites the challenge DB users to accept connections from any host. The test DB runs in Docker,
# so without this the challenge tests fail with "Access denied for user 'csrfChalFour'@'192.168.65.1'".
# Never commit this change.
sed -i.bak "s/'@'localhost'/'@'%'/g" src/main/resources/database/moduleSchemas.sql
docker ps -aq --filter 'name=^mongo-[0-9]+$' | xargs -r docker rm -f   # leftovers hold port 27017
mvn -Pdocker validate -B
TEST_MYSQL_PASSWORD=CowSaysMoo mvn verify -DskipUTs=true -DmongoDocker -B
```

`image-smoke-test.sh` runs the app from the images that `docker compose build` just built: it runs setup, logs in, changes the admin password (native Argon2) and loads a database-backed lesson. It starts them as a compose project of its own, with its own container names, volumes and host ports (18080, 18443, 13307, 27018; override with `SMOKE_HTTP_PORT`, `SMOKE_HTTPS_PORT`, `SMOKE_DB_PORT`, `SMOKE_MONGO_PORT`), and removes them afterwards, so a running `secshep_*` stack is left alone. On failure it prints Tomcat's output and SEVERE log lines.

Each integration-test run overwrites the local `mongo:4.1.13` tag (see "Integration tests fail building the Mongo image" below). Restore the official image before `docker compose build` and before every integration-test run, not only after a failure.

When a step fails, look at the full Maven output for the root cause. `-q` and filtered output can hide it.

Only infrastructure outages (Docker Hub errors, `build.shibboleth.net` timeouts) are acceptable reasons to learn about a failure from CI rather than locally.

### Docker DB init fails with SQL syntax error

If the MariaDB container exits immediately with `ERROR 1064 (42000) at line 181` referencing a `CREATE PROCEDURE` statement, the image was built from SQL scripts whose `DELIMITER` lines were never uncommented. It is not a bug in the SQL. There are two causes: the scripts were generated with `mvn -Pdocker validate` alone, or Docker reused a **stale image cache**.

The SQL source files have `DELIMITER` statements commented out (for compatibility with tools that don't support `DELIMITER`). A build script (`docker/scripts/convert-sql-scripts.sh`) uncomments them when Maven copies the files to `docker/mariadb/target/`. If Docker reuses a cached image from before the conversion ran, the procedures fail to parse.

Fix: `mvn -Pdocker generate-sources`, then `docker compose build --no-cache db && docker compose up -d db`

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

### Upgrading a local Docker environment to Tomcat 11

The Tomcat container mounts a named `conf` volume over `/usr/local/tomcat/conf`. A volume created by an older Tomcat 9/10.1 image keeps its old `server.xml` and `web.xml`, and Tomcat 11 will not see the new image's config, so the volume has to be recreated. Data volumes are unaffected.

The volume also holds the TLS keystore and `database.properties` / `mongo.properties`. Back it up before removing it. The archive contains the TLS private key and the database password, so create it mode 600 in a mode-700 directory **outside the repository**. The repository root is the Tomcat image's Docker build context, and a root-owned archive there breaks the next build. Run `mkdir -p ~/shepherd-conf-backup && chmod 700 ~/shepherd-conf-backup`, then `docker run --rm -v securityshepherd_conf:/conf -v ~/shepherd-conf-backup:/backup alpine sh -c 'umask 077 && touch /backup/shepherd-conf-backup.tgz && chmod 600 /backup/shepherd-conf-backup.tgz && tar czf /backup/shepherd-conf-backup.tgz -C /conf .'`, and delete the directory after the upgrade. Then run `docker compose down && docker volume rm securityshepherd_conf`, `docker compose build --pull --no-cache web` and `docker compose up -d --build` (detached, so the databases stay up while files are restored and `web` is restarted). Restore only customised keystore or `.properties` files from the backup. Never restore the old `server.xml` / `web.xml`: re-apply any hand edits to the new Tomcat 11 files instead. The full steps are in `docs/upgrading.md`.

### Integration tests fail building the Mongo image (`chown ... Operation not permitted`)

The `it-test` profile (`-DmongoDocker`) tags the image it builds as `mongo:${MONGODB_VERSION}`. That overwrites the official image's local tag, so the next build uses the previous custom image (which runs as `mongodb`) as its base. Restore the official image with `docker rmi mongo:4.1.13 && docker pull mongo:4.1.13`. CI starts from a clean Docker and is unaffected.

### First-time app setup

After `docker compose up`, the app redirects to `https://localhost/setup.jsp` for initial database configuration. **Ask the user before performing setup** — they may prefer to configure it themselves via the browser. If they ask you to do it:

1. Get the auth token:
   ```bash
   docker exec secshep_tomcat cat /usr/local/tomcat/conf/SecurityShepherd.auth
   ```
2. Submit the setup via curl (the TLS cert is self-signed, use `-k`):
   ```bash
   curl -k -s -X POST https://localhost/setup \
     -d "dbhost=secshep_mariadb" \
     -d "dbport=3306" \
     -d "dbuser=root" \
     -d "dbpass=CowSaysMoo" \
     -d "dboverride=override" \
     -d "dbauth=<AUTH_TOKEN>" \
     -d "mhost=secshep_mongo" \
     -d "mport=27017"
   ```

The setup servlet parameter names (from `Setup.java`) are:
- `dbhost`, `dbport`, `dbuser`, `dbpass` — MySQL/MariaDB connection
- `dboverride` — set to `override` to reinitialize schemas
- `dbauth` — the auth token (NOT `authToken`)
- `mhost`, `mport` — MongoDB connection (required, even if not using mongo challenges)

The hostname must be the **Docker container name** (e.g. `secshep_mariadb`), not `localhost`, since the Tomcat container connects over the Docker network.

## Git workflow

- Never commit directly to `master` or `dev`
- Branch naming: `dev#{issueNumber}` (e.g. `dev#536`)
- PRs always target `dev`, not `master`

## Versioning and releases

- [Semantic Versioning 2.0.0](https://semver.org/). Release tags are plain `X.Y.Z` (e.g. `4.0.1`), with no `v` prefix, pre-release suffix or build metadata. A tag ruleset rejects any other format.
- `pom.xml` on `dev` is always `X.Y.Z-SNAPSHOT`. Don't change the version as part of unrelated work.
- Tags can't be moved or deleted, and only admins can create them. Never create or push tags unless the user explicitly asks.
- The release steps are in `docs/releasing.md`.

## Project structure

- `src/main/java/` — web app source
  - `dbProcs/` — database layer: `Database.java` (connection pooling), `Getter.java` (read queries), `Setter.java` (write queries), `MongoDatabase.java`, `Constants.java`, `FileInputProperties.java`
  - `servlets/` — platform servlets (Login, Register, Setup, etc.)
  - `servlets/admin/` — admin servlets (`config/`, `moduleManagement/`, `userManagement/`)
  - `servlets/module/` — module framework (`SolutionSubmit.java`, `GetModule.java`, etc.) plus `challenge/` and `lesson/` (intentionally vulnerable — do not fix)
  - `utils/` — shared utilities (validation, hashing, scoring, XSS/SQL filters, SAML SSO via `SamlSso`)
- `src/main/resources/` — config and challenge properties files
- `src/test/java/` — unit tests
- `src/it/java/` — integration tests (require running DB containers)
- `mobile/MobileShepherd/` — independent Android apps (Gradle-based, not part of Maven build)
- `docs/` — documentation, published to https://owasp.github.io/SecurityShepherd/ (see "Documentation site" below)

### Documentation site

The documentation site is built from `docs/` with [Zensical](https://zensical.org/) by `.github/workflows/docs.yml`, and published to GitHub Pages from `dev` until 4.0.0 is released, then from `master` (#1070). The GitHub wiki describes 3.1 and is being retired.

- `zensical.toml` holds the site configuration and the navigation. A new page in `docs/` must be added to `nav` there.
- `pyproject.toml` pins Zensical, and `uv.lock` pins every dependency with its sha256 hash. Preview the site with `uv run zensical serve`. To update Zensical, change its exact version in `pyproject.toml`, run `uv lock`, and commit both files (`uv lock --upgrade-package zensical` alone can't move an exact pin). Dependabot's uv updates do both. CI installs with `uv sync --locked --no-build`: it fails if the lock is stale and never builds a package from source.
- The `docs-build` job runs on every PR that changes the site (`docs/`, the site configuration or the workflow), and the site is only deployed if it passes. It fails on:
  - a link to a page or anchor that doesn't exist (`zensical build --strict`);
  - a `nav` entry whose file doesn't exist, a page in `docs/` that isn't in `nav`, or a link to an image or other file that doesn't exist (`.github/scripts/check-docs-site.py`).
- Write GitHub-flavoured Markdown that reads the same on github.com and on the site. Link to other pages in `docs/` with relative links to the `.md` file. Link to anything outside `docs/` with an absolute `https://github.com/OWASP/SecurityShepherd/blob/dev/…` URL, because relative `../` links break on the site.
- `docs/docker-hub-overview.md` is the Docker Hub overview, not a site page, and is excluded in `zensical.toml`.
- **The level catalogue is generated.** The tables in `docs/levels.md` and `docs/mobile-levels.md`, between the `BEGIN GENERATED` and `END GENERATED` markers, come from the `INSERT INTO modules` lines in `coreSchema.sql` and each topic's OWASP Top 10 category and CWE in `.github/scripts/level-classification.toml`. Run `uv run --locked --no-sync python .github/scripts/generate-level-catalogue.py` after changing either, and commit the result. The `docs-build` job runs it with `--check` and fails if the pages are stale. A new topic needs an entry in the TOML file.
- **Diagrams must be readable at page width.** Mermaid scales a diagram down to the content width (about 690 px), and the site can't zoom, so a wide diagram's text becomes unreadable. Sequence diagrams have at most four or five participants and short message labels. Flowcharts are `flowchart TB` unless they still read well left to right, with about eight or nine boxes at most, each a name plus a short detail, never a full path. Don't label arrows: Mermaid draws a box behind each label, visible in dark mode, and bends the arrow around it. Use `subgraph` groups only when needed, because groups of different widths knock boxes out of line and bend the arrows. Put the detail in the boxes or the surrounding text. Before finishing, render every changed diagram in the built site in light and dark mode at desktop width, and check its text is about page size and its arrows are straight apart from branches. The full rules are in CONTRIBUTING.md's "Writing Documentation" section.
- **Mermaid is self-hosted.** The theme loads Mermaid from unpkg.com unless a `mermaid` global exists, so `docs/assets/javascripts/mermaid/mermaid.min.js` (12.2.0, with its MIT `LICENSE`) is loaded on every page through `extra_javascript` in `zensical.toml`. Dependabot doesn't update it. To update it, download the npm tarball for the new version, check it against the `dist.integrity` sha512 in `https://registry.npmjs.org/mermaid/<version>`, copy `package/dist/mermaid.min.js` and `package/LICENSE`, update the version in `zensical.toml`'s comment and here, and check a page with a diagram renders, with no request to unpkg.com.
- **The published site and the repo files are separate.** `docs/docker-hub-overview.md` isn't on the site, so it links to it instead of repeating it. `README.md` and `CONTRIBUTING.md` stand apart from the site. `CONTRIBUTING.md` links out for detail, but its setup section is self-contained, because contributors arrive there from GitHub. `README.md` points to that section for the prerequisites and keeps the short build commands. The prerequisites live in `docs/getting-started.md` and that section, and the build commands in `README.md` and `docs/getting-started.md`, so change both copies together, and keep them free of commands for one operating system. Only `docs/manual-setup.md`, which is written for Ubuntu, names a package. Pages for people running Security Shepherd build with `mvn -Pdocker clean package`, which stops before Spotless's `verify` phase and so works on JDK 27. Pages for contributors use `install` and need JDK 17.

## Database

- MySQL/MariaDB for the core app and SQL-based challenges
- MongoDB for NoSQL challenges
- Each challenge uses isolated DB credentials scoped to its own schema — do not consolidate challenge DB users
- Encoding must be `utf8mb4` (not `utf8`) for full Unicode support
- Password hashing uses Argon2 (requires `libargon2` native library)

### SAML single sign-on

SSO is optional, and is turned on by `sso.enabled=true` in `sso.properties`. It is implemented in `utils/SamlSso.java` on **pac4j-saml 6.x** (OpenSAML 5). The five servlets `SSOLogin`, `ACS`, `SLS`, `SSOMetadata` and `Logout` delegate to it.

- Configuration lives in `onelogin.saml.properties`, which keeps the key names of the java-saml toolkit used previously; see `src/main/resources/onelogin.saml.properties.sample`. The SP's PEM certificate and key are loaded into an in-memory keystore. The IdP settings are turned into an IdP metadata document. Nothing is written to disk.
- `sso.properties` maps IdP attributes to users and roles (`utils/SsoUserMapper.java`). Blank identifiers are rejected.
- The SSO endpoints answer 404 unless `sso.enabled=true`, even when a SAML configuration is present.
- **Only SP-initiated login is supported.** `utils/SamlRequestStore.java` binds each AuthnRequest to the browser through a cookie of its own, `shepherd_saml_request_<hash of the request ID>` (HttpOnly, Secure, SameSite=None, scoped to the ACS URL's directory such as `/saml/`, deleted once the login completes), so logins started in several tabs at once all work. Responses without a matching `InResponseTo` from that browser are refused, which blocks login CSRF, and so are unsolicited IdP-initiated responses. The bearer `SubjectConfirmationData` inside the signed assertion must name the same request, because pac4j checks only the Response's `InResponseTo`, which is unsigned unless the whole message is signed (`SamlSso.AlternativeAuthnContextClient`).

### Dependency trust: OpenSAML and PGP verification

pac4j-saml depends on **OpenSAML 5, which is not on Maven Central**. Shibboleth, its maintainer, publishes it only to its own repository (`build.shibboleth.net`, declared in `pom.xml` with checksum failures enforced). Three controls apply:

- **Repository filtering** (`.mvn/maven.config`, `.mvn/rrf/groupId-shibboleth-releases.txt`): the Shibboleth repository may serve only the `org.opensaml` and `net.shibboleth` groupIds. Everything else comes from Central only.
  - This needs **Maven 3.9.2 or newer**: older versions don't expand the path in `.mvn/maven.config` and silently skip the filter. `maven-enforcer-plugin` refuses to build on older Maven.
  - The enforcer itself resolves dependencies before it fails, so if an older Maven was ever used on a machine, delete its local repository (`~/.m2/repository`) before building again.
- **PGP signature verification** (`pgpverify-maven-plugin` in `pom.xml`, rules in `.mvn/pgp-keys.map`): every dependency's signature is checked on every build. There are no `noSig` exceptions; don't add one without reviewing why the artifact is unsigned.
  - `org.opensaml` / `net.shibboleth` are pinned to the Shibboleth release key `B5B5DD332142AD657E8D87AC7D27E610B8A3DC52`, from https://shibboleth.net/downloads/PGP_KEYS. Verify any new key there before changing the pin.
  - Every other groupId is listed by name and needs a valid signature. A new dependency from an unlisted groupId fails with `Not allowed artifact`: review it, then add its groupId.
  - **Never add a wildcard rule** (`*`, `org.*`, …): any matching rule lets an artifact pass, so a broad rule would silently undo the Shibboleth pin.
  - Keys are fetched from `keys.openpgp.org` / `keyserver.ubuntu.com` and cached in the local repository.
- **Test:** `bash src/test/scripts/pgp-verification-test.sh` (run in CI's build job) proves three things on the Maven that runs it: the repository filter is enforced, a tampered OpenSAML jar fails the build, and an OpenSAML artifact signed by another key fails the build.

A build failing with `Could not find artifact org.opensaml:...` / `build.shibboleth.net failed to respond` means that repository was unreachable. Re-run the build.
- Tests generate their own key pairs and signed SAML responses (`src/test/java/utils/SamlTestSupport.java`). Do not commit private keys.

### SQL schema files

- `src/main/resources/database/coreSchema.sql` — **source of truth** for the core database: users table, settings table, stored procedures (user management, authentication, class management, scoring), and seed data (default admin user, default settings)
- `src/main/resources/database/moduleSchemas.sql` — challenge and lesson schemas with intentionally vulnerable data (treat as training content, not infrastructure)
- `docker/mariadb/target/` — **build-generated copies** produced by `mvn -Pdocker`; not source of truth, do not edit directly

The `settings` table stores admin-configurable options as key-value pairs (`setting` VARCHAR, `value` VARCHAR). Known keys: `adminCheatsEnabled`, `playerCheatsEnabled`, `moduleLayout`, `enableFeedback`, `openRegistration`, `scoreboardStatus`, `scoreboardClass`, `hasStartTime`, `startTime`, `hasLockTime`, `lockTime`, `hasEndTime`, `endTime`, `enableTranslations`, `defaultClass`.

Stored procedures use `DELIMITER` statements that are commented out in the source SQL (for tool compatibility). The build script `docker/scripts/convert-sql-scripts.sh` uncomments them when copying to `docker/mariadb/target/`. See the "Docker DB init fails" section above if procedures fail to parse.

## Agent behaviors

### Always run tests before pushing

Before pushing any code change, run the whole CI pipeline locally, as described in "Running the full CI pipeline locally", and push only once every step passes. Do not rely on CI to catch failures.

Also review your own diff for edge cases before pushing, especially in authentication and SAML code: expiry, overlapping or concurrent requests, cookie scope, and which fields are signed. Automated reviewers should not be the first to find them.

### Fix only what your change introduces

For every problem you find while working on a change, including comments from automated reviewers, first check whether the base branch already has it (`git show origin/dev:<file>`):
- **Introduced by your change:** fix it in the same PR, with a test.
- **Already on the base branch:** open a GitHub issue ("Discovered during #NNN", with the repo's labels) and leave it out of the PR, even when the fix is one line in a file the PR touches.

This keeps large changes, such as the Jakarta migration (#861), focused and reviewable.

### Bug fixes must include tests

When fixing a bug, always write a test that reproduces the bug first, then apply the fix. The test should fail without the fix and pass with it.

### Keep documentation up to date

When making significant changes (new features, architectural changes, build/config changes, new dependencies), check whether these files need updating:

- `AGENTS.md` (this file)
- `CONTRIBUTING.md`
- `README.md`
- Files in `docs/`

Do not scan these files on every change — only review and update them when your changes would make their content inaccurate.
