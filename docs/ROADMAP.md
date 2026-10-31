# Mandala Art — Product & Engineering Roadmap

> **Status:** Living execution plan  
> **Baseline reviewed:** 2026-10-31  
> **Repository:** [SamoTech/mandala-art-android](https://github.com/SamoTech/mandala-art-android)  
> **Current planning baseline:** `main` at `ab70db9c085410644baee1ba3362fe3d3100b327`

This document is the canonical roadmap for product and engineering work on Mandala Art. New work should follow the sequence, dependencies, acceptance criteria, and delivery rules below. It is intentionally milestone-based rather than date-based: a milestone advances only when its exit criteria are demonstrated.

## 1. Product direction

Build a reliable, delightful Android mandala-coloring studio that works well offline, protects each user's artwork, supports Arabic and English, and can be distributed through Google Play. Expand the creative toolset only after save/sync reliability and release quality are established.

### Product principles

1. **Protect artwork first.** Local saves, account isolation, deletion semantics, and cloud reconciliation take precedence over new features.
2. **Offline-first.** Core coloring and access to already available designs must remain usable without a network connection. Cloud features must fail clearly and must not silently discard local work.
3. **No false completion.** A sync/save operation may report success only when the relevant data is durably persisted or reconciled.
4. **Measure before optimizing.** Performance work must be supported by profiling or reproducible measurements, not assumptions.
5. **Accessible by design.** Arabic RTL, scalable layouts, touch targets, contrast, TalkBack labels, and tablet/S Pen support are part of product quality.
6. **Privacy and safety.** Minimize collected data, secure account-scoped cloud access, provide compliant deletion paths before public release, and moderate any user-generated community content.
7. **Small, reviewable changes.** Each pull request should deliver one coherent unit with tests and explicit acceptance criteria.

## 2. Current baseline — verified implementation inventory

The following reflects a source review of `main` on 2026-10-31. “Implemented” means code exists; it does not imply every edge case has complete regression coverage or that a feature has been released through Google Play.

| Area | Current implementation | Roadmap status |
|---|---|---|
| Local artwork persistence | Room database and account-scoped owner partitions | Implemented; continue regression testing |
| Cloud progress sync | Firestore merge with field revisions and account checks | Implemented; harden edge cases |
| Concurrent local/cloud sync | Bounded reconciliation loop; server-only ID discovery | Merged in PR #30; CI passed |
| Account deletion | In-app reauthentication, server-side deletion lock, cloud/local cleanup | Merged in PR #29; pre-Play compliance review remains |
| Favorites | Separate favorite revision/actor; update path also updates edit timestamp | Implemented; add/retain focused persistence and conflict tests |
| Delete/reset semantics | Tombstones, deletion revisions, explicit restoration proof | Implemented; expand adversarial regression tests as needed |
| Autosave | Conflated worker, debounce, explicit flush, background error callback, owner-bound ViewModel persistence, ON_STOP flush in ViewModel scope | M0 regression coverage merged in PRs #32, #36, and #37; lifecycle test covers ON_STOP, not process death |
| Daily Inspiration | Stable date-based design selection; Home screen keys it by local date | Implemented; verify timezone/day-boundary behavior |
| Share image rendering | Bitmap rendering on IO dispatcher; age-based cache cleanup | Implemented; verify memory and cleanup behavior |
| Arabic localization | English default resources plus Arabic `values-ar/strings.xml` (164 matching keys); resource integrity and locale smoke tests merged in PRs #48–#49; rendered RTL/device verification pending | In progress — physical UI/accessibility validation remains |
| Release minification | Release `isMinifyEnabled = false` | Not implemented |
| Gallery caching | No dedicated image/card cache established in reviewed source | Profile first; do not add speculative cache |
| Random design controls | Existing algorithmic design library/generator | User-facing parameter controls not yet established |
| Color by Number | No complete mode established | Planned |
| Gradient/preset palettes | Custom color and palette UI exist; gradient/preset feature scope needs definition | Planned / validate current coverage |
| S Pen/tablet UX | No complete dedicated workflow established | Planned |
| Daily challenge/streak/achievements | Existing progress/level concepts may be reusable; feature implementation not established | Planned |
| Time-lapse | No recording/export pipeline established | Planned |
| Community gallery | Firestore is present for private progress; public UGC architecture is not established | Later-stage proposal |
| Google Play distribution | CI has an AAB build path; Play Console publication and production rollout are not established | Release milestone |

**Important:** Do not reopen work merely because it appears in the original idea list. First inspect the current implementation and tests. For example, favorite timestamps, tombstones, Daily Inspiration, and off-main-thread share rendering already have implementation paths. The next work should verify their behavior, close missing coverage, and fix only demonstrated defects.

## 3. Milestones and execution order

### M0 — Data integrity and editor lifecycle (P0, completed)

**Goal:** Never lose a user's latest coloring edits, resurrect a deleted artwork incorrectly, or mix data between accounts.

Work items:

- [x] Reproduce and audit the editor autosave path when Firebase account changes while the editor is open.
- [x] Trace the complete lifecycle: editor ViewModel ownership, navigation stack reset, `viewModelScope` cancellation, pending worker signal, `flushPendingSave()`, and repository owner UID.
- [x] Define an explicit policy for pending edits on account switch: persist to the original owner partition before disposal, or block/suspend the switch until a bounded flush completes. Never silently save to the newly active account.
- [x] Cover a pending editor save across an account switch before debounce.
- [x] Add deterministic UID-ownership tests for account switches during an in-flight save and after a failed save/retry.
- [x] Add a lifecycle integration test proving ON_STOP flush completes without crossing account ownership.
- [x] Surface background autosave and ON_STOP failures as an actionable in-app error state with an explicit retry path; retain the pending snapshot until persistence succeeds (PR #39).
- [x] Add focused DAO/repository tests proving favorite changes persist the timestamp and independent favorite revision without overwriting newer progress.
- [x] Expand sync/tombstone tests for offline edits, delete-vs-edit races, restore proof, repeated sync, and account isolation.
- [x] Verify sync failure is surfaced to the UI and never reported as success when server access or reconciliation fails.

**Acceptance / exit criteria:**

- A deterministic regression test proves the newest submitted snapshot is persisted to the correct UID partition before debounce, during an in-flight save, after a failed save/retry, and when the host lifecycle reaches ON_STOP.
- No pending editor snapshot can be written under a different account UID at the tested debounce, in-flight, retry, and ON_STOP boundaries.
- Failed saves remain recoverable through an explicit retry/flush path and expose an actionable error state.
- Favorite and tombstone tests cover both local persistence and merge behavior.
- Firestore emulator security tests, Android unit tests, lint, and debug build pass.
- Manual test on a physical device: edit, switch account from another screen/session, return/sign in, and verify each account sees only its own data.

**Completion evidence:** PRs #29–#39 are merged. PR #36 added deterministic in-flight save and failed-save retry coverage. PR #37 added an ON_STOP lifecycle integration test using the real editor ViewModel, in-memory Room, and owner-bound persistence callback; it verifies that a lifecycle-triggered flush remains attached to the original account when the active account changes. PR #39 added retained autosave error state, an in-editor Retry/Keep Editing dialog, and deterministic tests for debounce failure, ON_STOP failure, failed retry retention, and successful retry. The post-merge workflow for commit `f09fecfc0655feb273beac937a8737ae6ee7a305` passed Android debug build, unit tests, lint, and Firestore security rules. The production release job was skipped as expected. Physical-device account-isolation verification was reported complete by the user on 2026-10-31; the ON_STOP integration test is Robolectric-based and does not claim physical-device process-death coverage. Automated sync tests exercise the deterministic reconciliation/merge layer, not live Firestore end-to-end tests. All defined M0 exit criteria are now met; process-death and live Firestore E2E remain outside the demonstrated coverage.

### M1 — Arabic localization and accessibility foundation (P1)

**Goal:** Deliver a complete Arabic experience without breaking English or RTL layouts.

Work items:

- [x] Audit existing string resources, hardcoded UI text, dynamic labels, accessibility descriptions, and RTL-sensitive screens (see `docs/LOCALIZATION_AUDIT.md`).
- [x] Add `app/src/main/res/values-ar/strings.xml`; keep the default `values/strings.xml` as English (164 matching keys; current PR).
- [x] Replace user-visible strings in the audited screens with Android string resources, including dialogs, errors, empty states, ads/reward messaging, account flows, and identified accessibility descriptions (PRs #43–#47).
- [x] Review pluralization and formatting placeholders; Arabic plural forms and parity checks are included in the current PR. Continue to review new strings as they are added.
- [ ] Validate RTL mirroring for navigation, gallery, editor controls, progress indicators, dialogs, and settings.
- [ ] Review font fallback, Arabic shaping, truncation, line wrapping, and large-font scaling.
- [x] Add resource integrity and Arabic/English locale smoke tests (PRs #48–#49).
- [ ] Verify rendered Compose layout, typography, TalkBack semantics, and critical screens on physical devices in Arabic and English (see `docs/RTL_DEVICE_TEST_MATRIX.md`).

**Acceptance / exit criteria:**

- No user-facing screen contains untranslated hardcoded English except approved brand/product names or third-party SDK UI.
- All placeholders and plurals are valid in both locales.
- Arabic screens render RTL correctly; icons with semantic direction are mirrored where appropriate.
- English remains unchanged and regression tests pass.

### M2 — Release hardening, performance, and observability (P1)

**Goal:** Reduce release risk and improve performance based on measurements.

Work items:

- [ ] Establish release baseline: APK/AAB size, startup time, gallery first-render time, editor frame performance, and share memory peak.
- [ ] Enable R8/minification only in a dedicated change. Inspect dependencies and reflection/serialization use; add narrow keep rules only when evidence requires them.
- [ ] Build and install minified release APK/AAB; run smoke tests across account, Firestore, Room, ads, navigation, and share flows.
- [ ] Compare release size and startup/performance before and after minification.
- [ ] Profile gallery scrolling and image/design loading on a low-memory or lower-tier Android device.
- [ ] Add gallery caching only if profiling demonstrates repeated expensive work. Define cache key, memory budget, eviction, invalidation, and lifecycle behavior.
- [ ] Review share rendering for bitmap allocation, cancellation, partial-file cleanup, cache size limits, and concurrent share requests.
- [ ] Add actionable diagnostics for save/sync failures without logging email, passwords, artwork contents, or other sensitive data.

**Acceptance / exit criteria:**

- R8 release build installs and passes smoke tests; no broad keep rules without justification.
- Measured size/performance comparison is attached to the PR.
- Gallery cache (if justified) has bounded memory and tests for invalidation/eviction.
- Share export does not block the main thread and cleans up failed/expired files.
- No sensitive user data is written to logs.

### M3 — Creative tools: generator, palettes, and Color by Number (P2)

**Goal:** Increase creative variety while reusing the current vector/region coloring engine where practical.

Work items:

- [ ] Audit generator model and current design parameters before exposing controls.
- [ ] Define a deterministic recipe model: symmetry, petal/sector count, ring count, complexity, and seed.
- [ ] Add a user-facing Random Mandala flow with bounded parameter ranges and a reproducible seed so a design can be reopened/shared consistently.
- [ ] Validate generated geometry for closed paths, non-overlapping/valid regions where required, reasonable region counts, and rendering performance.
- [ ] Define preset palettes and gradient semantics. Specify whether gradients apply per region, across the whole artwork, or to selected regions.
- [ ] Add Color by Number as a separate editor mode with stable region-to-number mapping, palette legend, progress, and accessibility labels.
- [ ] Keep existing coloring, undo/redo, autosave, reset, share, and sync behavior working in every mode.

**Acceptance / exit criteria:**

- Same recipe seed and parameters produce the same design.
- Generator input limits prevent pathological geometry or excessive memory use.
- Every generated region can be hit-tested and colored reliably.
- Color by Number survives process death, restore, and cloud sync without losing its palette mapping.
- Undo/redo, save, share, and reset tests cover new modes.

### M4 — Engagement: daily challenge, streaks, and achievements (P2)

**Goal:** Encourage meaningful repeat use without manipulative or fragile reward mechanics.

Work items:

- [ ] Define a daily challenge contract: design/recipe, local-day identity, completion rule, and reset behavior.
- [ ] Make challenge selection deterministic for a given day and version.
- [ ] Define streak semantics for timezone changes, offline completion, missed days, and device clock changes.
- [ ] Store challenge completion idempotently; prevent duplicate rewards from retries or multiple devices.
- [ ] Define achievement criteria and ensure they can be recomputed or safely reconciled.
- [ ] Keep notifications opt-in and avoid excessive reminders.

**Acceptance / exit criteria:**

- A challenge remains stable for the intended local day and handles timezone/day-boundary changes explicitly.
- Offline completion reconciles once after reconnecting.
- Streaks and achievements cannot be duplicated or inflated by repeated requests.
- Privacy settings and notification controls are clear.

### M5 — Advanced creation: S Pen, tablets, and time-lapse (P3)

**Goal:** Make the editor comfortable on larger screens and precise input devices.

Work items:

- [ ] Establish responsive layouts for phones, foldables, and tablets.
- [ ] Test touch targets and canvas scaling in portrait and landscape.
- [ ] Add stylus input support only after defining expected behavior for hover, pressure, barrel button, palm rejection, and eraser hardware.
- [ ] Keep touch interaction fully supported when no stylus is present.
- [ ] Design time-lapse capture as a lightweight sequence of edit events or checkpoints, not continuous full-resolution bitmap snapshots by default.
- [ ] Define recording consent, pause/resume, storage limits, export format, cancellation, and cleanup.
- [ ] Ensure recording does not degrade editor frame rate or autosave reliability.

**Acceptance / exit criteria:**

- Phone layouts remain unaffected by tablet changes.
- Stylus and touch can be used predictably; palm rejection is validated on supported hardware.
- Time-lapse export is cancellable, storage-bounded, and does not capture private UI/account information.
- Battery, memory, and frame-time impact are measured.

### M6 — Community gallery and social features (P3, gated)

**Goal:** Introduce sharing between users only after a security, privacy, moderation, and operational design review.

Work items:

- [ ] Define the public artwork data model separately from private progress documents.
- [ ] Design Firestore rules for ownership, read visibility, write authorization, rate limits, and deletion.
- [ ] Decide whether community content is public, unlisted, or follower-based; document visibility before upload.
- [ ] Add report, block, takedown, and moderation workflows before opening uploads to users.
- [ ] Define content policy, abuse response, storage quotas, image processing, and retention.
- [ ] Provide deletion of published artwork and associated metadata.
- [ ] Evaluate backend cost and abuse scenarios before enabling broad access.

**Acceptance / exit criteria:**

- Security rules are tested in the Firebase emulator, including unauthorized reads/writes and cross-user access.
- Moderation/reporting and user deletion workflows are operational before public launch.
- Public content is never stored in or exposed through private account-progress paths.
- Privacy policy and in-app disclosures match actual behavior.

### M7 — Google Play readiness and launch (P0 for public release; execute after M0–M6 required gates)

**Goal:** Publish a stable, compliant Android App Bundle and operate releases safely.

Work items:

- [ ] Verify the signed AAB produced by CI and test it through an internal Play testing track.
- [ ] Complete Play Console app listing, screenshots, feature graphic, content rating, target audience, data safety, ads declaration, and app access instructions.
- [ ] Complete privacy policy and account/data deletion compliance, including any required external deletion request path.
- [ ] Review the planned authentication transition to Google Play Games Services before implementation. Define account linking, migration, guest/local data behavior, recovery, and deletion first; do not switch authentication as an incidental release change.
- [ ] Verify Play Games Services configuration, OAuth/client setup, tester access, and sign-in behavior if that authentication plan remains approved.
- [ ] Test upgrades from existing GitHub APK builds and preserve signing continuity where distribution requires it.
- [ ] Run internal testing, closed testing if required, staged rollout, crash/ANR monitoring, and rollback planning.
- [ ] Keep production publishing a deliberate release action; CI validation alone must not publish unexpectedly.

**Acceptance / exit criteria:**

- AAB is signed with the correct upload key and accepted by Play Console.
- Existing users have a documented, tested upgrade and account/data continuity path.
- Account deletion, privacy disclosures, ads declarations, and data safety answers match the shipped app.
- Critical flows pass on supported Android versions and representative devices.
- Rollout owner, monitoring, and rollback procedure are documented.

## 4. Priority queue — next execution order

The following is the working queue. Do not skip ahead because a later feature is more visible.

| Order | Work item | Priority | Status |
| 0 | Cold-start cloud restore and in-app Arabic/English selection | P0 | In progress — `fix/startup-cloud-restore-and-app-language` |
|---:|---|---|---|
| 1 | Autosave/account-switch lifecycle audit and regression tests | P0 | Completed — PRs #32, #36, #37, #39; post-merge CI passed |
| 2 | Favorite timestamp persistence and tombstone edge-case coverage | P0 | Completed — PR #33; post-merge CI passed |
| 3 | Offline sync and account-isolation regression coverage | P0 | Completed — PR #34; post-merge Android and Firestore CI passed; device verification user-reported |
| 4 | Arabic resources and RTL pass | P1 | PRs #48–#49 merged; resource/locale tests passed; physical RTL, typography, and TalkBack verification remains |
| 5 | Release R8/minification experiment with measured comparison | P1 | Planned |
| 6 | Profile gallery and share paths; optimize only measured bottlenecks | P1 | Planned |
| 7 | User-configurable deterministic mandala generator | P2 | Discovery |
| 8 | Gradient and preset palettes | P2 | Scope |
| 9 | Color by Number mode | P2 | Planned |
| 10 | Daily challenge, streak, achievements | P2 | Planned |
| 11 | Tablet and S Pen experience | P3 | Planned |
| 12 | Time-lapse export | P3 | Planned |
| 13 | Community gallery and moderation | P3 | Architecture gate |
| 14 | Google Play launch and auth/compliance transition | Release gate | Planned |

## 5. Delivery workflow — mandatory for every work item

Use this workflow for all future implementation. The roadmap is not permission to batch unrelated changes or bypass review.

1. **Select one queue item.** State its objective, affected components, dependencies, and explicit out-of-scope items.
2. **Inspect before editing.** Read current implementation, tests, schema, and recent history. Confirm whether the requested behavior already exists.
3. **Write acceptance criteria first.** Include normal behavior, failure behavior, offline behavior, account isolation, and relevant accessibility expectations.
4. **Create a focused branch.** Use `feat/<topic>`, `fix/<topic>`, `perf/<topic>`, or `docs/<topic>`. Never work directly on `main`.
5. **Implement the smallest safe change.** Preserve existing behavior and data compatibility. Add migration only when schema changes require it.
6. **Test proportionally.** Run targeted tests first, then the complete relevant unit suite, Firestore emulator rules tests, lint, and build. Add instrumentation/manual device checks for lifecycle, UI, performance, and Play-specific behavior.
7. **Review the diff.** Check scope, accidental changes, privacy/security, coroutine cancellation, account ownership, data migrations, and test quality.
8. **Open one PR per coherent work item.** Include problem, root cause, implementation, tests/results, risks, and manual verification steps. Keep it draft if acceptance criteria or evidence are incomplete.
9. **Merge only after evidence.** Required CI must pass; review findings must be resolved; the final head SHA must match the reviewed code. Do not merge based on a prior run for a different commit.
10. **Verify after merge.** Confirm the merge commit, main-branch CI, artifacts, and release behavior. Do not claim a release unless a release was actually published.
11. **Update this roadmap.** Change checkboxes/status only after the work is merged and verified. Record the PR/commit reference and any deferred follow-up.

### PR checklist

- [ ] One roadmap work item or tightly related subtask only.
- [ ] Acceptance criteria are listed in the PR description.
- [ ] Tests cover success, failure, and relevant race/offline/account cases.
- [ ] No secrets, personal data, or artwork content in logs.
- [ ] Database/schema compatibility reviewed.
- [ ] Diff reviewed; no unrelated generated files or formatting churn.
- [ ] CI checks passed for the exact PR head SHA.
- [ ] Review threads resolved.
- [ ] Post-merge main CI checked.
- [ ] Roadmap status updated after verification.

### Definition of Done

A task is **not done** merely because code was written or a PR was opened. It is done only when the acceptance criteria are met, automated checks pass, relevant manual/device checks are recorded, the change is merged, and post-merge CI is successful. A skipped release job is not a published release.

## 6. Technical guardrails

- Preserve account-scoped Room partitions and Firestore ownership checks.
- Do not remove tombstones or weaken deletion locks without a separately reviewed data-consistency design.
- Never silently switch the owner UID of a pending editor save.
- Do not treat Firestore cache data as proof of server synchronization or account deletion completion.
- Keep expensive bitmap rendering and file work off the main thread.
- Bound caches, retries, batch operations, and generated geometry.
- Keep debug/test ad identifiers out of production and production ad identifiers out of debug builds.
- Keep signing keys and secrets out of source control.
- Avoid adding dependencies unless a concrete requirement justifies the maintenance and security cost.
- Treat community uploads as untrusted user-generated content.
- Any authentication, schema, or deletion-contract change requires a migration and rollback plan.

## 7. Roadmap change control

This file should be updated when:
- a milestone is completed and verified;
- source inspection proves an item already exists or is obsolete;
- a new critical defect changes priority;
- product scope or a release requirement changes.

Every change to priority or scope must include a short reason. Do not silently reorder the queue. Keep completed work in the baseline/history rather than deleting it, and link the validating PR or commit where possible.

---

**Current next action:** Complete and verify the P0 cold-start cloud restore and in-app language selector on `fix/startup-cloud-restore-and-app-language`. After merge, resume physical-device RTL, typography, and TalkBack verification using `docs/RTL_DEVICE_TEST_MATRIX.md`; fix only reproducible issues, then close M1. PR #49 merged at `ab70db9c085410644baee1ba3362fe3d3100b327`; its PR CI passed, but no post-merge workflow run was returned. Preserve artwork IDs, persisted values, backend fields, and analytics names.