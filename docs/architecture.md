[← Getting Started](getting-started.md) · [Back to README](../README.md) · [Build and Test →](build-and-test.md)

# Architecture

## High-Level Overview

BibleAxis is a single-module Android app (`:bible`) with a mixed Java/Kotlin codebase. The code follows package-level layering rather than multi-module Gradle separation.

Primary runtime entry points:

- `BibleAxisApp` as application root
- `SplashActivity` as launcher activity
- `ReaderActivity` and companion screens for reading, search, bookmarks, settings, and history

## Repository Structure

```text
bibleaxis/
├── bible/
│   ├── build.gradle
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── assets/
│       │   ├── java/de/wladimirwendland/bibleaxis/
│       │   └── res/
│       └── test/
├── build.gradle
├── settings.gradle
└── signing.gradle
```

## Main Package Areas

| Package | Responsibility |
|---------|----------------|
| `presentation` | Activities, views, presenters, and UI state |
| `domain` | Entities, repository interfaces, search logic, and formatters |
| `dal` | Data access controllers and repository implementations |
| `data` | Cache, migration helpers, and logger implementations |
| `di` | Dagger components/modules/scopes |
| `managers` | App-level managers for library, bookmarks, history, and tags |
| `utils` | Shared formatting, parsing, and platform utility helpers |

## Data and Control Flow

Typical flow is presentation -> domain contracts -> data access implementation:

1. UI action starts in an activity/presenter.
2. Domain-level contracts define behavior (`repository`, `controller`, `entity`).
3. `dal`/`data` implementations provide storage and file-system backed behavior.
4. Results return to presentation for rendering.

Dependency injection is wired with Dagger modules/components under `di/`.

## Compatibility Notes

- Java and Kotlin coexist, so refactors should keep API compatibility between both languages.
- Build targets are `minSdkVersion 24` and `targetSdkVersion 35`.
- Android manifest still declares `requestLegacyExternalStorage`, which influences file access behavior on newer Android versions.

## See Also

- [Getting Started](getting-started.md) - setup and first run
- [Build and Test](build-and-test.md) - verification pipeline
- [Signing](signing.md) - signing flow and secrets handling
