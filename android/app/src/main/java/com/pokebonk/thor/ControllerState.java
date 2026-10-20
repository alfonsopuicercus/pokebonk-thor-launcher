package com.pokebonk.thor;

import android.view.KeyEvent;
import android.view.MotionEvent;
import java.util.Arrays;

/** Fixed Android layout; centers come from declared ranges, never the first/held sample. */
public final class ControllerState {

    // The W3C standard gamepad layout shared with native-gamepad.js.
    public static final int AXIS_COUNT = 4;
    public static final int BUTTON_COUNT = 17;
    public static final int BUTTON_SELECT = 8;
    public static final int BUTTON_START = 9;
    public static final int NO_AXIS = -1;
    public static final float STICK_DEADZONE = 0.22f;
    public static final float BUTTON_PRESSED_THRESHOLD = 0.5f;

    private static final int LEFT_STICK_X = 0;
    private static final int LEFT_STICK_Y = 1;
    private static final int RIGHT_STICK_X = 2;
    private static final int RIGHT_STICK_Y = 3;
    private static final int BUTTON_LEFT_TRIGGER = 6;
    private static final int BUTTON_RIGHT_TRIGGER = 7;
    private static final int BUTTON_DPAD_UP = 12;
    private static final int BUTTON_DPAD_DOWN = 13;
    private static final int BUTTON_DPAD_LEFT = 14;
    private static final int BUTTON_DPAD_RIGHT = 15;
    private static final float HAT_DIRECTION_THRESHOLD = 0.5f;

    // Array position is the standard Gamepad API button index, not an Android key code.
    private static final int[] ANDROID_BUTTON_CODES = {
        KeyEvent.KEYCODE_BUTTON_A,
        KeyEvent.KEYCODE_BUTTON_B,
        KeyEvent.KEYCODE_BUTTON_X,
        KeyEvent.KEYCODE_BUTTON_Y,
        KeyEvent.KEYCODE_BUTTON_L1,
        KeyEvent.KEYCODE_BUTTON_R1,
        KeyEvent.KEYCODE_BUTTON_L2,
        KeyEvent.KEYCODE_BUTTON_R2,
        KeyEvent.KEYCODE_BUTTON_SELECT,
        KeyEvent.KEYCODE_BUTTON_START,
        KeyEvent.KEYCODE_BUTTON_THUMBL,
        KeyEvent.KEYCODE_BUTTON_THUMBR,
        KeyEvent.KEYCODE_DPAD_UP,
        KeyEvent.KEYCODE_DPAD_DOWN,
        KeyEvent.KEYCODE_DPAD_LEFT,
        KeyEvent.KEYCODE_DPAD_RIGHT,
        KeyEvent.KEYCODE_BUTTON_MODE,
    };

    private final boolean[] digital = new boolean[BUTTON_COUNT];
    private final float[] axes = new float[AXIS_COUNT];
    private float leftTrigger;
    private float rightTrigger;
    private float hatX;
    private float hatY;

    public boolean key(int code, boolean down) {
        for (int button = 0; button < ANDROID_BUTTON_CODES.length; button++) {
            if (ANDROID_BUTTON_CODES[button] == code) {
                digital[button] = down;
                return true;
            }
        }
        return false;
    }

    private static float finite(float value) {
        return Float.isFinite(value) ? value : 0;
    }

    private static float clamp(float value, float low, float high) {
        return Math.max(low, Math.min(high, finite(value)));
    }

    private static float stick(float value) {
        value = clamp(value, -1, 1);
        return Math.abs(value) <= STICK_DEADZONE ? 0 : value;
    }

    public void motion(
        float x,
        float y,
        float rx,
        float ry,
        float left,
        float right,
        float hatX,
        float hatY
    ) {
        axes[LEFT_STICK_X] = stick(x);
        axes[LEFT_STICK_Y] = stick(y);
        axes[RIGHT_STICK_X] = stick(rx);
        axes[RIGHT_STICK_Y] = stick(ry);
        leftTrigger = clamp(left, 0, 1);
        rightTrigger = clamp(right, 0, 1);
        this.hatX = clamp(hatX, -1, 1);
        this.hatY = clamp(hatY, -1, 1);
    }

    public float axis(int index) {
        return axes[index];
    }

    public float buttonValue(int index) {
        float analog = switch (index) {
            case BUTTON_LEFT_TRIGGER -> leftTrigger;
            case BUTTON_RIGHT_TRIGGER -> rightTrigger;
            case BUTTON_DPAD_UP -> hatY < -HAT_DIRECTION_THRESHOLD ? 1 : 0;
            case BUTTON_DPAD_DOWN -> hatY > HAT_DIRECTION_THRESHOLD ? 1 : 0;
            case BUTTON_DPAD_LEFT -> hatX < -HAT_DIRECTION_THRESHOLD ? 1 : 0;
            case BUTTON_DPAD_RIGHT -> hatX > HAT_DIRECTION_THRESHOLD ? 1 : 0;
            default -> 0;
        };
        return Math.max(digital[index] ? 1 : 0, analog);
    }

    public void clear() {
        Arrays.fill(digital, false);
        Arrays.fill(axes, 0);
        leftTrigger = rightTrigger = hatX = hatY = 0;
    }

    public static float centered(float value, float min, float max, float flat) {
        if (!Float.isFinite(value) || !Float.isFinite(min) || !Float.isFinite(max) || max <= min) {
            return 0;
        }
        float midpoint = (min + max) / 2;
        float halfRange = (max - min) / 2;
        return Math.abs(value - midpoint) <= Math.max(0, flat)
            ? 0
            : clamp((value - midpoint) / halfRange, -1, 1);
    }

    public static float trigger(float value, float min, float max) {
        return max > min && Float.isFinite(value) ? clamp((value - min) / (max - min), 0, 1) : 0;
    }

    public static int rightAxisStart(boolean hasRx, boolean hasRy, boolean hasZ, boolean hasRz) {
        if (hasRx && hasRy) {
            return MotionEvent.AXIS_RX;
        }
        return hasZ && hasRz ? MotionEvent.AXIS_Z : NO_AXIS;
    }
}
