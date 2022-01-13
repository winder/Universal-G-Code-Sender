/*
    Copyright 2022 Will Winder

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
package com.willwinder.ugs.nbp.designer.gui;

import net.miginfocom.swing.MigLayout;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Component;
import java.awt.Font;

/**
 * @author Joacim Breiler
 */
public class FontDropDownRenderer extends JPanel implements javax.swing.ListCellRenderer<String> {

    public static final int PREVIEW_FONT_SIZE = 10;
    private final JLabel label;
    private final JLabel preview;

    FontDropDownRenderer() {
        setLayout(new MigLayout("insets 0"));
        setBorder(new EmptyBorder(0, 0, 0, 0));
        setOpaque(true);

        label = new JLabel();
        add(label);

        preview = new JLabel("ABCDEF");
        preview.setEnabled(false);
        add(preview);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        Font font = new Font(value, Font.PLAIN, PREVIEW_FONT_SIZE);
        label.setText(font.getFamily());
        preview.setFont(font);
        return this;
    }
}
