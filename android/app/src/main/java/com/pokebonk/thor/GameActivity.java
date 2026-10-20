package com.pokebonk.thor;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.hardware.input.InputManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Display;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Official online game with direct native Android gamepad input. */
public final class GameActivity extends Activity implements InputManager.InputDeviceListener {

    private static final String GAME_URL = "https://pokebonk.com/?touch=0";
    private static final String BROWSER_GAME_URL = "https://pokebonk.com/";
    // A heartbeat keeps held input current; the JS bridge releases input after 500 ms without one.
    private static final long HEARTBEAT_INTERVAL_MS = 50;
    private static final long DEBUG_LOG_INTERVAL_MS = 2000;
    private static final int SCRIPT_READ_BUFFER_BYTES = 4096;
    private static final int WEB_BACKGROUND_COLOR = 0xff182338;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Map<Integer, ControllerState> controllers = new LinkedHashMap<>();
    private WebView web;
    private InputManager input;
    private boolean ready, foreground, focused;
    private long lastDebug;
    private String script,
        raw = "No motion received yet",
        axisMapping = "Waiting for controller motion";
    private final Runnable heartbeat = new Runnable() {
        public void run() {
            emit(false);
            handler.postDelayed(this, HEARTBEAT_INTERVAL_MS);
        }
    };

