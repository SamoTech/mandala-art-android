<!--
  Mandala Art — Official product page
  Keep product claims aligned with the current app implementation.
-->

<div align="center">

<img src="docs/images/mandala-art-readme-banner.png" alt="Mandala Art — Color, Relax, Create" width="100%" />

<br />

# Mandala Art

### Color. Relax. Create.

A little space for creativity, calm, and color.

<br />

[![Latest version](https://img.shields.io/github/v/release/SamoTech/mandala-art-android?display_name=tag&style=for-the-badge&label=LATEST%20VERSION)](https://github.com/SamoTech/mandala-art-android/releases/latest)
[![Android CI](https://img.shields.io/github/actions/workflow/status/SamoTech/mandala-art-android/android-ci.yml?branch=main&style=for-the-badge&label=BUILD)](https://github.com/SamoTech/mandala-art-android/actions/workflows/android-ci.yml)
![Android](https://img.shields.io/badge/ANDROID-7.0%2B-3DDC84?style=for-the-badge&logo=android&logoColor=white)

<br />

## Make room for a little color

Explore mandala designs, experiment with color palettes, and build a collection of artwork that feels like yours.

<br />

[ **GET THE LATEST APK** ](https://github.com/SamoTech/mandala-art-android/releases/latest)

[Release notes](https://github.com/SamoTech/mandala-art-android/releases) · [Report an issue](https://github.com/SamoTech/mandala-art-android/issues) · [Explore the project](https://github.com/SamoTech/mandala-art-android)

</div>

---

## Your creative pause

Mandala Art is an Android coloring experience made for creative downtime. Choose a design, color it your way, and return to your artwork whenever you want another moment to create.

<table>
  <tr>
    <td width="33%" align="center">
      <h3>✦ Explore</h3>
      Browse mandala designs and collections.
    </td>
    <td width="33%" align="center">
      <h3>✎ Color</h3>
      Use coloring tools and palettes to make a design your own.
    </td>
    <td width="33%" align="center">
      <h3>♡ Keep creating</h3>
      Revisit your personal artwork collection and continue refining your creations.
    </td>
  </tr>
</table>

## Designed around creativity

- **Mandala gallery** — discover designs and collections.
- **Color palettes** — make each design your own.
- **Personal artwork** — keep track of your creations in the app.
- **Undo and redo** — step back and refine your work while editing.
- **Free designs** — standard designs are available without watching an ad.
- **Optional bonus designs** — rewarded ads may be used to unlock additional designs.

> Features can evolve between releases. Check the latest release notes for changes and fixes.

---

## Get Mandala Art

<div align="center">

### Ready to color?

Download the latest production APK from GitHub Releases.

[ **DOWNLOAD LATEST VERSION** ](https://github.com/SamoTech/mandala-art-android/releases/latest)

<sub>On the release page, open **Assets** and select the file named <code>Mandala-Art-*.apk</code>. Download only releases published by this repository.</sub>

</div>

### Install on Android

1. Open the [latest release](https://github.com/SamoTech/mandala-art-android/releases/latest).
2. Expand **Assets** and download the APK file.
3. Open the downloaded APK on your Android device.
4. If Android asks, allow installation from that source, then follow the on-screen prompts.

**Compatibility:** Android 7.0 (API 24) or later. Android may show a security prompt when installing apps from outside Google Play.

### Verify your download

Production releases include a `SHA256SUMS.txt` file. Use it to verify the APK checksum if you have a checksum utility available. A checksum confirms file integrity; it does not independently prove who published the file, so download from this repository's release page.

---

## Product details

| Detail | Information |
|---|---|
| Product | Mandala Art — Color, Relax, Create |
| Platform | Android |
| Minimum Android version | Android 7.0 (API 24) |
| App technology | Kotlin, Jetpack Compose |
| Distribution | APK through GitHub Releases |
| Latest version | [View latest release](https://github.com/SamoTech/mandala-art-android/releases/latest) |

## Privacy and ads

The app includes Google Mobile Ads integration, including optional rewarded ads for bonus designs. Advertising behavior, data handling, and consent requirements may vary by configuration and release. Review the app's current implementation and the applicable policies before making privacy assumptions. Do not assume that no data is transmitted.

## About this project

Mandala Art is maintained by [SamoTech](https://github.com/SamoTech). The repository includes the Android app source, automated build and test workflow, and production release process.

- [Latest release and changelog](https://github.com/SamoTech/mandala-art-android/releases/latest)
- [All releases](https://github.com/SamoTech/mandala-art-android/releases)
- [Build status and workflow](https://github.com/SamoTech/mandala-art-android/actions/workflows/android-ci.yml)
- [Issues and feedback](https://github.com/SamoTech/mandala-art-android/issues)

<details>
<summary><strong>For developers: build and test locally</strong></summary>

### Requirements

- Android Studio and a compatible Android SDK.
- A JDK compatible with the project's Gradle configuration.

### Clone the repository

```bash
git clone https://github.com/SamoTech/mandala-art-android.git
cd mandala-art-android
```

Open the project in Android Studio and allow Gradle sync to complete.

### Build a debug APK

**Windows PowerShell**

```powershell
.\gradlew.bat assembleDebug
```

**macOS / Linux**

```bash
./gradlew assembleDebug
```

### Run unit tests

```bash
./gradlew :app:testDebugUnitTest
```

On Windows, use ` .\gradlew.bat ` in place of ` ./gradlew `. The debug APK is normally generated at `app/build/outputs/apk/debug/app-debug.apk`.

Debug builds are for development and testing. They use the debug signing configuration and test ad IDs; they are not the recommended download for end users. Production builds are signed and published by the main-branch GitHub Actions workflow after the required checks succeed.

</details>

---

<div align="center">

**Mandala Art** · A little space to color, relax, and create.

Maintained with care by [SamoTech](https://github.com/SamoTech).

</div>
