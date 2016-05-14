package com.willwinder.universalgcodesender.uielements.connection;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.uielements.SendStatusPanel;
import com.willwinder.universalgcodesender.uielements.jog.JogPanel;
import com.willwinder.universalgcodesender.uielements.machinestatus.MachineStatusPanel;
import com.willwinder.universalgcodesender.utils.CommUtils;
import com.willwinder.universalgcodesender.utils.FirmwareUtils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

import static com.willwinder.universalgcodesender.utils.GUIHelpers.displayErrorDialog;

public class ConnectionPanel extends JPanel implements UGSEventListener, ControllerListener {

    private final JLabel portLabel = new JLabel(Localization.getString("mainWindow.swing.portLabel"));
    private final JLabel baudLabel = new JLabel(Localization.getString("mainWindow.swing.baudLabel"));
    private final JLabel firmwareLabel = new JLabel(Localization.getString("mainWindow.swing.firmwareLabel"));

    private final JComboBox portCombo = new JComboBox();
    private final JComboBox baudCombo = new JComboBox();
    private final JComboBox firmwareCombo = new JComboBox();

    private final JButton refreshButton = new JButton(new javax.swing.ImageIcon(getClass().getResource("/resources/refresh.gif")));
    private final JButton openCloseButton = new JButton(Localization.getString("mainWindow.swing.opencloseButton"));

    private final JButton sendPauseResumeButton = new JButton(Localization.getString("mainWindow.swing.sendButton"));
    private final JButton browseCancelButton = new JButton(Localization.getString("mainWindow.swing.browseButton"));

    private final javax.swing.JFileChooser fileChooser;

    private final JPanel connection = new JPanel();
    private final MachineStatusPanel machineStatus;

    private final JLabel currentFile = new JLabel();
    private final SendStatusPanel sendStatusPanel;

    private final JogPanel jogPanel;

    private final BackendAPI backend;

    /**
     * No-Arg constructor to make this control work in the UI builder tools
     * @deprecated Use constructor with BackendAPI.
     */
    @Deprecated
    public ConnectionPanel() {
        this(null);
    }

    public ConnectionPanel(BackendAPI backend) {
        this.backend = backend;
        if (this.backend != null) {
            this.backend.addUGSEventListener(this);
        }

        machineStatus = new MachineStatusPanel(backend);
        machineStatus.setVisible(false);
        sendStatusPanel = new SendStatusPanel(backend);
        setName(Localization.getString("mainWindow.swing.connectionPanel"));

        fileChooser = new JFileChooser();

        jogPanel = new JogPanel(backend);

        initComponents();
    }

