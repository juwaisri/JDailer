# JDialer Production Release Checklist (APK Signing + Play Store)

## 1) Release build variants
- Ensure release variant builds:
  - `./gradlew assembleRelease`
  - artifact path: `app/build/outputs/apk/release/`
- For privacy-sensitive channels, verify:
  - `./gradlew testDebugUnitTest`
  - `./gradlew lintDebug`
  - `./gradlew test`

## 2) Prepare signing artifacts
Create a strong keystore:

```bash
keytool -genkeypair \
  -v \
  -keystore jdailer-release.jks \
  -keyalg RSA \
  -keysize 4096 \
  -validity 3650 \
  -alias jdailer-release
```

Create `keystore.properties` at repo root (do not commit secrets):

```properties
storeFile=jdailer-release.jks
storePassword=<keystore_password>
keyAlias=jdailer-release
keyPassword=<key_password>
```

`app/build.gradle.kts` reads this file at build time for release signing.

## 3) Build signed release locally

```bash
./gradlew clean assembleRelease
```

## 4) CI/CD release pipeline checks
- Workflow: `.github/workflows/build-apk.yml`
- Required actions:
  - Unit tests + lint
  - `assembleDebug`
  - `assembleRelease`
  - Upload both APK artifacts

## 5) Verify artifact integrity
- Ensure version metadata is correct:
  - `versionCode`
  - `versionName`
  - package name and appId suffixes (debug vs release)
- Confirm signed status:
  - `jarsigner -verify -verbose -certs app-release.apk`
  - or Android Studio > Build > Analyze APK

## 6) Play Console upload
- Use `app-release.apk` or App Bundle from CI.
- Fill release notes per track (internal → closed → production).
- Add `what's new` and permission disclosures.
- Verify:
  - `READ_SMS`, `SEND_SMS`, `READ_CALL_LOG`, `RECORD_AUDIO`, contacts, etc.
- Validate staged rollout with internal testing first.

## 7) Security and privacy controls to verify before ship
- Enable `IntegrationPrivacyPolicyStore` defaults:
  - third-party integrations on
  - media sharing off unless explicit policy changed
  - explicit consent toggle enforced in settings UI
- Confirm `PlatformIntentLauncher` returns `Unavailable` for blocked policy states.
- Validate no hard-coded API keys (spam endpoint remains placeholder until configured).
- Confirm crash handling paths are user-safe for integration failures.

