# Pokébonk for AYN Thor

A tiny, unofficial Android launcher for the **official online [Pokébonk](https://pokebonk.com/)**. Install the APK and open the game in Chrome with the Thor's physical controls. Android 8+; internet required.

**[Download the APK](https://github.com/alfonsopuicercus/pokebonk-thor-launcher/releases/download/v0.1.0-preview/PokeBonk-Thor-Launcher-0.1.0.apk)** · **[Installation and controls](INSTALL.md)**

Pokébonk v0.7.5 already supports movement, camera, gameplay buttons, menu navigation, and remapping through the browser Gamepad API. This launcher uses that existing support. **No Firefox extension, userscript, pairing key, background service, or separate emulator is needed.**

## Accounts and saves

The game opens in the real browser at `https://pokebonk.com/`, retaining that browser's cookies, account, guest progress, and original server saves. The APK has no WebView, network permission, account database, credential access, or bundled game assets. Keep using the same browser and normal profile as your existing save.

**As checked on 19 October 2026, Pokébonk offers guest access and username/password accounts, but does not expose Google sign-in.** This launcher preserves the sign-in methods offered by the official website; it cannot add a Google provider to the game's server. It will open future website updates normally.

## Controls

Use the Thor's **normal gamepad output**, disable any touch-mapping overlay for this game, then tap the game once for sound and press a face button with sticks released. Xbox-style labels below describe the standard positions; the letters printed on a Thor can differ.

| Input | In game | In menus |
|---|---|---|
| Left stick | Move | Navigate |
| Right stick | Camera | Vertical scrolling |
| Bottom face / A | Jump; hold to glide | Confirm |
| Right face / B | Dash | Back |
| Left face / X | Interact | Reroll where offered |
| Top face / Y | Ultimate | Skip where offered |
| LB | Auto/manual aim | Previous tab |
| RB | Target lock | Next tab / banish where offered |
| LT / RT | Dash / Ultimate | — |
| Start | Pause | Back |
| Select | Map | — |
| D-pad | Up/down zoom | Navigate |

Use the game's **Settings → Controls** to remap buttons or adjust the deadzone. If performance is low, lower its graphics quality.

## Preview validation

- Published game controller code exercised with a simulated standard Gamepad: movement/deadzone, nine action mappings and releases, target lock, camera, zoom, disconnect neutralization, menu confirm/back, and remapping passed.
- Official sign-in UI inspected: guest, username/password login, and registration. No real account or save was used in QA.
- Android release build, lint, APK signature and package/permission checks completed; see [verification](docs/VERIFICATION.md).
- **Physical AYN Thor controls, browser compatibility on the actual device, sound, and authenticated server save restoration remain to be checked on your Thor.** This is a launcher preview, not a claim of physical-device acceptance.

## Build

JDK 17, Android SDK 35, Node.js (packaging only). Set `android/local.properties` to your Android SDK, or set `ANDROID_HOME`.

```sh
cd android
./gradlew :app:assembleDebug :app:lintDebug
```

An optional private `android/.signing/release.properties` supplies `storePassword` and `keyPassword` for `android/.signing/release.jks`, alias `release`. Without those ignored files, `assembleRelease` produces an unsigned APK. A different signing key cannot update the distributed APK in place.

## Credits

Launcher by Alfonso Puicercus, MIT license. Game by **Charcher** and its credited contributors, served only by the official website. No game code, Pokémon artwork, music, or saves are distributed here. This fan launcher is not affiliated with the game developer, Nintendo, or The Pokémon Company. Gradle wrapper retains its upstream license.
