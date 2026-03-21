[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

# BibleAxis

[![](http://scripturesoftware.org/wp-content/uploads/2012/06/icon.png)](http://scripturesoftware.org/wp-content/uploads/2012/06/icon.png)

> Android Bible study app for deep reading, navigation, and cross-references.

BibleAxis is an independent Android fork of BibleQuote focused on day-to-day Bible reading and study. The app combines a fast reader, search, bookmarks/tags, and reading history in a mixed Java/Kotlin codebase.

## Quick Start

```bash
./gradlew :bible:testDebugUnitTest
./gradlew :bible:assembleDebug
./gradlew :bible:installDebug
```

For full setup (JDK/SDK requirements and local signing), see [Getting Started](docs/getting-started.md).

## Key Features

- **Reader-first experience** - clean interface focused on Scripture text.
- **Fast navigation** - quickly jump between books, chapters, and verses.
- **Study tools** - cross-references, bookmarks, tags, and highlights.
- **Powerful search** - tested search processors and algorithms.
- **Reading continuity** - history and state retention across sessions.

## Example

```bash
# Build and install debug app
./gradlew :bible:assembleDebug :bible:installDebug

# Start SplashActivity directly on a connected device
adb shell am start -n \
  de.wladimirwendland.bibleaxis.debug/\
  de.wladimirwendland.bibleaxis.presentation.splash.SplashActivity
```

## Fork Notice

This repository is maintained independently from the original BibleQuote Android project.

- Earlier code history remains credited to original authors.
- New changes are maintained in this fork.
- Distribution remains under Apache License 2.0.

---

## Documentation

| Guide | Description |
|-------|-------------|
| [Getting Started](docs/getting-started.md) | Requirements, setup, and first debug run |
| [Architecture](docs/architecture.md) | Module layout and package-level boundaries |
| [Build and Test](docs/build-and-test.md) | Gradle tasks, CI workflow, and validation |
| [Signing](docs/signing.md) | Debug/release signing rules and local.properties keys |
| [Troubleshooting](docs/troubleshooting.md) | Common local setup and run-time issues |

## Contacts

- Website: https://wladimir-wendland.de
- Support: wladimir.wendland@gmail.com

## Credits

- Copyright (C) 2011 Scripture Software and contributors.
- Copyright (C) 2026 Wladimir Wendland.

## License

Apache License 2.0. See `LICENSE`.
