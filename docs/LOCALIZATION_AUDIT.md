# M1 Localization & Accessibility Audit

**Repository baseline:** `main` at `95bb4d659d015681cc94ddd58568ab8884c75cfa`  
**Audit date:** 2026-10-31  
**Scope:** Android user-visible text, locale resources, accessibility labels, and RTL-sensitive UI.

## 1. Current localization baseline

- The app has one default resource file: `app/src/main/res/values/strings.xml`.
- The default resource set is English-only.
- No `app/src/main/res/values-ar/strings.xml` directory exists yet.
- The Android manifest already declares `android:supportsRtl="true"`.
- Several screens already use `stringResource(R.string...)`, but the conversion is incomplete.
- There is no evidence yet of a complete Arabic locale test suite or screenshot verification.
- Existing autosave error dialog copy added in PR #39 is currently in the default English resource file and will need Arabic equivalents.

## 2. Screen-level hardcoded text inventory

The following files contain confirmed user-facing hardcoded English text or accessibility descriptions. The examples are representative, not an exhaustive literal-by-literal extraction; implementation PRs must re-scan each file before closing it.

| File | Confirmed hardcoded text / labels | Required work |
|---|---|---|
| `MainActivity.kt` | Ad unlock prompts, Watch Video, Later, Keep Coloring, Retry, Explore Free Designs, unlock/failure toasts | Move dialog, button, and toast copy to resources |
| `HomeScreen.kt` | Featured Artwork, All Mandalas, Tap to color | Convert remaining home labels; retain already resource-backed labels |
| `GalleryScreen.kt` | Back, Level locked | Resource-backed accessibility labels |
| `MyArtScreen.kt` | Back, Share, Delete, delete confirmation, artwork-removed toast | Resource-backed dialog, toast, and accessibility labels |
| `EditorScreen.kt` | Back, premium-color dialog, rewarded-ad feedback, artwork-reset toast | Resource-backed dialog, toast, and accessibility labels |
| `CustomColorPickerDialog.kt` | Select, Cancel | Resource-backed button labels |
| `ColorPaletteBar.kt` | Locked color | Resource-backed accessibility label |
| `MandalaCard.kt` | Favorite, Locked, Level, Bonus, Completed, areas | Resource-backed labels and plural resources |
| `SettingsScreen.kt` | Privacy/data-safety, licenses, consent, account/sync/import/delete flows, email/password labels, dialogs | Extract all account and settings copy, including conditional button states |

## 3. Data-driven labels that also need localization

These are not ordinary Compose literals, but they are displayed to users and must not be overlooked:

- `MandalaCategory` and `Difficulty` labels in `model/MandalaModels.kt`.
- Palette names in `model/ColorPalette.kt`.
- Level names in `model/LevelSystem.kt`.
- Category/difficulty strings assembled in `MandalaCard.kt`.
- Region-count text such as `N areas`, which requires a plural resource rather than string concatenation.
- Generator design titles in `generator/MandalaLibrary.kt`. These are canonical editorial artwork titles (for example, “Lotus Bloom” and “Golden Peacock”), not UI category labels. Keep them unchanged across locales in this milestone so artwork identity and user references remain stable; revisit translated display aliases only as a separate product decision. Never translate design IDs or persisted identifiers.

**Dynamic-label implementation note (PR #47):** category and difficulty labels, palette tab labels, and level-pack names are now resolved through Android string resources in the UI layer. Model enums and level progression logic no longer own English display strings. Palette identifiers remain stable for test tags. Canonical artwork titles remain intentionally untranslated.

## 4. Accessibility inventory

- Several navigation and action descriptions are hardcoded English (Back, Favorite, Share, Delete, Locked, Locked color, Level locked).
- Decorative artwork and visual-only icons currently use `contentDescription = null` in multiple places; verify that each is genuinely decorative and that its containing control has an accessible label.
- Every icon-only actionable control must expose a localized semantic label.
- Progress indicators and completion state need meaningful semantics, not only visible percentages or color.

## 5. RTL and typography verification targets

- Home, gallery, My Art, editor toolbar/palette, settings, dialogs, and bottom sheets.
- Start/end alignment, horizontal scrolling, navigation/back affordances, and any direction-sensitive icons.
- Arabic shaping and fallback fonts, mixed Arabic/Latin text (email addresses, artwork names, numbers), truncation, line wrapping, and large font scaling.
- Touch target size and TalkBack traversal order in both locales.
- Preserve `supportsRtl=true`; verify actual Compose behavior rather than assuming the manifest flag alone guarantees correct layout.

## 6. Recommended implementation sequence

1. Extract remaining user-visible strings from screen and dialog code into default English resources.
2. Localize data-driven labels through resource IDs or UI-layer mappings; add plural and formatted resources.
3. Add `values-ar/strings.xml` with matching keys and valid placeholders.
4. Add resource integrity tests that compare required keys and validate formatting/plural definitions.
5. Add locale/RTL UI tests or screenshot coverage for critical screens, then perform manual device verification with Arabic and English system locales.
6. Keep artwork IDs, database values, analytics event names, Firebase fields, and other persisted/backend identifiers language-neutral and unchanged.

## 7. Out of scope for the first extraction PR

- Translating third-party SDK-owned UI.
- Changing database identifiers, Firestore field names, analytics event names, or design IDs.
- Rewriting the visual design system or changing navigation behavior.
- Assuming that `supportsRtl=true` proves full RTL correctness.

**Exit condition for this audit:** all major user-facing text sources and dynamic labels have been identified and implementation boundaries are explicit. This audit does not claim that Arabic localization is implemented or complete.
