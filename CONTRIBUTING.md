
# OWASP Security Shepherd Contributing

## GitFlow
Shepherd uses [GitFlow](https://datasift.github.io/gitflow/IntroducingGitFlow.html). That basically means you never directly commit to master / dev.

## Where do I put new code?
To add a new feature or fix a bug in Shepherd, create a fork or branch from the [dev branch](https://github.com/OWASP/SecurityShepherd/tree/dev). When you're branch is complete and your JUnit's have been created / run clear, create a pull request to merge your branch into dev. Squash your commits if you like, if you don't that will be done be at merge.

## Branch Naming Convention
If you're working on an issue from the backlog, call your branch dev#{issueNumber}

## Code Format
Shepherd enforces [Google Java Format](https://github.com/google/google-java-format) via [Spotless](https://github.com/diffplug/spotless), bound to the Maven build. The format version is pinned in `pom.xml`, so local and CI use the exact same formatter. Pull requests with incorrectly formatted Java files will fail the `lint-java` check (`mvn spotless:check`).

### Formatting your code

**Option 1 — Maven (recommended):**

```bash
# Reformat all Java sources in place
mvn spotless:apply

# Verify formatting without changing files (this is what CI runs)
mvn spotless:check
```

`mvn verify` also runs `spotless:check` automatically, so unformatted code fails the build before you push.

**Option 2 — IDE plugins:**

- **IntelliJ IDEA / Android Studio:** Install the [google-java-format plugin](https://plugins.jetbrains.com/plugin/8527-google-java-format) and enable it under *Settings → google-java-format*.
- **Eclipse:** Import the [Eclipse style config](https://github.com/google/styleguide/blob/gh-pages/eclipse-java-google-style.xml) under *Preferences → Java → Code Style → Formatter*.
- **VS Code:** Use the [Google Java Format extension](https://marketplace.visualstudio.com/items?itemName=ilkka.google-java-format) or configure the built-in formatter.

IDE plugins should be set to the same Google Java Format version pinned in `pom.xml` (`spotless-maven-plugin` → `<googleJavaFormat><version>`) to avoid producing output that differs from CI.

Always format before committing to avoid CI failures.

## How do I see the Backlog?
Install ZenHub for your browser and click the ZenHub tab that will appear in this repo. The Pipelines are as follows
1. *New Issues* - Issues yet to be reviewed for priority
2. *Ice Box* - Issues that are valid, but have not been prioritized for the backlog
3. *Backlog* - The Backlog order for priority.
4. *In Progess* - Items that are currently being worked
5. *QA Review* - Issues that have pull requests and require review / approval
6. *Closed* - Item is Done

## How do I setup my dev environment?
[Like This](https://github.com/OWASP/SecurityShepherd/wiki/Create-a-Security-Shepherd-Dev-Environment)

## Development Workflow
See [docs/development-workflow.md](docs/development-workflow.md) for the full development cycle, including how to:
- Create your branch and make changes
- Build and test your changes in the runtime environment using Docker
- Run the automated test suite

## Running Tests
See [docs/testing.md](docs/testing.md) for instructions on running unit and integration tests with Docker.

## Database Configuration
See [docs/database-configuration.md](docs/database-configuration.md) for connection pooling configuration and database setup.

## Is there a Definition of Done?
*Work in Progess*  
- [ ] New Code has 'Good' JUnit Tests that cover it
- [ ] All JUnit Tests Pass
- [ ] Acceptance Criteria of Epic has been satisfied where applicable
- [ ] Code does not introduce a vulnerability that can be leveraged to exploit the system/other users
