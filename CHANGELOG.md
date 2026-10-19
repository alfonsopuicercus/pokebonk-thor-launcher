# Changelog

## 0.3.0 community preview — 2026-10-19

- Opening the app icon goes straight to the game, keeping the existing app identity for updates.
- Moved help, diagnostics, and optional browser fallback into the in-game menu, opened with Start + Select or Android Back.
- Added clearer installation/account guidance, contribution instructions, issue forms, a pull request template, and automated build/test checks.
- Kept the native controller fixes and the same signing key/package for updates in place.

## 0.2.0 native controls preview — 2026-10-19

- Replaced browser-only play with the official online game in a WebView and direct Android controller input.
- Fixed reproduced startup menu drift with fixed axis normalization, a neutral initial poll, and release handling across lifecycle/device changes.
- Added native controller diagnostics, unit regressions, and rendered browser/emulator checks.

## 0.1.0 preview — 2026-10-19

- Initial browser launcher, superseded by native play. Preserved in the release history.
