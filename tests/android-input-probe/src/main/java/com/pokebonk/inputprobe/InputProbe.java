package com.pokebonk.inputprobe;

import android.app.Instrumentation;
import android.graphics.Rect;
import android.os.*;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;
import org.json.*;

/** Test APK only: inject genuine Android events into the foreground game Activity. */
public final class InputProbe extends Instrumentation {

    private Bundle args;

    @Override
    public void onCreate(Bundle bundle) {
        args = bundle;
        start();
    }

    @Override
    public void onStart() {
        Bundle result = new Bundle();
        try {
            if ("ui".equals(args.getString("mode"))) {
                AccessibilityNodeInfo root = null;
                for (int attempt = 0; attempt < 50 && root == null; attempt++) {
                    root = getUiAutomation().getRootInActiveWindow();
                    if (root == null) SystemClock.sleep(100);
                }
                if (root == null) throw new IllegalStateException("No active accessibility window");
                JSONArray nodes = new JSONArray();
                snapshot(root, nodes);
                result.putString("ui", nodes.toString());
                finish(0, result);
                return;
            }
            long now = SystemClock.uptimeMillis();
            MotionEvent.PointerProperties pp = new MotionEvent.PointerProperties();
            pp.id = 0;
            MotionEvent.PointerCoords coords = new MotionEvent.PointerCoords();
            String[] values = args.getString("axes", "0,0,0,0").split(",");
            int[] axes = {
                MotionEvent.AXIS_X,
                MotionEvent.AXIS_Y,
                MotionEvent.AXIS_Z,
                MotionEvent.AXIS_RZ,
            };
            for (int n = 0; n < Math.min(4, values.length); n++) coords.setAxisValue(
                axes[n],
                Float.parseFloat(values[n])
            );
            MotionEvent e = MotionEvent.obtain(
                now,
                now,
                MotionEvent.ACTION_MOVE,
                1,
                new MotionEvent.PointerProperties[] { pp },
                new MotionEvent.PointerCoords[] { coords },
                0,
                0,
                1,
                1,
                -1,
                0,
                InputDevice.SOURCE_JOYSTICK,
                0
            );
            result.putBoolean("injected", getUiAutomation().injectInputEvent(e, true));
            e.recycle();
            finish(0, result);
        } catch (Exception e) {
            result.putString("error", e.toString());
            finish(1, result);
        }
    }

    private void snapshot(AccessibilityNodeInfo node, JSONArray nodes) throws JSONException {
        if (node == null) return;
        String kind = String.valueOf(node.getClassName());
        Rect bounds = new Rect();
        node.getBoundsInScreen(bounds);
        nodes.put(
            new JSONObject()
                .put("text", String.valueOf(node.getText()))
                .put("kind", kind)
                .put(
                    "bounds",
                    new JSONArray(new int[] {
                        bounds.left,
                        bounds.top,
                        bounds.right,
                        bounds.bottom,
                    })
                )
        );
        // Native dialogs and the WebView boundary are all this smoke test needs.
        if (!"android.webkit.WebView".equals(kind)) for (
            int n = 0;
            n < node.getChildCount();
            n++
        ) snapshot(node.getChild(n), nodes);
    }
}
