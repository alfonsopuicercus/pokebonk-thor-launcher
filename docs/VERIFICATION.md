# Verification — 2026-10-19

Official website inspected: Pokébonk v0.7.5. Published gameplay bundle `main-CWBcjqTD-mvffzczn.js`, SHA-256 `9d2d8bd201fcb100ebbc87c97695e60a64b84a763e1896b648cb119426c7fae1`.

## Controller characterization

Executed the actual published input/controller module in an isolated JavaScript environment with a simulated standard Gamepad and small DOM/input fixtures. Passed: analog movement/deadzone/release; jump, dash, interact, Ultimate, aim, trigger actions, map and pause key transitions; RB target lock; right-stick camera; D-pad zoom; disconnect release; menu confirmation and back; saved-binding remap behavior. This verifies the inspected code path, not physical hardware or browser Gamepad discovery.

Inspected the unmodified live account UI and published code. It offers guest, username/password login, and registration. No Google provider is exposed. No credentials, account creation, or authenticated saves were used.

## Android package

Built with JDK 17, Android SDK 35 and Gradle/AGP 8.8.2. Release compilation and Android lint pass (zero errors, one backup-policy warning; the launcher stores no account or game data). Signed APK verified with Android `apksigner`; final manifest has no requested permissions and no embedded WebView.

Installed the signed release on an Android 15/API 35 ARM64 emulator. A native gamepad A button on the initially focused Play button launched Chrome with ACTION_VIEW and the exact URL https://pokebonk.com/. Chrome was at its first-run screen, so no browser terms, account sign-in or game session were completed. An initial emulator System UI ANR occurred during cold boot; the game launcher process did not crash. A separate read-only source/documentation review found no actionable issues. Publication checks are recorded in the release and vault handoff.

## Acceptance still needed

Actual Thor browser discovery of the controller, dual-screen placement, graphics performance, speaker output, suspend/resume, and restoring expected progress with the user's own official Pokébonk account. Website updates can change input behavior or login methods. Real Google authentication cannot be verified or supplied while the website has no Google provider.
