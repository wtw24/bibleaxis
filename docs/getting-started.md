[Back to README](../README.md) · [Architecture →](architecture.md)

# Getting Started

## Prerequisites

| Tool | Required version | Notes |
|------|------------------|-------|
| JDK | 17 | Matches `sourceCompatibility`/`jvmTarget` |
| Android SDK | Installed locally | Use SDK/build tools required by Android Gradle Plugin |
| adb | Available in `PATH` | Used for direct activity launch and device checks |
| Device/Emulator | Any Android target supported by app | Debug install target |

## Prepare Project Root

From the repository root, create a debug keystore if you do not already have one:

```bash
keytool -genkeypair -v \
  -keystore debug-bibleaxis.keystore \
  -alias bibleaxisdebug \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -storepass android -keypass android \
  -dname "CN=BibleAxis Debug, OU=Development, O=Local, L=Local, ST=Local, C=DE"
```

Add debug signing values to `local.properties`:

```properties
DEBUG_STORE_FILE=debug-bibleaxis.keystore
DEBUG_STORE_PASSWORD=android
DEBUG_KEY_ALIAS=bibleaxisdebug
DEBUG_KEY_PASSWORD=android
```

## First Local Run

Run unit tests, build debug APK, and install:

```bash
./gradlew :bible:testDebugUnitTest
./gradlew :bible:assembleDebug
./gradlew :bible:installDebug
```

## Verify App Launch

Use `adb` to launch the app entry activity:

```bash
adb shell am start -n \
  de.wladimirwendland.bibleaxis.debug/\
  de.wladimirwendland.bibleaxis.presentation.splash.SplashActivity
```

Expected result: Splash screen opens, then app navigates into the main reading flow.

## Next Steps

- Continue with project structure in [Architecture](architecture.md).
- Review development tasks in [Build and Test](build-and-test.md).
- Configure release signing in [Signing](signing.md) when preparing a release build.

## See Also

- [Architecture](architecture.md) - package map and boundaries
- [Build and Test](build-and-test.md) - daily development commands
- [Troubleshooting](troubleshooting.md) - setup and runtime issues
