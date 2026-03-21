[← Signing](signing.md) · [Back to README](../README.md)

# Troubleshooting

## Signing Errors

### `keystore not found`

- Verify `DEBUG_STORE_FILE` or `STORE_FILE` path in `local.properties`.
- Confirm the file exists at that exact path.

### `bad password` or alias mismatch

- Check `DEBUG_STORE_PASSWORD`, `DEBUG_KEY_PASSWORD`, `DEBUG_KEY_ALIAS`.
- For release builds, check `STORE_PASSWORD`, `KEY_PASSWORD`, `KEY_ALIAS`.

### `Release signing is required for release tasks`

- Add all required release keys to `local.properties` (or CI secret store): `STORE_FILE`, `STORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
- Ensure all values are non-empty.

### `Release keystore not found`

- Verify `STORE_FILE` points to an existing file.
- Use an absolute path or a workspace-relative path from repository root.

## Build Issues

### Gradle/JDK mismatch

- Use JDK 17 locally, matching project build configuration.
- Re-import Gradle project after changing JDK in IDE.

### Android SDK not resolved

- Ensure Android SDK is installed and visible to Gradle.
- Sync project again after SDK updates.

## Runtime Launch Check

If install succeeds but app does not open from launcher, try direct start:

```bash
adb shell am start -n \
  de.wladimirwendland.bibleaxis.debug/\
  de.wladimirwendland.bibleaxis.presentation.splash.SplashActivity
```

## Where to Look Next

- Build/test task details: [Build and Test](build-and-test.md)
- Signing behavior details: [Signing](signing.md)
- Setup baseline: [Getting Started](getting-started.md)

## See Also

- [Getting Started](getting-started.md) - expected local setup
- [Build and Test](build-and-test.md) - standard verification flow
- [Signing](signing.md) - signing source and fail-fast behavior
