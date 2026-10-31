#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

NOTES='Added support for `draw`.
Second line stays on a separate line.
Literal shell text: $(touch SHOULD_NOT_RUN)'
(
  cd "$TMP_DIR"
  RELEASE_TAG='v1.2.3' NOTES="$NOTES" GITHUB_OUTPUT="$TMP_DIR/github-output" \
    bash "$ROOT_DIR/scripts/compose-release-body.sh"
)

test -s "$TMP_DIR/release-body.md"
test -s "$TMP_DIR/github-output"
grep -Fq '1. Download `Mandala-Art-v1.2.3.apk` from the Assets section to install directly on an Android device.' "$TMP_DIR/release-body.md"
grep -Fq '2. Download `Mandala-Art-v1.2.3.aab` to upload the app bundle to Google Play Console.' "$TMP_DIR/release-body.md"
grep -Fq 'AAB signature was verified with `jarsigner`.' "$TMP_DIR/release-body.md"
grep -Fq 'Added support for `draw`.' "$TMP_DIR/release-body.md"
grep -Fq 'Second line stays on a separate line.' "$TMP_DIR/release-body.md"
grep -Fq 'Literal shell text: $(touch SHOULD_NOT_RUN)' "$TMP_DIR/release-body.md"
! grep -Fq '\n' "$TMP_DIR/release-body.md"
test ! -e "$TMP_DIR/SHOULD_NOT_RUN"

DELIMITER="$(sed -n '1s/^body<<//p' "$TMP_DIR/github-output")"
test -n "$DELIMITER"
test "$(grep -Fc "$DELIMITER" "$TMP_DIR/github-output")" -eq 2
sed '1d;$d' "$TMP_DIR/github-output" > "$TMP_DIR/extracted-body.md"
cmp "$TMP_DIR/release-body.md" "$TMP_DIR/extracted-body.md"

echo 'Release body smoke test passed.'
