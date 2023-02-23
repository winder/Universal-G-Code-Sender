package com.willwinder.universalgcodesender.listeners;

import org.junit.Before;
import org.junit.Test;

import javax.swing.JLabel;
import java.awt.event.KeyEvent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LongPressKeyListenerTest {
    public static final int LONG_PRESS_DELAY = 100;
    private LongPressKeyListener longPressKeyListener;
    private boolean isKeyPressed;
    private boolean isKeyReleased;
    private boolean isKeyLongPressed;
    private boolean isKeyLongRelease;
    private KeyEvent event;

    @Before
    public void setUp() {
        isKeyPressed = false;
        isKeyReleased = false;
        isKeyLongPressed = false;
        isKeyLongRelease = false;

        JLabel component = new JLabel();
        component.setEnabled(true);
        event = new KeyEvent(component, KeyEvent.KEY_PRESSED, System.currentTimeMillis(), KeyEvent.META_DOWN_MASK, KeyEvent.VK_E, 'e');

        longPressKeyListener = new LongPressKeyListener(LONG_PRESS_DELAY) {

            @Override
            protected void onKeyPressed(KeyEvent e) {
                isKeyPressed = true;
            }

            @Override
            protected void onKeyRelease(KeyEvent e) {
                isKeyReleased = true;
            }

            @Override
            protected void onKeyLongPressed(KeyEvent e) {
                isKeyLongPressed = true;
            }

            @Override
            protected void onKeyLongRelease(KeyEvent e) {
                isKeyLongRelease = true;
            }
        };
    }

    @Test
    public void keyPressedShouldTriggerLongPressedAfterDelay() throws InterruptedException {
        longPressKeyListener.keyPressed(event);
        assertTrue(isKeyPressed);
        assertFalse(isKeyLongPressed);

        Thread.sleep(LONG_PRESS_DELAY + 100); // add a couple of milliseconds to make sure it gets triggered
        longPressKeyListener.keyReleased(event);

        assertTrue(isKeyLongPressed);
        assertFalse(isKeyReleased);
    }

    @Test
    public void keyPressedShouldTriggerLongReleaseAfterDelay() throws InterruptedException {
        longPressKeyListener.keyPressed(event);
        assertTrue(isKeyPressed);
        assertFalse(isKeyLongPressed);

        Thread.sleep(LONG_PRESS_DELAY + 100); // add a couple of milliseconds to make sure it gets triggered
        longPressKeyListener.keyReleased(event);

        assertTrue(isKeyLongPressed);
        assertTrue(isKeyLongRelease);
    }

    @Test
    public void keyPressedShouldNotTriggerLongPressedIfReleasedBeforeDelay() {
        longPressKeyListener.keyPressed(event);
        assertTrue(isKeyPressed);
        assertFalse(isKeyReleased);
        assertFalse(isKeyLongPressed);

        longPressKeyListener.keyReleased(event);

        assertTrue(isKeyReleased);
        assertFalse(isKeyLongPressed);
        assertFalse(isKeyLongRelease);
    }
}
