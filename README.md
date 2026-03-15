[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)

# BibleAxis

[![](http://scripturesoftware.org/wp-content/uploads/2012/06/icon.png)](http://scripturesoftware.org/wp-content/uploads/2012/06/icon.png)

BibleAxis is a powerful and simple Bible study application designed to help you
explore Scripture deeper.

## Features

- Multiple Bible translations
- Fast navigation between books and verses
- Cross-references
- Bookmarks and tags
- Verse highlighting
- Powerful search
- Reading history
- Clean and distraction-free interface

## Fork Notice

This repository is an independent fork of BibleQuote for Android.

- Original project and earlier code history remain credited to their authors.
- New changes in this fork are maintained independently.
- Distribution remains under Apache License 2.0 (see `LICENSE`).

## Developer Setup (Quick Start: 3-5 minutes)

### 1) Requirements

- JDK 17
- Android SDK (with platform/build tools required by the project)
- `adb` available in `PATH`
- Android device or emulator

### 2) Create or connect a local debug keystore

The debug build is signed with a local keystore. Do not commit keystore files to git.

Create a local keystore in the project root:

```bash
keytool -genkeypair -v \
  -keystore debug-bibleaxis.keystore \
  -alias bibleaxisdebug \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass android -keypass android \
  -dname "CN=BibleAxis Debug, OU=Development, O=Local, L=Local, ST=Local, C=DE"
```

Add these values to `local.properties`:

```properties
DEBUG_STORE_FILE=debug-bibleaxis.keystore
DEBUG_STORE_PASSWORD=android
DEBUG_KEY_ALIAS=bibleaxisdebug
DEBUG_KEY_PASSWORD=android
```

If you already have a personal debug keystore, set its path and credentials in
`local.properties` instead.

### 3) Verify local setup

Run tests, build, and install:

```bash
./gradlew :bible:testDebugUnitTest
./gradlew :bible:assembleDebug
./gradlew :bible:installDebug
```

## Release Signing (no secrets in repo)

Release signing values must come from local machine settings or CI secrets, not from git.

Required fields in `local.properties` (or CI env/secret mapping):

```properties
STORE_FILE=/absolute/or/workspace-relative/path/to/release.keystore
STORE_PASSWORD=...
KEY_ALIAS=...
KEY_PASSWORD=...
```

Important: debug/fallback signing is acceptable for local development only.
It is not acceptable for publishing release builds to a store.

## Troubleshooting

- `keystore not found` or `bad password`: verify `DEBUG_*` or release `STORE_*`/`KEY_*` values in `local.properties`, check file path, alias, and passwords.
- Need to open app directly on device: use `adb shell am start -n de.wladimirwendland.bibleaxis.debug/de.wladimirwendland.bibleaxis.presentation.splash.SplashActivity`.

## Contacts

- Website: https://wladimir-wendland.de
- Privacy policy: https://wladimir-wendland.de/privacy
- Support: wladimir.wendland@gmail.com

## Credits

- Copyright (C) 2011 Scripture Software and contributors.
- Copyright (C) 2026 Wladimir Wendland.
