/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.ugs.designer.gui;

import com.willwinder.ugs.designer.entities.cuttable.PlungeType;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import java.awt.Component;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A combo box for selecting how the tool should engage the material
 *
 * @author Joacim Breiler
 */
public class PlungeTypeCombo extends JComboBox<PlungeType> {
    public PlungeTypeCombo() {
        Arrays.stream(PlungeType.values()).forEach(this::addItem);
        setSelectedItem(PlungeType.STRAIGHT);
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof PlungeType plungeType) {
                    setText(plungeType.getLabel());
                }
                return this;
            }
        });

        setMinimumSize(new Dimension(100, 24));
    }

    public PlungeType getSelectedPlungeType() {
        return (PlungeType) getSelectedItem();
    }

    public void setPlungeTypes(List<PlungeType> plungeTypes) {
        if (!needsUpdating(plungeTypes)) {
            return;
        }

        PlungeType selected = getSelectedPlungeType();
        setModel(new DefaultComboBoxModel<>(plungeTypes.toArray(PlungeType[]::new)));
        if (selected != null && plungeTypes.contains(selected)) {
            setSelectedItem(selected);
        }
    }

    private boolean needsUpdating(List<PlungeType> plungeTypes) {
        ComboBoxModel<PlungeType> model = getModel();
        if (model.getSize() != plungeTypes.size()) {
            return true;
        }

        Set<PlungeType> current = new HashSet<>();
        for (int i = 0; i < model.getSize(); i++) {
            current.add(model.getElementAt(i));
        }

        return !current.equals(new HashSet<>(plungeTypes));
    }
}
