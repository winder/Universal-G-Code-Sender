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
package com.willwinder.ugs.nbp.core.statusline;

import javax.swing.*;
import java.awt.*;

/**
 * A panel for separate components in the status line with a line.
 * This will also add all necessary paddings.
 *
 * @author Joacim Breiler
 */
public class SeparatorPanel extends JComponent {

    private static final int SPACING = 8;
    private static final String SEPARATOR_CONSTRAINT = BorderLayout.EAST;

    public SeparatorPanel(Component component) {
        BorderLayout borderLayout = new BorderLayout();
        borderLayout.setHgap(SPACING);
        setLayout(borderLayout);
        setBorder(BorderFactory.createEmptyBorder(0, SPACING, 0, 0));

        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        separator.setPreferredSize(new Dimension(1, 0)); // Height is unimportant

        add(component);
        add(separator, SEPARATOR_CONSTRAINT);
    }
}
