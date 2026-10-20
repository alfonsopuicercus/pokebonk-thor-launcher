package com.pokebonk.thor;

import static org.junit.Assert.*;

import org.junit.Test;

public class ControllerStateTest {

    @Test
    public void centeredSticksStayNeutralWithJitter() {
        ControllerState s = new ControllerState();
        for (int n = 0; n < 36000; n++) {
            s.motion(.10f, -.14f, .08f, -.12f, 0, 0, 0, 0);
            for (int a = 0; a < 4; a++) assertEquals(0, s.axis(a), 0);
        }
    }

    @Test
    public void heldAtStartupThenReleasedDoesNotRecenterToHeldValue() {
        ControllerState s = new ControllerState();
        s.motion(0, 1, 0, -1, 0, 0, 0, 0);
        assertEquals(1, s.axis(1), 0);
        assertEquals(-1, s.axis(3), 0);
        s.motion(0, 0, 0, 0, 0, 0, 0, 0);
        for (int a = 0; a < 4; a++) assertEquals(0, s.axis(a), 0);
    }

    @Test
    public void heldDirectionRemainsHeldInsteadOfBeingAutoCalibratedAway() {
        ControllerState s = new ControllerState();
        for (int n = 0; n < 36000; n++) s.motion(0, -1, 0, 0, 0, 0, 0, 0);
        assertEquals(-1, s.axis(1), 0);
    }

    @Test
    public void invalidAndOutOfRangeInputIsBounded() {
        ControllerState s = new ControllerState();
        s.motion(Float.NaN, Float.POSITIVE_INFINITY, 4, -4, -1, 2, Float.NaN, 0);
        assertEquals(0, s.axis(0), 0);
        assertEquals(0, s.axis(1), 0);
        assertEquals(1, s.axis(2), 0);
        assertEquals(-1, s.axis(3), 0);
        assertEquals(0, s.buttonValue(6), 0);
        assertEquals(1, s.buttonValue(7), 0);
    }

    @Test
    public void buttonsHatAndTriggersHaveIndependentReleases() {
        ControllerState s = new ControllerState();
        assertTrue(s.key(96, true));
        assertEquals(1, s.buttonValue(0), 0);
        s.key(96, false);
        assertEquals(0, s.buttonValue(0), 0);
        s.key(104, true);
        s.motion(0, 0, 0, 0, 0, 0, 0, -1);
        assertEquals(1, s.buttonValue(12), 0);
        assertEquals(1, s.buttonValue(6), 0);
        s.motion(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals(0, s.buttonValue(12), 0);
        s.key(104, false);
        assertEquals(0, s.buttonValue(6), 0);
    }

    @Test
    public void suspendAndDisconnectClearEverything() {
        ControllerState s = new ControllerState();
        s.key(96, true);
        s.motion(1, -1, 1, -1, 1, 1, -1, -1);
        s.clear();
        for (int a = 0; a < 4; a++) assertEquals(0, s.axis(a), 0);
        for (int b = 0; b < 17; b++) assertEquals(0, s.buttonValue(b), 0);
    }

    @Test
    public void rangeNormalizationDoesNotTreatRestingTriggerAsAStick() {
        assertEquals(0, ControllerState.trigger(-1, -1, 1), 0);
        assertEquals(1, ControllerState.trigger(1, -1, 1), 0);
        assertEquals(0, ControllerState.centered(128, 0, 256, 0), 0);
        assertEquals(-1, ControllerState.centered(-1, -1, 1, 0), 0);
        assertEquals(0, ControllerState.centered(.05f, -1, 1, .1f), 0);
        assertEquals(12, ControllerState.rightAxisStart(true, true, true, true));
        assertEquals(11, ControllerState.rightAxisStart(false, false, true, true));
        assertEquals(-1, ControllerState.rightAxisStart(false, false, false, false));
    }
}
