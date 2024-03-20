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
package com.willwinder.universalgcodesender.uielements.components;

import com.willwinder.universalgcodesender.firmware.IOverrideManager;
import com.willwinder.universalgcodesender.listeners.OverrideType;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OverrideRadioButtons extends JPanel {
    private final Map<Integer, JToggleButton> buttons = new HashMap<>();
    private final ButtonGroup buttonGroup;
    private final int defaultValue;
    private final List<ChangeListener> listeners = new ArrayList<>();

    public OverrideRadioButtons(IOverrideManager overrideManager, OverrideType type) {
        setLayout(new MigLayout("fill, inset 0"));
        List<Integer> steps = overrideManager.getRadioSteps(type);
        buttonGroup = new ButtonGroup();
        defaultValue = overrideManager.getRadioDefault(type);

        steps.forEach(step -> {
            JToggleButton button = new JToggleButton();
            button.setAction(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    listeners.forEach(l -> l.stateChanged(new ChangeEvent(this)));
                }
            });
            button.setText(step + "%");
            button.setMargin(new Insets(4, 0, 4, 0));

            buttonGroup.add(button);
            buttons.put(step, button);

            add(button, "growx, sg 1, w 40::");
        });

        setValue(defaultValue);
    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public int getValue() {
        return buttons.entrySet()
                .stream()
                .filter(b -> b.getValue().isSelected())
                .map(Map.Entry::getKey).findFirst()
                .orElse(defaultValue);
    }

    public void setValue(int value) {
        JToggleButton button = buttons.get(value);
        if (button != null) {
            buttonGroup.setSelected(button.getModel(), true);
        }
    }
}
