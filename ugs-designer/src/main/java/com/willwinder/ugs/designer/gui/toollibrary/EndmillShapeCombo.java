/*
    Copyright 2026 Damian Nikodem

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
package com.willwinder.ugs.designer.gui.toollibrary;

import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Optional;

public class EndmillShapeCombo extends JComboBox<EndmillShape> {
    public EndmillShapeCombo() {
        Arrays.stream(EndmillShape.values()).forEach(this::addItem);
        setSelectedItem(EndmillShape.UPCUT);
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                EndmillShape shape = Optional.ofNullable((EndmillShape) value).orElse(EndmillShape.UPCUT);
                setText(shape.getDisplayName());
                setIcon(new ToolShapeIcon(shape, 16));
                return this;
            }
        });
        setMinimumSize(new Dimension(120, 24));
    }

    public EndmillShape getSelectedShape() {
        return Optional.ofNullable((EndmillShape) getSelectedItem()).orElse(EndmillShape.UPCUT);
    }
}
