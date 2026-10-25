# Releasing

Security Shepherd follows [Semantic Versioning 2.0.0](https://semver.org/).

## Version format

- Releases are tagged as plain `MAJOR.MINOR.PATCH`, e.g. `4.0.0`, `4.0.1`, `4.1.0`. Don't use a `v` prefix, pre-release suffixes (`-rc.1`) or build metadata (`+build.1`).
- `dev` always carries a `-SNAPSHOT` version in `pom.xml` (e.g. `4.1.0-SNAPSHOT`). Snapshot versions are never tagged in git, but `dev` builds publish them as Docker image tags (see [Docker images](#docker-images)).
- Bump the version by the kind of change since the last release:
  - **MAJOR**: breaking changes, e.g. a new minimum Tomcat or Java version, removed features, or config and database changes that need manual migration.
  - **MINOR**: new modules, challenges or features that are backwards compatible.
  - **PATCH**: backwards-compatible bug fixes only.

Older tags (`2.3`, `2.4`, `v3.0`, `v3.1`) predate this scheme and are left as they are.

## Docker images

Every push to `dev` publishes the `owasp/security-shepherd` image as `dev` and as the snapshot version in `pom.xml`, e.g. `4.1.0-SNAPSHOT`. Both tags move with each push. When `dev` starts the next snapshot, the previous snapshot tag stays on the last build of that version. Releases are published by the tag push described in [Release steps](#release-steps).

The image is built for `linux/amd64` and `linux/arm64` and pushed as one multi-platform image. The runner is amd64, so the arm64 image is built under QEMU emulation, which makes its `RUN` steps slower. Pull requests build both platforms too, so a Dockerfile change that breaks either one fails before it is merged.

Each pushed image carries an SPDX SBOM and a SLSA provenance attestation, stored with it on Docker Hub. [Checking the Docker images](verifying-images.md) shows users how to read them.

Before a release, scan the `dev` image with Docker Scout, Trivy and Grype. Fix each finding that affects Security Shepherd in the image. For one that doesn't, add a `not_affected` statement with its justification to [`.vex/security-shepherd.openvex.json`](../.vex/security-shepherd.openvex.json), and bump the document's `version` and `timestamp`. [Check for known vulnerabilities](verifying-images.md#3-check-for-known-vulnerabilities) shows how users apply it.

The release workflow also signs a SLSA build provenance attestation for each pushed image with [GitHub artifact attestations](https://docs.github.com/en/actions/concepts/security/artifact-attestations), which users verify as described in [Signed provenance](verifying-images.md#signed-provenance). The build and the signing run in the reusable workflow [`release-build.yml`](../.github/workflows/release-build.yml), which `release.yml` calls but can't change. That is what makes the attestation SLSA Build Level 3, so keep build and push steps in `release-build.yml`: a step moved into `release.yml` would no longer be covered. Within `release-build.yml`, only the `attest` and `attest-vex` jobs hold the signing identity (`id-token: write`). Keep `attest` to the attest step alone, with no checkout and nothing that runs code or reads configuration from the repository.

The `attest-vex` job signs [`.vex/security-shepherd.openvex.json`](../.vex/security-shepherd.openvex.json) for each pushed image and pushes the attestation to Docker Hub, so it also logs in to Docker Hub. It checks out only that file, at the commit being built, and runs nothing from the repository. Don't pass it the file from a job that ran build steps, because such a job could change what gets signed. An image keeps the VEX it was built with. Never attest a second VEX for an image that has already been published: Trivy applies the first one it finds, and a later VEX change reaches that image through the file.

### Docker Hub overview

The overview on the [Docker Hub page](https://hub.docker.com/r/owasp/security-shepherd) is [`docker-hub-overview.md`](docker-hub-overview.md), and its short description is set in [`release.yml`](../.github/workflows/release.yml). The `docker-hub-description` job publishes both on each release tag, after the images are pushed. Edits on `dev` don't reach Docker Hub until the next release, and an edit made on Docker Hub itself is overwritten by it.

Docker Hub renders the overview on its own, so use absolute links, pointing at `master`. It is limited to 25,000 bytes, and the short description to 100. The job needs the Docker Hub account behind `DOCKERHUB_USERNAME` to have Admin on the repository, and `DOCKERHUB_TOKEN` to be a personal access token with read/write/delete scope.

## Tag rules

Repository rulesets enforce the following for tags:

- New tags must match `^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)$`.
- Only repository admins can create tags.
- Tags can't be deleted or moved once pushed, so check the commit before tagging.

## Release steps

1. **Set the release version on `dev`.** In a normal PR to `dev`, change `pom.xml` from `X.Y.Z-SNAPSHOT` to `X.Y.Z`. The version change has to land on `dev`, not only in the release PR, so that `dev` and `master` don't drift apart. Check that [`docker-hub-overview.md`](docker-hub-overview.md) still describes the release, because the tag push publishes it.
2. **Open the release PR from `dev` to `master`** and merge it with a merge commit. `master` only allows merge commits.
3. **Tag the merge commit on `master`**:
   ```bash
   git fetch origin master
   git tag -a X.Y.Z origin/master -m "Security Shepherd X.Y.Z"
   git push origin X.Y.Z
   ```
   The tag push runs the [Release to Dockerhub](../.github/workflows/release.yml) workflow. It fails if the tag doesn't match the `pom.xml` version. If the versions match, it publishes the `owasp/security-shepherd` image as `X.Y.Z`, `X.Y`, `X` and `latest`, and then the [Docker Hub overview](#docker-hub-overview).
4. **Create the GitHub release** for the tag, with release notes.
5. **Start the next snapshot on `dev`.** In a PR, set `pom.xml` to the next expected version, e.g. `X.(Y+1).0-SNAPSHOT`. If the next release turns out to be only fixes, set the release version in step 1 to the patch version instead.
