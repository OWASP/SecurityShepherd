# OWASP Security Shepherd

Security Shepherd is a web and mobile application security training platform. It helps people of any skill level, from AppSec beginners to experienced engineers, build their penetration testing skills.

The source code, issues and releases are on [GitHub](https://github.com/OWASP/SecurityShepherd). New to Security Shepherd? Start with [Getting started](getting-started.md).

## Playing

- [How Shepherd works](how-shepherd-works.md): lessons and challenges, layouts, result keys and scoring
- [Setting up your tools](setting-up-your-tools.md): an intercepting proxy and your browser
- [The levels](levels.md): every web level, by topic, with its OWASP Top 10 category and CWE
- [Mobile levels](mobile-levels.md): the Android levels

## Running Security Shepherd

- [Deploying into production](deploying-into-production.md): passwords, TLS certificates and the network, before other people use it
- [Manual setup](manual-setup.md): running Security Shepherd in your own Tomcat, without Docker
- [Upgrading](upgrading.md): up-to-date images, and moving an installation to 4.0
- [Backups and restore](backups.md): what to back up, and how to restore it
- [Running a CTF](running-a-ctf.md): set up levels, classes, the scoreboard and rules for an event
- [Admin menu reference](admin-menu.md): every page in the Admin menu
- [Single sign-on](single-sign-on.md): log players in through your organisation's SAML identity provider
- [Translations](translations.md): the languages, and how much of each is translated
- [Reskinning](reskinning.md): your own logo, colours and name
- [Troubleshooting](troubleshooting.md): common problems and where the logs are

## Security

- [Verifying the images](verifying-images.md): where an image came from and what's inside it
- [Vulnerability management](vulnerability-management.md): how vulnerabilities in the platform are handled, and how to scan an image

## Reference

- [Configuration](configuration.md): `.env`, the properties files and the settings in the database
- [Glossary](glossary.md): the terms used in Security Shepherd

## Contributing

- [Development workflow](development-workflow.md): branches, pull requests and formatting
- [Running tests](testing.md): unit and integration tests
- [Architecture](architecture.md): the parts of the application, and how a level is played and scored
- [Database layer](database-layer.md): how Security Shepherd connects to its databases
- [Creating a level](creating-a-level.md): adding a lesson or a challenge
- [Translating Security Shepherd](translating.md): improving a translation, or adding a language
- [Releasing](releasing.md): how a release is cut and published

These pages are built from the [`docs/`](https://github.com/OWASP/SecurityShepherd/tree/dev/docs) folder. To change one, open a pull request against `dev`.
