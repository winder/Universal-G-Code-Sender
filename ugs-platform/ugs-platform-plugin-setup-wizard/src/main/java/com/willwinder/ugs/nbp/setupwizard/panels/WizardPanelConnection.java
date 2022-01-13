/*
    Copyright 2018-2020 Will Winder

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
package com.willwinder.ugs.nbp.setupwizard.panels;

import com.willwinder.ugs.nbp.setupwizard.AbstractWizardPanel;
import com.willwinder.universalgcodesender.connection.ConnectionFactory;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.BaudRateEnum;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.utils.FirmwareUtils;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.ImageUtilities;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import static com.willwinder.universalgcodesender.utils.GUIHelpers.displayErrorDialog;

/**
 * A wizard step panel for connecting to a controller.
 *
 * @author Joacim Breiler
 */
public class WizardPanelConnection extends AbstractWizardPanel implements UGSEventListener {

    /**
     * A time interval to wait before querying the controller for version.
     * This gives the controller to time to properly boot
     */
    private static final int CONNECT_TIME = 3000;

    /**
     * If the controller should be finished connecting
     */
    private boolean finishedConnecting = false;

    /**
     * A timer that will check the version of the controller after its connected with a delay
     * defined in {@link #CONNECT_TIME}
     */
    private ScheduledFuture<?> finishedConnectingFuture;

    private JComboBox<String> firmwareCombo;
    private JComboBox<String> baudCombo;
    private JComboBox<String> portCombo;
    private JButton connectButton;
    private JLabel labelVersion;
    private JLabel labelFirmware;
    private JLabel labelBaud;
    private JLabel labelPort;
    private JLabel labelDescription;
    private JLabel labelNotSupported;

    public WizardPanelConnection(BackendAPI backend) {
        super(backend, Localization.getString("platform.plugin.setupwizard.connection.title"), false);

        initComponents();
        initLayout();
    }

    private void initLayout() {
        JPanel panel = new JPanel(new MigLayout("fillx, inset 0, gap 5, hidemode 3"));
        panel.add(labelDescription, "growx, wrap, gapbottom 10");
        panel.add(labelFirmware, "wrap");
        panel.add(firmwareCombo, "wmin 250, wrap");
        panel.add(labelBaud, "wrap");
        panel.add(baudCombo, "wmin 250, wrap");
        panel.add(labelPort, "wrap");
        panel.add(portCombo, "wmin 250, wrap");
        panel.add(connectButton, "wmin 250, hmin 36, wrap, gaptop 10");
        panel.add(labelVersion, "grow, aligny top, wrap");
        panel.add(labelNotSupported, "grow, aligny top, wrap");
        getPanel().add(panel, "aligny top, growx");
    }

