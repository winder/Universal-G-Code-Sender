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

import com.willwinder.universalgcodesender.connection.ConnectionFactory;
import com.willwinder.universalgcodesender.connection.IConnectionDevice;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import com.willwinder.universalgcodesender.utils.RefreshThread;

import javax.swing.*;
import java.util.List;

public class PortComboBox extends JComboBox<IConnectionDevice> implements UGSEventListener {
    private final transient BackendAPI backend;
    private transient RefreshThread refreshThread;

    public PortComboBox(BackendAPI backend) {
        super(new PortComboBoxModel());
        this.backend = backend;
        setRenderer(new PortCellRenderer());
        setEditable(true);
        addActionListener(e -> onUpdate());
        backend.addUGSEventListener(this);

        setSelectedItem(backend.getSettings().getPort());

        startRefreshing();
    }

    private void onUpdate() {
        Object selectedItem = getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        if (selectedItem instanceof IConnectionDevice selectedDevice) {
            backend.getSettings().setPort(selectedDevice.getAddress());
            selectedDevice.getPort().ifPresent(port -> backend.getSettings().setPortRate(port.toString()));
        } else {
            backend.getSettings().setPort(selectedItem.toString());
        }
    }

    private void refreshPorts() {
        if (!isVisible()) {
            return;
        }

        List<? extends IConnectionDevice> availablePorts = ConnectionFactory.getDevices(backend.getSettings().getConnectionDriver());
        PortComboBoxModel model = (PortComboBoxModel) getModel();
        model.setElements(availablePorts.stream().map(s -> (IConnectionDevice)(s)).toList());
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof SettingChangedEvent) {
            setSelectedItem(backend.getSettings().getPort());
        }
    }
    public void startRefreshing() {
        if (refreshThread == null || !refreshThread.isAlive()) {
            try {
                refreshThread = new RefreshThread(this::refreshPorts, 3000);
                refreshThread.start();
            } catch (IllegalThreadStateException e) {
                // Ignore
            }
        }
    }

    public void stopRefreshing() {
        refreshThread.interrupt();
    }
}
