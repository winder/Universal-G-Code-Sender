package com.willwinder.ugs.nbp.jog;

import org.junit.Before;
import org.junit.Test;

import javax.swing.JLabel;
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
    private MouseEvent event;

    @Before
    public void setUp() {
        isMouseClicked = false;
        isMouseLongClicked = false;
        isMousePressed = false;
        isMouseRelease = false;
        isMouseLongPressed = false;
        isMouseLongRelease = false;

        JLabel component = new JLabel();
        component.setEnabled(true);
        event = new MouseEvent(component, 0, 0, 0, 0, 0, 0, false, 0);

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
        longPressMouseListener.mousePressed(event);
        assertTrue(isMousePressed);
        assertFalse(isMouseLongPressed);

        Thread.sleep(LONG_PRESS_DELAY + 100); // add a couple of milliseconds to make sure it gets triggered
        assertTrue(isMouseLongPressed);
        assertFalse(isMouseLongRelease);
    }

    @Test
    public void mousePressedShouldTriggerLongReleaseAfterDelay() throws InterruptedException {
        longPressMouseListener.mousePressed(event);
        assertTrue(isMousePressed);
        assertFalse(isMouseLongPressed);

        Thread.sleep(LONG_PRESS_DELAY + 100); // add a couple of milliseconds to make sure it gets triggered
        longPressMouseListener.mouseReleased(event);

        assertTrue(isMouseLongPressed);
        assertTrue(isMouseLongRelease);
        assertTrue(isMouseLongClicked);
    }

    @Test
    public void mousePressedShouldNotTriggerLongPressedIfReleasedBeforeDelay() throws InterruptedException {
        longPressMouseListener.mousePressed(event);
        assertTrue(isMousePressed);
        assertFalse(isMouseClicked);
        assertFalse(isMouseRelease);
        assertFalse(isMouseLongPressed);

        longPressMouseListener.mouseReleased(event);

        Thread.sleep(LONG_PRESS_DELAY);
        assertTrue(isMouseRelease);
        assertFalse(isMouseClicked);
        assertFalse(isMouseLongPressed);
        assertFalse(isMouseLongRelease);
    }
}
