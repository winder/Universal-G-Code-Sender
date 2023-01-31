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
package com.willwinder.ugs.nbp.designer.gui.clipart;

import com.willwinder.universalgcodesender.uielements.components.RoundedPanel;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;
import net.miginfocom.swing.MigLayout;

import java.awt.Color;
import java.awt.Dimension;

/**
 * A button that displays a clipart
 *
 * @author Joacim Breiler
 */
public class ClipartButton extends RoundedPanel {
    private final Clipart clipart;

    public ClipartButton(Clipart clipart, ClipartTooltip tooltip) {
        super(12);
        this.clipart = clipart;
        setLayout(new MigLayout("fill, inset 0"));
        setMinimumSize(new Dimension(128, 128));
        setForeground(ThemeColors.LIGHT_GREY);
        setBackground(Color.WHITE);
        setHoverBackground(ThemeColors.LIGHT_GREY);
        setPressedBackground(ThemeColors.VERY_LIGHT_BLUE_GREY);
        add(clipart.getPreview(), "grow");
        addMouseListener(tooltip);
    }

    public Clipart getClipart() {
        return clipart;
    }
}