    private void initComponents() {
        labelDescription = new JLabel("<html><body><p>" +
                Localization.getString("platform.plugin.setupwizard.connection.intro") +
                "</p></body></html>");

        // Firmware options
        firmwareCombo = new JComboBox<>();
        firmwareCombo.addActionListener(a -> setFirmware());
        labelFirmware = new JLabel("Firmware:");

        // Baud rate options
        baudCombo = new JComboBox<>();
        baudCombo.setModel(new DefaultComboBoxModel<>(BaudRateEnum.getAllBaudRates()));
        baudCombo.setSelectedIndex(6);
        baudCombo.setToolTipText("Select baudrate to use for the serial port.");
        baudCombo.addActionListener(e -> this.setBaudRate());
        labelBaud = new JLabel(Localization.getString("platform.plugin.setupwizard.port-rate"));

        portCombo = new JComboBox<>();
        portCombo.addActionListener(e -> this.setPort());
        labelPort = new JLabel(Localization.getString("platform.plugin.setupwizard.port"));

        connectButton = new JButton(Localization.getString("platform.plugin.setupwizard.connect"));
        connectButton.addActionListener((e) -> {
            try {
                Settings settings = getBackend().getSettings();
                getBackend().connect(settings.getFirmwareVersion(), settings.getPort(), Integer.parseInt(settings.getPortRate()));
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        });

        labelVersion = new JLabel(Localization.getString("platform.plugin.setupwizard.unknown-version"));
        labelVersion.setVisible(false);

        labelNotSupported = new JLabel(Localization.getString("platform.plugin.setupwizard.connection.not-supported"));
        labelNotSupported.setIcon(ImageUtilities.loadImageIcon("icons/information24.png", false));
        labelNotSupported.setVisible(false);
    }

    private void setPort() {
        if (getBackend().getSettings() != null && portCombo.getSelectedItem() != null) {
            getBackend().getSettings().setPort(portCombo.getSelectedItem().toString());
        }
    }

    private void setBaudRate() {
        if (getBackend().getSettings() != null && baudCombo.getSelectedItem() != null) {
            String baudRate = baudCombo.getSelectedItem().toString();
            getBackend().getSettings().setPortRate(baudRate);
        }
    }

    private void setFirmware() {
        if (getBackend().getSettings() != null && firmwareCombo.getSelectedItem() != null) {
            String firmware = firmwareCombo.getSelectedItem().toString();
            getBackend().getSettings().setFirmwareVersion(firmware);
        }
    }

    private void firmwareUpdated() {
        firmwareCombo.setSelectedItem(getBackend().getSettings().getFirmwareVersion());
    }

    private void refreshPorts() {
        portCombo.removeAllItems();
        List<String> portList = ConnectionFactory.getPortNames(getBackend().getSettings().getConnectionDriver());
        if (portList.size() < 1) {
            if (getBackend().getSettings().isShowSerialPortWarning()) {
                displayErrorDialog(Localization.getString("mainWindow.error.noSerialPort"));
            }
        } else {
            for (String port : portList) {
                if (StringUtils.isNotEmpty(port)) {
                    portCombo.addItem(port);
                }
            }
            if (StringUtils.isNotEmpty(getBackend().getSettings().getPort())) {
                portCombo.setSelectedItem(getBackend().getSettings().getPort());
            } else {
                portCombo.setSelectedIndex(0);
            }

            portCombo.repaint();
        }
    }


    @Override
    public void destroy() {
        getBackend().removeUGSEventListener(this);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void initialize() {
        getBackend().addUGSEventListener(this);

        refreshComponents();
        finishedConnecting = false;
        if (getBackend().isConnected()) {
            startConnectTimer();
        }
    }

    private void refreshComponents() {
        String selectedFirmware = getBackend().getSettings().getFirmwareVersion();
        firmwareCombo.removeAllItems();
        FirmwareUtils.getFirmwareList().forEach(firmwareCombo::addItem);
        firmwareCombo.setSelectedItem(selectedFirmware);

        boolean isConnected = getBackend().isConnected();
        setValid(isConnected && getBackend().getController().getCapabilities().hasSetupWizardSupport());
        labelPort.setVisible(!isConnected);

        portCombo.setVisible(!isConnected);
        portCombo.setSelectedItem(getBackend().getSettings().getPort());

        labelBaud.setVisible(!isConnected);
        baudCombo.setVisible(!isConnected);
        baudCombo.setSelectedItem(getBackend().getSettings().getPortRate());

        labelFirmware.setVisible(!isConnected);
        firmwareCombo.setVisible(!isConnected);
        connectButton.setVisible(!isConnected);

        if (isConnected && StringUtils.isNotEmpty(getBackend().getController().getFirmwareVersion()) && getBackend().getController().getCapabilities().hasSetupWizardSupport() && finishedConnecting) {
            labelVersion.setVisible(true);
            labelVersion.setText("<html><body><h2> " + Localization.getString("platform.plugin.setupwizard.connection.connected-to") + " " + getBackend().getController().getFirmwareVersion() + "</h2></body></html>");
            labelVersion.setIcon(ImageUtilities.loadImageIcon("icons/checked24.png", false));
            labelNotSupported.setVisible(false);
            setFinishPanel(false);
        } else if (isConnected && !getBackend().getController().getCapabilities().hasSetupWizardSupport() && finishedConnecting) {
            labelVersion.setVisible(true);
            labelVersion.setText("<html><body><h2>" + Localization.getString("platform.plugin.setupwizard.connection.connected-to") + " " + getBackend().getController().getFirmwareVersion() + "</h2></body></html>");
            labelVersion.setIcon(null);
            labelNotSupported.setVisible(true);
            setFinishPanel(true);
        } else if (isConnected && !finishedConnecting) {
            labelVersion.setVisible(true);
            labelVersion.setText("<html><body><h2>" + Localization.getString("platform.plugin.setupwizard.connection.connecting") + "</h2></body></html>");
            labelVersion.setIcon(null);
            labelNotSupported.setVisible(false);
            setFinishPanel(false);
        } else {
            labelVersion.setVisible(false);
            labelNotSupported.setVisible(false);
            labelVersion.setIcon(null);
            refreshPorts();
            setFinishPanel(false);
        }
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof ControllerStateEvent) {
            firmwareUpdated();
            refreshComponents();
            startConnectTimer();
        }
    }

    private void startConnectTimer() {
        if (!finishedConnecting && finishedConnectingFuture == null) {
            finishedConnectingFuture = ThreadHelper.invokeLater(() -> {
                finishedConnecting = true;
                finishedConnectingFuture = null;
                refreshComponents();
            }, CONNECT_TIME);
        }
    }
}
