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

GitHub Actions workflow lives in `.github/workflows/android.yml` and currently:

- runs on `ubuntu-latest`
- configures Java in CI
- executes `./gradlew clean assembleDebug`
- executes `./gradlew testDebugUnitTest`
- uploads `bible/build/reports/tests/` as an artifact

## Test Coverage Scope

Current unit tests cover key areas including:

- search processors and algorithms
- formatter behavior
- presenters in selected UI flows
- migration and repository behavior
- cache/logger/config components

## Release Build Note

`assembleRelease` requires valid signing values. See [Signing](signing.md) before running release builds.

## See Also

- [Getting Started](getting-started.md) - local environment setup
- [Signing](signing.md) - release signing requirements
- [Troubleshooting](troubleshooting.md) - common build failures
