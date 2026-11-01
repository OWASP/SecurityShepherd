
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

Run these Maven commands on JDK 17, as CI does. The pinned Google Java Format version cannot run on JDK 25 or newer, so Spotless stops with a `google-java-format(jvm-version)` error. [How do I setup my dev environment?](#how-do-i-setup-my-dev-environment) shows how to switch Maven to JDK 17.

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
| You need | Version | Check with |
|---|---|---|
| Git | Any | `git --version` |
| Docker with Docker Compose | Compose v2 or newer | `docker compose version` |
| JDK | 17 | `mvn -v` shows "Java version" |
| Maven | 3.9.2 or newer | `mvn -v` shows "Apache Maven" |

Use JDK 17 specifically: on JDK 25 or newer the build stops at its formatting check with a `google-java-format(jvm-version)` error. If `mvn -v` shows another version, install JDK 17, for example from [Adoptium](https://adoptium.net/), and set `JAVA_HOME` to its folder.

You also need about 3 GB of free disk space, and ports 80, 443, 3306 and 27017 free.

Then build and start Security Shepherd:

```bash
git clone https://github.com/OWASP/SecurityShepherd.git
cd SecurityShepherd
mvn -Pdocker clean install -DskipTests
docker compose up -d --build
```

Then open https://localhost and complete the [first-time setup](docs/getting-started.md#first-time-setup).

## Development Workflow
See [docs/development-workflow.md](docs/development-workflow.md) for the full development cycle, including how to:
- Create your branch and make changes
- Build and test your changes in the runtime environment using Docker
- Run the automated test suite

## Running Tests
See [docs/testing.md](docs/testing.md) for instructions on running unit and integration tests with Docker.

## Database Configuration
See [docs/database-layer.md](docs/database-layer.md) for how Security Shepherd connects to its databases, and [docs/configuration.md](docs/configuration.md) for the connection settings.

## Writing Documentation
The documentation in [`docs/`](docs/) is published as a site at https://owasp.github.io/SecurityShepherd/, and is also read on GitHub. Write it so that it reads the same in both places:

- **GitHub-flavoured Markdown only:** headings, lists, tables, code blocks, task lists, and Mermaid diagrams in code blocks marked `mermaid`. Don't use syntax only the site understands, such as admonitions (`!!! note`), content tabs or code annotations, unless the maintainers agree to it first.
- **Links to other pages in `docs/`** are relative links to the `.md` file, such as `[Configuration](configuration.md#event-times)`.
- **Links to anything outside `docs/`** are absolute: `https://github.com/OWASP/SecurityShepherd/blob/dev/…`. A relative `../` link works on GitHub but breaks on the site.
- **The site and the repo files are separate.** `docs/docker-hub-overview.md` isn't on the site, so it links to it instead of repeating it. `README.md` and `CONTRIBUTING.md` stand apart from the site. `CONTRIBUTING.md` links out for detail, but repeats what's needed to get set up, because contributors arrive there from GitHub. `README.md` points to it for the prerequisites and keeps the short build commands. If the prerequisites or those commands change, change every copy.
- **A new page** goes in the `nav` list in `zensical.toml`, and in `docs/index.md`. The docs build fails on a page that isn't in `nav`.
- **Diagrams** are Mermaid, in code blocks marked `mermaid`, so they render on GitHub and on the site. A diagram is scaled down to the page's width, so a wide one ends up with text too small to read, and the site has no way to zoom in. Keep them readable at page width:
    - **Sequence diagrams:** four or five participants at most, and message labels of a few words. Put the detail in the text around the diagram.
    - **Flowcharts:** draw them top to bottom (`flowchart TB`) unless they still read well left to right, keep to about eight or nine boxes, and give each box a name and a short detail, not a full file path.
    - **No labels on arrows.** Mermaid draws each arrow label on a box of its own, which shows against the page in dark mode, and bends the arrow around it. Put the detail in the boxes or in the text.
    - **Groups (`subgraph`) only when they're needed.** Groups of different widths push boxes out of line, which bends the arrows between them. A sentence before the diagram can often say what a group would.
    - **Check it** in the built site, in light and dark mode at a normal desktop width. Its text should be about the size of the page's text, and its arrows straight, apart from where one box branches to several.
    - **No attacks.** Don't diagram how to exploit a level.
- **Images** go in `docs/assets/images/`, with alt text that describes what the image shows. Screenshots must not show a level's solution or result key.
- **No solutions.** Public pages explain how to play, not how to complete a level. Cheat sheets are for admins, in the app.
- **The level catalogue** in `levels.md` and `mobile-levels.md` is generated, so don't edit its tables by hand. Change `coreSchema.sql` or `.github/scripts/level-classification.toml`, then run `uv run --locked --no-sync python .github/scripts/generate-level-catalogue.py`.

Preview the site with `uv run zensical serve`. Before you push, run the same checks as CI:

```bash
uv sync --locked --no-build
uv run --locked --no-sync python .github/scripts/generate-level-catalogue.py --check
uv run --locked --no-sync zensical build --clean --strict
uv run --locked --no-sync python .github/scripts/check-docs-site.py
```

## Versioning and Releases
Shepherd follows [Semantic Versioning](https://semver.org/). Releases are tagged as plain `X.Y.Z` (e.g. `4.0.0`, with no `v` prefix), and `dev` always carries an `X.Y.Z-SNAPSHOT` version. See [docs/releasing.md](docs/releasing.md) for the release steps.

## Is there a Definition of Done?
*Work in Progess*  
- [ ] New Code has 'Good' JUnit Tests that cover it
- [ ] All JUnit Tests Pass
- [ ] Acceptance Criteria of Epic has been satisfied where applicable
- [ ] Code does not introduce a vulnerability that can be leveraged to exploit the system/other users
