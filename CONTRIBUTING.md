# Contributing

Thanks for helping improve Pokébonk for Thor. Reports from real devices are especially useful. This project maintains the Android app and controller bridge; the game and account server are operated by the official Pokébonk team.

## Reports and ideas

Search [existing issues](https://github.com/alfonsopuicercus/pokebonk-thor-launcher/issues) first, then use [the report forms](https://github.com/alfonsopuicercus/pokebonk-thor-launcher/issues/new/choose). Include the APK version, device/controller model, Android and Android System WebView versions, expected behavior, actual behavior, and reproduction steps. For drift, include the **Controller diagnostics** readings with the sticks released and say whether a touch mapper is enabled.

Please keep reports constructive. Remove passwords, tokens, personal account details, and private save data. Do not attach a full device log containing unrelated personal information. Check whether an issue also occurs in the official website's browser version when practical; indicate which environment you tested.

## Local development

Install JDK 17, Node.js 22 or newer, and Android SDK Platform 35 / Build Tools 35.0.0. Set `ANDROID_HOME` or `android/local.properties` (`sdk.dir=/absolute/sdk/path`). The Gradle wrapper is included; no global Gradle installation is required.

```sh
node --test tests/*.test.cjs
./android/gradlew -p android :app:testDebugUnitTest :app:assembleDebug :app:lintRelease
```

Install `android/app/build/outputs/apk/debug/app-debug.apk` on a test device. The `.debug` package installs alongside the published app. Debug builds enable WebView inspection; release builds do not.

| Path | Purpose |
|---|---|
| `android/app/src/main/java/com/pokebonk/thor/` | App entry, WebView lifecycle, native controller mapping, app dialogs |
| `android/app/src/main/assets/native-gamepad.js` | Standard Gamepad API bridge with input release handling |
| `android/app/src/test/` | Java controller regressions |
| `tests/*.test.cjs` | JavaScript bridge regressions |
| `tests/android-launch-smoke.cjs` | Installed APK startup and in-app options smoke test |
| `tests/android-input-probe/` | Separate emulator-only joystick injector |
| `docs/` | Input design, reproduction, verification history |

### Emulator smoke test

Use a dedicated, unlocked test emulator with the debug APK and the separate input probe installed. The probe reads native UI accessibility nodes without walking the game's web content. The test force-stops and opens the app, navigates its native dialogs, and sends input events. It never signs in or edits saves. Set `ADB` if `adb` is not on your PATH.

```sh
./android/gradlew -p android -PwithInputProbe :inputprobe:assembleDebug
adb -s emulator-5554 install -r tests/android-input-probe/build/outputs/apk/debug/inputprobe-debug.apk
ANDROID_SERIAL=emulator-5554 node tests/android-launch-smoke.cjs
```

The optional input probe builds with:

```sh
./android/gradlew -p android -PwithInputProbe :inputprobe:assembleDebug
```

It is a separate instrumentation APK and is never bundled into the game. Emulator tests do not certify physical Thor compatibility. For controller changes, check idle menus, intentional navigation and release, movement/camera release, Start pause/resume, Start + Select options, background/return, and controller disconnect. Use a test account or local fixture for gameplay testing, and describe the environment honestly.

## Pull requests

Fork the repo, make a focused branch, and open a pull request. Explain the problem, what changes for the user, and how you tested it. Add a regression for controller or lifecycle bugs. Documentation-only fixes need no new tests. GitHub Actions checks unit tests, Android compilation, and lint; physical device checks should be recorded in the PR.

Keep the official HTTPS origin and account handling intact. Do not commit credentials, signing files, user saves, downloaded game bundles, or third-party game assets. Contributions to this app are covered by its MIT license; game content has separate ownership.

## Release builds

`./android/gradlew -p android :app:assembleRelease` produces an unsigned APK without private signing files. Official updates use the maintainer's existing private signing key; contributor builds cannot replace an installed official release unless signed with that key. Use the separate debug package for development.

For maintainers: ignored `android/.signing/release.properties` supplies `storePassword` and `keyPassword` for `android/.signing/release.jks`, alias `release`. Keep these local. After verification, place release evidence in ignored `dist/QA.json` and run `node tools/package-release.cjs` to package the APK, install guide, QA receipt, and checksums. Review the APK version/signature and anonymously download the published assets to verify them before announcing a release. CI does not sign or publish releases.
