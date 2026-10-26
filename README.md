<div align="center">

<img src="docs/images/mandala-art-readme-banner.png" alt="Mandala Art — Color, Relax, Create" width="100%" />

# Mandala Art — Color, Relax, Create

**A calmer moment, one color at a time.**

Color • Relax • Create • Be Happy

[![Latest release](https://img.shields.io/github/v/release/SamoTech/mandala-art-android?display_name=tag&label=latest%20version)](https://github.com/SamoTech/mandala-art-android/releases/latest)
[![Android CI](https://github.com/SamoTech/mandala-art-android/actions/workflows/android-ci.yml/badge.svg?branch=main)](https://github.com/SamoTech/mandala-art-android/actions/workflows/android-ci.yml)
![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)

**[Download Latest APK](https://github.com/SamoTech/mandala-art-android/releases/latest) · [Release Notes](https://github.com/SamoTech/mandala-art-android/releases/latest) · [View Source](https://github.com/SamoTech/mandala-art-android)**

[Features](#features) • [Getting Started](#getting-started) • [Build & Test](#build-and-test) • [Privacy](#privacy)

</div>
---

## About

Mandala Art is an Android coloring app designed for calm, creative downtime. Browse mandala designs, color them with your preferred palette, and revisit your artwork. The app is built with **Kotlin** and **Jetpack Compose**.

> **Development status:** Debug and CI builds are for testing, not production distribution. Google test ad IDs are intentionally enabled during development.

## Features

- Mandala design gallery and collections.
- Coloring tools and color palettes.
- Personal artwork collection.
- Undo and redo support in the editing flow.
- Standard free designs remain available without watching ads.
- Optional bonus designs use rewarded ads; a design should unlock only after the Google Mobile Ads SDK confirms the reward.
- Ad failures or simply closing an ad must not grant a reward.
- Automated Android builds and tests through GitHub Actions.

Feature availability may change as development continues; check the current source and workflow for the exact implementation.

## Technology

| Area | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Build | Gradle |
| Platform | Android |
| Ads | Google Mobile Ads |
| CI | GitHub Actions |

## Getting Started

### Requirements

- Android Studio and a compatible Android SDK.
- A JDK compatible with the repository's Gradle wrapper.
- Android 7.0 (API 24) or later, matching the configured minimum SDK.

### Clone

```bash
git clone https://github.com/SamoTech/mandala-art-android.git
cd mandala-art-android
```

Open the project in Android Studio and allow Gradle sync to complete.

## Build and Test

**Windows — build debug APK**

```powershell
.\gradlew.bat assembleDebug
```

**macOS/Linux — build debug APK**

```bash
./gradlew assembleDebug
```

**Run unit tests**

```bash
./gradlew :app:testDebugUnitTest
```

On Windows, use ` .\gradlew.bat ` instead of ` ./gradlew `. The debug APK is normally written to `app/build/outputs/apk/debug/app-debug.apk`.

## Continuous Integration and Releases

- [Android CI workflow](https://github.com/SamoTech/mandala-art-android/actions/workflows/android-ci.yml)
- [Workflow runs](https://github.com/SamoTech/mandala-art-android/actions)
- [Download the latest production APK](https://github.com/SamoTech/mandala-art-android/releases/latest)
- [All GitHub Releases](https://github.com/SamoTech/mandala-art-android/releases)

On every successful push to `main`, GitHub Actions builds the debug variant, runs unit tests, and then builds and signs the production APK. The workflow verifies the APK signature, publishes the APK and `SHA256SUMS.txt` as direct assets on a GitHub Release, and keeps a workflow artifact as a backup.

Production releases receive a unique tag in the form `v1.0.RUN_NUMBER`. The Android `versionCode` is set to the monotonically increasing GitHub Actions run number, while `versionName` and the release tag are generated from the configured base version and that run number. This supports Android in-place upgrades, provided the same application ID and compatible signing key are retained.

Debug APKs use the Android debug signing configuration and test ad IDs. They are for development/testing only. Production builds use the configured release signing key and production ad configuration. Before distributing publicly, review privacy/consent requirements, ad policy compliance, store listing, and release readiness.

## Rewarded Ads: Development Safety

Keep `AdMobConstants.USE_TEST_ADS = true` while developing. Do not replace test IDs with production ad unit IDs until the release configuration has been deliberately reviewed.

- Grant bonus access only from the SDK's reward-earned callback.
- Do not grant rewards when an ad fails to load, fails to show, or closes without a confirmed reward.
- Ensure a single ad cannot grant duplicate rewards.
- Review Google Mobile Ads policies, consent requirements, and Google Play Data safety requirements before release.

## Project Structure

```text
mandala-art-android/
├── .github/workflows/android-ci.yml
├── app/src/main/java/com/example/
│   ├── MainActivity.kt
│   └── ads/AdMobManager.kt
├── app/src/test/
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

This is a high-level guide; verify exact paths against the current repository tree.

## Banner and Screenshots

The banner referenced above should be committed at `docs/images/mandala-art-readme-banner.png`. Promotional mockups are illustrative, not verified screenshots of the current app. Add genuine app screenshots when available.

## Privacy

Verify the actual implementation's storage, analytics, advertising, and network behavior before making privacy claims. Do not state that all data stays local or that no data is transmitted unless the code and SDK configuration have been audited. Publish a privacy policy and complete applicable Google Play Data safety declarations before release.

## Contributing

For bug reports, include the device model, Android version, steps to reproduce, expected and actual results, and relevant logs with personal information removed. Include the app version or commit SHA when possible. Run relevant Gradle tests before submitting a pull request.

## License

No license is asserted by this README. Add the chosen license file before describing the project as MIT-licensed or otherwise open source.

---

<div align="center">
<sub>Built with Kotlin and Jetpack Compose · Maintained by <a href="https://github.com/SamoTech">SamoTech</a></sub>
</div>
