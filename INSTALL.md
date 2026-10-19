# Pokébonk native controls — AYN Thor

1. Open [the 0.2.0 release](https://github.com/alfonsopuicercus/pokebonk-thor-launcher/releases/tag/v0.2.0-preview) on the Thor and download **PokeBonk-Thor-Native-0.2.0.apk** from Assets.
2. Install it over 0.1.0. Same package/signing key, so no uninstall is needed.
3. Open **Pokébonk for Thor → Play with native controls**. Keep normal Android gamepad mode enabled; disable any old touch-mapping overlay. No gesture mapping or screen tap is needed for controls.
4. Leave the sticks released initially. Navigate with the left stick/D-pad, confirm with the bottom face button, and go back with the right face button. The game remaps actions under **Settings → Controls**.
5. Sign in once inside native play with your existing Pokébonk username/password and check expected server progress before a longer run.

**Accounts:** native play has its own persistent app session, separate from Chrome. Existing Chrome guest progress is not automatically imported. Use **Open in Chrome (browser fallback)** if relying on that browser's guest save. No browser data is cleared. Avoid clearing app data/uninstalling if relying on a native guest session. PokéSurvivor is a different game and its progress is not imported. Pokébonk currently offers no Google sign-in provider.

## If a menu still moves by itself

Press **Start + Select** together, or Android Back, to open controller checks. The screen shows raw stick readings and the right-stick axis pair. Use **Release all inputs**, return with sticks released, and check the menu again. The native deadzone is 22%; centers come from Android's axis ranges rather than the first sample. A physical Thor issue outside the reproduced cases may still need its actual readings.

## Buttons

Left stick: move / menu navigation. Right stick: camera / scrolling. Bottom/right/left/top face: jump / dash / interact / Ultimate. LB: aim. RB: target lock. LT/RT: dash/Ultimate. Start: pause. Select: map. D-pad: menu navigation, zoom during gameplay. LB/RB switch menu tabs. Printed button letters can differ from Xbox-style positions.

## Preview limits

Internet and a WebGL-capable, current Android System WebView are required. The official game updates online. No ROM, offline game copy, touch mapper or companion service is installed. Tests cover the reproduced input faults and APK path; physical Thor and authenticated save restoration remain separate acceptance checks. Chrome fallback uses Chrome's original controller behavior rather than the new native input layer.
