[← Build and Test](build-and-test.md) · [Back to README](../README.md) · [Troubleshooting →](troubleshooting.md)

# Signing

## Security Rule

Do not commit keystore files or signing secrets. Keep values in local machine configuration (`local.properties`) or CI secret storage.

## Debug Signing

Debug signing is resolved by `debugSigningValues()` in `signing.gradle`.

- Defaults point to `debug-bibleaxis.keystore`
- Defaults use `android` passwords and alias `bibleaxisdebug`
- Values can be overridden via `DEBUG_*` keys in `local.properties`

Supported debug keys:

```properties
DEBUG_STORE_FILE=debug-bibleaxis.keystore
DEBUG_STORE_PASSWORD=android
DEBUG_KEY_ALIAS=bibleaxisdebug
DEBUG_KEY_PASSWORD=android
```

## Release Signing

Release signing is resolved by `releaseSigningValues()` in `signing.gradle`.

- If required release values are missing, release config falls back to debug values.
- Fallback is acceptable for local development only.
- Publishing builds must use proper release keystore credentials.

Required release keys:

```properties
STORE_FILE=/absolute/or/workspace-relative/path/to/release.keystore
STORE_PASSWORD=...
KEY_ALIAS=...
KEY_PASSWORD=...
```

## CI Guidance

Map CI secrets to the same keys used by `local.properties` before running release build tasks.

## See Also

- [Getting Started](getting-started.md) - debug setup and first run
- [Build and Test](build-and-test.md) - build task reference
- [Troubleshooting](troubleshooting.md) - key signing errors
