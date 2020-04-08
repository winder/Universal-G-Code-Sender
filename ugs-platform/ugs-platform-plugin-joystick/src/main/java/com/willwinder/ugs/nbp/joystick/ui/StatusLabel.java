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
package com.willwinder.ugs.nbp.joystick.ui;

import javax.swing.*;
import java.awt.*;

/**
 * A label that can display a green background if set to active.
 *
 * @author Joacim Breiler
 */
public class StatusLabel extends JLabel {
    public StatusLabel(String text) {
        super(text);
    }

    public void setActive(boolean isActive) {
        if (isActive) {
            setBackground(Color.GREEN);
            setOpaque(true);
        } else {
            setBackground(null);
            setOpaque(false);
        }
        invalidate();
        repaint();
    }
}
