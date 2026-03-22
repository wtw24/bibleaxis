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

## Build Toolchain Matrix

Current PR1 baseline:

| Component | Version | Source |
|-----------|---------|--------|
| Gradle Wrapper | 8.11.1 | `gradle/wrapper/gradle-wrapper.properties` |
| Android Gradle Plugin | 8.9.1 | `build.gradle` |
| Kotlin Gradle Plugin | 2.2.21 | `build.gradle` |
| Google Services Plugin | 4.4.2 | `build.gradle` |
| Crashlytics Gradle Plugin | 2.9.9 | `build.gradle` |
| compileSdk | 36 | `build.gradle` |
| JDK | 17 | `.github/workflows/android.yml` |

Major runtime and build-toolchain upgrades must stay isolated in separate PRs.

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

Release builds use resource shrinking, code minification, and obfuscation (R8). Keep `mapping.txt` artifacts for troubleshooting and symbolization.

## See Also

- [Getting Started](getting-started.md) - local environment setup
- [Signing](signing.md) - release signing requirements
- [Troubleshooting](troubleshooting.md) - common build failures
