/*
    Copyright 2024 Will Winder

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

import javax.swing.JComboBox;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;

/**
 * A combo box for selecting a font
 *
 * @author Joacim Breiler
 */
public class  FontCombo extends JComboBox<String> {
    public FontCombo() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Arrays.stream(ge.getAvailableFontFamilyNames()).distinct().forEach(this::addItem);
        setRenderer(new FontDropDownRenderer());

        Dimension minimumSize = getMinimumSize();
        setMinimumSize(new Dimension(100, minimumSize.height));
        setFontFamily(Font.SANS_SERIF);
    }

    public void setFontFamily(String font) {
        setSelectedItem(font);
    }
}
