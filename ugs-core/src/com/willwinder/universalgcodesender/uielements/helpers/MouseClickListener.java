/*
    Copyright 2020 Will Winder

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
package com.willwinder.universalgcodesender.uielements.helpers;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * A mouse listener for listening to only click events.
 * Fixes problem with mouse clicks not being detected if the mouse is moved during click.
 */
public abstract class MouseClickListener implements MouseListener {
    private Component pressedComponent;

    @Override
    public void mouseClicked(MouseEvent e) {
        // Not used due to unpredictable behaviour
    }

    @Override
    public void mousePressed(MouseEvent e) {
        pressedComponent = e.getComponent();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (pressedComponent != null && pressedComponent.contains(e.getPoint())) {
            onClick(e);
            pressedComponent = null;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    public abstract void onClick(MouseEvent e);
}
