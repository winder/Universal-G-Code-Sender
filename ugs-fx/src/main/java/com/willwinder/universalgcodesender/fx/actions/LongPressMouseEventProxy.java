/*
    Copyright 2025 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.actions;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A mouse listener that will listen for long press events on a component.
 * <p>
 * When a button is pressed a timer will start that will check if the button was
 * pressed for the time given in {@link LongPressMouseEventProxy#longPressDelay} in
 * milliseconds. If the time is exceeded a event will be fired to
 * {@link LongPressMouseEventProxy#handle(javafx.scene.input.MouseEvent)} (MouseEvent)}. Upon release the
 * method {@link LongPressMouseEventProxy#handle(javafx.scene.input.MouseEvent)} (MouseEvent)} will be
 * triggered
 * <p>
 * Ordinary mouse clicks will be triggered using
 * {@link LongPressMouseEventProxy#handle(javafx.scene.input.MouseEvent)} (MouseEvent)},
 * {@link LongPressMouseEventProxy#handle(javafx.scene.input.MouseEvent)} (MouseEvent)}
 * {@link LongPressMouseEventProxy#handle(javafx.scene.input.MouseEvent)} (MouseEvent)}.
 *
 * @author Joacim Breiler
 */
public class LongPressMouseEventProxy implements EventHandler<MouseEvent> {
    private static final ScheduledExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadScheduledExecutor();
    public static final EventType<MouseEvent> MOUSE_LONG_PRESSED =
            new EventType<>(MouseEvent.ANY, "MOUSE_LONG_PRESSED");
    public static final EventType<MouseEvent> MOUSE_LONG_RELEASE =
            new EventType<>(MouseEvent.ANY, "MOUSE_LONG_RELEASE");
    /**
     * The delay time for how long the button must be pressed before a mouse
     * click event is considered a long press.
     */
    private final long longPressDelay;
    private final EventHandler<MouseEvent> handler;

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
    private Node pressedSource;

    /**
     * Constructor for creating a long press mouse listener
     *
     * @param longPressDelay time in milliseconds before a button press will be
     *                       considered a long press
     */
    public LongPressMouseEventProxy(long longPressDelay, EventHandler<MouseEvent> handler) {
        this.longPressDelay = longPressDelay;
        this.handler = handler;
    }


    @Override
    public void handle(MouseEvent event) {
        if (event.getEventType() == MouseEvent.MOUSE_PRESSED) {
            mousePressed(event);
        } else if (event.getEventType() == MouseEvent.MOUSE_RELEASED) {
            mouseReleased(event);
        }
    }

    public void mousePressed(MouseEvent e) {
        pressedSource = (Node) e.getSource();
        isLongPressed = false;
        handler.handle(e);

        if (longPressTimer == null || longPressTimer.isDone()) {
            longPressTimer = EXECUTOR_SERVICE.schedule(
                    () -> {
                        // After the given delay time has passed consider it a long press
                        isLongPressed = true;
                        handler.handle(e.copyFor(e.getSource(), e.getTarget(), MOUSE_LONG_PRESSED));
                    },
                    longPressDelay,
                    TimeUnit.MILLISECONDS);
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (isLongPressed) {
            e.copyFor(e.getSource(), e.getTarget(), MOUSE_LONG_PRESSED);
            handler.handle(e.copyFor(e.getSource(), e.getTarget(), MOUSE_LONG_RELEASE));
        } else {
            handler.handle(e);
        }

        if (pressedSource.contains(e.getX(), e.getY())) {
            handler.handle(e);
        }

        if (longPressTimer != null) {
            longPressTimer.cancel(true);
        }

        // Reset internal state
        longPressTimer = null;
    }
}
