# OWASP Security Shepherd

[OWASP Security Shepherd](https://github.com/OWASP/SecurityShepherd) is a web and mobile application security training platform. Players work through lessons and challenges on the OWASP Top 10 and more (SQL and NoSQL injection, XSS, CSRF, broken authentication, insecure crypto and others) and score points as they go. It suits self-study, university courses, corporate training and CTF-style events.

> **⚠️ Deliberately vulnerable.** The lessons and challenges are insecure on purpose. Run it on a laptop or an isolated lab network, never on a host reachable from the internet.

## What's in this image

This image is **only the web application**: Security Shepherd on Tomcat 11 and Java 17. It does not contain a database. To run Security Shepherd you also need:

- **MariaDB** (12.x tested) for the platform and the SQL challenges, with Security Shepherd's schemas and challenge users loaded
- **MongoDB** (4.1.13 tested) for the NoSQL injection challenges

There are no published database images yet. The simplest way to get all three running is the Docker Compose quick start below, which builds the two database images for you.

## Quick start (Docker Compose)

Follow [Getting started](https://owasp.github.io/SecurityShepherd/getting-started/). It lists the [prerequisites](https://owasp.github.io/SecurityShepherd/getting-started/#prerequisites), then builds the images, starts all three containers and takes you through the first login.

The defaults (passwords in `.env`, a self-signed certificate, MongoDB without authentication) are for a quick start, not a long-lived or shared deployment.

## Tags

| Tag | What it is |
| --- | --- |
| `X.Y.Z`, e.g. `4.0.0` | A release. Never changes once published. |
| `X.Y`, `X`, `latest` | The newest release in that line. |
| `X.Y.Z-SNAPSHOT`, `dev` | The latest build of the `dev` branch. Untested pre-release. |

Images from 4.0.0 on run on `linux/amd64` and `linux/arm64`, including Apple Silicon. The older `3.0` and `3.1` images are Tomcat 9, `linux/amd64` only, and aren't signed.

## Ports, volumes and settings

| | |
| --- | --- |
| Ports | `8080` (HTTP, redirects to HTTPS) and `8443` (HTTPS) |
| `/usr/local/tomcat/conf` | Tomcat config, database connection settings and the TLS keystore. Keep it in a volume. |
| `/usr/local/tomcat/logs` | Tomcat and application logs |
| `HTTPS_PORT` | The HTTPS port browsers are redirected to (default `443`) |
| `TLS_KEYSTORE_FILE`, `TLS_KEYSTORE_PASS`, `ALIAS` | The PKCS12 keystore in `conf/` to use |
| `DNAME` | Subject of the generated certificate |

The image contains no TLS private key. On first start it generates a self-signed certificate for `localhost` in `conf/`. To use your own, put a PKCS12 keystore there and set `TLS_KEYSTORE_PASS` and `ALIAS` to match it.

## Verifying the image

Every image from 4.0.0 on is built by GitHub Actions from this repository and carries a signed SLSA build provenance, an SPDX SBOM and a VEX document. [Verifying the images](https://github.com/OWASP/SecurityShepherd/blob/master/docs/verifying-images.md) shows how to verify them with `gh attestation verify`, and [Vulnerability management](https://github.com/OWASP/SecurityShepherd/blob/master/docs/vulnerability-management.md) how to scan them with Docker Scout, Grype or Trivy.

## Links

- Source and issues: <https://github.com/OWASP/SecurityShepherd>
- OWASP project page: <https://owasp.org/www-project-security-shepherd>
- License: [GPL-3.0](https://github.com/OWASP/SecurityShepherd/blob/master/LICENSE)