    private static boolean official(String url) {
        try {
            Uri u = Uri.parse(url);
            return (
                "https".equals(u.getScheme()) &&
                ("pokebonk.com".equals(u.getHost()) || "www.pokebonk.com".equals(u.getHost()))
            );
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        try (InputStream in = getAssets().open("native-gamepad.js")) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] bytes = new byte[SCRIPT_READ_BUFFER_BYTES];
            int count;
            while ((count = in.read(bytes)) != -1) out.write(bytes, 0, count);
            script = out.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            finish();
            return;
        }
        web = new WebView(this);
        web.setBackgroundColor(WEB_BACKGROUND_COLOR);
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setSafeBrowsingEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true);
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        web.setWebViewClient(
            new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                    if (!request.isForMainFrame()) return false;
                    String url = request.getUrl().toString();
                    if (official(url)) return false;
                    openExternal(request.getUrl());
                    return true;
                }

                @Override
                public void onPageStarted(WebView view, String url, android.graphics.Bitmap icon) {
                    ready = false;
                    clear();
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    if (official(url)) web.evaluateJavascript(script, result -> {
                        if (official(web.getUrl())) {
                            ready = true;
                            if (BuildConfig.DEBUG) android.util.Log.d(
                                "PokeBonkNative",
                                "page input installed"
                            );
                            emit(false);
                        }
                    });
                }

                @Override
                public void onReceivedError(
                    WebView view,
                    WebResourceRequest request,
                    WebResourceError error
                ) {
                    if (request.isForMainFrame()) new AlertDialog.Builder(GameActivity.this)
                        .setMessage(
                            "Could not load the official game. Check the internet connection."
                        )
                        .setPositiveButton("Retry", (d, w) -> web.loadUrl(GAME_URL))
                        .setNegativeButton("Close", (d, w) -> finish())
                        .show();
                }
            }
        );
        web.setWebChromeClient(
            new WebChromeClient() {
                private View custom;
                private CustomViewCallback customCallback;

                @Override
                public void onShowCustomView(View view, CustomViewCallback callback) {
                    if (custom != null) {
                        callback.onCustomViewHidden();
                        return;
                    }
                    custom = view;
                    customCallback = callback;
                    setContentView(view);
                }

                @Override
                public void onHideCustomView() {
                    if (custom == null) return;
                    setContentView(web);
                    custom = null;
                    customCallback.onCustomViewHidden();
                    immersive();
                }

                @Override
                public boolean onConsoleMessage(ConsoleMessage message) {
                    if (BuildConfig.DEBUG) android.util.Log.d(
                        "PokeBonk",
                        message.messageLevel() + ": " + message.message()
                    );
                    return true;
                }
            }
        );
        setContentView(web);
        web.requestFocus();
        immersive();
        input = getSystemService(InputManager.class);
        input.registerInputDeviceListener(this, handler);
        for (int id : input.getInputDeviceIds()) onInputDeviceAdded(id);
        web.loadUrl(GAME_URL);
        handler.post(heartbeat);
    }

    private void immersive() {
        if (web != null) web.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        );
    }

    private void openExternal(Uri uri) {
        if (!Arrays.asList("https", "http", "mailto").contains(uri.getScheme())) return;
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (ActivityNotFoundException ignored) {}
    }

    private static boolean source(int value) {
        return (
            (value & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
            (value & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK ||
            (value & InputDevice.SOURCE_DPAD) == InputDevice.SOURCE_DPAD
        );
    }

    private ControllerState state(int id) {
        return controllers.computeIfAbsent(id, k -> new ControllerState());
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (source(event.getSource()) && ready && foreground && focused) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                if (event.getAction() == KeyEvent.ACTION_UP) showMenu();
                return true;
            }
            if (
                event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() > 0
            ) return true;
            if (
                event.getAction() != KeyEvent.ACTION_DOWN && event.getAction() != KeyEvent.ACTION_UP
            ) return super.dispatchKeyEvent(event);
            if (
                state(event.getDeviceId()).key(
                    event.getKeyCode(),
                    event.getAction() == KeyEvent.ACTION_DOWN
                )
            ) {
                if (
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                    state(event.getDeviceId()).buttonValue(ControllerState.BUTTON_SELECT) >
                        ControllerState.BUTTON_PRESSED_THRESHOLD &&
                    state(event.getDeviceId()).buttonValue(ControllerState.BUTTON_START) >
                        ControllerState.BUTTON_PRESSED_THRESHOLD
                ) {
                    clear();
                    showMenu();
                } else emit(true);
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    private static InputDevice.MotionRange range(InputDevice device, int axis) {
        return device == null ? null : device.getMotionRange(axis, InputDevice.SOURCE_JOYSTICK);
    }

    private static boolean signed(InputDevice d, int axis) {
        InputDevice.MotionRange r = range(d, axis);
        return r != null && r.getMin() < 0 && r.getMax() > 0;
    }

    private static float axis(MotionEvent event, int axis) {
        if (axis < 0) return 0;
        InputDevice.MotionRange r = range(event.getDevice(), axis);
        float v = event.getAxisValue(axis);
        if (r != null) return ControllerState.centered(v, r.getMin(), r.getMax(), r.getFlat());
        return event.getDeviceId() < 0 ? ControllerState.centered(v, -1, 1, 0) : 0;
    }

    private static float trigger(MotionEvent event, int primary, int fallback) {
        InputDevice.MotionRange r = range(event.getDevice(), primary);
        int axis = primary;
        if (r == null) {
            r = range(event.getDevice(), fallback);
            axis = fallback;
        }
        return r == null
            ? 0
            : ControllerState.trigger(event.getAxisValue(axis), r.getMin(), r.getMax());
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        if (
            (event.getSource() & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK &&
            event.getAction() == MotionEvent.ACTION_MOVE &&
            ready &&
            foreground &&
            focused
        ) {
            InputDevice device = event.getDevice();
            int rightXAxis = ControllerState.rightAxisStart(
                signed(device, MotionEvent.AXIS_RX),
                signed(device, MotionEvent.AXIS_RY),
                signed(device, MotionEvent.AXIS_Z),
                signed(device, MotionEvent.AXIS_RZ)
            );
            if (event.getDeviceId() < 0 && rightXAxis < 0) rightXAxis = MotionEvent.AXIS_Z;
            int rightYAxis =
                rightXAxis == MotionEvent.AXIS_RX
                    ? MotionEvent.AXIS_RY
                    : rightXAxis == MotionEvent.AXIS_Z
                      ? MotionEvent.AXIS_RZ
                      : -1;
            ControllerState controller = state(event.getDeviceId());
            float[] previousButtonValues = new float[ControllerState.BUTTON_COUNT];
            for (
                int button = 0;
                button < ControllerState.BUTTON_COUNT;
                button++
            ) previousButtonValues[button] = controller.buttonValue(button);
            controller.motion(
                axis(event, MotionEvent.AXIS_X),
                axis(event, MotionEvent.AXIS_Y),
                axis(event, rightXAxis),
                axis(event, rightYAxis),
                trigger(event, MotionEvent.AXIS_LTRIGGER, MotionEvent.AXIS_BRAKE),
                trigger(event, MotionEvent.AXIS_RTRIGGER, MotionEvent.AXIS_GAS),
                event.getAxisValue(MotionEvent.AXIS_HAT_X),
                event.getAxisValue(MotionEvent.AXIS_HAT_Y)
            );
            raw = String.format(
                Locale.ROOT,
                "Raw X %.3f · Y %.3f · Z %.3f · RZ %.3f · RX %.3f · RY %.3f",
                event.getAxisValue(MotionEvent.AXIS_X),
                event.getAxisValue(MotionEvent.AXIS_Y),
                event.getAxisValue(MotionEvent.AXIS_Z),
                event.getAxisValue(MotionEvent.AXIS_RZ),
                event.getAxisValue(MotionEvent.AXIS_RX),
                event.getAxisValue(MotionEvent.AXIS_RY)
            );
            axisMapping =
                rightXAxis == MotionEvent.AXIS_RX
                    ? "Right stick: RX/RY"
                    : rightXAxis == MotionEvent.AXIS_Z
                      ? "Right stick: Z/RZ"
                      : "No signed right-stick pair found; camera axes stay neutral";
            boolean buttonEdge = false;
            for (int button = 0; button < ControllerState.BUTTON_COUNT; button++) {
                boolean wasPressed =
                    previousButtonValues[button] >= ControllerState.BUTTON_PRESSED_THRESHOLD;
                boolean isPressed =
                    controller.buttonValue(button) >= ControllerState.BUTTON_PRESSED_THRESHOLD;
                if (wasPressed != isPressed) {
                    buttonEdge = true;
                }
            }
            emit(buttonEdge);
            return true;
        }
        return super.dispatchGenericMotionEvent(event);
    }

    /** Merge devices into one standard pad using the strongest axis/button value. */
    private JSONObject frame() {
        JSONObject p = new JSONObject();
        JSONArray axes = new JSONArray(),
            buttons = new JSONArray();
        try {
            for (int n = 0; n < ControllerState.AXIS_COUNT; n++) {
                float value = 0;
                for (ControllerState s : controllers.values())
                    if (Math.abs(s.axis(n)) > Math.abs(value)) value = s.axis(n);
                axes.put(value);
            }
            for (int n = 0; n < ControllerState.BUTTON_COUNT; n++) {
                float value = 0;
                for (ControllerState s : controllers.values())
                    value = Math.max(value, s.buttonValue(n));
                buttons.put(new JSONObject().put("value", value));
            }
            p.put("axes", axes).put("buttons", buttons);
        } catch (JSONException ignored) {}
        return p;
    }

    private void emit(boolean discrete) {
        if (BuildConfig.DEBUG && SystemClock.uptimeMillis() - lastDebug > DEBUG_LOG_INTERVAL_MS) {
            lastDebug = SystemClock.uptimeMillis();
            android.util.Log.d(
                "PokeBonkNative",
                "ready=" +
                    ready +
                    " foreground=" +
                    foreground +
                    " focused=" +
                    focused +
                    " official=" +
                    (web != null && official(web.getUrl()))
            );
        }
        if (!ready || web == null || !foreground || !focused || !official(web.getUrl())) return;
        web.evaluateJavascript(
            "window.__PB_NATIVE__?.update(" + frame() + "," + discrete + ")",
            null
        );
    }

    private void clear() {
        for (ControllerState s : controllers.values()) s.clear();
    }

    private void suspend() {
        clear();
        if (ready && web != null && official(web.getUrl())) web.evaluateJavascript(
            "window.__PB_NATIVE__?.suspend()",
            null
        );
    }

    private void resumeInput() {
        if (ready && web != null && official(web.getUrl())) web.evaluateJavascript(
            "window.__PB_NATIVE__?.resume()",
            r -> emit(false)
        );
    }

    private void showMenu() {
        suspend();
        AlertDialog menu = new AlertDialog.Builder(this)
            .setTitle("Pokébonk")
            .setItems(
                new String[] {
                    "Continue playing",
                    "Controls & sign-in",
                    "Controller diagnostics",
                    "Open in browser…",
                    "Close app",
                },
                (dialog, choice) -> {
                    switch (choice) {
                        case 0:
                            resumeInput();
                            break;
                        case 1:
                            showHelp();
                            break;
                        case 2:
                            showDiagnostics();
                            break;
                        case 3:
                            showBrowserFallback();
                            break;
                        case 4:
                            finish();
                            break;
                    }
                }
            )
            .setOnCancelListener(dialog -> resumeInput())
            .create();
        showDialog(menu);
    }

    private void showDialog(AlertDialog dialog) {
        // Android dialogs do not consistently treat the physical A/B buttons as confirm/back.
        dialog.setOnKeyListener((d, code, event) -> {
            if (code == KeyEvent.KEYCODE_BUTTON_B) {
                if (event.getAction() == KeyEvent.ACTION_UP) dialog.cancel();
                return true;
            }
            if (code != KeyEvent.KEYCODE_BUTTON_A) return false;
            if (dialog.getListView() != null) {
                if (event.getAction() == KeyEvent.ACTION_UP) {
                    android.widget.ListView list = dialog.getListView();
                    int position = Math.max(0, list.getSelectedItemPosition());
                    list.performItemClick(
                        list.getChildAt(position - list.getFirstVisiblePosition()),
                        position,
                        list.getItemIdAtPosition(position)
                    );
                }
                return true;
            }
            return dialog.dispatchKeyEvent(
                new KeyEvent(
                    event.getDownTime(),
                    event.getEventTime(),
                    event.getAction(),
                    KeyEvent.KEYCODE_DPAD_CENTER,
                    event.getRepeatCount(),
                    event.getMetaState(),
                    event.getDeviceId(),
                    event.getScanCode(),
                    event.getFlags(),
                    event.getSource()
                )
            );
        });
        dialog.setOnShowListener(d -> {
            if (dialog.getListView() != null) {
                dialog.getListView().requestFocusFromTouch();
                dialog.getListView().setSelection(0);
            } else if (dialog.getButton(AlertDialog.BUTTON_POSITIVE) != null) dialog
                .getButton(AlertDialog.BUTTON_POSITIVE)
                .requestFocusFromTouch();
        });
        dialog.show();
    }

    private void showHelp() {
        showDialog(
            new AlertDialog.Builder(this)
                .setTitle("Controls & sign-in")
                .setMessage(
                    "The app opens the official online game directly. Keep normal Android gamepad mode enabled and disable touch-mapping overlays.\n\n" +
                        "Left stick: Move / navigate\nRight stick: Camera / scroll\nBottom face: Jump / confirm\nRight face: Dash / back\nLeft face: Interact\nTop face: Ultimate\nLB / RB: Aim / target lock; menu tabs\nLT / RT: Dash / Ultimate\nStart: Pause\nSelect: Map\nD-pad: Navigate / zoom\n\n" +
                        "Start + Select or Android Back opens this menu. Remap actions in the game's Settings → Controls.\n\n" +
                        "Sign in with your existing Pokébonk username/password inside the game. The app keeps its own session, separate from Chrome. Chrome guest progress is not imported. Avoid clearing app data or uninstalling if you rely on a guest session.\n\n" +
                        "The website currently offers no Google sign-in. Internet and a current Android System WebView are required. This is an unofficial app."
                )
                .setPositiveButton("Back to game", (dialog, which) -> resumeInput())
                .setOnCancelListener(dialog -> resumeInput())
                .create()
        );
    }

    private void showDiagnostics() {
        showDialog(
            new AlertDialog.Builder(this)
                .setTitle("Controller diagnostics")
                .setMessage(
                    axisMapping +
                        "\n" +
                        raw +
                        "\n\nCentered axes should read near zero. Deadzone: 22%. Return to the game with sticks released."
                )
                .setPositiveButton("Back to game", (dialog, which) -> resumeInput())
                .setNeutralButton("Release all inputs", (dialog, which) -> {
                    clear();
                    resumeInput();
                })
                .setOnCancelListener(dialog -> resumeInput())
                .create()
        );
    }

    private void showBrowserFallback() {
        showDialog(
            new AlertDialog.Builder(this)
                .setTitle("Browser fallback")
                .setMessage(
                    "Use this only if you want the browser's separate session or guest save. Native controller fixes apply inside this app. Returning here keeps the game open."
                )
                .setPositiveButton("Open Chrome", (dialog, which) -> openBrowser(true))
                .setNeutralButton("Choose browser", (dialog, which) -> openBrowser(false))
                .setNegativeButton("Back to game", (dialog, which) -> resumeInput())
                .setOnCancelListener(dialog -> resumeInput())
                .create()
        );
    }

    private void openBrowser(boolean chrome) {
        Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(BROWSER_GAME_URL));
        browser.addCategory(Intent.CATEGORY_BROWSABLE);
        if (chrome) browser.setPackage("com.android.chrome");
        else browser = Intent.createChooser(browser, "Use the browser with your Pokébonk save");
        try {
            startActivity(
                browser,
                ActivityOptions.makeBasic().setLaunchDisplayId(Display.DEFAULT_DISPLAY).toBundle()
            );
        } catch (ActivityNotFoundException missing) {
            if (chrome) openBrowser(false);
            else browserUnavailable();
        } catch (RuntimeException placementUnavailable) {
            try {
                startActivity(browser);
            } catch (ActivityNotFoundException missing) {
                if (chrome) openBrowser(false);
                else browserUnavailable();
            }
        }
    }

    private void browserUnavailable() {
        showDialog(
            new AlertDialog.Builder(this)
                .setMessage("No browser is available. You can continue playing inside this app.")
                .setPositiveButton("Back to game", (dialog, which) -> resumeInput())
                .setOnCancelListener(dialog -> resumeInput())
                .create()
        );
    }

    @Override
    public void onBackPressed() {
        showMenu();
    }

    @Override
    protected void onPause() {
        foreground = false;
        suspend();
        if (web != null) web.onPause();
        CookieManager.getInstance().flush();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        foreground = true;
        if (web != null) {
            web.onResume();
            immersive();
            resumeInput();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean value) {
        super.onWindowFocusChanged(value);
        focused = value;
        if (web == null) return;
        if (value) {
            immersive();
            resumeInput();
        } else suspend();
    }

    @Override
    public void onInputDeviceAdded(int id) {
        InputDevice d = InputDevice.getDevice(id);
        if (d != null && source(d.getSources())) {
            state(id);
            emit(false);
        }
    }

    @Override
    public void onInputDeviceRemoved(int id) {
        controllers.remove(id);
        if (ready && web != null && official(web.getUrl())) web.evaluateJavascript(
            "window.__PB_NATIVE__?.replaceDevices(" + frame() + ")",
            null
        );
    }

    @Override
    public void onInputDeviceChanged(int id) {
        onInputDeviceRemoved(id);
        onInputDeviceAdded(id);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (input != null) input.unregisterInputDeviceListener(this);
        ready = false;
        if (web != null) {
            web.destroy();
            web = null;
        }
        super.onDestroy();
    }
}
