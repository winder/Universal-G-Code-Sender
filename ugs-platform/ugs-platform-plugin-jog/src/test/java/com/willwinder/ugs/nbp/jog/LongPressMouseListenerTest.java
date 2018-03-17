package com.willwinder.ugs.nbp.jog;

import org.junit.Before;
import org.junit.Test;

import java.awt.event.MouseEvent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LongPressMouseListenerTest {
    private LongPressMouseListener longPressMouseListener;
    public static final int LONG_PRESS_DELAY = 100;

    private boolean isMouseClicked;
    private boolean isMouseLongClicked;
    private boolean isMousePressed;
    private boolean isMouseRelease;
    private boolean isMouseLongPressed;
    private boolean isMouseLongRelease;

    @Before
    public void setUp() {
        isMouseClicked = false;
        isMouseLongClicked = false;
        isMousePressed = false;
        isMouseRelease = false;
        isMouseLongPressed = false;
        isMouseLongRelease = false;

        longPressMouseListener = new LongPressMouseListener(LONG_PRESS_DELAY) {
            @Override
            protected void onMouseClicked(MouseEvent e) {
                isMouseClicked = true;
            }

            @Override
            protected void onMouseLongClicked(MouseEvent e) {
                isMouseLongClicked = true;
            }

            @Override
            protected void onMousePressed(MouseEvent e) {
                isMousePressed = true;
            }

            @Override
            protected void onMouseRelease(MouseEvent e) {
                isMouseRelease = true;
            }

            @Override
            protected void onMouseLongPressed(MouseEvent e) {
                isMouseLongPressed = true;
            }

            @Override
            protected void onMouseLongRelease(MouseEvent e) {
                isMouseLongRelease = true;
            }
        };
    }

    @Test
    public void mousePressedShouldTriggerLongPressedAfterDelay() throws InterruptedException {
        longPressMouseListener.mousePressed(null);
        assertTrue(isMousePressed);
        assertFalse(isMouseLongPressed);

        Thread.sleep(LONG_PRESS_DELAY + 10); // add a couple of milliseconds to make sure it gets triggered
        assertTrue(isMouseLongPressed);
    }

    @Test
    public void mousePressedShouldNotTriggerLongPressedIfReleasedBeforeDelay() throws InterruptedException {
        longPressMouseListener.mousePressed(null);
        assertTrue(isMousePressed);
        assertFalse(isMouseClicked);
        assertFalse(isMouseRelease);
        assertFalse(isMouseLongPressed);

        longPressMouseListener.mouseReleased(null);

        Thread.sleep(LONG_PRESS_DELAY);
        assertTrue(isMouseRelease);
        assertFalse(isMouseClicked);
        assertFalse(isMouseLongPressed);
    }
}
