/*
    Copyright 2026 Joacim Breiler

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

import com.willwinder.universalgcodesender.listeners.LongPressMouseListener;

import javax.swing.SwingUtilities;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * A mouse listener for jog buttons that only reacts to the left mouse button. A short click triggers a single
 * jog while a long press starts a continuous jog that stops when the button is released.
 *
 * @author Joacim Breiler
 */
public class JogButtonMouseListener extends LongPressMouseListener {
    private final Consumer<MouseEvent> onClick;
    private final Consumer<MouseEvent> onLongPressStart;
    private final Consumer<MouseEvent> onLongPressStop;

    /**
     * Creates a listener for jog buttons.
     *
     * @param longPressDelay   time in milliseconds before a button press is considered a long press
     * @param onClick          triggered when the button is clicked
     * @param onLongPressStart triggered when the button is long pressed
     * @param onLongPressStop  triggered when a long pressed button is released
     */
    public JogButtonMouseListener(long longPressDelay, Consumer<MouseEvent> onClick, Consumer<MouseEvent> onLongPressStart, Consumer<MouseEvent> onLongPressStop) {
        super(longPressDelay);
        this.onClick = onClick;
        this.onLongPressStart = onLongPressStart;
        this.onLongPressStop = onLongPressStop;
    }

    @Override
    protected void onMouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            onClick.accept(e);
        }
    }

    @Override
    protected void onMousePressed(MouseEvent e) {
        // Not needed
    }

    @Override
    protected void onMouseRelease(MouseEvent e) {
        // Not needed
    }

    @Override
    protected void onMouseLongPressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            onLongPressStart.accept(e);
        }
    }

    @Override
    protected void onMouseLongRelease(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            onLongPressStop.accept(e);
        }
    }
}
