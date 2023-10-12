/*
Copyright 2023 Will Winder

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
package com.willwinder.ugs.nbp.core.ui;

import com.willwinder.universalgcodesender.connection.ConnectionDriver;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.BaudRateEnum;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;

import javax.swing.*;

public class BaudComboBox extends JComboBox<String> implements UGSEventListener {
    private static final String[] TCP_PORTS = {"23", "80"};

    private final transient BackendAPI backend;

    public BaudComboBox(BackendAPI backend) {
        super(new DefaultComboBoxModel<>(BaudRateEnum.getAllBaudRates()));
        this.backend = backend;
        setEditable(true);
        setToolTipText("Select baudrate to use for the serial port.");
        addActionListener(e -> onUpdate());
        backend.addUGSEventListener(this);

        updateBaudRate();
    }

    private void onUpdate() {
        Object selectedItem = getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        this.backend.getSettings().setPortRate(selectedItem.toString());
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        // If a setting has changed elsewhere, update the combo box
        if (evt instanceof SettingChangedEvent) {
            updateBaudRate();
        }
    }

    private void updateBaudRate() {
        if (backend.getSettings().getConnectionDriver() == ConnectionDriver.TCP || backend.getSettings().getConnectionDriver() == ConnectionDriver.WS) {
            setModel(new DefaultComboBoxModel<>(TCP_PORTS));
        } else {
            setModel(new DefaultComboBoxModel<>(BaudRateEnum.getAllBaudRates()));
        }

        setSelectedItem(backend.getSettings().getPortRate());
    }
}
