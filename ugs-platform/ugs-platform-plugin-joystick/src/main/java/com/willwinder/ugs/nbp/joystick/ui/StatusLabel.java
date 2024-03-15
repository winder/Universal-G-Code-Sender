/*
    Copyright 2020-2024 Will Winder

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

import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.FlowLayout;

/**
 * A label that can display a green background if set to active.
 *
 * @author Joacim Breiler
 */
public class StatusLabel extends JPanel {
    private final PiePanelStatus pie;

    public StatusLabel(String text) {
        setLayout(new FlowLayout(FlowLayout.RIGHT));
        JLabel label = new JLabel(text, SwingConstants.RIGHT);
        this.pie = new PiePanelStatus(16, ThemeColors.GREEN);
        add(label);
        add(pie);
    }

    public void setActive(boolean isActive) {
        pie.setValue(isActive ? 1 : 0);
        invalidate();
        repaint();
    }

    public void setAnalogValue(float value) {
        pie.setValue(value);
        invalidate();
        repaint();
    }
}
