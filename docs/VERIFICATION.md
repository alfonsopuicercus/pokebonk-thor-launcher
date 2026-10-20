# Verification — 2026-10-19

## Source readability cleanup — 2026-10-20

Current main uses consistent formatting, named controller codes/layout/timing constants, and a pinned formatter check. The 7 Java and 10 JavaScript tests pass, along with debug/release compilation, the optional test-probe build, and release lint. A Java differential trace compares all key codes 0–300 plus 25,000 deterministic input frames against the previous mapper. The bridge AST matches the previous version after accounting for named constants, local-variable names, and formatting; independent review also compares 10,000 bridge operations against the baseline.

This is a source-maintenance change. The distributed 0.3.0 community-preview APK and its original verification receipt remain attached to their release commit. Obtainium's prerelease setting is documented in the current README/install guide.

## 0.3.0 community preview

The initial Thor user reports that native play now works reasonably well. This is an informal device report, not exhaustive hardware certification.

The app icon now opens or resumes GameActivity immediately through the existing launcher component, which finishes without showing a menu. Help, diagnostics, optional browser fallback, and Close app are available through Start + Select or Android Back. App dialogs explicitly handle native A/B and start with the Continue row selected.

Fresh checks: 7 Java and 10 JavaScript unit tests pass; the official input-module/bridge regression passes 60 simulated seconds of centered menu input. Android debug and signed release builds pass, with lint reporting 0 errors and the same 3 reviewed warnings described below. The installed debug APK smoke test passes direct app-icon launch, WebView visibility without a chooser, in-app help and return using native A, diagnostics/release, native A after touch, D-pad selection, and native B cancelling browser options. The test now waits for the expected active UI before sending the next event; early harness failures included accessibility startup timing and Android's slower UI dump process. A separate, test-only instrumentation helper snapshots native dialogs without traversing game content.

The APK retains application ID com.pokebonk.thor and the existing signing certificate, with versionCode 3. No account/session migration or input-mapper change is introduced. Contribution guides, issue forms, a pull request template and read-only CI are included. CI builds/tests/lints; it does not sign or publish APKs.

Authenticated server-save restoration, broader physical controller/device coverage, speaker output and sleep/resume still need user or community checks. Detailed 0.2.0 reproduction and fixture evidence follow.

## 0.2.0 native controls evidence

User clarified the fault is **menu selection moving upward**. The original 0.1.0 Chrome launcher did not normalize input. Its standard-pad characterization was insufficient to establish physical Thor compatibility.

## Reproduction and regression

Against the official v0.7.5 gameplay bundle `main-CWBcjqTD-mvffzczn.js` (SHA-256 `9d2d8bd201fcb100ebbc87c97695e60a64b84a763e1896b648cb119426c7fae1`): a nonstandard pad first sampled at Y=+1, then released to zero, retained a +1 baseline and kept `navDir=up` for 60 simulated seconds. A resting fourth axis of -1 also caused camera motion. These reproduce software failure classes, not the user's unobserved physical readings.

With the native bridge and actual official input module: held-at-startup → 60 seconds centered → intentional Up → release passes with no menu repeat at rest.

Seven Java mapper tests pass: idle jitter over 36,000 samples, startup hold/release, sustained intentional hold, invalid values, independent buttons/hats/triggers, suspend/disconnect clear, range and right-stick-pair selection. Ten JavaScript bridge tests pass: native-only standard input, quick press/release queue, startup/release, stale/lifecycle handling, hidden/invalid frames, overflow release, origin/frame restriction, guaranteed initial neutral poll, disconnect queue flush, and immediate consumer release while rendering is paused. Tests were introduced before implementation; startup and disconnect regressions were observed failing before fixes.

## Rendered official game

Chrome at 960×540, with the official game and an isolated local backend fixture. Passed stable idle menu selection, D-pad release stopping repeat, guest-menu confirmation, centered player/camera, movement and stop-on-release, camera and stop-on-release, mapped action sampling, Start pause/resume, and suspension release. No page exceptions or backend mutation requests. This is fixture validation; no real sign-in or server save write.

## Android package

Android 15/API 35 ARM64 emulator runs the actual app WebView and accepts native Android gamepad/joystick events. A separate emulator-only instrumentation helper supplies joystick events; it is excluded from the game APK. The native run passed: native A confirms the guest fixture; idle menu focus stays stable; D-pad and joystick navigation stop repeating on release; native movement stops on release; Start pauses/resumes; Home releases held input and returning stays neutral. The audio context ran without a screen tap (speaker output was disabled in the emulator). Zero page exceptions and zero backend mutation requests. Native run results are retained with the vault handoff and release receipt.

Release uses the existing 0.1.0 signing key and application ID, versionCode 2. Android build, mapper tests and lint must pass; reviewed lint warnings concern required JavaScript, a newer back-navigation manifest attribute ignored on older Android, and backup-policy metadata. No JavaScript-to-Java interface is exposed; file/content access and mixed content are disabled. The APK requests INTERNET only, with normal HTTPS verification. External top-level links open outside the game view. The WebView session is separate from Chrome.

A separate source review found a queued-input replay on device removal. Fixed by clearing pending frames before replacement input; a regression now covers it. Native Home testing also exposed held game state surviving rendering suspension; suspension now invokes the game’s own blur release handlers, covered by a separate failing-then-passing regression. The review found no actionable origin/account exposure. QA initially encountered emulator cold-start/rendering warnings and fixture harness issues (onboarding timing, `paused` state naming, a legitimate before-navigation warning after a test run, and needing interval polling when animation frames stop in the background); those are distinguished from the reported menu drift.

## Acceptance still needed

Physical AYN Thor raw axis readings, screen placement/performance, speaker output, sleep/resume and restoring expected server progress with the user's own Pokébonk account remain unverified. Website updates can change input or login behavior. Google sign-in is not supplied because the website exposes no Google provider. Browser fallback retains the upstream browser input behavior.

Historical 0.1.0: source `a7c74b70349931f29ca6aa7e129628afb8eb3787`, launcher-only APK SHA-256 `43c5ef76f1630b4db32a46927815a0b2c1ec274caa55027ee31be2e25c3b5e2b`. Superseded for native control testing; original release remains available.
