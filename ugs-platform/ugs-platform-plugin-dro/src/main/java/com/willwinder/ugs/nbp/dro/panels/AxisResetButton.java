/*
    Copyright 2020-2023 Will Winder

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

import com.willwinder.ugs.nbp.dro.FontManager;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.uielements.components.RoundedPanel;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;
import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import java.awt.Color;

public class AxisResetButton extends RoundedPanel {
    private static final int RADIUS = 7;
    private final JLabel axisLabel;
    private final JLabel zeroLabel;

    public AxisResetButton(Axis axis, FontManager fontManager) {
        super(RADIUS);

        setLayout(new MigLayout("inset 4 10 4 12, gap 0"));
        axisLabel = new JLabel(String.valueOf(axis));
        add(axisLabel, "al center, dock center, id axis");

        zeroLabel = new JLabel("0");
        add(zeroLabel, "id zero, pos (axis.x + axis.w - 4) (axis.y + axis.h - zero.h)");

        fontManager.addAxisResetLabel(axisLabel);
        fontManager.addAxisResetZeroLabel(zeroLabel);
        setEnabled(isEnabled());
    }

    @Override
    public void setEnabled(boolean enabled) {
        Color background = enabled ? ThemeColors.DARK_BLUE_GREY : ThemeColors.VERY_DARK_GREY;
        setBackground(background);

        Color hoverBackground = enabled ? ThemeColors.MED_BLUE_GREY : ThemeColors.VERY_DARK_GREY;
        setHoverBackground(hoverBackground);

        Color foreGround = enabled ? ThemeColors.LIGHT_BLUE : ThemeColors.LIGHT_BLUE_GREY;
        setForeground(foreGround);

        axisLabel.setForeground(foreGround);
        zeroLabel.setForeground(foreGround);
    }

    @Override
    public boolean isEnabled() {
        // Force returning true or else we will get problems with colors on some LaFs
        return true;
    }
}
