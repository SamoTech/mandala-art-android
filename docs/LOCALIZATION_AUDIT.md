# M1 Localization & Accessibility Audit

**Repository baseline:** `main` at `ab70db9c085410644baee1ba3362fe3d3100b327`  
**Audit date:** 2026-10-31  
**Scope:** Android user-visible text, locale resources, accessibility labels, and RTL-sensitive UI.

## 1. Current localization baseline

- The default resource file remains English: `app/src/main/res/values/strings.xml`.
- Arabic resources now exist at `app/src/main/res/values-ar/strings.xml`.
- The current baseline contains 164 resource keys: 158 strings and 6 plural resources.
- The Arabic file contains the same 164 keys and resource types. All six Arabic plural resources define the six Arabic quantity categories: zero, one, two, few, many, and other.
- A resource integrity test compares English/Arabic key sets and types, checks required plural quantities, and compares formatting placeholders.
- The Android manifest declares `android:supportsRtl="true"`.
- Arabic RTL layout, typography, TalkBack traversal, and physical-device screenshots have not yet been verified. Resource parity is not evidence of correct RTL rendering.

## 2. Screen-level resource coverage

The following screen and dialog files were inspected after PRs #43–#47. Their visible labels, dialogs, toasts, and identified action descriptions resolve through Android string/plural resources rather than hardcoded English UI copy:

| File | Current status |
|---|---|
| `MainActivity.kt` | Reward prompts, level-lock dialogs, buttons, and feedback messages use resources |
| `HomeScreen.kt` | Home labels and category filter labels use resources; canonical artwork titles remain unchanged |
| `GalleryScreen.kt` | Gallery title, level/progress labels, and category labels use resources |
| `MyArtScreen.kt` | Empty states, filters, actions, delete confirmation, and feedback use resources |
| `EditorScreen.kt` | Editor hints, autosave/reset/reward dialogs, and feedback use resources |
| `CustomColorPickerDialog.kt` | Picker title, channel values, and action labels use resources |
| `ColorPaletteBar.kt` | Palette tab names and color-picker accessibility description use resources |
| `MandalaCard.kt` | Category/difficulty, state labels, and region counts use resources/plurals |
| `SettingsScreen.kt` | Settings, privacy/legal, account, cloud-sync, and deletion copy use resources |

This is a targeted source inspection of the files above, not a claim that every string in every source file or third-party SDK is localized. Re-scan newly added UI code in each future PR.

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

## 5. RTL source review and automated locale smoke coverage

Static review of the primary Compose screens after Arabic resource integration found:

- The manifest opts in with `android:supportsRtl="true"`.
- Back navigation in Gallery, My Art, Editor, and Settings uses `Icons.AutoMirrored.Filled.ArrowBack`; editor Undo/Redo also use auto-mirrored icons.
- No explicit `Arrangement.Left/Right`, `Alignment.Left/Right`, or left/right padding modifiers were found in the reviewed Home, Gallery, My Art, Editor, Palette, and Settings screen sources.
- Rows, horizontal scrolling containers, and lazy horizontal lists rely on Compose's current `LayoutDirection`; this is intentional but still requires rendered UI verification.
- Reviewed `contentDescription = null` usages are decorative icons or icons nested inside controls that also expose visible text. Icon-only controls in the reviewed flows use localized descriptions.

PR #49 merged Robolectric locale smoke tests that verify the manifest RTL flag, Arabic configuration direction and resource resolution, and unchanged English LTR/resource resolution. The PR's Android debug build, unit tests, Android lint, and Firestore security rules all passed on head `f0db056696d91cd578836e91e73cc5172da4aefa`; Codex review completed with no findings and no open review threads. These tests validate Android locale configuration; they do not prove Compose screenshot geometry, TalkBack traversal, or physical-device typography. No post-merge workflow run was returned for merge commit `ab70db9c085410644baee1ba3362fe3d3100b327`.

## 6. RTL and typography verification targets

- Home, gallery, My Art, editor toolbar/palette, settings, dialogs, and bottom sheets.
- Start/end alignment, horizontal scrolling, navigation/back affordances, and any direction-sensitive icons.
- Arabic shaping and fallback fonts, mixed Arabic/Latin text (email addresses, artwork names, numbers), truncation, line wrapping, and large font scaling.
- Touch target size and TalkBack traversal order in both locales.
- Preserve `supportsRtl=true`; verify actual Compose behavior rather than assuming the manifest flag alone guarantees correct layout.

## 7. Recommended implementation sequence

1. Extract remaining user-visible strings from screen and dialog code into default English resources.
2. Localize data-driven labels through resource IDs or UI-layer mappings; add plural and formatted resources.
3. Add `values-ar/strings.xml` with matching keys and valid placeholders.
4. Add resource integrity tests that compare required keys and validate formatting/plural definitions.
5. Add locale/RTL UI tests or screenshot coverage for critical screens, then perform manual device verification with Arabic and English system locales.
6. Keep artwork IDs, database values, analytics event names, Firebase fields, and other persisted/backend identifiers language-neutral and unchanged.

## 8. Out of scope for M1

- Translating third-party SDK-owned UI.
- Changing database identifiers, Firestore field names, analytics event names, or design IDs.
- Rewriting the visual design system or changing navigation behavior.
- Assuming that `supportsRtl=true` proves full RTL correctness.

**Current M1 status:** English UI extraction and dynamic-label resource mapping are merged in PRs #43–#47. Arabic translations and 164-key resource parity checks are merged in PR #48. Arabic RTL/English LTR locale smoke tests and static RTL source review are merged in PR #49. PR #49 CI passed on its exact head SHA and Codex reported no findings. Full rendered Compose RTL, typography, TalkBack, and physical-device verification remain open; M1 is not complete.
