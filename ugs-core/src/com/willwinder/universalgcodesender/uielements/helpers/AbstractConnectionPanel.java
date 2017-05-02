/*
    Copywrite 2016 Will Winder

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
package com.willwinder.universalgcodesender.uielements.helpers;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.uielements.actions.ConnectDisconnectAction;
import com.willwinder.universalgcodesender.utils.CommUtils;
import com.willwinder.universalgcodesender.utils.FirmwareUtils;
import com.willwinder.universalgcodesender.utils.Settings;
import java.awt.Component;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.commons.lang3.StringUtils;

import static com.willwinder.universalgcodesender.model.UGSEvent.ControlState.COMM_DISCONNECTED;
import static com.willwinder.universalgcodesender.utils.GUIHelpers.displayErrorDialog;

/**
 * @author wwinder
 */
public abstract class AbstractConnectionPanel extends JPanel implements UGSEventListener {
    protected final BackendAPI backend;

    // Icon paths
    static private final String REFRESH_ICON = "/resources/icons/refresh.gif";
    static private final String CONNECTED_ICON = "/resources/icons/connect.png";
    static private final String DISCONNECTED_ICON = "/resources/icons/disconnect.gif";

    // Icon resources
    private final ImageIcon refreshIcon =  new ImageIcon(getClass().getResource(REFRESH_ICON));
    private final ImageIcon connectedIcon = new ImageIcon(getClass().getResource(CONNECTED_ICON));
    private final ImageIcon disconnectedIcon = new ImageIcon(getClass().getResource(DISCONNECTED_ICON));

    // Localized labels
    protected final JLabel portLabel = new JLabel(Localization.getString("mainWindow.swing.portLabel"));
    protected final JLabel baudLabel = new JLabel(Localization.getString("mainWindow.swing.baudLabel"));
    protected final JLabel firmwareLabel = new JLabel(Localization.getString("mainWindow.swing.firmwareLabel"));

    // Combo boxes, these need to be synchronized with Settings
    protected final JComboBox<String> firmwareCombo = new JComboBox<>();
    protected final JComboBox<String> portCombo = new JComboBox<>();
    protected final JComboBox<String> baudCombo = new JComboBox<>();
    protected final JButton refreshButton = new JButton();
    protected final JButton connectDisconnectButton = new JButton();
    
    private boolean initializing = true;

    public AbstractConnectionPanel(BackendAPI backend) {
        this.backend = backend;
        this.backend.addUGSEventListener(this);

        loadFirmwareSelector();
        initComponents();

        initializing = false;
        updateComponents();
    }

    /**
     * Implementing classes should define the layout of all desired components
     * in this method. It will be called once during construction.
     */
    protected abstract void layoutComponents();

    @Override
    public void UGSEvent(UGSEvent evt) {
        // If a setting has changed elsewhere, update the combo boxes.
        if (evt.isSettingChangeEvent()) {
            System.out.println("Setting changed");
            updateComponents();
        }
        // if the state has changed, check if the 
        else if (evt.isStateChangeEvent()) {
            if (evt.getControlState() == COMM_DISCONNECTED) {
                connectDisconnectButton.setIcon(disconnectedIcon);
                firmwareCombo.setEnabled(true);
                baudCombo.setEnabled(true);
                portCombo.setEnabled(true);
                refreshButton.setEnabled(true);
            } else {
                connectDisconnectButton.setIcon(connectedIcon);
                firmwareCombo.setEnabled(false);
                baudCombo.setEnabled(false);
                portCombo.setEnabled(false);
                refreshButton.setEnabled(false);
            }
        }
    }

    private void initComponents() {
        // People should be able to type in custom values.
        portCombo.setEditable(true);
        baudCombo.setEditable(true);

        // Baud rate options.
        baudCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"2400", "4800", "9600", "19200", "38400", "57600", "115200", "230400"}));
        baudCombo.setSelectedIndex(6);
        baudCombo.setToolTipText("Select baudrate to use for the serial port.");

        // Hookup button actions
        refreshButton.addActionListener(evt -> loadPortSelector());
        connectDisconnectButton.setAction(new ConnectDisconnectAction(backend));

        // Customize appearance
        refreshButton.setIcon(refreshIcon);
        connectDisconnectButton.setIcon(disconnectedIcon);
        connectDisconnectButton.setBorderPainted(false);

        // Hookup change reporters
        firmwareCombo.addActionListener(a -> componentUpdated(firmwareCombo));
        portCombo.addActionListener(a -> componentUpdated(portCombo));
        baudCombo.addActionListener(a -> componentUpdated(baudCombo));

        // Layout of components by implementing class.
        layoutComponents();
    }

    synchronized private void updateComponents() {
        if (initializing) return;
        initializing = true;
        Settings s = backend.getSettings();
        portCombo.setSelectedItem(s.getPort());
        baudCombo.setSelectedItem(s.getPortRate());
        firmwareCombo.setSelectedItem(s.getFirmwareVersion());
        initializing = false;
    }

    private void componentUpdated(Component c) {
        if (initializing) return;
        if (c == portCombo) {
            backend.getSettings().setPort(portCombo.getSelectedItem() + "");
        }
        else if (c == baudCombo) {
            backend.getSettings().setPortRate(baudCombo.getSelectedItem() + "");
        }
        else if (c == firmwareCombo) {
            backend.getSettings().setFirmwareVersion(firmwareCombo.getSelectedItem() + "");
        }
    }

    private void loadPortSelector() {
        portCombo.removeAllItems();
        String[] portList = CommUtils.getSerialPortList();

        if (portList.length < 1) {
            if (backend.getSettings().isShowSerialPortWarning()) {
                displayErrorDialog(Localization.getString("mainWindow.error.noSerialPort"));
            }
        } else {
            for (String port : portList) {
                if (StringUtils.isNotEmpty(port)) {
                    System.out.println(">>>>>> ");
                    portCombo.addItem(port);
                }
            }

            portCombo.setSelectedIndex(0);
            portCombo.repaint();
        }
    }

    private void loadFirmwareSelector() {
        firmwareCombo.removeAllItems();
        List<String> firmwareList = FirmwareUtils.getFirmwareList();

        if (firmwareList.size() < 1) {
            displayErrorDialog(Localization.getString("mainWindow.error.noFirmware"));
        } else {
            firmwareList.forEach(firmwareCombo::addItem);
        }
    }
}
