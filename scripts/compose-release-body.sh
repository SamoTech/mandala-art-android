#!/usr/bin/env bash
set -euo pipefail

: "${RELEASE_TAG:?RELEASE_TAG is required}"
: "${NOTES:?NOTES is required}"
: "${GITHUB_OUTPUT:?GITHUB_OUTPUT is required}"

{
  printf '%s\n' '## Mandala Art for Android' ''
  printf '%s\n' 'A signed production build of Mandala Art, a mandala coloring studio for Android.' ''
  printf '%s\n' '## Download and install' ''
  printf '%s\n' "1. Download \`Mandala-Art-${RELEASE_TAG}.apk\` from the Assets section to install directly on an Android device."
  printf '%s\n' "2. Download \`Mandala-Art-${RELEASE_TAG}.aab\` to upload the app bundle to Google Play Console. Do not install the AAB directly."
  printf '%s\n' '3. To update an existing installation from the APK, install over the current app. Do not uninstall the app or clear its data as part of a normal update.' ''
  printf '%s\n' '## Build and integrity' ''
  printf '%s\n' '- The debug build and unit tests passed in GitHub Actions before publication.'
  printf '%s\n' '- The production APK signature was verified with `apksigner`; the AAB signature was verified with `jarsigner`.'
  printf '%s\n' '- `SHA256SUMS.txt` contains SHA-256 checksums for both release artifacts.' ''
  printf '%s\n' '## Changes in this release' ''
  printf '%s\n' "${NOTES}"
} > release-body.md

# Use a per-run delimiter so generated release notes cannot terminate
# the multiline GITHUB_OUTPUT value early.
OUTPUT_DELIMITER="RELEASE_BODY_$(openssl rand -hex 16)"
{
  echo "body<<${OUTPUT_DELIMITER}"
  cat release-body.md
  echo "${OUTPUT_DELIMITER}"
} >> "${GITHUB_OUTPUT}"
