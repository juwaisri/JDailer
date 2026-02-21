# Repository Guidelines

## Project Structure & Module Organization
JDialer is a single-module Android app.
- `app/` is the only Gradle module.
- Kotlin sources live under `app/src/main/kotlin/com/jdailer/`:
  - `presentation/` UI screens (contacts, dialer, in-call, settings, widgets).
  - `service/` foreground services (bubble, call screening/recording).
  - `receiver/` boot and phone-state receivers.
  - `sync/` account and sync stubs.
  - `JDialerApplication.kt` is the app entry point.
- Resources are in `app/src/main/res/`, and the manifest is `app/src/main/AndroidManifest.xml`.
- Test directories are not present yet; add `app/src/test/kotlin` and `app/src/androidTest/kotlin` when introducing tests.

## Architecture Overview
- MVVM + Clean Architecture foundations (ViewModel, LiveData, Room, DataStore).
- Koin for dependency injection; coroutines + WorkManager for background work.
- UI uses Material 3 with a mix of Compose and view binding.

## Build, Test, and Development Commands
- Use the Gradle wrapper (`./gradlew`); it is committed to the repo for repeatable builds.
- `./gradlew assembleDebug` builds a debug APK (this is what CI runs).
- `./gradlew assembleRelease` builds a release APK with shrinking/Proguard enabled.
- `./gradlew detekt` runs static analysis using `app/config/detekt.yml`.

## Coding Style & Naming Conventions
- Kotlin + AndroidX, targeting Java 17 (per Gradle config).
- Use 4-space indentation and standard Kotlin naming: `PascalCase` classes, `lowerCamelCase` functions/vars, `UPPER_SNAKE_CASE` constants.
- Keep packages feature-focused (mirror `presentation/dialer`, `presentation/contacts`, etc.).
- Compose and view binding are both enabled; follow the style already used in nearby screens.

## Testing Guidelines
- Dependencies include JUnit4, MockK, coroutines-test, and Turbine for unit tests; AndroidX Test and Espresso for instrumentation.
- Place unit tests in `app/src/test/kotlin` and instrumentation tests in `app/src/androidTest/kotlin`.
- Run `./gradlew testDebugUnitTest` and `./gradlew connectedDebugAndroidTest` (requires a device or emulator).
- No explicit coverage thresholds are configured.

## Commit & Pull Request Guidelines
- Recent commits use short, imperative sentences without conventional-commit prefixes (e.g., "Add baseline app component stubs"). Keep that style.
- PRs should include: a clear summary, test notes, and screenshots/GIFs for UI changes.
- Link related issues when applicable and keep PRs focused on a single change.

## CI & Release
- `.github/workflows/build-apk.yml` builds a debug APK on push and pull requests.
- Artifacts are uploaded from `app/build/outputs/apk/debug/`.

## Configuration & Security Tips
- `build.gradle.kts` defines `SPAM_API_URL` as a placeholder; keep real endpoints or secrets in local `gradle.properties` or CI secrets.
- Release builds use Proguard (`app/proguard-rules.pro`); update rules when adding reflection-heavy libraries.
