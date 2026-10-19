# Historical 0.1.0 design

Superseded by [0.2.0 native controls](NATIVE-CONTROLS.md) after a user-reported menu drift. Retained as implementation history.

# Pokébonk Thor launcher

Intent: downloadable Android APK on Alfonso's GitHub that plays the official online Pokébonk with physical controls and preserves the site's accounts and server saves.

Inspection on 2026-10-19: official v0.7.5 already supplies Gamepad API input, dual-stick movement/camera, gameplay actions, menu navigation and remapping. Its account screen offers guest, username/password login and registration. No Google provider is exposed.

Architecture decision: a permission-free native Android launcher opens https://pokebonk.com/ in regular Chrome (or the selected default browser). Browser cookies, authentication, storage, game updates and saves remain on the official origin. No WebView, copied game, proxy, userscript or second-screen service is required. Existing Chrome sessions are retained. A browser chooser supports an existing profile in another browser. Internet is required by the browser.

Plan:
1. Characterize the published game's controller mapping, menus, release/disconnect behavior and available sign-in methods; save QA evidence outside the repository.
2. Build a small native launcher with Play, browser choice and a physical-button-accessible controls/sign-in guide. Preserve the unmodified official URL and original browser profile. Build/lint and verify a signed release APK; keep signing material ignored.
3. Publish source and a preview APK with installation guide and checksums to a new public GitHub repository. Verify anonymous downloads and hashes; record handoff in PARA Vault.

Review focus: absent Chrome must fall back to a browser chooser; never embed or intercept credentials; no duplicate controller mapping over upstream; no fake Google support; browser profile switching must explain that existing progress does not automatically transfer; actual Thor acceptance remains distinct from local/emulator checks.

Validation: inspect the live website UI and shipped bundle, execute controller characterization against the inspected bundle, compile/lint APK, inspect final manifest/signature, install/launch in the task Android emulator if available, and verify published download hashes.
