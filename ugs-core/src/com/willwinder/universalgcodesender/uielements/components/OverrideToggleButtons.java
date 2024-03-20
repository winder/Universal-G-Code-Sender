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
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

public class OverrideToggleButtons extends JPanel {
    private final IOverrideManager overrideManager;
    private Map<OverrideType, JToggleButton> toggleButtons = new HashMap<>();

    public OverrideToggleButtons(IOverrideManager overrideManager) {
        this.overrideManager = overrideManager;
        setLayout(new MigLayout("fillx, inset 0"));
        overrideManager.getToggleTypes().forEach(this::createAndAddToggleButtons);

    }

    private void createAndAddToggleButtons(OverrideType overrideType) {
        JToggleButton toggleButton = new JToggleButton(overrideType.name());
        toggleButton.setMargin(new Insets(4, 0, 4, 0));
        toggleButton.setAction(new AbstractAction(overrideType.getLabel()) {
            @Override
            public void actionPerformed(ActionEvent e) {
                overrideManager.toggle(overrideType);
                toggleButton.setSelected(!overrideManager.isToggled(overrideType));
                ThreadHelper.invokeLater(() -> toggleButton.setSelected(overrideManager.isToggled(overrideType)), 200);
            }
        });
        add(toggleButton, "growx, sg 1, w 40:40:");
        toggleButtons.put(overrideType, toggleButton);
    }

    public void setSelected(OverrideType type, boolean toggled) {
        JToggleButton toggleButton = toggleButtons.get(type);
        if (toggleButton != null) {
            toggleButton.setSelected(toggled);
        }
    }
}
