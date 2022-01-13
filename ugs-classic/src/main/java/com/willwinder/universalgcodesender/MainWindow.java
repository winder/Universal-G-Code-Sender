/*
    Copyright 2012-2018 Will Winder

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

package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.actions.ConfigureFirmwareAction;
import com.willwinder.universalgcodesender.actions.OpenMacroSettingsAction;
import com.willwinder.universalgcodesender.connection.ConnectionFactory;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.model.BaudRateEnum;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.events.*;
import com.willwinder.universalgcodesender.uielements.components.GcodeFileTypeFilter;
import com.willwinder.universalgcodesender.uielements.macros.MacroActionPanel;
import com.willwinder.universalgcodesender.uielements.panels.CommandPanel;
import com.willwinder.universalgcodesender.uielements.panels.ConnectionSettingsPanel;
import com.willwinder.universalgcodesender.uielements.panels.ControllerProcessorSettingsPanel;
import com.willwinder.universalgcodesender.uielements.*;
import com.willwinder.universalgcodesender.utils.FirmwareUtils;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;
import com.willwinder.universalgcodesender.utils.KeepAwakeUtils;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import com.willwinder.universalgcodesender.utils.Version;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.pendantui.PendantUI;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.visualizer.VisualizerWindow;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.model.GUIBackend;
import static com.willwinder.universalgcodesender.utils.GUIHelpers.displayErrorDialog;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.Timer;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import static com.willwinder.universalgcodesender.model.Axis.*;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.pendantui.PendantURLBean;
import com.willwinder.universalgcodesender.services.JogService;
import com.willwinder.universalgcodesender.uielements.jog.JogPanel;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;

import javax.swing.text.DefaultEditorKit;
import org.apache.commons.lang3.SystemUtils;

/**
 * Main window for Universal Gcode Sender Classic
 *
 * @author wwinder
 */
public class MainWindow extends JFrame implements UGSEventListener {
    private static final Logger logger = Logger.getLogger(MainWindow.class.getName());

    private PendantUI pendantUI;
    private Settings settings;
    
    private BackendAPI backend;
    
    // My Variables
    private javax.swing.JFileChooser fileChooser;

    // Other windows
    private VisualizerWindow vw = null;
    private String gcodeFile = null;
    private String processedGcodeFile = null;
    
    // Duration timer
    private Timer timer;

    private JogPanel jogPanel;
    private final MacroActionPanel macroPanel;

