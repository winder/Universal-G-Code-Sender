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

import com.willwinder.ugs.nbp.designer.entities.cuttable.CutType;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Arrays;

/**
 * A combo box for selecting a cut type
 *
 * @author Joacim Breiler
 */
public class CutTypeCombo extends JComboBox<CutType> {
    public CutTypeCombo() {
        Arrays.stream(CutType.values()).forEach(this::addItem);
        setSelectedItem(CutType.NONE);
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                CutType cutType = (CutType) value;
                setText(cutType.getName());
                setIcon(new CutTypeIcon(cutType, CutTypeIcon.Size.SMALL));
                return this;
            }
        });

        setMinimumSize(new Dimension(100, 24));
    }
}
