# Install Pokébonk for Thor

1. Open [the latest community preview](https://github.com/alfonsopuicercus/pokebonk-thor-launcher/releases/tag/v0.3.0-preview) on your Thor. Under **Assets**, download **PokeBonk-Thor-Native-0.3.0.apk**.
2. Open the downloaded APK and allow installation from that browser or file manager if Android asks. Install or update **Pokébonk for Thor**. Version 0.3.0 uses the same package and signing key as 0.1.0 and 0.2.0, so no uninstall is needed.
3. Keep normal Android gamepad mode enabled and disable any touch-mapping overlay for this game.
4. Open **Pokébonk for Thor**. The game loads directly inside the app; there is no launcher menu or browser to choose. Internet and a current, WebGL-capable Android System WebView are required.
5. Leave the sticks released initially. Use the left stick/D-pad to navigate, the bottom face button to confirm, and the right face button to go back. Remap actions in the game's **Settings → Controls**.

## Updates with Obtainium

1. Add [the repository URL](https://github.com/alfonsopuicercus/pokebonk-thor-launcher) as a **GitHub** source, rather than using a fixed APK download URL.
2. Enable **Include prereleases** / **Allow prereleases**. All current APK releases are community previews; filtering for stable releases hides them.
3. Let Obtainium find the release APK and install updates normally. The published APK uses the same application ID and signing key across releases.

The 0.3.0 release has one APK, so no filename or architecture filter is required. Obtainium settings can vary by version; see its [GitHub source documentation](https://wiki.obtainium.imranr.dev/sources/#github).

## App options

Press **Start + Select** together or **Android Back**:

- **Continue playing** returns to the current game.
- **Controls & sign-in** shows button mappings and account guidance.
- **Controller diagnostics** shows raw stick readings, the right-stick axis pair, and **Release all inputs**. After releasing inputs, return with the sticks centered. The native minimum deadzone is 22%.
- **Open in browser…** is an optional fallback for an existing browser session. Native controller fixes apply inside the app.
- **Close app** exits.

The extra options never appear on startup. No gesture mapper, extension, or companion app is needed.

## Accounts

Use the game's existing Pokébonk username/password sign-in. Sign in once inside this app and check that your expected server progress appears before a longer run. The website currently offers no Google sign-in provider.

The app keeps its own session, separate from Chrome. Existing browser guest progress is not imported or cleared; use browser fallback if you rely on it. Avoid clearing app data or uninstalling when relying on an app guest session. Updating in place retains app data. PokéSurvivor is a different game and its progress is not imported.

## Troubleshooting and feedback

If a menu moves by itself, open **Controller diagnostics → Release all inputs**. If it continues, note the readings with the sticks centered and [report the issue](https://github.com/alfonsopuicercus/pokebonk-thor-launcher/issues/new/choose), including app version, device, and Android/WebView versions.

For a blank or slow game, check your connection and update Android System WebView. Lower graphics quality in the game's settings if needed. The official game updates online; [verification](docs/VERIFICATION.md) describes what has been checked and what still needs device testing.

The release includes **SHA256SUMS.txt** and **QA.json**. On a computer, `shasum -a 256 PokeBonk-Thor-Native-0.3.0.apk` can be compared with the checksum file. Bugs, suggestions, and pull requests are welcome through [GitHub](https://github.com/alfonsopuicercus/pokebonk-thor-launcher).
