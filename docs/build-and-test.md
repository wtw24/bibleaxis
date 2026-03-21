[← Architecture](architecture.md) · [Back to README](../README.md) · [Signing →](signing.md)

# Build and Test

## Core Gradle Tasks

| Goal | Command |
|------|---------|
| Run JVM unit tests | `./gradlew :bible:testDebugUnitTest` |
| Build debug APK | `./gradlew :bible:assembleDebug` |
| Install debug APK | `./gradlew :bible:installDebug` |
| Build release APK | `./gradlew :bible:assembleRelease` |

## Typical Local Validation

Run the commands in this order for daily development:

```bash
./gradlew :bible:testDebugUnitTest
./gradlew :bible:assembleDebug
./gradlew :bible:installDebug
```

This sequence validates domain/presentation tests, compiles the app, and confirms installability.

## CI Workflow

GitHub Actions workflow lives in `.github/workflows/android.yml` and now runs two jobs:

- `build` job:
  - validates Gradle wrapper integrity
  - configures Java 17 (Temurin)
  - runs `./gradlew :bible:lintDebug` (blocking for new issues; legacy findings are tracked in lint baseline)
  - runs `./gradlew :bible:testDebugUnitTest`
  - runs `./gradlew :bible:assembleDebug`
  - uploads unit test and lint reports as artifacts
- `security` job:
  - runs dependency review on pull requests (fails on high+ severity)
  - runs repository secret scan

CI is configured with least-privilege permissions and read-only defaults.

## Local Equivalent Checks

Run these before opening a PR:

```bash
./gradlew :bible:lintDebug
./gradlew :bible:testDebugUnitTest
./gradlew :bible:assembleDebug
```

Do not print signing values, tokens, or other secrets in command output or logs.

Lint baseline policy:

- Existing legacy lint debt is captured in `bible/lint-baseline.xml`.
- CI fails on newly introduced lint issues outside the baseline.

## Test Coverage Scope

Current unit tests cover key areas including:

- search processors and algorithms
- formatter behavior
- presenters in selected UI flows
- migration and repository behavior
- cache/logger/config components

## Release Build Note

`assembleRelease` requires valid release signing values and fails fast when keys are missing or keystore path is invalid. See [Signing](signing.md) before running release builds.

## See Also

- [Getting Started](getting-started.md) - local environment setup
- [Signing](signing.md) - release signing requirements
- [Troubleshooting](troubleshooting.md) - common build failures
