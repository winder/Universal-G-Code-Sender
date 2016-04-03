package com.willwinder.universalgcodesender.uielements.connection;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.utils.CommUtils;
import com.willwinder.universalgcodesender.utils.FirmwareUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.List;

import static com.willwinder.universalgcodesender.utils.GUIHelpers.displayErrorDialog;

public class ConnectionPanel extends JPanel implements UGSEventListener {

    private final JLabel portLabel = new JLabel(Localization.getString("mainWindow.swing.portLabel"));
    private final JLabel baudLabel = new JLabel(Localization.getString("mainWindow.swing.baudLabel"));
    private final JLabel firmwareLabel = new JLabel(Localization.getString("mainWindow.swing.firmwareLabel"));

    private final JComboBox portCombo = new JComboBox();
    private final JComboBox baudCombo = new JComboBox();
    private final JComboBox firmwareCombo = new JComboBox();

    private final JButton refreshButton = new JButton(new javax.swing.ImageIcon(getClass().getResource("/resources/refresh.gif")));
    private final JButton openCloseButton = new JButton(Localization.getString("mainWindow.swing.opencloseButton"));

    private final BackendAPI backend;

    public ConnectionPanel() {
        this(null);
    }

    public ConnectionPanel(BackendAPI backend) {
        setBorder(javax.swing.BorderFactory.createTitledBorder(Localization.getString("mainWindow.swing.connectionPanel")));
        setMaximumSize(new java.awt.Dimension(247, 130));
        setMinimumSize(new java.awt.Dimension(247, 130));
        setName(Localization.getString("mainWindow.swing.connectionPanel"));
        setPreferredSize(new java.awt.Dimension(247, 130));

        portCombo.setEditable(true);

        baudCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"2400", "4800", "9600", "19200", "38400", "57600", "115200"}));
        baudCombo.setSelectedIndex(2);
        baudCombo.setToolTipText("Select baudrate to use for the serial port.");

        openCloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                opencloseButtonActionPerformed(evt);
            }
        });

        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        loadFirmwareSelector();

        this.backend = backend;
        if (this.backend != null) {
            this.backend.addUGSEventListener(this);
        }

        initComponents();
    }

    private void initComponents() {
        // MigLayout... 3rd party layout library.
        setLayout(new MigLayout("fill, wrap 4"));
        add(portLabel, "al right");
        add(portCombo, "span 3");
        add(baudLabel, "al right");
        add(baudCombo);
        add(refreshButton, "w 25!");
        add(openCloseButton);
        add(firmwareLabel, "al right");
        add(firmwareCombo, "span 3");
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt.isFileChangeEvent() || evt.isStateChangeEvent()) {
            updateControls();
        }
    }

    private void updateControls() {
        switch (backend.getControlState()) {
            case COMM_DISCONNECTED:
                this.updateConnectionControlsStateOpen(false);
                break;
            case COMM_IDLE:
                this.updateConnectionControlsStateOpen(true);
                break;
            case COMM_SENDING:
                break;
            case COMM_SENDING_PAUSED:
                break;
            default:
        }
    }

    private void opencloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_opencloseButtonActionPerformed
        if (this.openCloseButton.getText().equalsIgnoreCase(Localization.getString("open"))) {
            String firmware = this.firmwareCombo.getSelectedItem().toString();
            String port = portCombo.getSelectedItem().toString();
            int baudRate = Integer.parseInt(baudCombo.getSelectedItem().toString());

            try {
                this.backend.connect(firmware, port, baudRate);
            } catch (Exception e) {
                displayErrorDialog(e.getMessage());
            }
        } else {
            try {
                this.backend.disconnect();
            } catch (Exception e) {
                displayErrorDialog(e.getMessage());
            }
        }
    }

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        loadPortSelector();
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
                portCombo.addItem(port);
            }

            portCombo.setSelectedIndex(0);
        }
    }

    private void loadFirmwareSelector() {
        firmwareCombo.removeAllItems();
        List<String> firmwareList = FirmwareUtils.getFirmwareList();

        if (firmwareList.size() < 1) {
            displayErrorDialog(Localization.getString("mainWindow.error.noFirmware"));
        } else {
            java.util.Iterator<String> iter = firmwareList.iterator();
            while (iter.hasNext()) {
                firmwareCombo.addItem(iter.next());
            }
        }
    }

    public void saveSettings() {
        backend.getSettings().setPort(portCombo.getSelectedItem().toString());
        backend.getSettings().setPortRate(baudCombo.getSelectedItem().toString());
        backend.getSettings().setFirmwareVersion(firmwareCombo.getSelectedItem().toString());
    }

    public void loadSettings() {
        portCombo.setSelectedItem(backend.getSettings().getPort());
        baudCombo.setSelectedItem(backend.getSettings().getPortRate());
        firmwareCombo.setSelectedItem(backend.getSettings().getFirmwareVersion());
    }

    public void updateConnectionControlsStateOpen(boolean isOpen) {
        this.portCombo.setEnabled(!isOpen);
        this.baudCombo.setEnabled(!isOpen);
        this.refreshButton.setEnabled(!isOpen);
        this.firmwareCombo.setEnabled(!isOpen);

        if (isOpen) {
            this.openCloseButton.setText(Localization.getString("close"));
        } else {
            this.openCloseButton.setText(Localization.getString("open"));
        }
    }

}
