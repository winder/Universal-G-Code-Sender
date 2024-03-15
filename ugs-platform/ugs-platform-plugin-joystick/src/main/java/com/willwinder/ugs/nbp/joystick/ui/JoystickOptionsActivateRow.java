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
 */package com.willwinder.ugs.nbp.joystick.ui;

import com.willwinder.ugs.nbp.joystick.Settings;
import com.willwinder.ugs.nbp.joystick.action.OpenCustomMappingsDialogAction;
import com.willwinder.ugs.nbp.joystick.service.JoystickService;
import com.willwinder.universalgcodesender.i18n.Localization;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.event.ActionListener;

public class JoystickOptionsActivateRow extends JPanel {

    private final JCheckBox activeCheckbox;

    public JoystickOptionsActivateRow(JoystickService joystickService) {
        setLayout(new MigLayout("fillx, inset 0", "[grow]10[shrink]10[shrink]"));
        joystickService.setActivateActionDispatcher(false);

        activeCheckbox = new JCheckBox(Localization.getString("platform.plugin.joystick.activate"), Settings.isActive());
        activeCheckbox.addActionListener(e -> {
            Settings.setActive(activeCheckbox.isSelected());
            if (activeCheckbox.isSelected()) {
                joystickService.initialize();
            } else {
                joystickService.destroy();
            }
        });

        add(activeCheckbox);
        JLabel connectedLabel = new JoystickStatusLine(joystickService, true);
        add(connectedLabel);
        JButton settingsButton = new JButton(new OpenCustomMappingsDialogAction(joystickService));
        add(settingsButton);
    }

    public void addActionListener(ActionListener actionListener) {
        activeCheckbox.addActionListener(actionListener);
    }

    public boolean isActive() {
        return activeCheckbox.isSelected();
    }
}