    /** Creates new form MainWindow */
    public MainWindow(BackendAPI backend) {
        this.backend = backend;
        this.settings = SettingsFactory.loadSettings();

        boolean fullyLocalized = Localization.initialize(settings.getLanguage());
        if (!fullyLocalized) {
            GUIHelpers.displayErrorDialog(Localization.getString("incomplete.localization"));
        }
        try {
            backend.applySettings(settings);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        JogService jogService = new JogService(backend);

        this.jogPanel = new JogPanel(backend, jogService, true);

        this.macroPanel = new MacroActionPanel(backend);

        initComponents();
        this.jogPanelPanel.setLayout(new BorderLayout());
        this.jogPanelPanel.add(jogPanel, BorderLayout.CENTER);
        initProgram();
        Utils.checkNightlyBuild(settings);
        backend.addUGSEventListener(this);
        KeepAwakeUtils.start(backend);

        fileChooser = new JFileChooser(settings.getLastOpenedFilename());
        commPortComboBox.setSelectedItem(settings.getPort());
        baudrateSelectionComboBox.setSelectedItem(settings.getPortRate());
        scrollWindowCheckBox.setSelected(settings.isScrollWindowEnabled());
        showVerboseOutputCheckBox.setSelected(settings.isVerboseOutputEnabled());
        showCommandTableCheckBox.setSelected(settings.isCommandTableEnabled());
        firmwareComboBox.setSelectedItem(settings.getFirmwareVersion());

        setSize(settings.getMainWindowSettings().width, settings.getMainWindowSettings().height);
        setLocation(settings.getMainWindowSettings().xLocation, settings.getMainWindowSettings().yLocation);

        initFileChooser();
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down...");

            settings.setPort(commPortComboBox.getSelectedItem().toString());
            settings.setPortRate(baudrateSelectionComboBox.getSelectedItem().toString());
            settings.setScrollWindowEnabled(scrollWindowCheckBox.isSelected());
            settings.setVerboseOutputEnabled(showVerboseOutputCheckBox.isSelected());
            settings.setCommandTableEnabled(showCommandTableCheckBox.isSelected());
            settings.setFirmwareVersion(firmwareComboBox.getSelectedItem().toString());

            SettingsFactory.saveSettings(settings);

            if(pendantUI!=null){
                pendantUI.stop();
            }
        }));
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        // Fix look and feel to use CMD+C/X/V/A instead of CTRL
        if (SystemUtils.IS_OS_MAC)
        {
            Collection<InputMap> ims = new ArrayList<>();
            ims.add((InputMap) UIManager.get("TextField.focusInputMap"));
            ims.add((InputMap) UIManager.get("TextArea.focusInputMap"));
            ims.add((InputMap) UIManager.get("EditorPane.focusInputMap"));
            ims.add((InputMap) UIManager.get("FormattedTextField.focusInputMap"));
            ims.add((InputMap) UIManager.get("PasswordField.focusInputMap"));
            ims.add((InputMap) UIManager.get("TextPane.focusInputMap"));

            int c = KeyEvent.VK_C;
            int v = KeyEvent.VK_V;
            int x = KeyEvent.VK_X;
            int a = KeyEvent.VK_A;
            int meta = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();

            for (InputMap im : ims) {
                im.put(KeyStroke.getKeyStroke(c, meta), DefaultEditorKit.copyAction);
                im.put(KeyStroke.getKeyStroke(v, meta), DefaultEditorKit.pasteAction);
                im.put(KeyStroke.getKeyStroke(x, meta), DefaultEditorKit.cutAction);
                im.put(KeyStroke.getKeyStroke(a, meta), DefaultEditorKit.selectAllAction);
            }
        }
        
         /* Create the form */
        GUIBackend backend = new GUIBackend();
        final MainWindow mw = new MainWindow(backend);
        
        /* Apply the settings to the MainWindow bofore showing it */
        mw.fileChooser = new JFileChooser(mw.settings.getLastOpenedFilename());
        mw.commPortComboBox.setSelectedItem(mw.settings.getPort());
        mw.baudrateSelectionComboBox.setSelectedItem(mw.settings.getPortRate());
        mw.scrollWindowCheckBox.setSelected(mw.settings.isScrollWindowEnabled());
        mw.showVerboseOutputCheckBox.setSelected(mw.settings.isVerboseOutputEnabled());
        mw.showCommandTableCheckBox.setSelected(mw.settings.isCommandTableEnabled());
        mw.showCommandTableCheckBoxActionPerformed(null);
        mw.firmwareComboBox.setSelectedItem(mw.settings.getFirmwareVersion());

        if(mw.settings.isAutoStartPendant()) {
            mw.pendantUI = new PendantUI(backend);
            mw.pendantUI.start();
            mw.startPendantServerButton.setEnabled(false);
            mw.stopPendantServerButton.setEnabled(true);
        }

        mw.setSize(mw.settings.getMainWindowSettings().width, mw.settings.getMainWindowSettings().height);
        mw.setLocation(mw.settings.getMainWindowSettings().xLocation, mw.settings.getMainWindowSettings().yLocation);

        mw.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent ce) {
                mw.settings.getMainWindowSettings().height = ce.getComponent().getSize().height;
                mw.settings.getMainWindowSettings().width = ce.getComponent().getSize().width;
            }

            @Override
            public void componentMoved(ComponentEvent ce) {
                mw.settings.getMainWindowSettings().xLocation = ce.getComponent().getLocation().x;
                mw.settings.getMainWindowSettings().yLocation = ce.getComponent().getLocation().y;
            }

            @Override
            public void componentShown(ComponentEvent ce) {}
            @Override
            public void componentHidden(ComponentEvent ce) {}
        });

        /* Display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                mw.setVisible(true);
            }
        });
        
        mw.initFileChooser();

        // Check command line for a file to open.
        boolean open = false;
        for (String arg : args) {
            if (open) {
                try {
                    GUIHelpers.openGcodeFile(new File(arg), backend);
                    open = false;
                } catch (Exception ex) {
                    Logger.getLogger(MainWindow.class.getName()).log(Level.SEVERE, null, ex);
                    System.exit(1);
                }
            }
            if (arg.equals("--open") || arg.equals("-o")) {
                open = true;
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        scrollWindowCheckBox = new javax.swing.JCheckBox();
        bottomTabbedPane = new javax.swing.JTabbedPane();
        commandPanel = new CommandPanel(backend);
        commandTableScrollPane = new javax.swing.JScrollPane();
        commandTable = new com.willwinder.universalgcodesender.uielements.components.GcodeTable();
        controlContextTabbedPane = new javax.swing.JTabbedPane();
        machineControlPanel = new javax.swing.JPanel();
        actionPanel = new javax.swing.JPanel();
        resetCoordinatesButton = new javax.swing.JButton();
        returnToZeroButton = new javax.swing.JButton();
        softResetMachineControl = new javax.swing.JButton();
        killAlarmLock = new javax.swing.JButton();
        performHomingCycleButton = new javax.swing.JButton();
        requestStateInformation = new javax.swing.JButton();
        helpButtonMachineControl = new javax.swing.JButton();
        toggleCheckMode = new javax.swing.JButton();
        resetZButton = new javax.swing.JButton();
        resetYButton = new javax.swing.JButton();
        resetXButton = new javax.swing.JButton();
        jogPanelPanel = new javax.swing.JPanel();
        macroEditPanel = new javax.swing.JScrollPane(macroPanel);
        connectionPanel = new javax.swing.JPanel();
        commPortComboBox = new javax.swing.JComboBox();
        baudrateSelectionComboBox = new javax.swing.JComboBox();
        opencloseButton = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();
        baudLabel = new javax.swing.JLabel();
        portLabel = new javax.swing.JLabel();
        firmwareLabel = new javax.swing.JLabel();
        firmwareComboBox = new javax.swing.JComboBox();
        showVerboseOutputCheckBox = new javax.swing.JCheckBox();
        statusPanel = new javax.swing.JPanel();
        activeStateLabel = new javax.swing.JLabel();
        activeStateValueLabel = new javax.swing.JLabel();
        machinePosition = new javax.swing.JLabel();
        machinePositionXLabel = new javax.swing.JLabel();
        machinePositionYLabel = new javax.swing.JLabel();
        machinePositionZLabel = new javax.swing.JLabel();
        workPositionLabel = new javax.swing.JLabel();
        workPositionXLabel = new javax.swing.JLabel();
        workPositionYLabel = new javax.swing.JLabel();
        workPositionZLabel = new javax.swing.JLabel();
        machinePositionXValueLabel = new javax.swing.JLabel();
        machinePositionYValueLabel = new javax.swing.JLabel();
        machinePositionZValueLabel = new javax.swing.JLabel();
        workPositionXValueLabel = new javax.swing.JLabel();
        workPositionYValueLabel = new javax.swing.JLabel();
        workPositionZValueLabel = new javax.swing.JLabel();
        latestCommentValueLabel = new javax.swing.JLabel();
        latestCommentLabel = new javax.swing.JLabel();
        showCommandTableCheckBox = new javax.swing.JCheckBox();
        fileModePanel = new javax.swing.JPanel();
        sendButton = new javax.swing.JButton();
        pauseButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        visualizeButton = new javax.swing.JButton();
        browseButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        fileRunPanel = new javax.swing.JPanel();
        remainingTimeValueLabel = new javax.swing.JLabel();
        sentRowsValueLabel = new javax.swing.JLabel();
        remainingRowsLabel = new javax.swing.JLabel();
        rowsValueLabel = new javax.swing.JLabel();
        remainingTimeLabel = new javax.swing.JLabel();
        durationValueLabel = new javax.swing.JLabel();
        durationLabel = new javax.swing.JLabel();
        remainingRowsValueLabel = new javax.swing.JLabel();
        sentRowsLabel = new javax.swing.JLabel();
        rowsLabel = new javax.swing.JLabel();
        mainMenuBar = new javax.swing.JMenuBar();
        settingsMenu = new javax.swing.JMenu();
        grblConnectionSettingsMenuItem = new javax.swing.JMenuItem();
        firmwareSettingsMenuItem = new javax.swing.JMenuItem(new ConfigureFirmwareAction(backend));
        macroSettingsMenuItem = new JMenuItem(new OpenMacroSettingsAction(backend));
        gcodeProcessorSettings = new javax.swing.JMenuItem();
        PendantMenu = new javax.swing.JMenu();
        startPendantServerButton = new javax.swing.JMenuItem();
        stopPendantServerButton = new javax.swing.JMenuItem();

        jMenuItem1.setText("jMenuItem1");

        jMenuItem3.setText("jMenuItem3");

        jMenuItem4.setText("jMenuItem4");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(640, 520));

        scrollWindowCheckBox.setSelected(true);
        scrollWindowCheckBox.setText("Scroll output window");
        scrollWindowCheckBox.addActionListener(this::scrollWindowCheckBoxActionPerformed);

        showVerboseOutputCheckBox.setText("Show verbose output");
        showVerboseOutputCheckBox.addActionListener(this::showVerboseCheckBoxActionPerformed);

        bottomTabbedPane.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        bottomTabbedPane.setMinimumSize(new java.awt.Dimension(0, 0));
        bottomTabbedPane.setPreferredSize(new java.awt.Dimension(468, 100));


        bottomTabbedPane.addTab("Commands", commandPanel);

        commandTable.setMaximumSize(new java.awt.Dimension(32767, 32767));
        commandTable.getTableHeader().setReorderingAllowed(false);
        commandTableScrollPane.setViewportView(commandTable);

        bottomTabbedPane.addTab("Command Table", commandTableScrollPane);

        controlContextTabbedPane.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        controlContextTabbedPane.setMinimumSize(new java.awt.Dimension(395, 175));
        controlContextTabbedPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                controlContextTabbedPaneComponentShown(evt);
            }
        });

        resetCoordinatesButton.setText("Reset Zero");
        resetCoordinatesButton.setEnabled(false);
        resetCoordinatesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetCoordinatesButtonActionPerformed(evt);
            }
        });

        returnToZeroButton.setText("Return to Zero");
        returnToZeroButton.setEnabled(false);
        returnToZeroButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                returnToZeroButtonActionPerformed(evt);
            }
        });

        softResetMachineControl.setText("Soft Reset");
        softResetMachineControl.setEnabled(false);
        softResetMachineControl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                softResetMachineControlActionPerformed(evt);
            }
        });

        killAlarmLock.setText("$X");
        killAlarmLock.setEnabled(false);
        killAlarmLock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                killAlarmLockActionPerformed(evt);
            }
        });

        performHomingCycleButton.setText("$H");
        performHomingCycleButton.setEnabled(false);
        performHomingCycleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                performHomingCycleButtonActionPerformed(evt);
            }
        });

        requestStateInformation.setText("$G");
        requestStateInformation.setEnabled(false);
        requestStateInformation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                requestStateInformationActionPerformed(evt);
            }
        });

        helpButtonMachineControl.setText("Help");
        helpButtonMachineControl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpButtonMachineControlActionPerformed(evt);
            }
        });

        toggleCheckMode.setText("$C");
        toggleCheckMode.setEnabled(false);
        toggleCheckMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleCheckModeActionPerformed(evt);
            }
        });

        resetZButton.setText("Reset Z Axis");
        resetZButton.setEnabled(false);
        resetZButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetZCoordinateButtonActionPerformed(evt);
            }
        });

        resetYButton.setText("Reset Y Axis");
        resetYButton.setEnabled(false);
        resetYButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetYCoordinateButtonActionPerformed(evt);
            }
        });

        resetXButton.setText("Reset X Axis");
        resetXButton.setEnabled(false);
        resetXButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetXCoordinateButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout actionPanelLayout = new org.jdesktop.layout.GroupLayout(actionPanel);
        actionPanel.setLayout(actionPanelLayout);
        actionPanelLayout.setHorizontalGroup(
            actionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(actionPanelLayout.createSequentialGroup()
                .add(actionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(actionPanelLayout.createSequentialGroup()
                        .add(requestStateInformation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 49, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(helpButtonMachineControl))
                    .add(resetCoordinatesButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 159, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(returnToZeroButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 159, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(softResetMachineControl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 159, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(actionPanelLayout.createSequentialGroup()
                        .add(performHomingCycleButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 49, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(6, 6, 6)
                        .add(killAlarmLock, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 49, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(6, 6, 6)
                        .add(toggleCheckMode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 49, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(6, 6, 6)
                .add(actionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(resetXButton)
                    .add(resetYButton)
                    .add(resetZButton))
                .add(0, 0, Short.MAX_VALUE))
        );
        actionPanelLayout.setVerticalGroup(
            actionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(actionPanelLayout.createSequentialGroup()
                .add(actionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(actionPanelLayout.createSequentialGroup()
                        .add(resetCoordinatesButton)
                        .add(6, 6, 6)
                        .add(returnToZeroButton)
                        .add(6, 6, 6)
                        .add(softResetMachineControl)
                        .add(6, 6, 6)
                        .add(actionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(performHomingCycleButton)
                            .add(killAlarmLock)
                            .add(toggleCheckMode))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(actionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(requestStateInformation)
                            .add(helpButtonMachineControl)))
                    .add(actionPanelLayout.createSequentialGroup()
                        .add(resetXButton)
                        .add(6, 6, 6)
                        .add(resetYButton)
                        .add(6, 6, 6)
                        .add(resetZButton)))
                .add(0, 58, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jogPanelPanelLayout = new org.jdesktop.layout.GroupLayout(jogPanelPanel);
        jogPanelPanel.setLayout(jogPanelPanelLayout);
        jogPanelPanelLayout.setHorizontalGroup(
            jogPanelPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 288, Short.MAX_VALUE)
        );
        jogPanelPanelLayout.setVerticalGroup(
            jogPanelPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout machineControlPanelLayout = new org.jdesktop.layout.GroupLayout(machineControlPanel);
        machineControlPanel.setLayout(machineControlPanelLayout);
        machineControlPanelLayout.setHorizontalGroup(
            machineControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(machineControlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(actionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(18, 18, Short.MAX_VALUE)
                .add(jogPanelPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        machineControlPanelLayout.setVerticalGroup(
            machineControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(machineControlPanelLayout.createSequentialGroup()
                .add(actionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .add(jogPanelPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        controlContextTabbedPane.addTab("Machine Control", machineControlPanel);
        controlContextTabbedPane.addTab("Macros", macroEditPanel);

        connectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Connection"));
        connectionPanel.setMaximumSize(new java.awt.Dimension(247, 100));
        connectionPanel.setMinimumSize(new java.awt.Dimension(247, 100));
        connectionPanel.setName("Connection"); // NOI18N
        connectionPanel.setPreferredSize(new java.awt.Dimension(247, 100));

        commPortComboBox.setEditable(true);

        baudrateSelectionComboBox.setModel(new javax.swing.DefaultComboBoxModel(BaudRateEnum.getAllBaudRates()));
        baudrateSelectionComboBox.setSelectedIndex(2);
        baudrateSelectionComboBox.setToolTipText("Select baudrate to use for the serial port.");
        baudrateSelectionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                baudrateSelectionComboBoxActionPerformed(evt);
            }
        });

        opencloseButton.setText("Open");
        opencloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                opencloseButtonActionPerformed(evt);
            }
        });

        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/icons/refresh.gif"))); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        baudLabel.setText("Baud:");

        portLabel.setText("Port:");

        firmwareLabel.setText("Firmware:");

        org.jdesktop.layout.GroupLayout connectionPanelLayout = new org.jdesktop.layout.GroupLayout(connectionPanel);
        connectionPanel.setLayout(connectionPanelLayout);
        connectionPanelLayout.setHorizontalGroup(
            connectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(connectionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(connectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, connectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(connectionPanelLayout.createSequentialGroup()
                            .add(portLabel)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(commPortComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 183, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(connectionPanelLayout.createSequentialGroup()
                            .add(baudLabel)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(baudrateSelectionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(refreshButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(opencloseButton)))
                    .add(connectionPanelLayout.createSequentialGroup()
                        .add(firmwareLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(firmwareComboBox, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        connectionPanelLayout.setVerticalGroup(
            connectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(connectionPanelLayout.createSequentialGroup()
                .add(connectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(commPortComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(portLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(connectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(connectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(baudLabel)
                        .add(baudrateSelectionComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(refreshButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(opencloseButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(connectionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(firmwareLabel)
                    .add(firmwareComboBox, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        statusPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Machine status"));
        statusPanel.setMinimumSize(new java.awt.Dimension(247, 160));
        statusPanel.setPreferredSize(new java.awt.Dimension(247, 160));

        activeStateLabel.setText("Active State:");
        activeStateLabel.setOpaque(true);

        activeStateValueLabel.setText(" ");
        activeStateValueLabel.setOpaque(true);

        machinePosition.setText("Machine Position:");

        machinePositionXLabel.setText("X:");

        machinePositionYLabel.setText("Y:");

        machinePositionZLabel.setText("Z:");

        workPositionLabel.setText("Work Position:");

        workPositionXLabel.setText("X:");

        workPositionYLabel.setText("Y:");

        workPositionZLabel.setText("Z:");

        machinePositionXValueLabel.setText("0");

        machinePositionYValueLabel.setText("0");

        machinePositionZValueLabel.setText("0");

        workPositionXValueLabel.setText("0");

        workPositionYValueLabel.setText("0");

        workPositionZValueLabel.setText("0");

        latestCommentValueLabel.setText(" ");

        latestCommentLabel.setText("Latest Comment:");

        org.jdesktop.layout.GroupLayout statusPanelLayout = new org.jdesktop.layout.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(statusPanelLayout.createSequentialGroup()
                        .add(latestCommentLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(latestCommentValueLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(statusPanelLayout.createSequentialGroup()
                        .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(statusPanelLayout.createSequentialGroup()
                                .add(activeStateLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(activeStateValueLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 120, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(statusPanelLayout.createSequentialGroup()
                                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(workPositionLabel)
                                    .add(statusPanelLayout.createSequentialGroup()
                                        .add(17, 17, 17)
                                        .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                            .add(statusPanelLayout.createSequentialGroup()
                                                .add(workPositionZLabel)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(workPositionZValueLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                            .add(statusPanelLayout.createSequentialGroup()
                                                .add(workPositionYLabel)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(workPositionYValueLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                            .add(statusPanelLayout.createSequentialGroup()
                                                .add(workPositionXLabel)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(workPositionXValueLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 65, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(machinePosition)
                                    .add(statusPanelLayout.createSequentialGroup()
                                        .add(17, 17, 17)
                                        .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                                            .add(statusPanelLayout.createSequentialGroup()
                                                .add(machinePositionZLabel)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(machinePositionZValueLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                            .add(statusPanelLayout.createSequentialGroup()
                                                .add(machinePositionYLabel)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(machinePositionYValueLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                            .add(statusPanelLayout.createSequentialGroup()
                                                .add(machinePositionXLabel)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(machinePositionXValueLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 65, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))))
                        .add(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelLayout.createSequentialGroup()
                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(activeStateLabel)
                    .add(activeStateValueLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(latestCommentLabel)
                    .add(latestCommentValueLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(statusPanelLayout.createSequentialGroup()
                        .add(workPositionLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(workPositionXLabel)
                            .add(workPositionXValueLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(workPositionYLabel)
                            .add(workPositionYValueLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(workPositionZLabel)
                            .add(workPositionZValueLabel)))
                    .add(statusPanelLayout.createSequentialGroup()
                        .add(machinePosition)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(machinePositionXLabel)
                            .add(machinePositionXValueLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(machinePositionYLabel)
                            .add(machinePositionYValueLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(machinePositionZLabel)
                            .add(machinePositionZValueLabel))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        showCommandTableCheckBox.setSelected(true);
        showCommandTableCheckBox.setText("Enable command table");
        showCommandTableCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showCommandTableCheckBoxActionPerformed(evt);
            }
        });

        fileModePanel.setMinimumSize(new java.awt.Dimension(389, 150));
        fileModePanel.setPreferredSize(new java.awt.Dimension(247, 258));
        fileModePanel.setLayout(new java.awt.GridBagLayout());

        sendButton.setText("Send");
        sendButton.setEnabled(false);
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        fileModePanel.add(sendButton, gridBagConstraints);

        pauseButton.setText("Pause");
        pauseButton.setEnabled(false);
        pauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.ipadx = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        fileModePanel.add(pauseButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        fileModePanel.add(cancelButton, gridBagConstraints);

        visualizeButton.setText("Visualize");
        visualizeButton.setEnabled(false);
        visualizeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                visualizeButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        fileModePanel.add(visualizeButton, gridBagConstraints);

        browseButton.setText("Browse");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        fileModePanel.add(browseButton, gridBagConstraints);

        saveButton.setText("Save");
        saveButton.setEnabled(false);
        saveButton.setMaximumSize(new java.awt.Dimension(88, 29));
        saveButton.setMinimumSize(new java.awt.Dimension(88, 29));
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        fileModePanel.add(saveButton, gridBagConstraints);

        remainingTimeValueLabel.setText("--:--:--");

        sentRowsValueLabel.setText("0");

        remainingRowsLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        remainingRowsLabel.setText("Remaining Rows:");
        remainingRowsLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        remainingRowsLabel.setMaximumSize(null);
        remainingRowsLabel.setMinimumSize(new java.awt.Dimension(106, 14));
        remainingRowsLabel.setPreferredSize(new java.awt.Dimension(106, 14));

        rowsValueLabel.setText("0");

        remainingTimeLabel.setText("Estimated Time Remaining:");

        durationValueLabel.setText("00:00:00");

        durationLabel.setText("Duration:");

        remainingRowsValueLabel.setText("0");

        sentRowsLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        sentRowsLabel.setText("Sent Rows:");
        sentRowsLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        sentRowsLabel.setMaximumSize(null);
        sentRowsLabel.setMinimumSize(new java.awt.Dimension(106, 14));
        sentRowsLabel.setPreferredSize(new java.awt.Dimension(106, 14));

        rowsLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        rowsLabel.setText("Rows In File:");
        rowsLabel.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        rowsLabel.setMinimumSize(new java.awt.Dimension(106, 14));
        rowsLabel.setPreferredSize(new java.awt.Dimension(106, 14));

        org.jdesktop.layout.GroupLayout fileRunPanelLayout = new org.jdesktop.layout.GroupLayout(fileRunPanel);
        fileRunPanel.setLayout(fileRunPanelLayout);
        fileRunPanelLayout.setHorizontalGroup(
            fileRunPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(fileRunPanelLayout.createSequentialGroup()
                .add(0, 0, 0)
                .add(fileRunPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, remainingRowsLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, remainingTimeLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, sentRowsLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, durationLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, rowsLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fileRunPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(durationValueLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(remainingRowsValueLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(sentRowsValueLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(remainingTimeValueLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(rowsValueLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 56, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        fileRunPanelLayout.setVerticalGroup(
            fileRunPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(fileRunPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(fileRunPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(rowsValueLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(rowsLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fileRunPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(sentRowsValueLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(sentRowsLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fileRunPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(remainingRowsValueLabel)
                    .add(remainingRowsLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fileRunPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(remainingTimeLabel)
                    .add(remainingTimeValueLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fileRunPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(durationLabel)
                    .add(durationValueLabel))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        fileModePanel.add(fileRunPanel, gridBagConstraints);

        settingsMenu.setText("Settings");

        grblConnectionSettingsMenuItem.setText("Sender Settings");
        grblConnectionSettingsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                grblConnectionSettingsMenuItemActionPerformed(evt);
            }
        });
        settingsMenu.add(grblConnectionSettingsMenuItem);

        firmwareSettingsMenuItem.setText("Firmware Settings");
        settingsMenu.add(firmwareSettingsMenuItem);

        gcodeProcessorSettings.setText("Gcode Processor Settings");
        gcodeProcessorSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gcodeProcessorSettingsActionPerformed(evt);
            }
        });
        settingsMenu.add(gcodeProcessorSettings);

        macroSettingsMenuItem.setText("Macro Settings");
        settingsMenu.add(macroSettingsMenuItem);

        mainMenuBar.add(settingsMenu);

        PendantMenu.setText("Pendant");

        startPendantServerButton.setText("Start...");
        startPendantServerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startPendantServerButtonActionPerformed(evt);
            }
        });
        PendantMenu.add(startPendantServerButton);

        stopPendantServerButton.setText("Stop...");
        stopPendantServerButton.setEnabled(false);
        stopPendantServerButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopPendantServerButtonActionPerformed(evt);
            }
        });
        PendantMenu.add(stopPendantServerButton);

        mainMenuBar.add(PendantMenu);

        setJMenuBar(mainMenuBar);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                        .add(connectionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(statusPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .add(fileModePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, bottomTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(layout.createSequentialGroup()
                        .add(scrollWindowCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(showVerboseOutputCheckBox)
                        .add(18, 18, 18)
                        .add(showCommandTableCheckBox)
                        .addContainerGap())
                    .add(controlContextTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(connectionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(statusPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE))
                    .add(layout.createSequentialGroup()
                        .add(controlContextTabbedPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 283, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(scrollWindowCheckBox)
                            .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                .add(showVerboseOutputCheckBox)
                                .add(showCommandTableCheckBox)))))
                .add(4, 4, 4)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(fileModePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 203, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 0, Short.MAX_VALUE))
                    .add(bottomTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 407, Short.MAX_VALUE))
                .add(4, 4, 4))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    /** End of generated code.
     */
    
    /** Generated callback functions, hand coded.
     */
    private void scrollWindowCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scrollWindowCheckBoxActionPerformed
        backend.getSettings().setScrollWindowEnabled(scrollWindowCheckBox.isSelected());
    }//GEN-LAST:event_scrollWindowCheckBoxActionPerformed

    private void showVerboseCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scrollWindowCheckBoxActionPerformed
        backend.getSettings().setVerboseOutputEnabled(showVerboseOutputCheckBox.isSelected());
    }

    private void opencloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_opencloseButtonActionPerformed
        if( this.opencloseButton.getText().equalsIgnoreCase(Localization.getString("open")) ) {
            this.commandTable.clear();
            this.sentRowsValueLabel.setText("0");

            String firmware = this.firmwareComboBox.getSelectedItem().toString();
            String port = commPortComboBox.getSelectedItem().toString();
            int baudRate = Integer.parseInt(baudrateSelectionComboBox.getSelectedItem().toString());
            
            try {
                this.backend.connect(firmware, port, baudRate);
            } catch (Exception e) {
                e.printStackTrace();
                displayErrorDialog(e.getMessage());
            }
        } else {
            try {
                this.backend.disconnect();
            } catch (Exception e) {
                displayErrorDialog(e.getMessage());
            }
        }
    }//GEN-LAST:event_opencloseButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        loadPortSelector();
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void baudrateSelectionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_baudrateSelectionComboBoxActionPerformed
    }//GEN-LAST:event_baudrateSelectionComboBoxActionPerformed

    // TODO: It would be nice to streamline this somehow...
    private void grblConnectionSettingsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_grblConnectionSettingsMenuItemActionPerformed
        UGSSettingsDialog gcsd = new UGSSettingsDialog(
                Localization.getString("sender.header"),
                new ConnectionSettingsPanel(settings),
                this, true);
        
        gcsd.setVisible(true);
        
        if (gcsd.saveChanges()) {
            try {
                backend.applySettings(settings);
            } catch (Exception e) {
                displayErrorDialog(e.getMessage());
            }
        }
    }//GEN-LAST:event_grblConnectionSettingsMenuItemActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                File gcodeFile = fileChooser.getSelectedFile();
                GUIHelpers.openGcodeFile(gcodeFile, backend);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Problem while browsing.", ex);
                displayErrorDialog(ex.getMessage());
            }
        } else {
            // Canceled file open.
        }
    }//GEN-LAST:event_browseButtonActionPerformed

    private void visualizeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_visualizeButtonActionPerformed
        // Create new object if it is null.
        if (this.vw == null) {
            this.vw = new VisualizerWindow(settings.getVisualizerWindowSettings());
            
            final MainWindow mw = this;
            vw.addComponentListener(new ComponentListener() {
                @Override
                public void componentResized(ComponentEvent ce) {
                    mw.settings.getVisualizerWindowSettings().height = ce.getComponent().getSize().height;
                    mw.settings.getVisualizerWindowSettings().width = ce.getComponent().getSize().width;
                }

                @Override
                public void componentMoved(ComponentEvent ce) {
                    mw.settings.getVisualizerWindowSettings().xLocation = ce.getComponent().getLocation().x;
                    mw.settings.getVisualizerWindowSettings().yLocation = ce.getComponent().getLocation().y;
                }

                @Override
                public void componentShown(ComponentEvent ce) {}
                @Override
                public void componentHidden(ComponentEvent ce) {}
            });

            setVisualizerFile();

            // Add listener
            this.backend.addUGSEventListener(vw);
        }

        // Display the form
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                vw.setVisible(true);
            }
        });
    }//GEN-LAST:event_visualizeButtonActionPerformed

    public void cancelButtonActionPerformed() {
        try {
            backend.cancel();
        } catch (Exception e) {
            displayErrorDialog(e.getMessage());
        }
    }
    
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        cancelButtonActionPerformed();
    }//GEN-LAST:event_cancelButtonActionPerformed

    public void pauseButtonActionPerformed() {
        try {
            this.backend.pauseResume();
        } catch (Exception e) {
            displayErrorDialog(e.getMessage());
        }
    }
    
    private void pauseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pauseButtonActionPerformed
        pauseButtonActionPerformed();
    }//GEN-LAST:event_pauseButtonActionPerformed
    
    private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendButtonActionPerformed
        // Timer for updating duration labels.
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                java.awt.EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            durationValueLabel.setText(Utils.formattedMillis(backend.getSendDuration()));
                            remainingTimeValueLabel.setText(Utils.formattedMillis(backend.getSendRemainingDuration()));

                            //sentRowsValueLabel.setText(""+sentRows);
                            sentRowsValueLabel.setText(""+backend.getNumCompletedRows());
                            remainingRowsValueLabel.setText("" + backend.getNumRemainingRows());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        };

        this.resetTimerLabels();

        if (timer != null){ timer.stop(); }
        timer = new Timer(1000, actionListener);

        // Note: there is a divide by zero error in the timer because it uses
        //       the rowsValueLabel that was just reset.

        try {
            if (commandTableScrollPane.isEnabled()) {
                commandTable.clear();
            }
            this.backend.send();
            this.resetSentRowLabels(backend.getNumRows());
            timer.start();
        } catch (Exception e) {
            timer.stop();
            logger.log(Level.INFO, "Exception in sendButtonActionPerformed.", e);
            displayErrorDialog(e.getMessage());
        }
        
    }//GEN-LAST:event_sendButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        //displayErrorDialog("Disabled for refactoring.");
        
        int returnVal = fileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                File newFile = fileChooser.getSelectedFile();
                IController controller = FirmwareUtils.getControllerFor("GRBL").get();
                backend.applySettingsToController(settings, controller);
                
                backend.preprocessAndExportToFile(newFile);
            } catch (FileNotFoundException ex) {
                displayErrorDialog(Localization.getString("mainWindow.error.openingFile")
                        + ": " + ex.getMessage());
            } catch (IOException e) {
                displayErrorDialog(Localization.getString("mainWindow.error.processingFile")
                        + ": "+e.getMessage());
            } catch (Exception e) {
                logger.log(Level.INFO, "Exception in saveButtonActionPerformed.", e);
                displayErrorDialog(Localization.getString("mainWindow.error.duringSave") +
                        ": " + e.getMessage());
            }
        }
        
    }//GEN-LAST:event_saveButtonActionPerformed

        private void startPendantServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startPendantServerButtonActionPerformed
            this.pendantUI = new PendantUI(backend);
            Collection<PendantURLBean> results = this.pendantUI.start();
            for (PendantURLBean result : results) {
                this.backend.dispatchMessage(MessageType.INFO, "Pendant URL: " + result.getUrlString());
            }
            this.startPendantServerButton.setEnabled(false);
            this.stopPendantServerButton.setEnabled(true);
        }//GEN-LAST:event_startPendantServerButtonActionPerformed

        private void stopPendantServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopPendantServerButtonActionPerformed
            this.pendantUI.stop();
            this.startPendantServerButton.setEnabled(true);
            this.stopPendantServerButton.setEnabled(false);
        }//GEN-LAST:event_stopPendantServerButtonActionPerformed

    private void showCommandTableCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showCommandTableCheckBoxActionPerformed
        showCommandTable(showCommandTableCheckBox.isSelected());
    }//GEN-LAST:event_showCommandTableCheckBoxActionPerformed

    private void controlContextTabbedPaneComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_controlContextTabbedPaneComponentShown
        // TODO add your handling code here:
    }//GEN-LAST:event_controlContextTabbedPaneComponentShown

    private void resetZCoordinateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetZCoordinateButtonActionPerformed
        try {
            this.backend.resetCoordinateToZero(Z);
        } catch (Exception ex) {
            displayErrorDialog(ex.getMessage());
        }
    }//GEN-LAST:event_resetZCoordinateButtonActionPerformed

    private void resetXCoordinateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetXCoordinateButtonActionPerformed
        try {
            this.backend.resetCoordinateToZero(X);
        } catch (Exception ex) {
            displayErrorDialog(ex.getMessage());
        }
    }//GEN-LAST:event_resetXCoordinateButtonActionPerformed

    private void killAlarmLockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_killAlarmLockActionPerformed
        try {
            this.backend.killAlarmLock();
        } catch (Exception ex) {
            displayErrorDialog(ex.getMessage());
        }
    }//GEN-LAST:event_killAlarmLockActionPerformed

    private void performHomingCycleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_performHomingCycleButtonActionPerformed
        try {
            this.backend.performHomingCycle();
        } catch (Exception ex) {
            displayErrorDialog(ex.getMessage());
        }
    }//GEN-LAST:event_performHomingCycleButtonActionPerformed

    private void resetCoordinatesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetCoordinatesButtonActionPerformed
        try {
            this.backend.resetCoordinatesToZero();
        } catch (Exception ex) {
            displayErrorDialog(ex.getMessage());
        }
    }//GEN-LAST:event_resetCoordinatesButtonActionPerformed

    private void toggleCheckModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleCheckModeActionPerformed
        try {
            this.backend.toggleCheckMode();
        } catch (Exception ex) {
            displayErrorDialog(ex.getMessage());
        }
    }//GEN-LAST:event_toggleCheckModeActionPerformed

    private void returnToZeroButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_returnToZeroButtonActionPerformed
        try {
            backend.returnToZero();
        } catch (Exception ex) {
            displayErrorDialog(ex.getMessage());
        }
    }//GEN-LAST:event_returnToZeroButtonActionPerformed

    private void requestStateInformationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_requestStateInformationActionPerformed
        try {
            this.backend.requestParserState();
        } catch (Exception ex) {
            displayErrorDialog(ex.getMessage());
        }
    }//GEN-LAST:event_requestStateInformationActionPerformed

    private void softResetMachineControlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_softResetMachineControlActionPerformed
        try {
            this.backend.issueSoftReset();
        } catch (Exception ex) {
            displayErrorDialog(ex.getMessage());
        }
    }//GEN-LAST:event_softResetMachineControlActionPerformed

    private void resetYCoordinateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetYCoordinateButtonActionPerformed
        try {
            this.backend.resetCoordinateToZero(Y);
        } catch (Exception ex) {
            displayErrorDialog(ex.getMessage());
        }
    }//GEN-LAST:event_resetYCoordinateButtonActionPerformed

    private void helpButtonMachineControlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpButtonMachineControlActionPerformed
        StringBuilder message = new StringBuilder()
        .append(Localization.getString("mainWindow.resetZero")).append("\n")
        .append(Localization.getString("mainWindow.returnToZero")).append("\n")
        .append(Localization.getString("mainWindow.softReset")).append("\n")
        .append(Localization.getString("mainWindow.homing")).append("\n")
        .append(Localization.getString("mainWindow.alarmLock")).append("\n")
        .append(Localization.getString("mainWindow.checkMode")).append("\n")
        .append(Localization.getString("mainWindow.getState")).append("\n")
        .append(Localization.getString("mainWindow.helpKeyboard")).append("\n")
        .append(Localization.getString("mainWindow.helpKeyX")).append("\n")
        .append(Localization.getString("mainWindow.helpKeyY")).append("\n")
        .append(Localization.getString("mainWindow.helpKeyZ")).append("\n")
        .append(Localization.getString("mainWindow.helpKeyPlusMinus")).append("\n")
        .append(Localization.getString("mainWindow.helpKeyDivMul")).append("\n")
        .append(Localization.getString("mainWindow.helpKeyZero")).append("\n")
        ;

        JOptionPane.showMessageDialog(new JFrame(),
            message,
            Localization.getString("mainWindow.helpDialog"),
            JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_helpButtonMachineControlActionPerformed

    private void gcodeProcessorSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gcodeProcessorSettingsActionPerformed
        UGSSettingsDialog gcsd = new UGSSettingsDialog(
                Localization.getString("settings.processors.header"),
                new ControllerProcessorSettingsPanel(settings, FirmwareUtils.getConfigFiles()),
                this, true);
        
        gcsd.setVisible(true);

        if (gcsd.saveChanges()) {
            // TODO: Reprocess gcode file?
            /*
            try {
                backend.applySettings(settings);
            } catch (Exception e) {
                displayErrorDialog(e.getMessage());
            }

            if (this.vw != null) {
                vw.setMinArcLength(backend.getSettings().getSmallArcThreshold());
                vw.setArcLength(backend.getSettings().getSmallArcSegmentLength());
            }
            */
        }
    }//GEN-LAST:event_gcodeProcessorSettingsActionPerformed

    private void showCommandTable(Boolean enabled) {
        if (enabled && (backend.isConnected() && !backend.isIdle())) {
            displayErrorDialog(Localization.getString("mainWindow.error.showTableActive"));
            showCommandTableCheckBox.setSelected(false);
            return;
        }

        this.commandTable.clear();
        this.bottomTabbedPane.setEnabledAt(1, enabled);
        commandTableScrollPane.setEnabled(enabled);
        if (!enabled) {
            this.bottomTabbedPane.setSelectedIndex(0);
        } else {
            this.bottomTabbedPane.setSelectedIndex(1);
        }
    }

    /**
     * FileChooser has to be initialized after JFrame is opened, otherwise the settings will not be applied.
     */
    private void initFileChooser() {
        this.fileChooser = GcodeFileTypeFilter.getGcodeFileChooser(settings.getLastOpenedFilename()); 
    }
        
    private void initProgram() {
        Localization.initialize(this.settings.getLanguage());
        try {
            backend.applySettings(settings);
        } catch (Exception e) {
            displayErrorDialog(e.getMessage());
        }
        
        this.setLocalLabels();
        this.loadPortSelector();
        this.loadFirmwareSelector();
        this.setTitle(Localization.getString("title") + " (" 
                + Localization.getString("version") + " " + Version.getVersionString() + ")");

        // Add keyboard listener for manual controls.
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
        .addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                // Check context.
                if (((jogPanel.isKeyboardMovementEnabled()) &&
                        e.getID() == KeyEvent.KEY_PRESSED)) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_RIGHT:
                        case KeyEvent.VK_KP_RIGHT:
                        case KeyEvent.VK_NUMPAD6:
                            jogPanel.xPlusButtonActionPerformed();
                            e.consume();
                            return true;
                        case KeyEvent.VK_LEFT:
                        case KeyEvent.VK_KP_LEFT:
                        case KeyEvent.VK_NUMPAD4:
                            jogPanel.xMinusButtonActionPerformed();
                            e.consume();
                            return true;
                        case KeyEvent.VK_UP:
                        case KeyEvent.VK_KP_UP:
                        case KeyEvent.VK_NUMPAD8:
                            jogPanel.yPlusButtonActionPerformed();
                            e.consume();
                            return true;
                        case KeyEvent.VK_DOWN:
                        case KeyEvent.VK_KP_DOWN:
                        case KeyEvent.VK_NUMPAD2:
                            jogPanel.yMinusButtonActionPerformed();
                            e.consume();
                            return true;
                        case KeyEvent.VK_PAGE_UP:
                        case KeyEvent.VK_NUMPAD9:
                            jogPanel.zPlusButtonActionPerformed();
                            e.consume();
                            return true;
                        case KeyEvent.VK_PAGE_DOWN:
                        case KeyEvent.VK_NUMPAD3:
                            jogPanel.zMinusButtonActionPerformed();
                            e.consume();
                            return true;
                        case KeyEvent.VK_ADD:
                            jogPanel.increaseStepActionPerformed();
                            e.consume();
                            return true;
                        case KeyEvent.VK_SUBTRACT:
                            jogPanel.decreaseStepActionPerformed();
                            e.consume();
                            return true;
                        case KeyEvent.VK_DIVIDE:
                            jogPanel.divideStepActionPerformed();
                            e.consume();
                            return true;
                        case KeyEvent.VK_MULTIPLY:
                            jogPanel.multiplyStepActionPerformed();
                            e.consume();
                            return true;
                        case KeyEvent.VK_INSERT:
                        case KeyEvent.VK_NUMPAD0:
                            resetCoordinatesButtonActionPerformed(null);
                            e.consume();
                            return true;
                        default:
                            break;
                    }
                }

                return false;
            }
        });
    }

    private void setStatusColorForState(ControllerState state) {
        Color color = null; // default to a transparent background.
        if (state == ControllerState.ALARM) {
            color = Color.RED;
        } else if (state == ControllerState.HOLD || state == ControllerState.DOOR || state == ControllerState.SLEEP) {
            color = Color.YELLOW;
        } else if (state == ControllerState.RUN || state == ControllerState.JOG || state == ControllerState.HOME) {
            color = Color.GREEN;
        } else {
            color = Color.WHITE;
        }

        this.activeStateLabel.setBackground(color);
        this.activeStateValueLabel.setBackground(color);
    }
    
    private void updateControls() {
        this.cancelButton.setEnabled(backend.canCancel());
        this.pauseButton.setEnabled(backend.canPause() || backend.isPaused());
        this.pauseButton.setText(backend.getPauseResumeText());
        this.sendButton.setEnabled(backend.canSend());
        
        boolean hasFile = backend.getGcodeFile() != null;
        if (hasFile) {
            this.saveButton.setEnabled(true);
            this.visualizeButton.setEnabled(true);
        }
        
        switch (backend.getControllerState()) {
            case DISCONNECTED:
            case UNKNOWN:
                this.updateConnectionControlsStateOpen(false);
                this.updateWorkflowControls(false);
                this.setStatusColorForState(ControllerState.UNKNOWN);
                break;
            case IDLE:
            case CHECK:
            case ALARM:
                this.updateConnectionControlsStateOpen(true);
                this.updateWorkflowControls(true);
                break;
            default:
                this.updateWorkflowControls(false);
                break;
        }
    }
    
    /**
     * Enable/disable connection frame based on connection state.
     */
    private void updateConnectionControlsStateOpen(boolean isOpen) {

        this.commPortComboBox.setEnabled(!isOpen);
        this.baudrateSelectionComboBox.setEnabled(!isOpen);
        this.refreshButton.setEnabled(!isOpen);

        if (isOpen) {
            this.opencloseButton.setText(Localization.getString("close"));
        } else {
            this.opencloseButton.setText(Localization.getString("open"));
        }
    }
    
    private void updateWorkflowControls(boolean enabled) {
        this.resetCoordinatesButton.setEnabled(enabled);
        this.resetXButton.setEnabled(enabled);
        this.resetYButton.setEnabled(enabled);
        this.resetZButton.setEnabled(enabled);
        this.returnToZeroButton.setEnabled(enabled);
        this.performHomingCycleButton.setEnabled(enabled);
        this.softResetMachineControl.setEnabled(enabled);
        this.killAlarmLock.setEnabled(enabled);
        this.toggleCheckMode.setEnabled(enabled);
        this.requestStateInformation.setEnabled(enabled);
    }
    
    private void resetTimerLabels() {
        // Reset labels
        this.durationValueLabel.setText("00:00:00");
        if (this.backend.isConnected()) {
            if (this.backend.getSendDuration() < 0) {
                this.remainingTimeValueLabel.setText("estimating...");
            } else if (this.backend.getSendDuration() == 0) {
                this.remainingTimeValueLabel.setText("--:--:--");
            } else {
                this.remainingTimeValueLabel.setText(Utils.formattedMillis(this.backend.getSendDuration()));
            }
        }
    }

    private void resetSentRowLabels(long numRows) {
        // Reset labels
        String totalRows =  String.valueOf(numRows);
        resetTimerLabels();
        this.sentRowsValueLabel.setText("0");
        this.remainingRowsValueLabel.setText(totalRows);
        this.rowsValueLabel.setText(totalRows);
    }
    
    /**
     * Updates all text labels in the GUI with localized labels.
     */
    private void setLocalLabels() {
        this.baudLabel.setText(Localization.getString("mainWindow.swing.baudLabel"));
        this.browseButton.setText(Localization.getString("mainWindow.swing.browseButton"));
        this.cancelButton.setText(Localization.getString("mainWindow.swing.cancelButton"));
        this.connectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(Localization.getString("mainWindow.swing.connectionPanel")));
        this.controlContextTabbedPane.setTitleAt(0, Localization.getString("mainWindow.swing.controlContextTabbedPane.machineControl"));
        this.controlContextTabbedPane.setTitleAt(1, Localization.getString("mainWindow.swing.controlContextTabbedPane.macros"));
        this.durationLabel.setText(Localization.getString("mainWindow.swing.durationLabel"));
        this.fileModePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(Localization.getString("mainWindow.swing.fileLabel")));
        this.firmwareLabel.setText(Localization.getString("mainWindow.swing.firmwareLabel"));
        this.firmwareSettingsMenuItem.setText(Localization.getString("mainWindow.swing.firmwareSettingsMenu"));
        this.grblConnectionSettingsMenuItem.setText(Localization.getString("mainWindow.swing.grblConnectionSettingsMenuItem"));
        this.helpButtonMachineControl.setText(Localization.getString("help"));
        this.settingsMenu.setText(Localization.getString("mainWindow.swing.settingsMenu"));
        this.statusPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(Localization.getString("mainWindow.swing.statusPanel")));
        this.bottomTabbedPane.setTitleAt(0, Localization.getString("mainWindow.swing.bottomTabbedPane.console"));
        this.bottomTabbedPane.setTitleAt(1, Localization.getString("mainWindow.swing.bottomTabbedPane.table"));
        this.latestCommentLabel.setText(Localization.getString("mainWindow.swing.latestCommentLabel"));
        this.machinePosition.setText(Localization.getString("mainWindow.swing.machinePosition"));
        this.opencloseButton.setText(Localization.getString("mainWindow.swing.opencloseButton"));
        this.pauseButton.setText(Localization.getString("mainWindow.swing.pauseButton"));
        this.portLabel.setText(Localization.getString("mainWindow.swing.portLabel"));
        this.remainingRowsLabel.setText(Localization.getString("mainWindow.swing.remainingRowsLabel"));
        this.remainingTimeLabel.setText(Localization.getString("mainWindow.swing.remainingTimeLabel"));
        this.resetCoordinatesButton.setText(Localization.getString("mainWindow.swing.resetCoordinatesButton"));
        this.returnToZeroButton.setText(Localization.getString("mainWindow.swing.returnToZeroButton"));
        this.rowsLabel.setText(Localization.getString("mainWindow.swing.rowsLabel"));
        this.saveButton.setText(Localization.getString("save"));
        this.scrollWindowCheckBox.setText(Localization.getString("mainWindow.swing.scrollWindowCheckBox"));
        this.sendButton.setText(Localization.getString("mainWindow.swing.sendButton"));
        this.sentRowsLabel.setText(Localization.getString("mainWindow.swing.sentRowsLabel"));
        this.showVerboseOutputCheckBox.setText(Localization.getString("mainWindow.swing.showVerboseOutputCheckBox"));
        this.showCommandTableCheckBox.setText(Localization.getString("mainWindow.swing.showCommandTableCheckBox"));
        this.softResetMachineControl.setText(Localization.getString("mainWindow.swing.softResetMachineControl"));
        this.visualizeButton.setText(Localization.getString("mainWindow.swing.visualizeButton"));
        this.workPositionLabel.setText(Localization.getString("mainWindow.swing.workPositionLabel"));
        this.macroPanel.setToolTipText(Localization.getString("mainWindow.swing.macroInstructions"));
        this.resetXButton.setText(Localization.getString("mainWindow.swing.resetX"));
        this.resetYButton.setText(Localization.getString("mainWindow.swing.resetY"));
        this.resetZButton.setText(Localization.getString("mainWindow.swing.resetZ"));
        this.activeStateLabel.setText(Localization.getString("mainWindow.swing.activeStateLabel"));
        this.PendantMenu.setText(Localization.getString("mainWindow.swing.pendant"));
        this.gcodeProcessorSettings.setText(Localization.getString("settings.processors.header"));
        this.startPendantServerButton.setText(Localization.getString("PendantMenu.item.StartServer"));
        this.stopPendantServerButton.setText(Localization.getString("PendantMenu.item.StopServer"));
    }
    
    // Scans for comm ports and puts them in the comm port combo box.
    private void loadPortSelector() {
        commPortComboBox.removeAllItems();

        List<String> portList = ConnectionFactory.getPortNames(backend.getSettings().getConnectionDriver());
        if (portList.size() < 1) {
            if (settings.isShowSerialPortWarning()) {
                displayErrorDialog(Localization.getString("mainWindow.error.noSerialPort"));
            }
        } else {
            // Sort?
            //java.util.Collections.sort(portList);

            for (String port : portList) {
                commPortComboBox.addItem(port);
            }

            commPortComboBox.setSelectedIndex(0);
        }
    }
    
    private void loadFirmwareSelector() {
        firmwareComboBox.removeAllItems();
        List<String> firmwareList = FirmwareUtils.getFirmwareList();
        
        if (firmwareList.size() < 1) {
            displayErrorDialog(Localization.getString("mainWindow.error.noFirmware"));
        } else {
            java.util.Iterator<String> iter = firmwareList.iterator();
            while ( iter.hasNext() ) {
                firmwareComboBox.addItem(iter.next());
            }
        }
    }
    
    void clearTable() {
        this.commandTable.clear();
    }

    public void updateControllerStatus(ControllerStatus status) {
        if (status == null) {
            return;
        }

        this.activeStateValueLabel.setText( Utils.getControllerStateText(status.getState()) );
        this.setStatusColorForState( status.getState() );

        UnitUtils.Units units = settings.getPreferredUnits();
        if (status.getMachineCoord() != null) {
            Position machineCoord = status.getMachineCoord().getPositionIn(units);
            this.machinePositionXValueLabel.setText( Utils.formatter.format(machineCoord.x) + units.abbreviation );
            this.machinePositionYValueLabel.setText( Utils.formatter.format(machineCoord.y) + units.abbreviation );
            this.machinePositionZValueLabel.setText( Utils.formatter.format(machineCoord.z) + units.abbreviation );
        }
        
        if (status.getWorkCoord() != null) {
            Position workCoord = status.getWorkCoord().getPositionIn(units);
            this.workPositionXValueLabel.setText( Utils.formatter.format(workCoord.x) + units.abbreviation );
            this.workPositionYValueLabel.setText( Utils.formatter.format(workCoord.y) + units.abbreviation );
            this.workPositionZValueLabel.setText( Utils.formatter.format(workCoord.z) + units.abbreviation );
        }
    }

    /**
     * Updates the visualizer with the processed gcode file if it is available,
     * otherwise uses the unprocessed file.
     */
    private void setVisualizerFile() {
        if (vw == null) return;

        if (processedGcodeFile == null) {
            if (gcodeFile == null) {
                return;
            }
            vw.setGcodeFile(gcodeFile);
        } else {
            vw.setProcessedGcodeFile(processedGcodeFile);
        }
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof FileStateEvent || evt instanceof ControllerStateEvent) {
            this.updateControls();
        }

        // If we changed settings such as the preferred unit settings we may need to refresh the GUI
        if (evt instanceof SettingChangedEvent && backend.getController() != null && backend.getController().getControllerStatus() != null) {
            updateControllerStatus(backend.getController().getControllerStatus());
        }

        if (evt instanceof SettingChangedEvent) {
            scrollWindowCheckBox.setSelected(backend.getSettings().isScrollWindowEnabled());
            showVerboseOutputCheckBox.setSelected(backend.getSettings().isVerboseOutputEnabled());
            commandTable.setAutoWindowScroll(backend.getSettings().isScrollWindowEnabled());
        }

        if (evt instanceof ControllerStatusEvent) {
            ControllerStatusEvent controllerStatusEvent = (ControllerStatusEvent) evt;
            updateControllerStatus(controllerStatusEvent.getStatus());
        } else if (evt instanceof FileStateEvent) {
            FileStateEvent fileStateEvent = (FileStateEvent) evt;
            switch(fileStateEvent.getFileState()) {
                case FILE_LOADING:
                    fileModePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(Localization.getString("mainWindow.swing.fileLabel") + ": " + backend.getGcodeFile().getName()));
                    fileModePanel.setToolTipText(backend.getGcodeFile().getAbsolutePath());
                    processedGcodeFile = null;
                    gcodeFile = fileStateEvent.getFile();
                    break;
                case FILE_LOADED:
                    processedGcodeFile = fileStateEvent.getFile();
                    if (commandTableScrollPane.isEnabled()) {
                        commandTable.clear();
                    }
                    try (IGcodeStreamReader gsr = new GcodeStreamReader(backend.getProcessedGcodeFile())) {
                        resetSentRowLabels(gsr.getNumRows());
                    } catch (IOException | GcodeStreamReader.NotGcodeStreamFile ex) {}
                    break;
                case FILE_STREAM_COMPLETE:
                    remainingTimeValueLabel.setText(Utils.formattedMillis(0));
                    remainingRowsValueLabel.setText("" + backend.getNumRemainingRows());
                    if (fileStateEvent.isSuccess()) {
                        EventQueue.invokeLater(() -> {
                            JOptionPane.showMessageDialog(new JFrame(),
                                    Localization.getString("mainWindow.ui.jobComplete") + " " + Utils.formattedMillis(backend.getSendDuration()),
                                    Localization.getString("success"), JOptionPane.INFORMATION_MESSAGE);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException ex) {}

                            // Stop the timer after a delay to make sure it is updated.
                            timer.stop();
                        });
                    } else {
                        displayErrorDialog(Localization.getString("mainWindow.error.jobComplete"));
                    }
                    break;
                default:
                    break;
            }

            setVisualizerFile();
        } else if (evt instanceof CommandEvent) {
            CommandEvent commandEvent = (CommandEvent) evt;
            GcodeCommand command = commandEvent.getCommand();
            if ((commandEvent.getCommandEventType() == CommandEventType.COMMAND_SKIPPED ||
                    commandEvent.getCommandEventType() == CommandEventType.COMMAND_SENT) && command.hasComment()) {
                latestCommentValueLabel.setText(command.getComment());
            }

            if(!command.isGenerated()) {
                if (commandEvent.getCommandEventType() == CommandEventType.COMMAND_COMPLETE) {
                    // update gui
                    EventQueue.invokeLater(() -> {
                        if (commandTableScrollPane.isEnabled()) {
                            commandTable.updateRow(command);
                        }

                        if (backend.isSendingFile() && vw != null && !command.isGenerated()) {
                            vw.setCompletedCommandNumber(command.getCommandNumber());
                        }
                    });
                } else if (commandEvent.getCommandEventType() == CommandEventType.COMMAND_SENT || commandEvent.getCommandEventType() == CommandEventType.COMMAND_SKIPPED) {
                    EventQueue.invokeLater(() -> {
                            if (commandTableScrollPane.isEnabled()) {
                                commandTable.addRow(command);
                            }
                        });
                }
            }
        }
    }

    // Generated variables.
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu PendantMenu;
    private javax.swing.JPanel actionPanel;
    private javax.swing.JLabel activeStateLabel;
    private javax.swing.JLabel activeStateValueLabel;
    private javax.swing.JLabel baudLabel;
    private javax.swing.JComboBox baudrateSelectionComboBox;
    private javax.swing.JTabbedPane bottomTabbedPane;
    private javax.swing.JButton browseButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox commPortComboBox;
    private com.willwinder.universalgcodesender.uielements.components.GcodeTable commandTable;
    private javax.swing.JScrollPane commandTableScrollPane;
    private CommandPanel commandPanel;
    private javax.swing.JPanel connectionPanel;
    private javax.swing.JTabbedPane controlContextTabbedPane;
    private javax.swing.JLabel durationLabel;
    private javax.swing.JLabel durationValueLabel;
    private javax.swing.JPanel fileModePanel;
    private javax.swing.JPanel fileRunPanel;
    private javax.swing.JComboBox firmwareComboBox;
    private javax.swing.JLabel firmwareLabel;
    private javax.swing.JMenuItem firmwareSettingsMenuItem;
    private javax.swing.JMenuItem gcodeProcessorSettings;
    private javax.swing.JMenuItem grblConnectionSettingsMenuItem;
    private javax.swing.JButton helpButtonMachineControl;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JPanel jogPanelPanel;
    private javax.swing.JButton killAlarmLock;
    private javax.swing.JLabel latestCommentLabel;
    private javax.swing.JLabel latestCommentValueLabel;
    private javax.swing.JPanel machineControlPanel;
    private javax.swing.JLabel machinePosition;
    private javax.swing.JLabel machinePositionXLabel;
    private javax.swing.JLabel machinePositionXValueLabel;
    private javax.swing.JLabel machinePositionYLabel;
    private javax.swing.JLabel machinePositionYValueLabel;
    private javax.swing.JLabel machinePositionZLabel;
    private javax.swing.JLabel machinePositionZValueLabel;
    private javax.swing.JScrollPane macroEditPanel;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JButton opencloseButton;
    private javax.swing.JButton pauseButton;
    private javax.swing.JButton performHomingCycleButton;
    private javax.swing.JLabel portLabel;
    private javax.swing.JButton refreshButton;
    private javax.swing.JLabel remainingRowsLabel;
    private javax.swing.JLabel remainingRowsValueLabel;
    private javax.swing.JLabel remainingTimeLabel;
    private javax.swing.JLabel remainingTimeValueLabel;
    private javax.swing.JButton requestStateInformation;
    private javax.swing.JButton resetCoordinatesButton;
    private javax.swing.JButton resetXButton;
    private javax.swing.JButton resetYButton;
    private javax.swing.JButton resetZButton;
    private javax.swing.JButton returnToZeroButton;
    private javax.swing.JLabel rowsLabel;
    private javax.swing.JLabel rowsValueLabel;
    private javax.swing.JButton saveButton;
    private javax.swing.JCheckBox scrollWindowCheckBox;
    private javax.swing.JButton sendButton;
    private javax.swing.JLabel sentRowsLabel;
    private javax.swing.JLabel sentRowsValueLabel;
    private javax.swing.JMenu settingsMenu;
    private javax.swing.JCheckBox showCommandTableCheckBox;
    private javax.swing.JCheckBox showVerboseOutputCheckBox;
    private javax.swing.JButton softResetMachineControl;
    private javax.swing.JMenuItem startPendantServerButton;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JMenuItem stopPendantServerButton;
    private javax.swing.JMenuItem macroSettingsMenuItem;
    private javax.swing.JButton toggleCheckMode;
    private javax.swing.JButton visualizeButton;
    private javax.swing.JLabel workPositionLabel;
    private javax.swing.JLabel workPositionXLabel;
    private javax.swing.JLabel workPositionXValueLabel;
    private javax.swing.JLabel workPositionYLabel;
    private javax.swing.JLabel workPositionYValueLabel;
    private javax.swing.JLabel workPositionZLabel;
    private javax.swing.JLabel workPositionZValueLabel;
    // End of variables declaration//GEN-END:variables
}
