# Gradle wrapper

`gradle-wrapper.jar` is a binary and is intentionally not committed here.
It is generated automatically the first time you:

- **Open `car-spotter/android/` in Android Studio** (recommended) — it syncs and
  creates the wrapper jar for you, or
- run `gradle wrapper --gradle-version 8.9` if you have a system Gradle installed.

After either step, `./gradlew assembleDebug` works as documented in the README.
