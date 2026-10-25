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

Each pushed image carries an SPDX SBOM and a SLSA provenance attestation, stored with it on Docker Hub. [Checking the Docker images](verifying-images.md) shows users how to read them.

The SBOM and provenance on Docker Hub are unsigned. For a check that can't be forged, the release workflow also signs a SLSA build provenance attestation with [GitHub artifact attestations](https://docs.github.com/en/actions/concepts/security/artifact-attestations) (Sigstore). The build and the signing run in the reusable workflow [`release-build.yml`](../.github/workflows/release-build.yml), which the triggering workflow can't change, so the attestation meets SLSA Build Level 3. Verify an image with the [GitHub CLI](https://cli.github.com/):

```bash
gh attestation verify oci://docker.io/owasp/security-shepherd:4.0.0 \
  --repo OWASP/SecurityShepherd \
  --signer-workflow OWASP/SecurityShepherd/.github/workflows/release-build.yml \
  --source-ref refs/tags/4.0.0
```

For a `dev` or snapshot image, use `--source-ref refs/heads/dev`. Keep the `release-build.yml` steps inside that workflow: a build or push step moved into `release.yml` would no longer be covered by this guarantee.

## Tag rules

Repository rulesets enforce the following for tags:

- New tags must match `^(0|[1-9]\d*)\.(0|[1-9]\d*)\.(0|[1-9]\d*)$`.
- Only repository admins can create tags.
- Tags can't be deleted or moved once pushed, so check the commit before tagging.

## Release steps

1. **Set the release version on `dev`.** In a normal PR to `dev`, change `pom.xml` from `X.Y.Z-SNAPSHOT` to `X.Y.Z`. The version change has to land on `dev`, not only in the release PR, so that `dev` and `master` don't drift apart.
2. **Open the release PR from `dev` to `master`** and merge it with a merge commit. `master` only allows merge commits.
3. **Tag the merge commit on `master`**:
   ```bash
   git fetch origin master
   git tag -a X.Y.Z origin/master -m "Security Shepherd X.Y.Z"
   git push origin X.Y.Z
   ```
   The tag push runs the [Release to Dockerhub](../.github/workflows/release.yml) workflow. It fails if the tag doesn't match the `pom.xml` version. If the versions match, it publishes the `owasp/security-shepherd` image as `X.Y.Z`, `X.Y`, `X` and `latest`.
4. **Create the GitHub release** for the tag, with release notes.
5. **Start the next snapshot on `dev`.** In a PR, set `pom.xml` to the next expected version, e.g. `X.(Y+1).0-SNAPSHOT`. If the next release turns out to be only fixes, set the release version in step 1 to the patch version instead.
