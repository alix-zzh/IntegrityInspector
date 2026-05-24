# CI/CD

IntegrityInspector uses GitHub Actions for pull-request validation and release automation.

## Pull Requests To `master`

All changes should go through a pull request into `master`.

The `PR Quality Gate` workflow runs on every pull request targeting `master`:

- `Maven quality gate`
  - builds the project with JDK 21;
  - runs the full Maven package lifecycle;
  - runs tests;
  - enforces JaCoCo line and branch coverage for the full codebase;
  - runs PMD and CPD;
  - enforces changed-line coverage with `diff-cover` at `>= 90%`;
  - uploads JaCoCo, diff coverage, PMD, and CPD reports as workflow artifacts.
- `Dependency review`
  - fails pull requests that introduce dependency changes with moderate or higher known vulnerability severity.
- `CodeQL analysis`
  - runs GitHub CodeQL for Java/Kotlin security and correctness analysis.

Generated ANTLR classes remain excluded from JaCoCo and PMD/CPD checks.

## Required Branch Protection

Configure `master` branch protection in GitHub repository settings:

1. Require a pull request before merging.
2. Require approvals from at least one human reviewer.
3. Require review from Code Owners. `.github/CODEOWNERS` currently assigns the repository owner as the default code owner.
4. Require status checks to pass before merging.
5. Require branches to be up to date before merging.
6. Require conversation resolution before merging.
7. Do not allow direct pushes to `master` except for the GitHub Actions release bot if repository policy requires the automated version bump.
8. Require these status checks:
   - `Maven quality gate`
   - `Dependency review`
   - `CodeQL analysis`

The release workflow needs `contents: write` permission so it can create releases, upload artifacts, and push version-bump commits. In repository settings, set GitHub Actions workflow permissions to allow read and write permissions.

## Release Flow

The `Release` workflow runs after a push to `master`, which normally happens when a pull request is merged.

The workflow:

1. Reads the Maven project version from `pom.xml`.
2. If the version ends with `-SNAPSHOT`, normalizes it to the release version and commits that release version to `master`.
3. Builds the release jar with `mvn package`.
4. Uploads the jar as a workflow artifact.
5. Creates a GitHub Release named `IntegrityInspector v<version>`.
6. Uploads `target/integrity-inspector-<version>.jar` to that release.
7. Bumps `master` to the next patch snapshot version, for example `0.9.0` -> `0.9.1-SNAPSHOT`.

Release and version-bump commits include `[skip release]` to avoid recursive release runs.
