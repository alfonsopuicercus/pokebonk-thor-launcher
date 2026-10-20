# Pokébonk for AYN Thor

**Open the app. Play with your controller.**

An unofficial Android app that loads the official [Pokébonk](https://pokebonk.com/) online game and reads the Thor's sticks and buttons directly. No touch mapping, browser extensions, companion app, or startup browser chooser.

**[Download the APK](https://github.com/alfonsopuicercus/pokebonk-thor-launcher/releases/download/v0.3.0-preview/PokeBonk-Thor-Native-0.3.0.apk)** · [Installation guide](INSTALL.md) · [Report a bug or suggest a fix](https://github.com/alfonsopuicercus/pokebonk-thor-launcher/issues/new/choose)

## Get started

1. Download **PokeBonk-Thor-Native-0.3.0.apk** from the [release page](https://github.com/alfonsopuicercus/pokebonk-thor-launcher/releases/tag/v0.3.0-preview) on your Thor and install it. Existing users can update in place without uninstalling.
2. Keep the controls in normal Android gamepad mode and disable any touch-mapping overlay for this game.
3. Open **Pokébonk for Thor**. The game opens immediately. Use the left stick or D-pad to navigate and the bottom face button to confirm.

Requires **Android 8 or later**, internet, and a current **Android System WebView** capable of WebGL. This runs the online game inside the app; it does not install a ROM or offline copy. The APK is a **community preview**.

### Updates with Obtainium

Add [this repository](https://github.com/alfonsopuicercus/pokebonk-thor-launcher) as a GitHub source in Obtainium and enable **Include prereleases** (called **Allow prereleases** in some versions). Releases are currently tagged as community previews, so a stable-only filter will not find them. Use the repository URL rather than a link to one APK so future releases can be detected. See [installation and updates](INSTALL.md) and [Obtainium's GitHub source documentation](https://wiki.obtainium.imranr.dev/sources/#github).

## Controls

Positions below use Xbox-style names; the letters printed on your device may differ. The game's **Settings → Controls** can remap actions.

| Input              | Gameplay            | Menus                |
| ------------------ | ------------------- | -------------------- |
| Left stick         | Move                | Navigate             |
| Right stick        | Camera              | Scroll               |
| Bottom face / A    | Jump; hold to glide | Confirm              |
| Right face / B     | Dash                | Back                 |
| Left face / X      | Interact            | Reroll where offered |
| Top face / Y       | Ultimate            | Skip where offered   |
| LB / RB            | Aim / target lock   | Previous / next tab  |
| LT / RT            | Dash / Ultimate     | —                    |
| Start / Select     | Pause / map         | Start goes back      |
| D-pad              | Up/down zoom        | Navigate             |
| **Start + Select** | **App options**     | **App options**      |

**Start + Select** or **Android Back** opens controls help, controller diagnostics, an optional browser fallback, and Close app. Continue returns to the same game. Browser choices appear only when you ask for them.

If a selection moves by itself, open **Controller diagnostics → Release all inputs**, then return with the sticks released. The native input layer uses Android's declared axis ranges, a 22% minimum stick deadzone, and explicit release handling. [How the drift fix works →](docs/NATIVE-CONTROLS.md)

## Accounts and saves

Sign in **inside the game** with your existing Pokébonk username and password. The app uses the official website and server and retains its own session between launches. Its session is separate from Chrome: browser guest progress is not automatically imported. Use **App options → Open in browser…** if you rely on a browser guest save. Avoid uninstalling or clearing app data when relying on an app guest session.

As checked on 19 October 2026, the website offers guest and username/password accounts, **no Google sign-in provider**. The app preserves the website's available sign-in options; it does not add a separate account service. See the [official game](https://pokebonk.com/) for current availability.

## Help improve it

Bug reports, controller compatibility reports, documentation fixes, and pull requests are welcome. You do not need to code to help.

- **[Report a bug](https://github.com/alfonsopuicercus/pokebonk-thor-launcher/issues/new?template=bug_report.yml)** — include your app version, device, Android/WebView versions, and steps to reproduce.
- **[Suggest an improvement](https://github.com/alfonsopuicercus/pokebonk-thor-launcher/issues/new?template=improvement.yml)** — explain what you would like to do and how it would help.
- **[Contribute a fix](CONTRIBUTING.md)** — build instructions, project layout, checks, and pull request guidance.

Share this repository or its release page so people can find the current APK and report feedback in one place. Never include passwords, session tokens, or private save data in public reports.

## Tested status

The initial Thor user reports that native controls now work reasonably well. Automated Java and JavaScript regressions cover the reproduced drift, quick presses, releases, disconnects, and backgrounding. Android emulator checks exercise the actual APK with native controller events. The direct launch and in-app help/diagnostics flow also have an emulator smoke test. See [verification and limits](docs/VERIFICATION.md).

Broader physical-device coverage, speaker output, sleep/resume, and authenticated server-save restoration still need community testing. Website updates may change behavior. Browser fallback uses the browser's original input handling.

## Build

Use **JDK 17**, **Node.js 22+**, and **Android SDK Platform 35 / Build Tools 35.0.0**. Set `ANDROID_HOME` or create `android/local.properties` with `sdk.dir=/path/to/your/sdk`.

```sh
npm ci --ignore-scripts
npm run format:check
node --test tests/*.test.cjs
./android/gradlew -p android :app:testDebugUnitTest :app:assembleDebug :app:lintRelease
```

The debug APK is at `android/app/build/outputs/apk/debug/app-debug.apk` and installs separately from the release. [CONTRIBUTING.md](CONTRIBUTING.md) explains emulator checks and release signing. GitHub Actions runs the unit tests, build, and lint for pushes and pull requests.

## Credits and license

App by Alfonso Puicercus, released under the [MIT license](LICENSE). The game is by **Charcher** and its credited contributors, served from the official website. This repository and its APK do not distribute game code, Pokémon artwork, music, or saves. Gradle wrapper retains its upstream license.

This fan app is unaffiliated with the game developer, Nintendo, or The Pokémon Company. Pokémon belongs to its respective owners. Game content and services remain subject to their own terms.
