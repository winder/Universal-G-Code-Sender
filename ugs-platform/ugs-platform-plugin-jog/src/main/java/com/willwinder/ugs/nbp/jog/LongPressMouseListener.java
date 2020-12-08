/*
    Copyright 2018 Will Winder

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
package com.willwinder.ugs.nbp.jog;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A mouse listener that will listen for long press events on a component.
 * <p>
 * When a button is pressed a timer will start that will check if the button was
 * pressed for the time given in {@link LongPressMouseListener#longPressDelay} in
 * milliseconds. If the time is exceeded a event will be fired to
 * {@link LongPressMouseListener#onMouseLongPressed(MouseEvent)}. Upon release the
 * method {@link LongPressMouseListener#onMouseLongRelease(MouseEvent)} will be
 * triggered
 * <p>
 * Ordinary mouse clicks will be triggered using
 * {@link LongPressMouseListener#onMouseClicked(MouseEvent)},
 * {@link LongPressMouseListener#onMousePressed(MouseEvent)}
 * {@link LongPressMouseListener#onMouseRelease(MouseEvent)}.
 *
 * @author Joacim Breiler
 */
public abstract class LongPressMouseListener implements MouseListener {
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();

    /**
     * The delay time for how long the button must be pressed before a the mouse
     * click event is considered a long press.
     */
    private final long longPressDelay;

    /**
     * A state variable indicating if the button was pressed for more than
     * {@link #longPressDelay}.
     */
    private boolean isLongPressed;

    /**
     * When a mouse press is triggered this will be the timer that
     * will trigger a long press event after the defined delay time.
     */
    private ScheduledFuture<?> longPressTimer;
    private Component pressedComponent;

    /**
     * Constructor for creating a long press mouse listener
     *
     * @param longPressDelay time in milliseconds before a button press will be
     *                       considered a long press
     */
    public LongPressMouseListener(long longPressDelay) {
        this.longPressDelay = longPressDelay;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Ignore these events as they behave unpredictable
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (!isSourceEnabled(e)) {
            return;
        }

        pressedComponent = e.getComponent();
        isLongPressed = false;
        onMousePressed(e);

        if (longPressTimer == null || longPressTimer.isDone()) {
            longPressTimer = EXECUTOR_SERVICE.schedule(
                    () -> {
                        // After the given delay time has passed consider it a long press
                        isLongPressed = true;
                        onMouseLongPressed(e);
                    },
                    longPressDelay,
                    TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (!isSourceEnabled(e)) {
            return;
        }

        if (isLongPressed) {
            onMouseLongRelease(e);
        } else {
            onMouseRelease(e);
        }

        if(pressedComponent.contains(e.getPoint())) {
            onMouseClicked(e);
        }

        if (longPressTimer != null) {
            longPressTimer.cancel(true);
        }

        // Reset internal state
        longPressTimer = null;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    private boolean isSourceEnabled(MouseEvent e) {
        return ((Component) e.getSource()).isEnabled();
    }

    /**
     * If the component was single clicked. This will not be triggered if
     * the button press was a long press.
     *
     * @param e the mouse event
     */
    protected abstract void onMouseClicked(MouseEvent e);

    /**
     * When the component is pressed. This may not trigger a onMouseRelease if
     * the button was pressed for more than the given long press delay.
     *
     * @param e the mouse event
     */
    protected abstract void onMousePressed(MouseEvent e);

    /**
     * When the component click was released. This may not be triggered
     * if the button was pressed for more than the given long press delay.
     *
     * @param e the mouse event
     */
    protected abstract void onMouseRelease(MouseEvent e);

    /**
     * If the button was long pressed for more than the given delay time. This
     * will result in a {@link #onMouseLongRelease(MouseEvent)} when the button is
     * released.
     *
     * @param e the mouse event
     */
    protected abstract void onMouseLongPressed(MouseEvent e);

    /**
     * If the button was released after a long press.
     *
     * @param e the mouse event
     */
    protected abstract void onMouseLongRelease(MouseEvent e);
}
