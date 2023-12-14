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
package com.willwinder.ugs.nbp.core.ui;

import com.willwinder.universalgcodesender.uielements.components.RoundedPanel;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;
import net.miginfocom.swing.MigLayout;
import org.openide.util.ImageUtilities;

import javax.swing.*;

/**
 * A label that displays a framed hint message with an info icon
 */
public class HintLabel extends JPanel {
    private final RoundedPanel hintPanel;

    public HintLabel(String text) {
        setLayout(new MigLayout("fill, insets 0"));

        hintPanel = new RoundedPanel(8);
        hintPanel.setLayout(new MigLayout("fill, inset 10, gap 0"));
        hintPanel.setBackground(ThemeColors.VERY_LIGHT_BLUE_GREY);
        hintPanel.setForeground(ThemeColors.LIGHT_GREY);
        hintPanel.add(new JLabel(ImageUtilities.loadImageIcon("resources/icons/hint24.svg", false)), "gapright 10");
        hintPanel.add(new JLabel("<html><body>" + text + "</body></html>"), "grow");
        add(hintPanel, "grow");
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        hintPanel.setEnabled(enabled);
    }
}
