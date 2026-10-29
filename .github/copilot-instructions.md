# Copilot instructions

OWASP Security Shepherd is a Java 17 servlet app (Jakarta EE 11, Tomcat 11, Maven). The full contributor rules are in `AGENTS.md` and `CONTRIBUTING.md`. When reviewing a pull request, check it against the points below and say plainly in the review when one is not met.

## Tests

- A bug fix must include a test that reproduces the bug: it fails without the fix and passes with it. If a fix has no such test, say so.
- New or changed behaviour in `src/main/java` needs JUnit tests: unit tests in `src/test/java`, database- or MongoDB-backed code in `src/it/java`.
- Changes to the Docker images, `docker/` or the CI workflows should add to or update the script tests in `src/test/scripts/` that cover that area.
- If a change can only be tested by hand, the PR description must give the manual test steps.

## Scope

- A PR fixes only what it introduces. A problem that already exists on `dev` belongs in a separate issue, even when it is one line in a file the PR touches. Report it as pre-existing rather than asking for the fix in this PR.
- PRs target `dev`, never `master`. The `X.Y.Z-SNAPSHOT` version in `pom.xml` changes only in a release.

## Intentionally vulnerable code

`src/main/java/servlets/module/lesson/`, `src/main/java/servlets/module/challenge/`, `mobile/MobileShepherd/` and the data in `src/main/resources/database/moduleSchemas.sql` are deliberately insecure training content. Don't flag their vulnerabilities or suggest fixes for them. Do flag a change that makes a lesson or challenge unsolvable.

Security problems anywhere else (authentication, sessions, admin servlets, setup, the DB layer, SSO) are in scope: the platform itself must not be exploitable.

## Platform rules

- Java 17 APIs only. No `javax.servlet` or other Java EE `javax.*` dependencies; JDK packages such as `javax.crypto` and `javax.sql` are fine.
- Never declare `spring-core` test-scoped, and don't remove the Spring modules pac4j-saml pulls in: SSO needs them at runtime.
- A dependency from a new groupId needs its own line in `.mvn/pgp-keys.map`. Reject wildcard rules and `noSig` exceptions there. The Shibboleth repository may serve only `org.opensaml` and `net.shibboleth`.
- Passwords are hashed with Argon2. Flag any other hashing of credentials.
- Databases use `utf8mb4`, not `utf8`. Each challenge keeps its own DB user. Schema changes go in `src/main/resources/database/`, never in the generated `docker/mariadb/target/`.
- Never commit: the `'@'%'` rewrite of `moduleSchemas.sql` used for local integration tests, `.env` changes to `TEST_MYSQL_PASSWORD`, private keys or keystores.

## Authentication and SAML changes

Check expiry, overlapping and concurrent requests, cookie scope and flags, and whether every field the code trusts is covered by the signature. Only SP-initiated SSO is supported.

## Documentation

If the PR changes the build, configuration, setup, dependencies or architecture, say which of `AGENTS.md`, `CONTRIBUTING.md`, `README.md` or `docs/` it makes inaccurate.

## Not needed

Don't comment on Java formatting: CI enforces Google Java Format with `mvn spotless:check`.
