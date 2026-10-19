# Pokébonk for AYN Thor

An unofficial Android app for the **official online [Pokébonk](https://pokebonk.com/)**, with **direct native stick/button input**. Android 8+, internet and a current Android System WebView required.

**[Download 0.2.0 APK](https://github.com/alfonsopuicercus/pokebonk-thor-launcher/releases/download/v0.2.0-preview/PokeBonk-Thor-Native-0.2.0.apk)** · **[Install and controls](INSTALL.md)**

Choose **Play with native controls**. No touch mapping, swipes, Firefox extensions or scripts to install. The app loads the official online game and supplies its Gamepad API with normalized Android controller events. Chrome remains an optional fallback.

## Upward-menu drift fix

Version 0.1.0 only launched Chrome. After a report of menu selection moving upward on its own, tests reproduced a bad startup baseline in the official input code. Version 0.2.0 uses fixed Android axis ranges, a 22% stick deadzone, an initial neutral poll, and separate stick/trigger/hat mappings. Quick button presses/releases are queued, and focus loss, pause, disconnect and stale input clear held actions. It does not learn the center from a stick held at startup.

Press **Start + Select** or Android Back for controller readings and **Release all inputs**. Physical Thor readings still require device acceptance; the reproduced software cases are not proof of the user's exact hardware mapping.

## Accounts and saves

Native play uses the official `https://pokebonk.com/` website and server, with a persistent app session. **Sign in once inside the app using your existing Pokébonk username/password.** The app's WebView session is separate from Chrome; Chrome guest progress is not automatically imported. Browser fallback and existing browser data are retained. Avoid uninstalling or clearing app data if relying on a native guest session.

As checked on 19 October 2026, Pokébonk exposes guest and username/password accounts, **no Google sign-in provider**. This app preserves the website's available account options. It contains no replacement account backend, game bundle, Pokémon artwork or JavaScript-to-Java interface. External top-level links open in the browser. HTTPS verification remains enabled.

## Controls

Xbox-style positions below may differ from the letters printed on a Thor.

| Input | In game | In menus |
|---|---|---|
| Left stick | Move | Navigate |
| Right stick | Camera | Scroll |
| Bottom face / A | Jump; hold to glide | Confirm |
| Right face / B | Dash | Back |
| Left face / X | Interact | Reroll where offered |
| Top face / Y | Ultimate | Skip where offered |
| LB / RB | Aim / target lock | Previous / next tab |
| LT / RT | Dash / Ultimate | — |
| Start / Select | Pause / map | Start goes back |
| D-pad | Up/down zoom | Navigate |
| Start + Select | Native controller checks | Native controller checks |

Keep the Thor in normal Android gamepad mode. The game's **Settings → Controls** remaps actions. Disable any existing touch-mapping overlay for this game. A controller button initializes audio without requiring a screen tap in native mode; actual speaker output remains a hardware check.

## Verification and limits

See [native design and reproduction](docs/NATIVE-CONTROLS.md) and [verification](docs/VERIFICATION.md). Mapper and bridge tests cover idle jitter, held startup, releases, remapping boundaries, queued presses, stale input and lifecycle clearing. Rendered UI tests use the official game with an isolated backend fixture, avoiding real account or save changes.

**Physical Thor controller mapping, speaker output and authenticated server-save restoration remain unverified.** The new APK is a hardware-test preview. Browser fallback retains the browser's own input behavior.

## Build and tests

JDK 17 and Android SDK 35. Set `android/local.properties` to your SDK path, or set `ANDROID_HOME`.

```sh
node --test tests/*.test.cjs
cd android
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

`-PwithInputProbe :inputprobe:assembleDebug` optionally builds a separate emulator-only instrumentation helper. It is not included in the game APK.

Private `android/.signing/release.properties` supplies `storePassword` and `keyPassword` for ignored `android/.signing/release.jks`, alias `release`. Without these files, `assembleRelease` produces an unsigned APK. The distributed 0.2.0 APK uses the same key/package as 0.1.0 and can update it in place.

## Credits

App by Alfonso Puicercus, MIT license. Game by **Charcher** and its credited contributors, served by the official website. No game code, Pokémon artwork, music or saves are distributed. This fan app is unaffiliated with the game developer, Nintendo or The Pokémon Company. Gradle wrapper retains its upstream license.
