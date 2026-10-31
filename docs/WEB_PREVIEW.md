# Mandala Art Web Preview

## Purpose

Build a browser-based companion preview without changing the native Android application. The first release is a deliberately scoped coloring studio used to validate browser UX, responsive layout, SVG interaction, Arabic/English directionality, local persistence, and image export.

## Architecture decision

- Separate React + TypeScript + Vite application under `web/`.
- SVG is the rendering and hit-testing surface; each mandala region is an independently clickable SVG path.
- Geometry is generated in TypeScript for the web preview. The Android generator uses Android `Path` and `RectF`, so its Kotlin implementation is not directly portable to browsers.
- Artwork progress is stored locally in the current browser using localStorage.
- No Firebase authentication, Firestore sync, AdMob, account deletion, or cross-device sync is enabled in this preview. Do not imply that web artwork syncs with Android.
- Export is client-side PNG rendering from SVG; no artwork is uploaded to a server.

## Preview scope

- Responsive gallery with category filters.
- Eight generated sample mandalas.
- Region coloring, color palette, custom color input, undo/redo, reset.
- Save and restore artwork locally per browser.
- PNG export.
- Arabic/English interface direction toggle.

## Validation

CI runs TypeScript compilation, Vitest geometry/storage tests, and a production Vite build. This does not replace browser/device visual QA. Manual acceptance should cover current Chrome on desktop and Android, narrow mobile viewport, Arabic RTL and English LTR, region taps, undo/redo, persistence after reload, reset confirmation, and PNG export.

## Known limitations

- This is a web preview, not feature parity with Android.
- The sample SVG geometry is independently implemented and is not yet a lossless export of the Android MandalaLibrary.
- No cloud sync or account system; browser-local data can be cleared by the user or browser.
- Google Fonts are loaded remotely; a later production pass should decide whether to self-host fonts or use system fallbacks.
- Touch/S Pen behavior, screen-reader semantics for individual SVG regions, and real-device performance require dedicated QA before public launch.

## Next steps

1. Validate the preview in desktop and Android Chrome.
2. Fix geometry, touch, responsive, and RTL issues found during manual testing.
3. Decide whether to port/export the full Android design catalog into a shared platform-neutral format.
4. Define web authentication and cross-platform sync only after reviewing Firebase web app configuration, account lifecycle, and security rules.
5. Add production hosting and a web-specific privacy disclosure before public release.
