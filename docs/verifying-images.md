# Checking the Docker images

The Security Shepherd Tomcat image is published to Docker Hub as [`owasp/security-shepherd`](https://hub.docker.com/r/owasp/security-shepherd). This page shows how to check where an image came from and what's inside it, before you run it.

## Image tags

| Tag | What it is |
| --- | --- |
| `X.Y.Z`, e.g. `4.0.0` | A release. Never changes once published. |
| `X.Y`, `X`, `latest` | The newest release in that line. Moves with each release. |
| `X.Y.Z-SNAPSHOT`, e.g. `4.1.0-SNAPSHOT` | The latest `dev` build of the version in progress. Moves with each push to `dev`. |
| `dev` | The latest `dev` build. Moves with each push to `dev`. |

Snapshot and `dev` images are untested pre-releases. For anything you rely on, use a release tag.

Images from 4.0.0 on, and `dev` and snapshot builds, run natively on `linux/amd64` and `linux/arm64`, including Apple Silicon. The older images (`3.0`, `3.1`, and `latest` until 4.0.0 is released) are `linux/amd64` only, and have none of the SBOM, provenance or signature described below.

## Choose the image to check

The commands below use these two variables, so set them first:

```bash
# A release: the tag, and the git tag it was built from
TAG=4.0.0
REF=refs/tags/4.0.0

# Or, to check a snapshot / dev build instead, uncomment these two lines
# TAG=4.0.0-SNAPSHOT
# REF=refs/heads/dev
```

## 1. Verify where it came from

This is the check that matters. The release workflow signs a [SLSA](https://slsa.dev/) build provenance for every image it pushes, with [GitHub artifact attestations](https://docs.github.com/en/actions/concepts/security/artifact-attestations). It's signed through [Sigstore](https://www.sigstore.dev/) and recorded in its public transparency log. You need the [GitHub CLI](https://cli.github.com/), logged in with any GitHub account (`gh auth login`).

```bash
# Proves the image was built from OWASP/SecurityShepherd by its release workflow, from $REF.
# --signer-workflow rejects anything signed by another workflow, even one in this repository.
gh attestation verify "oci://docker.io/owasp/security-shepherd:$TAG" \
  --repo OWASP/SecurityShepherd \
  --signer-workflow OWASP/SecurityShepherd/.github/workflows/release-build.yml \
  --source-ref "$REF"

# Same check, printing the whole provenance: the commit and the Actions run that built the image
gh attestation verify "oci://docker.io/owasp/security-shepherd:$TAG" \
  --repo OWASP/SecurityShepherd \
  --signer-workflow OWASP/SecurityShepherd/.github/workflows/release-build.yml \
  --source-ref "$REF" --format json
```

The image is built and signed in a reusable workflow, [`release-build.yml`](../.github/workflows/release-build.yml), that the workflow starting the build can't change. That makes the attestation [SLSA Build Level 3](https://slsa.dev/spec/v1.2/build-track-basics).

## 2. See what's inside it (SBOM)

Each image carries a software bill of materials in [SPDX](https://spdx.dev/) format: the operating system packages, Tomcat, and the Java libraries in the Security Shepherd WAR. Each platform has its own.

```bash
# Print the SBOM of the amd64 image. Use "linux/arm64" for the ARM image,
# or --format '{{ json .SBOM }}' for both, keyed by platform.
docker buildx imagetools inspect "owasp/security-shepherd:$TAG" \
  --format '{{ json (index .SBOM "linux/amd64").SPDX }}'

# Save it to a file to use with other tools
docker buildx imagetools inspect "owasp/security-shepherd:$TAG" \
  --format '{{ json (index .SBOM "linux/amd64").SPDX }}' > "shepherd-$TAG.spdx.json"

# List the packages with Docker Scout, which reads the same SBOM, for your machine's platform.
# Needs you to be logged in to Docker Hub, with any account.
docker scout sbom --format list "owasp/security-shepherd:$TAG"
```

## 3. Check for known vulnerabilities

```bash
# Scan the image with Docker Scout
docker scout cves "owasp/security-shepherd:$TAG"

# Or scan the saved SBOM with Grype (https://github.com/anchore/grype)
grype "sbom:./shepherd-$TAG.spdx.json"
```

Security Shepherd is deliberately vulnerable training software. Scanners report on the image's packages and libraries, not on the lessons and challenges, which are vulnerable by design.

Some findings don't affect Security Shepherd, for example a vulnerable binary that comes with the base image but never runs. Each of these is listed, with the reason, in [`.vex/security-shepherd.openvex.json`](../.vex/security-shepherd.openvex.json), an [OpenVEX](https://openvex.dev/) document. Findings that do affect Security Shepherd are fixed in the image, not listed there. Scanners only apply a VEX document when you pass it to them:

```bash
curl -sSfLO https://raw.githubusercontent.com/OWASP/SecurityShepherd/dev/.vex/security-shepherd.openvex.json

# Docker Scout trusts only VEX written by Docker unless --vex-author names the author
docker scout cves "owasp/security-shepherd:$TAG" \
  --vex-location security-shepherd.openvex.json \
  --vex-author 'OWASP Security Shepherd maintainers'

# Grype, scanning the image itself so that the VEX document's product matches it
grype "owasp/security-shepherd:$TAG" --vex security-shepherd.openvex.json

# Trivy (https://trivy.dev/). --show-suppressed lists what the VEX document covers.
trivy image --vex security-shepherd.openvex.json --show-suppressed "owasp/security-shepherd:$TAG"
```

Each statement names the vulnerable package and version it covers, such as `pkg:golang/stdlib@1.27.1`. A newer vulnerability in the same binary, or the same vulnerability in another version, is still reported.

Each image is also published with a signed copy of the VEX document as it was when the image was built. It's signed by the same release workflow as the provenance in step 1 and stored next to the image on Docker Hub. Trivy can read it from there, with no download:

```bash
# Proves the VEX document attached to the image came from this repository's release workflow
gh attestation verify "oci://docker.io/owasp/security-shepherd:$TAG" \
  --repo OWASP/SecurityShepherd \
  --signer-workflow OWASP/SecurityShepherd/.github/workflows/release-build.yml \
  --source-ref "$REF" \
  --predicate-type https://openvex.dev/ns/v0.2.0

# Trivy, applying the VEX document attached to the image
trivy image --vex oci --show-suppressed "owasp/security-shepherd:$TAG"

# Or save the image's own signed copy to use with Docker Scout or Grype, in place of the curl
# download above. It's the version signed for this image, rather than the latest file on dev.
gh attestation verify "oci://docker.io/owasp/security-shepherd:$TAG" \
  --repo OWASP/SecurityShepherd \
  --signer-workflow OWASP/SecurityShepherd/.github/workflows/release-build.yml \
  --source-ref "$REF" \
  --predicate-type https://openvex.dev/ns/v0.2.0 \
  --format json --jq '.[0].verificationResult.statement.predicate' > security-shepherd.openvex.json
```

An image's attached VEX never changes after it's published. Statements added to the file later, for vulnerabilities found since, apply only when you pass the file from `dev`. Docker Scout and Grype don't read the attached copy, so pass them a file: the signed copy above, or the latest from `dev`. Older images, including the 3.x releases, have no attached VEX.

If Trivy finds no VEX attached to an image that should have one, you may have an older copy of that tag pulled locally, which Trivy scans in preference. Run `docker pull "owasp/security-shepherd:$TAG"` first, or add `--image-src remote`.

## 4. See how it was built

```bash
# BuildKit's provenance for the amd64 image: base image, Dockerfile, build arguments.
# Stored next to the image on Docker Hub but not signed, so it shows how the image says it
# was built. The check in step 1 is the one that proves it.
docker buildx imagetools inspect "owasp/security-shepherd:$TAG" \
  --format '{{ json (index .Provenance "linux/amd64").SLSA }}'
```

## 5. Run exactly what you checked

A tag can move. A digest can't, so verify the digest and run the same one.

```bash
# The tag's digest. For a multi-platform image it's the image index, which covers every platform
# and is what's signed. A single platform's digest has no signed attestation of its own.
digest=$(docker buildx imagetools inspect "owasp/security-shepherd:$TAG" --format '{{ .Manifest.Digest }}')

# Verify it, then run it by digest
gh attestation verify "oci://docker.io/owasp/security-shepherd@$digest" \
  --repo OWASP/SecurityShepherd \
  --signer-workflow OWASP/SecurityShepherd/.github/workflows/release-build.yml \
  --source-ref "$REF"
docker run --rm -p 8443:8443 "owasp/security-shepherd@$digest"
```

The image ships no TLS key: on its first start it generates its own self-signed certificate, so expect a browser warning at `https://localhost:8443`. For the full stack with its databases, use the `docker compose` quick start in the [README](../README.md).
