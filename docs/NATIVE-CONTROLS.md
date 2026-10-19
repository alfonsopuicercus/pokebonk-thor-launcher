# Native input update — 0.2.0

This describes the input layer introduced in 0.2.0 and retained in 0.3.0. The app icon now opens native play directly. **Start + Select → Controller diagnostics**, or Android Back, opens readings and the release-all action; browser fallback is an optional in-game menu item.

User report: inputs seem to keep going upward. The 0.1.0 launcher only opened Chrome and could not normalize controller data; its earlier QA used a centered standard Gamepad and did not establish the Thor's actual idle/axis behavior.

Reproduction from official v0.7.5 input code: a nonstandard pad first read at Y=+1 then returned to zero retains a +1 baseline and continues full upward movement after 60 seconds. A supposedly standard pad with its fourth axis resting at -1 continues camera motion. Neither test proves which readings the user's physical Thor produces; no physical device is attached.

Native play loads the official online website in an Android WebView, captures Android KeyEvent/MotionEvent input, and presents one normalized standard Gamepad. No touch/pointer actions are synthesized. Fixed range midpoints, a 22% stick deadzone, explicit trigger/hat mapping, a guaranteed initial neutral poll, ordered discrete press/release frames, a 500ms stale cutoff, and pause/focus/disconnect clearing prevent the reproduced classes of drift. The right stick uses signed RX/RY ranges if supplied, otherwise signed Z/RZ; absent right-stick capability stays neutral.

The remote game, username/password authentication and server saves remain official. The native WebView has its own persistent session and does not import Chrome cookies or guest saves. Browser fallback remains available, and no existing browser data is cleared. Google sign-in remains unavailable on the website. An initial real controller button initializes game audio through an unused F24 key notification; gameplay buttons remain gamepad events. Android audio playback is configured without a touchscreen gesture requirement.

Plan: add mapper and bridge regression tests before implementation; test actual rendered UI using an isolated backend fixture, test native Android controller events through an emulator, review lifecycle/account isolation, build/lint/sign, publish a new preview only after the checks pass, retain explicit hardware acceptance limits.
