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

import com.willwinder.ugs.nbp.designer.entities.cuttable.Direction;

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
 * A combo box for selecting a cut direction
 *
 * @author Joacim Breiler
 */
public class DirectionCombo extends JComboBox<Direction> {
    public DirectionCombo() {
        Arrays.stream(Direction.values()).forEach(this::addItem);
        setSelectedItem(Direction.CLIMB);
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null) {
                    return this;
                }

                Direction direction = (Direction) value;
                setText(direction.getLabel());
                setIcon(new DirectionIcon(direction, DirectionIcon.Size.SMALL));
                return this;
            }
        });

        setMinimumSize(new Dimension(100, 24));
    }

    public Direction getSelectedDirection() {
        return (Direction) getSelectedItem();
    }

    public void setDirections(List<Direction> directions) {
        if (!needsUpdating(directions)) {
            return;
        }

        Direction selected = getSelectedDirection();
        setModel(new DefaultComboBoxModel<>(directions.toArray(Direction[]::new)));
        if (selected != null && directions.contains(selected)) {
            setSelectedItem(selected);
        }
    }

    private boolean needsUpdating(List<Direction> directions) {
        ComboBoxModel<Direction> model = getModel();
        if (model.getSize() != directions.size()) {
            return true;
        }

        Set<Direction> current = new HashSet<>();
        for (int i = 0; i < model.getSize(); i++) {
            current.add(model.getElementAt(i));
        }

        return !current.equals(new HashSet<>(directions));
    }
}
