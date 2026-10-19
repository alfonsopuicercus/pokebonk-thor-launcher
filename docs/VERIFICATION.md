# Verification — native 0.2.0 — 2026-10-19

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
