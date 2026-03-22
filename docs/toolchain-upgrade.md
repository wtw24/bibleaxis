[← Build and Test](build-and-test.md) · [Back to README](../README.md)

# Toolchain Upgrade Strategy

This project upgrades the toolchain in isolated PRs to keep failures diagnosable and rollback-safe.

## PR Sequence

1. **PR1: Build toolchain compatibility baseline**
   - Upgrade build matrix components together (AGP, wrapper, compileSdk, plugin compatibility).
   - Keep runtime networking behavior unchanged.

2. **PR2: OkHttp 5.x runtime reintroduction**
   - Upgrade OkHttp in isolation from toolchain changes.
   - Validate network smoke flows and regression signals.

3. **PR3: Gradle 9.x reintroduction**
   - Upgrade wrapper/plugins only after baseline and runtime stack are stable.
   - Keep runtime dependencies unchanged in this PR.

## Required Validation Per PR

```bash
./gradlew :bible:lintDebug
./gradlew :bible:testDebugUnitTest
./gradlew :bible:assembleDebug
```

Additionally for build-toolchain PRs:

```bash
./gradlew :bible:assembleRelease
```

## Rollback Policy

- Revert by whole PR, never partial cherry-pick rollback.
- Stop progression to the next PR if required checks are not green.
- Known hard blockers:
  - Kotlin metadata mismatch during compile/kapt.
  - AGP/kapt task creation/configuration failures.
