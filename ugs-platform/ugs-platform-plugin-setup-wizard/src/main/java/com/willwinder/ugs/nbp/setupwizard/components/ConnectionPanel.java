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
package com.willwinder.ugs.nbp.setupwizard.components;

import com.willwinder.ugs.nbp.core.ui.BaudComboBox;
import com.willwinder.ugs.nbp.core.ui.PortComboBox;
import com.willwinder.universalgcodesender.connection.ConnectionDriver;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import com.willwinder.universalgcodesender.utils.FirmwareUtils;
import net.miginfocom.swing.MigLayout;

import java.awt.Dimension;
import javax.swing.*;

public class ConnectionPanel extends JPanel implements UGSEventListener {
    private final transient BackendAPI backend;
    private final JComboBox<String> driverCombo;
    private final JComboBox<String> firmwareCombo;
    private final JComboBox<String> baudCombo;
    private final PortComboBox portCombo;
    private final JLabel labelPort;
    private final JLabel labelBaud;

    public ConnectionPanel(BackendAPI backend, Runnable onConnect) {
        this.backend = backend;
        this.backend.addUGSEventListener(this);
        setLayout(new MigLayout("fillx, inset 0, gap 5, hidemode 3"));

        JLabel labelDescription = new JLabel("<html><body><p>" + Localization.getString("platform.plugin.setupwizard.connection.intro") + "</p></body></html>");

        // Driver options
        driverCombo = new JComboBox<>(ConnectionDriver.getPrettyNames());
        driverCombo.addActionListener(d -> this.setDriver());
        JLabel labelDriver = new JLabel(Localization.getString("settings.connectionDriver"));
        labelDriver.setToolTipText("Select the driver to use to connect to your controller firmware. Serial connections should use JSerialComm. Network connections should use TCP.");

        // Firmware options
        firmwareCombo = new JComboBox<>();
        firmwareCombo.addActionListener(a -> setFirmware());
        JLabel labelFirmware = new JLabel(Localization.getString("mainWindow.swing.firmwareLabel"));
        labelFirmware.setToolTipText("Select the controller firmware to which you want to connect.");

        // Baud rate options
        baudCombo = new BaudComboBox(backend);
        baudCombo.addActionListener(e -> this.setBaudRate());
        labelBaud = new JLabel(Localization.getString("platform.plugin.setupwizard.port-rate"));

        portCombo = new PortComboBox(backend);
        portCombo.addActionListener(e -> this.setPort());
        labelPort = new JLabel(Localization.getString("platform.plugin.setupwizard.port"));

        JButton connectButton = new JButton(Localization.getString("platform.plugin.setupwizard.connect"));
        connectButton.addActionListener(e -> onConnect.run());

        add(labelDescription, "growx, wrap, gapbottom 5");
        add(labelDriver, "wrap");
        add(driverCombo, "wmin 250, wmax 250, hmax 24, wrap, gapbottom 5");
        add(labelFirmware, "wrap");
        add(firmwareCombo, "wmin 250, wmax 250, hmax 24, wrap, gapbottom 5");
        add(labelPort, "wrap");
        add(portCombo, "wmin 250, wmax 250, hmax 24, wrap, gapbottom 5");
        add(labelBaud, "wrap");
        add(baudCombo, "wmin 250, wmax 250, hmax 24, wrap, gapbottom 10");
        add(connectButton, "wmin 250, wmax 250, hmin 36, wrap");

        updateLabels();
    }

    private void setDriver() {
        if (backend.getSettings() != null && driverCombo.getSelectedItem() != null) {
            backend.getSettings().setConnectionDriver(ConnectionDriver.prettyNameToEnum(driverCombo.getSelectedItem().toString()));
        }
    }

    private void setPort() {
        if (backend.getSettings() != null && portCombo.getSelectedItem() != null) {
            backend.getSettings().setPort(portCombo.getSelectedItem().toString());
        }
    }

    private void setBaudRate() {
        if (backend.getSettings() != null && baudCombo.getSelectedItem() != null) {
            String baudRate = baudCombo.getSelectedItem().toString();
            backend.getSettings().setPortRate(baudRate);
        }
    }

    private void setFirmware() {
        if (backend.getSettings() != null && firmwareCombo.getSelectedItem() != null) {
            String firmware = firmwareCombo.getSelectedItem().toString();
            backend.getSettings().setFirmwareVersion(firmware);
        }
    }

    private void firmwareUpdated() {
        firmwareCombo.setSelectedItem(backend.getSettings().getFirmwareVersion());
    }

    private void refreshFirmwares() {
        String selectedFirmware = backend.getSettings().getFirmwareVersion();
        firmwareCombo.removeAllItems();
        FirmwareUtils.getFirmwareList().forEach(firmwareCombo::addItem);
        firmwareCombo.setSelectedItem(selectedFirmware);
    }

    private void refreshDrivers() {
        String selectedDriver = backend.getSettings().getConnectionDriver().getPrettyName();
        driverCombo.removeAllItems();
        String[] drivers = ConnectionDriver.getPrettyNames();
        for (String s: drivers) {
            driverCombo.addItem(s);
        }
        driverCombo.setSelectedItem(selectedDriver);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        if (visible) {
            refreshDrivers();
            refreshFirmwares();
            firmwareUpdated();
            portCombo.startRefreshing();
        } else {
            portCombo.stopRefreshing();
        }
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof SettingChangedEvent) {
            updateLabels();
        }
    }

    private void updateLabels() {
        if (backend.getSettings().getConnectionDriver() == ConnectionDriver.TCP || backend.getSettings().getConnectionDriver() == ConnectionDriver.WS) {
            labelPort.setText(Localization.getString("mainWindow.swing.hostLabel"));
            labelBaud.setText(Localization.getString("mainWindow.swing.portLabel"));
        } else {
            labelPort.setText(Localization.getString("mainWindow.swing.portLabel"));
            labelBaud.setText(Localization.getString("mainWindow.swing.baudLabel"));
        }
    }
}
