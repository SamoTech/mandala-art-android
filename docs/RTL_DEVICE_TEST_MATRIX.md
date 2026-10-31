# RTL and Accessibility — Physical Device Verification Matrix

**Status:** Pending physical-device execution  
**Roadmap milestone:** M1 — Arabic localization and accessibility foundation  
**Related audit:** [LOCALIZATION_AUDIT.md](LOCALIZATION_AUDIT.md)

## Purpose and limits

The automated tests in PRs #48–#49 verify resource parity, Arabic/English resource resolution, manifest RTL opt-in, and Android locale layout direction. They do **not** prove that the actual Compose screens render correctly, that TalkBack traversal is useful, or that text remains legible at larger font scales.

This matrix is the required manual acceptance record. Do not mark a test passed until it has been executed on a physical device and evidence has been recorded.

## Test setup

Record before testing:

| Field | Value |
|---|---|
| Device model | Pending |
| Android version / build | Pending |
| App commit / version | Pending |
| Arabic locale | Pending |
| English locale | Pending |
| Display size / resolution | Pending |
| Font scale | 100%, then 130% and 150% |
| TalkBack version / state | Pending |
| Test date and tester | Pending |

Use the same device and display settings for Arabic and English runs where practical. Clear only app test data when needed; do not use a real account or valuable artwork for destructive test cases.

## Screen and interaction matrix

| Screen / area | Arabic RTL checks | English LTR checks | Evidence / result |
|---|---|---|---|
| Home | Start/end alignment, cards, category chips, navigation actions, Arabic shaping | Original English copy and expected LTR order | Pending |
| Gallery | Grid order, horizontal scrolling, category filters, progress labels, back affordance | LTR grid/scroll and labels | Pending |
| My Art | Grid order, empty state, favorite/delete actions, confirmation dialog | LTR order and unchanged actions | Pending |
| Editor | Toolbar order, undo/redo direction, palette order, canvas controls, dialogs | LTR toolbar and palette behavior | Pending |
| Color palette | Tab order, selected state, custom color dialog, sliders/values | LTR tab and value presentation | Pending |
| Settings | Rows, switches, account/cloud sections, legal disclosures, back affordance | LTR layout and labels | Pending |
| Dialogs / bottom sheets | Title/body/button order, dismissal, button reachability, mixed-direction text | LTR order and no regressions | Pending |

## Typography and mixed-direction checks

For each screen above, inspect:

- Arabic letter shaping and joining; no isolated or disconnected glyphs where joining is expected.
- Fallback font coverage for Arabic punctuation, numerals, and symbols.
- Mixed Arabic/Latin strings, especially email addresses, artwork titles, version strings, and numeric values.
- Ellipsizing, wrapping, clipping, overlapping, and baseline alignment.
- Font scales at 100%, 130%, and 150%; controls must remain reachable and text must not overlap.
- No directional icon is misleading; back/forward and undo/redo semantics match the action.

## Accessibility checks

With TalkBack enabled:

- Navigate each screen using swipe navigation and verify a logical reading order.
- Confirm every icon-only actionable control has a meaningful Arabic or English label.
- Confirm decorative images/icons do not create redundant announcements.
- Confirm favorite state, locked state, progress, selected palette, and completion are announced as semantics rather than communicated by color alone.
- Confirm dialogs announce their title and content and that focus remains inside until dismissed.
- Confirm touch targets are practical and controls can be activated without relying on visual position alone.

## Defect recording

For every failure, capture:

1. Screen and exact interaction.
2. Locale, Android version, device, and font scale.
3. Expected result and actual result.
4. Screenshot or short screen recording with personal data excluded.
5. Whether the defect is RTL-only, both locales, or accessibility-only.
6. Reproduction frequency and retest result after the fix.

Do not include email addresses, account identifiers, private artwork, or other personal data in repository evidence.

## Exit criteria

M1 physical verification can be marked complete only when:

- All listed screens and dialogs have been exercised in Arabic and English.
- No blocking clipping, overlap, wrong-direction affordance, or broken horizontal navigation remains.
- Arabic shaping and mixed-direction content remain readable at the tested font scales.
- TalkBack labels, state announcements, and traversal order are acceptable.
- Defects are fixed in reviewed PRs and the full CI suite passes for the final code.
- Device metadata and evidence are recorded without exposing personal data.

**Current result:** Not run. This document is a test plan, not evidence that device verification has passed.