    private void initComponents() {
        loadFirmwareSelector();

        portCombo.setEditable(true);

        baudCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"2400", "4800", "9600", "19200", "38400", "57600", "115200"}));
        baudCombo.setSelectedIndex(2);
        baudCombo.setToolTipText("Select baudrate to use for the serial port.");

        openCloseButton.addActionListener(evt -> opencloseButtonActionPerformed(evt));
        refreshButton.addActionListener(evt -> loadPortSelector());
        sendPauseResumeButton.addActionListener(evt -> sendPauseResumeButtonActionPerformed(evt));
        browseCancelButton.addActionListener(evt -> browseCancelButtonActionPerformed(evt));

        Font f = currentFile.getFont();
        currentFile.setFont(f.deriveFont(f.getStyle() | Font.BOLD));

        sendStatusPanel.setBorder(BorderFactory.createTitledBorder(""));

        this.updateConnectionControlsStateOpen(false);

        // MigLayout... 3rd party layout library.
        setLayout(new MigLayout("flowy, hidemode 3"));
        add(openCloseButton, "wmin button, grow, flowx, split 3");
        add(sendPauseResumeButton, "r, wmin button, grow");
        add(browseCancelButton, "r, wmin button, grow");

        connection.setLayout(new MigLayout("fill, wrap 3"));
        connection.add(portLabel, "al right");
        connection.add(portCombo, "span 3");
        connection.add(baudLabel, "al right");
        connection.add(baudCombo);
        connection.add(refreshButton);
        connection.add(firmwareLabel, "al right");
        connection.add(firmwareCombo, "span 3");
        add(connection);
        add(machineStatus, "grow");
        add(currentFile, "grow");
        add(sendStatusPanel, "grow");
        add(jogPanel, "grow");
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt.isFileChangeEvent()) {
           switch(evt.getFileState()) {
               case FILE_LOADED:
                   updateConnectionControlsStateOpen(backend.isConnected());
                   updateCurrentFileLabel(new File(evt.getFile()));
                   break;

               case FILE_LOADING:
                   sendPauseResumeButton.setEnabled(false);
                   currentFile.setText("Loading...");
                   currentFile.setToolTipText("");
                   break;
           }
        }


        if (evt.isStateChangeEvent()) {
            switch (evt.getControlState()) {
                case COMM_DISCONNECTED:
                    this.updateConnectionControlsStateOpen(false);
                    break;

                case COMM_IDLE:
                    this.updateConnectionControlsStateOpen(true);
                    sendPauseResumeButton.setText(Localization.getString("mainWindow.swing.sendButton"));
                    browseCancelButton.setText(Localization.getString("mainWindow.swing.browseButton"));
                    break;

                case COMM_SENDING:
                    sendPauseResumeButton.setText(Localization.getString("mainWindow.swing.pauseButton"));
                    browseCancelButton.setText(Localization.getString("mainWindow.swing.cancelButton"));
                    break;

                case COMM_SENDING_PAUSED:
                    sendPauseResumeButton.setText(Localization.getString("mainWindow.ui.resumeButton"));
                    break;

                default:
            }
        }
    }

    @Override
    public void controlStateChange(UGSEvent.ControlState state) {
    }

    @Override
    public void fileStreamComplete(String filename, boolean success) {
        sendPauseResumeButton.setText(Localization.getString("mainWindow.swing.sendButton"));
        browseCancelButton.setText(Localization.getString("mainWindow.swing.browseButton"));
    }

    @Override
    public void commandSkipped(GcodeCommand command) {

    }

    @Override
    public void commandSent(GcodeCommand command) {

    }

    @Override
    public void commandComplete(GcodeCommand command) {

    }

    @Override
    public void commandComment(String comment) {

    }

    @Override
    public void messageForConsole(MessageType type, String msg) {

    }

    @Override
    public void statusStringListener(String state, Position machineCoord, Position workCoord) {

    }

    @Override
    public void postProcessData(int numRows) {

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
        backend.getSettings().setLastOpenedFilename(fileChooser.getSelectedFile().getAbsolutePath());
        jogPanel.saveSettings();
    }

    public void loadSettings() {
        portCombo.setSelectedItem(backend.getSettings().getPort());
        baudCombo.setSelectedItem(backend.getSettings().getPortRate());
        firmwareCombo.setSelectedItem(backend.getSettings().getFirmwareVersion());

        String lastOpenedFilename = backend.getSettings().getLastOpenedFilename();
        if (lastOpenedFilename != null && !lastOpenedFilename.isEmpty()) {
            fileChooser.setSelectedFile(new File(lastOpenedFilename));
        }

        updateCurrentFileLabel(backend.getGcodeFile());
        jogPanel.loadSettings();
    }

    private void updateCurrentFileLabel(File lastOpenedFilename) {
        if (lastOpenedFilename != null) {
            currentFile.setText(lastOpenedFilename.getName());
            currentFile.setToolTipText(lastOpenedFilename.getAbsolutePath());
        } else {
            currentFile.setText("");
            currentFile.setToolTipText("");
        }
    }

    private void updateConnectionControlsStateOpen(boolean isOpen) {
        connection.setVisible(!isOpen);
        machineStatus.setVisible(isOpen);

        sendPauseResumeButton.setEnabled(isOpen && backend != null && backend.getGcodeFile() != null);
        browseCancelButton.setEnabled(isOpen);

        if (isOpen) {
            this.openCloseButton.setText(Localization.getString("close"));
        } else {
            this.openCloseButton.setText(Localization.getString("open"));
        }
    }

    private void sendPauseResumeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendButtonActionPerformed
        try {
            if (sendPauseResumeButton.getText().equals(Localization.getString("mainWindow.swing.sendButton"))) {
                this.backend.send();
            } else {
                this.backend.pauseResume();
            }
        } catch (Exception e) {
            displayErrorDialog(e.getMessage());
        }
    }

    private void browseCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            if (browseCancelButton.getText().equals(Localization.getString("mainWindow.swing.browseButton"))) {
                openFileChooser();
            } else {
                backend.cancel();

            }
        } catch (Exception e) {
            displayErrorDialog(e.getMessage());
        }
    }

    private void openFileChooser() {
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                File gcodeFile = fileChooser.getSelectedFile();
                backend.setGcodeFile(gcodeFile);
            } catch (Exception ex) {
                displayErrorDialog(ex.getMessage());
            }
        } else {
            // Canceled file open.
        }
    }

    public String getDuration() {
        return sendStatusPanel.getDuration();
    }


}
