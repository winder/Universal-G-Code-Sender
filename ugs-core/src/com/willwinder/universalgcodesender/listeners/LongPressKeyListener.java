/*
    Copyright 2023 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender.listeners;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A key listener that will listen for long key events on a component.
 * <p>
 * When a button is pressed a timer will start that will check if the key was
 * pressed for the time given in {@link LongPressKeyListener#longPressDelay} in
 * milliseconds. If the time is exceeded an event will be fired to
 * {@link LongPressKeyListener#onKeyLongPressed(KeyEvent)}. Upon release the
 * method {@link LongPressKeyListener#onKeyLongRelease(KeyEvent)} will be
 * triggered
 * <p>
 * Ordinary key press will be triggered using
 * {@link LongPressKeyListener#onKeyPressed(KeyEvent)} (KeyEvent)},
 * {@link LongPressKeyListener#onKeyRelease(KeyEvent)} (KeyEvent)}
 *
 * @author Joacim Breiler
 */
public abstract class LongPressKeyListener implements KeyListener {
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();

    /**
     * The delay time for how long the button must be pressed before a mouse
     * click event is considered a long press.
     */
    private final long longPressDelay;

    /**
     * A state variable indicating if the button was pressed for more than
     * {@link #longPressDelay}.
     */
    private boolean isLongPressed;

    /**
     * When a key press is triggered this will be the timer that
     * will trigger a long press event after the defined delay time.
     */
    private ScheduledFuture<?> longPressTimer;

    /**
     * Constructor for creating a long press mouse listener
     *
     * @param longPressDelay time in milliseconds before a button press will be
     *                       considered a long press
     */
    public LongPressKeyListener(long longPressDelay) {
        this.longPressDelay = longPressDelay;
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Ignore these events as they behave unpredictable
    }

    @Override
    public void keyPressed(KeyEvent e) {
        isLongPressed = false;
        onKeyPressed(e);

        if (longPressTimer == null || longPressTimer.isDone()) {
            longPressTimer = EXECUTOR_SERVICE.schedule(
                    () -> {
                        // After the given delay time has passed consider it a long press
                        isLongPressed = true;
                        onKeyLongPressed(e);
                    },
                    longPressDelay,
                    TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (isLongPressed) {
            onKeyLongRelease(e);
        } else {
            onKeyRelease(e);
        }

        if (longPressTimer != null) {
            longPressTimer.cancel(true);
        }

        // Reset internal state
        longPressTimer = null;
    }

    /**
     * When the key is pressed. This may not trigger a onKeyRelease if
     * the key was pressed for more than the given long press delay.
     *
     * @param e the mouse event
     */
    protected abstract void onKeyPressed(KeyEvent e);

    /**
     * When the key was released. This may not be triggered
     * if the key was pressed for more than the given long press delay.
     *
     * @param e the key event
     */
    protected abstract void onKeyRelease(KeyEvent e);

    /**
     * If the key was long pressed for more than the given delay time. This
     * will result in a {@link #onKeyLongRelease(KeyEvent)} when the button is
     * released.
     *
     * @param e the key event
     */
    protected abstract void onKeyLongPressed(KeyEvent e);

    /**
     * If the key was released after a long press.
     *
     * @param e the key event
     */
    protected abstract void onKeyLongRelease(KeyEvent e);
}
