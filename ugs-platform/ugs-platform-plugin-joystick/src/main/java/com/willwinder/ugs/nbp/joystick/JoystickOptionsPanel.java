/*
    Copyright 2020 Will Winder

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
package com.willwinder.ugs.nbp.joystick;

import com.willwinder.ugs.nbp.joystick.model.JoystickState;
import com.willwinder.ugs.nbp.joystick.service.JoystickService;
import com.willwinder.ugs.nbp.joystick.service.JoystickServiceListener;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.options.AbstractOptionsPanel;

import javax.swing.*;
import java.awt.*;

public class JoystickOptionsPanel extends AbstractOptionsPanel implements JoystickServiceListener {

    private final JoystickService joystickService;
    private JPanel panel;
    private Checkbox activeCheckbox;

    JoystickOptionsPanel(JoystickOptionsPanelController controller) {
        super(controller);
        joystickService = CentralLookup.getDefault().lookup(JoystickService.class);
        joystickService.addListener(this);

        super.setLayout(new BorderLayout());
    }

    @Override
    public void load() {
        if (panel != null) {
            this.remove(panel);
        }

        panel = new JPanel();
        activeCheckbox = new Checkbox("Active", Settings.isActive());
        panel.add(activeCheckbox);

        add(panel, BorderLayout.CENTER);
        SwingUtilities.invokeLater(changer::changed);
    }

    @Override
    public void store() {
        if (activeCheckbox.getState()) {
            joystickService.initialize();
        } else {
            joystickService.destroy();
        }

        Settings.setActive(activeCheckbox.getState());
    }

    @Override
    public boolean valid() {
        return true;
    }

    @Override
    public void onUpdate(JoystickState state) {

    }
}
