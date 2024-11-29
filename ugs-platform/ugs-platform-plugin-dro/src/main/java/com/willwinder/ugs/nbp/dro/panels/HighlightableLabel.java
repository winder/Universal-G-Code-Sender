/*
    Copyright 2023-2024 Will Winder

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
package com.willwinder.ugs.nbp.dro.panels;

import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;

import javax.swing.JLabel;
import java.awt.Color;

/**
 * A label which can be highlighted using the {@link #setHighlighted(boolean)} function.
 *
 * @author Joacim Breiler
 */
public class HighlightableLabel extends JLabel {
    private boolean highlighted = false;
    private boolean isEnabled = true;

    @Override
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
        updateColor();
    }

    @Override
    public boolean isEnabled() {
        // Force returning true or else we will get problems with colors on some LaFs
        return true;
    }

    public void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
        updateColor();
    }

    private void updateColor() {
        Color color = isEnabled ? ThemeColors.LIGHT_BLUE : ThemeColors.LIGHT_BLUE_GREY;

        if (highlighted) {
            color = ThemeColors.GREEN;
        }

        setForeground(color);
    }
}
