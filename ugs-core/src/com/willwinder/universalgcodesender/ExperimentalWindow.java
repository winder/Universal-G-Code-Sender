/*
 * MainWindow.java
 *
 * Created on Jun 26, 2012, 3:04:38 PM
 */

/*
    Copywrite 2012-2016 Will Winder

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

import com.willwinder.universalgcodesender.model.*;
import com.willwinder.universalgcodesender.pendantui.SystemStateBean;
import com.willwinder.universalgcodesender.uielements.*;
import com.willwinder.universalgcodesender.utils.CommUtils;
import com.willwinder.universalgcodesender.utils.FirmwareUtils;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import com.willwinder.universalgcodesender.utils.Version;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.pendantui.PendantUI;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.visualizer.VisualizerWindow;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.model.Utils.Units;
import com.willwinder.universalgcodesender.uielements.LengthLimitedDocument;
import static com.willwinder.universalgcodesender.utils.GUIHelpers.displayErrorDialog;
import java.awt.Color;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultCaret;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.pendantui.PendantURLBean;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import java.awt.Toolkit;
import javax.swing.text.DefaultEditorKit;
import org.apache.commons.lang3.SystemUtils;

/**
 *
 * @author wwinder
 */
public class ExperimentalWindow extends JFrame implements ControllerListener, UGSEventListener {
    private static final Logger logger = Logger.getLogger(ExperimentalWindow.class.getName());

    final private static String VERSION = Version.getVersion() + " / " + Version.getTimestamp();

    private PendantUI pendantUI;

    BackendAPI backend;
    
    // My Variables
    private javax.swing.JFileChooser fileChooser;
    private final int consoleSize = 1024 * 1024;

    // TODO: Move command history box into a self contained object.
    private int commandNum = -1;
    private List<String> manualCommandHistory;

    // Other windows
    VisualizerWindow vw = null;
    String gcodeFile = null;
    String processedGcodeFile = null;
    
    // Duration timer
    private Timer timer;

    /** Creates new form ExperimentalWindow */
    public ExperimentalWindow() {
        this.backend = new GUIBackend();
        try {
            backend.applySettings(SettingsFactory.loadSettings());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (backend.getSettings().isShowNightlyWarning() && ExperimentalWindow.VERSION.contains("nightly")) {
            java.awt.EventQueue.invokeLater(new Runnable() { @Override public void run() {
                String message =
                        "This version of Universal Gcode Sender is a nightly build.\n"
                                + "It contains all of the latest features and improvements, \n"
                                + "but may also have bugs that still need to be fixed.\n"
                                + "\n"
                                + "If you encounter any problems, please report them on github.";
                JOptionPane.showMessageDialog(new JFrame(), message,
                        "", JOptionPane.INFORMATION_MESSAGE);
            }});
        }
        initComponents();
        initProgram();
        backend.addControllerListener(this);
        backend.addUGSEventListener(this);
        
        arrowMovementEnabled.setSelected(backend.getSettings().isManualModeEnabled());
        stepSizeSpinner.setValue(backend.getSettings().getManualModeStepSize());
        boolean unitsAreMM = backend.getSettings().getDefaultUnits().equals("mm");
        mmRadioButton.setSelected(unitsAreMM);
        inchRadioButton.setSelected(!unitsAreMM);
        fileChooser = new JFileChooser(backend.getSettings().getLastOpenedFilename());
        scrollWindowCheckBox.setSelected(backend.getSettings().isScrollWindowEnabled());
        checkScrollWindow();
        showVerboseOutputCheckBox.setSelected(backend.getSettings().isVerboseOutputEnabled());
        showCommandTableCheckBox.setSelected(backend.getSettings().isCommandTableEnabled());

        setSize(backend.getSettings().getMainWindowSettings().width, backend.getSettings().getMainWindowSettings().height);
        setLocation(backend.getSettings().getMainWindowSettings().xLocation, backend.getSettings().getMainWindowSettings().yLocation);

        connectionPanel.loadSettings();
        initFileChooser();
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (fileChooser.getSelectedFile() != null ) {
                    backend.getSettings().setLastOpenedFilename(fileChooser.getSelectedFile().getAbsolutePath());
                }
                
                backend.getSettings().setDefaultUnits(inchRadioButton.isSelected() ? "inch" : "mm");
                backend.getSettings().setManualModeStepSize(getStepSize());
                backend.getSettings().setManualModeEnabled(arrowMovementEnabled.isSelected());
                backend.getSettings().setScrollWindowEnabled(scrollWindowCheckBox.isSelected());
                backend.getSettings().setVerboseOutputEnabled(showVerboseOutputCheckBox.isSelected());
                backend.getSettings().setCommandTableEnabled(showCommandTableCheckBox.isSelected());

                connectionPanel.saveSettings();
                SettingsFactory.saveSettings(backend.getSettings());
                
                if(pendantUI!=null){
                    pendantUI.stop();
                }
            }
        });
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
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ExperimentalWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ExperimentalWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ExperimentalWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ExperimentalWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
//        GUIBackend backend = new GUIBackend();
        final ExperimentalWindow mw = new ExperimentalWindow();
        
        /* Apply the settings to the ExperimentalWindow bofore showing it */
        mw.arrowMovementEnabled.setSelected(mw.backend.getSettings().isManualModeEnabled());
        mw.stepSizeSpinner.setValue(mw.backend.getSettings().getManualModeStepSize());
        boolean unitsAreMM = mw.backend.getSettings().getDefaultUnits().equals("mm");
        mw.mmRadioButton.setSelected(unitsAreMM);
        mw.inchRadioButton.setSelected(!unitsAreMM);
        mw.fileChooser = new JFileChooser(mw.backend.getSettings().getLastOpenedFilename());
        mw.scrollWindowCheckBox.setSelected(mw.backend.getSettings().isScrollWindowEnabled());
        mw.showVerboseOutputCheckBox.setSelected(mw.backend.getSettings().isVerboseOutputEnabled());
        mw.showCommandTableCheckBox.setSelected(mw.backend.getSettings().isCommandTableEnabled());
        mw.showCommandTableCheckBoxActionPerformed(null);

        mw.setSize(mw.backend.getSettings().getMainWindowSettings().width, mw.backend.getSettings().getMainWindowSettings().height);
        mw.setLocation(mw.backend.getSettings().getMainWindowSettings().xLocation, mw.backend.getSettings().getMainWindowSettings().yLocation);

        mw.addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent ce) {
                mw.backend.getSettings().getMainWindowSettings().height = ce.getComponent().getSize().height;
                mw.backend.getSettings().getMainWindowSettings().width = ce.getComponent().getSize().width;
            }

            @Override
            public void componentMoved(ComponentEvent ce) {
                mw.backend.getSettings().getMainWindowSettings().xLocation = ce.getComponent().getLocation().x;
                mw.backend.getSettings().getMainWindowSettings().yLocation = ce.getComponent().getLocation().y;
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

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (mw.fileChooser.getSelectedFile() != null ) {
                    mw.backend.getSettings().setLastOpenedFilename(mw.fileChooser.getSelectedFile().getAbsolutePath());
                }
                
                mw.backend.getSettings().setDefaultUnits(mw.inchRadioButton.isSelected() ? "inch" : "mm");
                mw.backend.getSettings().setManualModeStepSize(mw.getStepSize());
                mw.backend.getSettings().setManualModeEnabled(mw.arrowMovementEnabled.isSelected());
                mw.backend.getSettings().setScrollWindowEnabled(mw.scrollWindowCheckBox.isSelected());
                mw.backend.getSettings().setVerboseOutputEnabled(mw.showVerboseOutputCheckBox.isSelected());
                mw.backend.getSettings().setCommandTableEnabled(mw.showCommandTableCheckBox.isSelected());

                mw.connectionPanel.saveSettings();

                if(mw.pendantUI!=null){
                    mw.pendantUI.stop();
                }
            }
        });
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

        lineBreakGroup = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jogUnitsGroup = new javax.swing.ButtonGroup();
        jMenuItem2 = new javax.swing.JMenuItem();
        scrollWindowCheckBox = new javax.swing.JCheckBox();
        bottomTabbedPane = new javax.swing.JTabbedPane();
        commandsPanel = new javax.swing.JPanel();
        commandLabel = new javax.swing.JLabel();
        commandTextField = new com.willwinder.universalgcodesender.uielements.CommandTextArea(backend);
        consoleScrollPane = new javax.swing.JScrollPane();
        consoleTextArea = new javax.swing.JTextArea();
        commandTableScrollPane = new javax.swing.JScrollPane();
        commandTable = new com.willwinder.universalgcodesender.uielements.GcodeTable();
        controlContextTabbedPane = new javax.swing.JTabbedPane();
        machineControlPanel = new javax.swing.JPanel();
        helpButtonMachineControl = new javax.swing.JButton();
        resetYButton = new javax.swing.JButton();
        softResetMachineControl = new javax.swing.JButton();
        requestStateInformation = new javax.swing.JButton();
        returnToZeroButton = new javax.swing.JButton();
        toggleCheckMode = new javax.swing.JButton();
        resetCoordinatesButton = new javax.swing.JButton();
        performHomingCycleButton = new javax.swing.JButton();
        killAlarmLock = new javax.swing.JButton();
        resetXButton = new javax.swing.JButton();
        resetZButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        macroActionPanel = new com.willwinder.universalgcodesender.uielements.MacroActionPanel(backend.getSettings(), backend);
        keyboardMovementPanel = new javax.swing.JPanel();
        stepSizeSpinner = new javax.swing.JSpinner();
        arrowMovementEnabled = new javax.swing.JCheckBox();
        movementButtonPanel = new javax.swing.JPanel();
        zMinusButton = new javax.swing.JButton();
        yMinusButton = new javax.swing.JButton();
        xPlusButton = new javax.swing.JButton();
        xMinusButton = new javax.swing.JButton();
        zPlusButton = new javax.swing.JButton();
        yPlusButton = new javax.swing.JButton();
        stepSizeLabel = new javax.swing.JLabel();
        inchRadioButton = new javax.swing.JRadioButton();
        mmRadioButton = new javax.swing.JRadioButton();
        macroPane = new javax.swing.JScrollPane();
        macroPanel = new com.willwinder.universalgcodesender.uielements.MacroPanel(backend.getSettings(), backend);
        showVerboseOutputCheckBox = new javax.swing.JCheckBox();
        showCommandTableCheckBox = new javax.swing.JCheckBox();
        fileModePanel = new javax.swing.JPanel();
        sendButton = new javax.swing.JButton();
        pauseButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        visualizeButton = new javax.swing.JButton();
        browseButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        sendStatusPanel = new com.willwinder.universalgcodesender.uielements.SendStatusPanel(backend);
        machineStatusPanel = new com.willwinder.universalgcodesender.uielements.machinestatus.MachineStatusPanel(backend);
        connectionPanel = new com.willwinder.universalgcodesender.uielements.connection.ConnectionPanel(backend);
        mainMenuBar = new javax.swing.JMenuBar();
        settingsMenu = new javax.swing.JMenu();
        grblConnectionSettingsMenuItem = new javax.swing.JMenuItem();
        firmwareSettingsMenu = new javax.swing.JMenu();
        grblFirmwareSettingsMenuItem = new javax.swing.JMenuItem();
        PendantMenu = new javax.swing.JMenu();
        startPendantServerButton = new javax.swing.JMenuItem();
        stopPendantServerButton = new javax.swing.JMenuItem();

        jMenuItem1.setText("jMenuItem1");

        jMenuItem3.setText("jMenuItem3");

        jMenuItem4.setText("jMenuItem4");

        jMenuItem2.setText("jMenuItem2");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(640, 520));

        scrollWindowCheckBox.setSelected(true);
        scrollWindowCheckBox.setText("Scroll output window");
        scrollWindowCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scrollWindowCheckBoxActionPerformed(evt);
            }
        });

        bottomTabbedPane.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        bottomTabbedPane.setMinimumSize(new java.awt.Dimension(0, 0));
        bottomTabbedPane.setPreferredSize(new java.awt.Dimension(468, 100));

        commandsPanel.setLayout(new java.awt.GridBagLayout());

        commandLabel.setText("Command");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        commandsPanel.add(commandLabel, gridBagConstraints);

        commandTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                commandTextFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        commandsPanel.add(commandTextField, gridBagConstraints);

        consoleTextArea.setEditable(false);
        consoleTextArea.setColumns(20);
        consoleTextArea.setDocument(new LengthLimitedDocument(consoleSize));
        consoleTextArea.setRows(5);
        consoleTextArea.setMaximumSize(new java.awt.Dimension(32767, 32767));
        consoleTextArea.setMinimumSize(new java.awt.Dimension(0, 0));
        consoleScrollPane.setViewportView(consoleTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        commandsPanel.add(consoleScrollPane, gridBagConstraints);

        bottomTabbedPane.addTab("Commands", commandsPanel);

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

        machineControlPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                machineControlPanelComponentShownHandler(evt);
            }
        });

        helpButtonMachineControl.setText("Help");
        helpButtonMachineControl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpButtonMachineControlActionPerformed(evt);
            }
        });

        resetYButton.setText("Reset Y Axis");
        resetYButton.setEnabled(false);
        resetYButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetYCoordinateButtonActionPerformed(evt);
            }
        });

        softResetMachineControl.setText("Soft Reset");
        softResetMachineControl.setEnabled(false);
        softResetMachineControl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                softResetMachineControlActionPerformed(evt);
            }
        });

        requestStateInformation.setText("$G");
        requestStateInformation.setEnabled(false);
        requestStateInformation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                requestStateInformationActionPerformed(evt);
            }
        });

        returnToZeroButton.setText("Return to Zero");
        returnToZeroButton.setEnabled(false);
        returnToZeroButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                returnToZeroButtonActionPerformed(evt);
            }
        });

        toggleCheckMode.setText("$C");
        toggleCheckMode.setEnabled(false);
        toggleCheckMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleCheckModeActionPerformed(evt);
            }
        });

        resetCoordinatesButton.setText("Reset Zero");
        resetCoordinatesButton.setEnabled(false);
        resetCoordinatesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetCoordinatesButtonActionPerformed(evt);
            }
        });

        performHomingCycleButton.setText("$H");
        performHomingCycleButton.setEnabled(false);
        performHomingCycleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                performHomingCycleButtonActionPerformed(evt);
            }
        });

        killAlarmLock.setText("$X");
        killAlarmLock.setEnabled(false);
        killAlarmLock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                killAlarmLockActionPerformed(evt);
            }
        });

        resetXButton.setText("Reset X Axis");
        resetXButton.setEnabled(false);
        resetXButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetXCoordinateButtonActionPerformed(evt);
            }
        });

        resetZButton.setText("Reset Z Axis");
        resetZButton.setEnabled(false);
        resetZButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetZCoordinateButtonActionPerformed(evt);
            }
        });

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setAutoscrolls(true);
        jScrollPane1.setPreferredSize(new java.awt.Dimension(50, 1000));
        jScrollPane1.setRequestFocusEnabled(false);

        org.jdesktop.layout.GroupLayout macroActionPanelLayout = new org.jdesktop.layout.GroupLayout(macroActionPanel);
        macroActionPanel.setLayout(macroActionPanelLayout);
        macroActionPanelLayout.setHorizontalGroup(
            macroActionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 643, Short.MAX_VALUE)
        );
        macroActionPanelLayout.setVerticalGroup(
            macroActionPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 250, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(macroActionPanel);

        keyboardMovementPanel.setPreferredSize(new java.awt.Dimension(247, 180));

        stepSizeSpinner.setModel(new StepSizeSpinnerModel(1.0, 0.0, null, 1.0));
        stepSizeSpinner.setEnabled(false);
        stepSizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                stepSizeSpinnerStateChanged(evt);
            }
        });

        arrowMovementEnabled.setText("Enable Keyboard Movement");
        arrowMovementEnabled.setEnabled(false);

        zMinusButton.setText("Z-");
        zMinusButton.setEnabled(false);
        zMinusButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zMinusButtonActionPerformed(evt);
            }
        });

        yMinusButton.setText("Y-");
        yMinusButton.setEnabled(false);
        yMinusButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yMinusButtonActionPerformed(evt);
            }
        });

        xPlusButton.setText("X+");
        xPlusButton.setEnabled(false);
        xPlusButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xPlusButtonActionPerformed(evt);
            }
        });

        xMinusButton.setText("X-");
        xMinusButton.setEnabled(false);
        xMinusButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xMinusButtonActionPerformed(evt);
            }
        });

        zPlusButton.setText("Z+");
        zPlusButton.setEnabled(false);
        zPlusButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zPlusButtonActionPerformed(evt);
            }
        });

        yPlusButton.setText("Y+");
        yPlusButton.setEnabled(false);
        yPlusButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yPlusButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout movementButtonPanelLayout = new org.jdesktop.layout.GroupLayout(movementButtonPanel);
        movementButtonPanel.setLayout(movementButtonPanelLayout);
        movementButtonPanelLayout.setHorizontalGroup(
            movementButtonPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(movementButtonPanelLayout.createSequentialGroup()
                .add(xMinusButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(movementButtonPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(yPlusButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(yMinusButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(xPlusButton, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(movementButtonPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, zMinusButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, zPlusButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );
        movementButtonPanelLayout.setVerticalGroup(
            movementButtonPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(movementButtonPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                .add(xMinusButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(xPlusButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(movementButtonPanelLayout.createSequentialGroup()
                    .add(yPlusButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(yMinusButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(movementButtonPanelLayout.createSequentialGroup()
                    .add(zPlusButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 46, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(zMinusButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        stepSizeLabel.setText("Step size:");
        stepSizeLabel.setEnabled(false);

        jogUnitsGroup.add(inchRadioButton);
        inchRadioButton.setText("inch");
        inchRadioButton.setEnabled(false);
        inchRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inchRadioButtonActionPerformed(evt);
            }
        });

        jogUnitsGroup.add(mmRadioButton);
        mmRadioButton.setText("millimeters");
        mmRadioButton.setEnabled(false);
        mmRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mmRadioButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout keyboardMovementPanelLayout = new org.jdesktop.layout.GroupLayout(keyboardMovementPanel);
        keyboardMovementPanel.setLayout(keyboardMovementPanelLayout);
        keyboardMovementPanelLayout.setHorizontalGroup(
            keyboardMovementPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(keyboardMovementPanelLayout.createSequentialGroup()
                .add(keyboardMovementPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(arrowMovementEnabled)
                    .add(keyboardMovementPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(keyboardMovementPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(keyboardMovementPanelLayout.createSequentialGroup()
                                .add(keyboardMovementPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(inchRadioButton)
                                    .add(stepSizeLabel))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(keyboardMovementPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                    .add(stepSizeSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                    .add(mmRadioButton)))
                            .add(movementButtonPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        keyboardMovementPanelLayout.setVerticalGroup(
            keyboardMovementPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(keyboardMovementPanelLayout.createSequentialGroup()
                .add(arrowMovementEnabled)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(keyboardMovementPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(stepSizeLabel)
                    .add(stepSizeSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(keyboardMovementPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.CENTER)
                    .add(inchRadioButton)
                    .add(mmRadioButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(movementButtonPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        keyboardMovementPanelLayout.linkSize(new java.awt.Component[] {stepSizeLabel, stepSizeSpinner}, org.jdesktop.layout.GroupLayout.VERTICAL);

        org.jdesktop.layout.GroupLayout machineControlPanelLayout = new org.jdesktop.layout.GroupLayout(machineControlPanel);
        machineControlPanel.setLayout(machineControlPanelLayout);
        machineControlPanelLayout.setHorizontalGroup(
            machineControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(machineControlPanelLayout.createSequentialGroup()
                .add(machineControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(machineControlPanelLayout.createSequentialGroup()
                        .add(requestStateInformation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 49, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(helpButtonMachineControl))
                    .add(resetCoordinatesButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 159, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(returnToZeroButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 159, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(softResetMachineControl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 159, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(machineControlPanelLayout.createSequentialGroup()
                        .add(performHomingCycleButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 49, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(6, 6, 6)
                        .add(killAlarmLock, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 49, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(6, 6, 6)
                        .add(toggleCheckMode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 49, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .add(6, 6, 6)
                .add(machineControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(resetXButton)
                    .add(resetYButton)
                    .add(resetZButton))
                .add(9, 9, 9)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(keyboardMovementPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
        );
        machineControlPanelLayout.setVerticalGroup(
            machineControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(machineControlPanelLayout.createSequentialGroup()
                .add(machineControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(machineControlPanelLayout.createSequentialGroup()
                        .add(resetCoordinatesButton)
                        .add(6, 6, 6)
                        .add(returnToZeroButton)
                        .add(6, 6, 6)
                        .add(softResetMachineControl)
                        .add(6, 6, 6)
                        .add(machineControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(performHomingCycleButton)
                            .add(killAlarmLock)
                            .add(toggleCheckMode))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(machineControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(requestStateInformation)
                            .add(helpButtonMachineControl)))
                    .add(machineControlPanelLayout.createSequentialGroup()
                        .add(resetXButton)
                        .add(6, 6, 6)
                        .add(resetYButton)
                        .add(6, 6, 6)
                        .add(resetZButton)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .add(keyboardMovementPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
            .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );

        controlContextTabbedPane.addTab("Machine Control", machineControlPanel);

        macroPane.setViewportView(macroPanel);

        controlContextTabbedPane.addTab("Macros", macroPane);

        showVerboseOutputCheckBox.setText("Show verbose output");

        showCommandTableCheckBox.setSelected(true);
        showCommandTableCheckBox.setText("Enable command table");
        showCommandTableCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showCommandTableCheckBoxActionPerformed(evt);
            }
        });

        fileModePanel.setMaximumSize(new java.awt.Dimension(247, 350));
        fileModePanel.setMinimumSize(new java.awt.Dimension(247, 350));
        fileModePanel.setPreferredSize(new java.awt.Dimension(247, 350));
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
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        fileModePanel.add(sendStatusPanel, gridBagConstraints);

        settingsMenu.setText("Settings");

        grblConnectionSettingsMenuItem.setText("Sender Settings");
        grblConnectionSettingsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                grblConnectionSettingsMenuItemActionPerformed(evt);
            }
        });
        settingsMenu.add(grblConnectionSettingsMenuItem);

        firmwareSettingsMenu.setText("Firmware Settings");

        grblFirmwareSettingsMenuItem.setText("GRBL");
        grblFirmwareSettingsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                grblFirmwareSettingsMenuItemActionPerformed(evt);
            }
        });
        firmwareSettingsMenu.add(grblFirmwareSettingsMenuItem);

        settingsMenu.add(firmwareSettingsMenu);

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
                    .add(fileModePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(machineStatusPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, connectionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
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
                    .add(controlContextTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 612, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(controlContextTabbedPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 283, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(scrollWindowCheckBox)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(showVerboseOutputCheckBox)
                        .add(showCommandTableCheckBox)))
                .add(4, 4, 4)
                .add(bottomTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(4, 4, 4))
            .add(layout.createSequentialGroup()
                .add(connectionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(machineStatusPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 124, Short.MAX_VALUE)
                .add(fileModePanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 191, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(161, 161, 161))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    /** End of generated code.
     */
    
    /** Generated callback functions, hand coded.
     */
    private void scrollWindowCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scrollWindowCheckBoxActionPerformed
        checkScrollWindow();
    }//GEN-LAST:event_scrollWindowCheckBoxActionPerformed


    private void increaseStepActionPerformed(java.awt.event.ActionEvent evt) {                                             
        double stepSize = this.getStepSize();
        if (stepSize >= 1) {
            stepSize++;
        } else if (stepSize >= 0.1) {
            stepSize = stepSize + 0.1;
        } else if (stepSize >= 0.01) {
            stepSize = stepSize + 0.01;
        } else {
            stepSize = 0.01;
        }
        this.setStepSize(stepSize);
    }                                            

    private void decreaseStepActionPerformed(java.awt.event.ActionEvent evt) {                                             
        double stepSize = this.getStepSize();
        if (stepSize > 1) {            
            stepSize--;
        } else if (stepSize > 0.1) {
            stepSize = stepSize - 0.1;
        } else if (stepSize > 0.01) {
            stepSize = stepSize - 0.01;
        }
        this.setStepSize(stepSize);
    }                                            
    
    private void divideStepActionPerformed(java.awt.event.ActionEvent evt) {                                             
        double stepSize = this.getStepSize();

        if (stepSize > 100) {            
            stepSize = 100;
        } else if (stepSize <= 100 && stepSize > 10) {
            stepSize = 10;
        } else if (stepSize <= 10 && stepSize > 1) {
            stepSize = 1;
        } else if (stepSize <= 1 && stepSize > 0.1) {
            stepSize = 0.1;
        } else if (stepSize <= 0.1 ) {
            stepSize = 0.01;
        } 
        
        this.setStepSize(stepSize);
    }                                            

    private void multiplyStepActionPerformed(java.awt.event.ActionEvent evt) {                                             
        double stepSize = this.getStepSize();

        if (stepSize < 0.01) {            
            stepSize = 0.01;
        } else if (stepSize >= 0.01 && stepSize < 0.1) {            
            stepSize = 0.1;
        }  else if (stepSize >= 0.1 && stepSize < 1) {            
            stepSize = 1;
        }  else if (stepSize >= 1 && stepSize < 10) {            
            stepSize = 10;
        }  else if (stepSize >= 10) {            
            stepSize = 100;
        }

        this.setStepSize(stepSize);
    }                                            

    // TODO: It would be nice to streamline this somehow...
    private void grblConnectionSettingsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_grblConnectionSettingsMenuItemActionPerformed
        ConnectionSettingsDialog gcsd = new ConnectionSettingsDialog(this, true);
        
        // Set initial values.
        gcsd.setSpeedOverrideEnabled(backend.getSettings().isOverrideSpeedSelected());
        gcsd.setSpeedOverridePercent((int) backend.getSettings().getOverrideSpeedValue());
        gcsd.setMaxCommandLength(backend.getSettings().getMaxCommandLength());
        gcsd.setTruncateDecimalLength(backend.getSettings().getTruncateDecimalLength());
        gcsd.setSingleStepModeEnabled(backend.getSettings().isSingleStepMode());
        gcsd.setRemoveAllWhitespace(backend.getSettings().isRemoveAllWhitespace());
        gcsd.setStatusUpdatesEnabled(backend.getSettings().isStatusUpdatesEnabled());
        gcsd.setStatusUpdatesRate(backend.getSettings().getStatusUpdateRate());
        gcsd.setStateColorDisplayEnabled(backend.getSettings().isDisplayStateColor());
        gcsd.setConvertArcsToLines(backend.getSettings().isConvertArcsToLines());
        gcsd.setSmallArcThreshold(backend.getSettings().getSmallArcThreshold());
        gcsd.setSmallArcSegmentLengthSpinner(backend.getSettings().getSmallArcSegmentLength());
        gcsd.setselectedLanguage(backend.getSettings().getLanguage());
        gcsd.setAutoConnectEnabled(backend.getSettings().isAutoConnectEnabled());
        gcsd.setAutoReconnect(backend.getSettings().isAutoReconnect());

        gcsd.setVisible(true);
        
        if (gcsd.saveChanges()) {
            backend.getSettings().setOverrideSpeedSelected(gcsd.getSpeedOverrideEnabled());
            backend.getSettings().setOverrideSpeedValue(gcsd.getSpeedOverridePercent());
            backend.getSettings().setMaxCommandLength(gcsd.getMaxCommandLength());
            backend.getSettings().setTruncateDecimalLength(gcsd.getTruncateDecimalLength());
            backend.getSettings().setSingleStepMode(gcsd.getSingleStepModeEnabled());
            backend.getSettings().setRemoveAllWhitespace(gcsd.getRemoveAllWhitespace());
            backend.getSettings().setStatusUpdatesEnabled(gcsd.getStatusUpdatesEnabled());
            backend.getSettings().setStatusUpdateRate(gcsd.getStatusUpdatesRate());
            backend.getSettings().setDisplayStateColor(gcsd.getDisplayStateColor());
            backend.getSettings().setConvertArcsToLines(gcsd.getConvertArcsToLines());
            backend.getSettings().setSmallArcThreshold(gcsd.getSmallArcThreshold());
            backend.getSettings().setSmallArcSegmentLength(gcsd.getSmallArcSegmentLength());
            backend.getSettings().setLanguage(gcsd.getLanguage());
            backend.getSettings().setAutoConnectEnabled(gcsd.getAutoConnectEnabled());
            backend.getSettings().setAutoReconnect(gcsd.getAutoReconnect());

//            try {
//                backend.applySettings(backend.getSettings());
//            } catch (Exception e) {
//                displayErrorDialog(e.getMessage());
//            }

            if (this.vw != null) {
                vw.setMinArcLength(gcsd.getSmallArcThreshold());
                vw.setArcLength(gcsd.getSmallArcSegmentLength());
            }
        }
    }//GEN-LAST:event_grblConnectionSettingsMenuItemActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        int returnVal = fileChooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                fileModePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(Localization.getString("mainWindow.swing.fileLabel") + ": " + fileChooser.getSelectedFile().getName()));
                fileModePanel.setToolTipText(fileChooser.getSelectedFile().getAbsolutePath());
                File gcodeFile = fileChooser.getSelectedFile();
                backend.setGcodeFile(gcodeFile);
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
            this.vw = new VisualizerWindow(backend.getSettings().getVisualizerWindowSettings());
            
            final ExperimentalWindow mw = this;
            vw.addComponentListener(new ComponentListener() {
                @Override
                public void componentResized(ComponentEvent ce) {
                    mw.backend.getSettings().getVisualizerWindowSettings().height = ce.getComponent().getSize().height;
                    mw.backend.getSettings().getVisualizerWindowSettings().width = ce.getComponent().getSize().width;
                }

                @Override
                public void componentMoved(ComponentEvent ce) {
                    mw.backend.getSettings().getVisualizerWindowSettings().xLocation = ce.getComponent().getLocation().x;
                    mw.backend.getSettings().getVisualizerWindowSettings().yLocation = ce.getComponent().getLocation().y;
                }

                @Override
                public void componentShown(ComponentEvent ce) {}
                @Override
                public void componentHidden(ComponentEvent ce) {}
            });

            vw.setMinArcLength(backend.getSettings().getSmallArcThreshold());
            vw.setArcLength(backend.getSettings().getSmallArcSegmentLength());
            setVisualizerFile();

            // Add listener
            this.backend.addControllerListener(vw);
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
//                            durationValueLabel.setText(Utils.formattedMillis(backend.getSendDuration()));
//                            remainingTimeValueLabel.setText(Utils.formattedMillis(backend.getSendRemainingDuration()));
//
//                            //sentRowsValueLabel.setText(""+sentRows);
//                            sentRowsValueLabel.setText(""+backend.getNumSentRows());
//                            remainingRowsValueLabel.setText("" + backend.getNumRemainingRows());

                            if (backend.isSending()) {
                                if (vw != null) {
                                    vw.setCompletedCommandNumber((int)backend.getNumSentRows());
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        };

//        this.resetTimerLabels();

        if (timer != null){ timer.stop(); }
        timer = new Timer(1000, actionListener);

        // Note: there is a divide by zero error in the timer because it uses
        //       the rowsValueLabel that was just reset.

        try {
            this.backend.send();
//            this.resetSentRowLabels(backend.getNumRows());
            timer.start();
        } catch (Exception e) {
            timer.stop();
            logger.log(Level.INFO, "Exception in sendButtonActionPerformed.", e);
            displayErrorDialog(e.getMessage());
        }
        
    }//GEN-LAST:event_sendButtonActionPerformed

    private void grblFirmwareSettingsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_grblFirmwareSettingsMenuItemActionPerformed
        try {
            if (!this.backend.isConnected()) {
                displayErrorDialog(Localization.getString("mainWindow.error.noFirmware"));
            } else if (this.backend.getController() instanceof GrblController) {
                    GrblFirmwareSettingsDialog gfsd = new GrblFirmwareSettingsDialog(this, true, this.backend);
                    gfsd.setVisible(true);
            } else {
                displayErrorDialog(Localization.getString("mainWindow.error.notGrbl"));
            }
        } catch (Exception ex) {
                displayErrorDialog(ex.getMessage());
        }
    }//GEN-LAST:event_grblFirmwareSettingsMenuItemActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        //displayErrorDialog("Disabled for refactoring.");
        
        int returnVal = fileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                File newFile = fileChooser.getSelectedFile();
                AbstractController control = FirmwareUtils.getControllerFor(FirmwareUtils.GRBL);
                backend.applySettingsToController(backend.getSettings(), control);
                
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
                this.messageForConsole("Pendant URL: " + result.getUrlString(), false);
            }
            this.startPendantServerButton.setEnabled(false);
            this.stopPendantServerButton.setEnabled(true);
            this.backend.addControllerListener(pendantUI);
        }//GEN-LAST:event_startPendantServerButtonActionPerformed

        private void stopPendantServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopPendantServerButtonActionPerformed
            this.pendantUI.stop();
            this.startPendantServerButton.setEnabled(true);
            this.stopPendantServerButton.setEnabled(false);
        }//GEN-LAST:event_stopPendantServerButtonActionPerformed

    private Units getSelectedUnits() {
        if (this.inchRadioButton.isSelected()) {
            return Units.INCH;
        } if (this.mmRadioButton.isSelected()) {
            return Units.MM;
        } else {
            return Units.UNKNOWN;
        }
    }
    
    private void adjustManualLocation(int x, int y, int z) {
        try {
            this.backend.adjustManualLocation(x, y, z, this.getStepSize(), getSelectedUnits());
        } catch (Exception e) {
            displayErrorDialog(e.getMessage());
        }
    }
    private void showCommandTableCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showCommandTableCheckBoxActionPerformed
        showCommandTable(showCommandTableCheckBox.isSelected());
    }//GEN-LAST:event_showCommandTableCheckBoxActionPerformed

    private void commandTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_commandTextFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_commandTextFieldActionPerformed

    private void controlContextTabbedPaneComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_controlContextTabbedPaneComponentShown
        // TODO add your handling code here:
    }//GEN-LAST:event_controlContextTabbedPaneComponentShown

    private void machineControlPanelComponentShownHandler(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_machineControlPanelComponentShownHandler
        macroActionPanel.doLayout();
    }//GEN-LAST:event_machineControlPanelComponentShownHandler

    private void mmRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mmRadioButtonActionPerformed

    }//GEN-LAST:event_mmRadioButtonActionPerformed

    private void inchRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inchRadioButtonActionPerformed

    }//GEN-LAST:event_inchRadioButtonActionPerformed

    private void yPlusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yPlusButtonActionPerformed
        this.adjustManualLocation(0, 1, 0);
    }//GEN-LAST:event_yPlusButtonActionPerformed

    private void zPlusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zPlusButtonActionPerformed
        this.adjustManualLocation(0, 0, 1);
    }//GEN-LAST:event_zPlusButtonActionPerformed

    private void xMinusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xMinusButtonActionPerformed
        this.adjustManualLocation(-1, 0, 0);
    }//GEN-LAST:event_xMinusButtonActionPerformed

    private void xPlusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xPlusButtonActionPerformed
        this.adjustManualLocation(1, 0, 0);
    }//GEN-LAST:event_xPlusButtonActionPerformed

    private void yMinusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yMinusButtonActionPerformed
        this.adjustManualLocation(0, -1, 0);
    }//GEN-LAST:event_yMinusButtonActionPerformed

    private void zMinusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zMinusButtonActionPerformed
        this.adjustManualLocation(0, 0, -1);
    }//GEN-LAST:event_zMinusButtonActionPerformed

    private void stepSizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_stepSizeSpinnerStateChanged

    }//GEN-LAST:event_stepSizeSpinnerStateChanged

    private void resetZCoordinateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetZCoordinateButtonActionPerformed
        try {
            this.backend.resetCoordinateToZero('Z');
        } catch (Exception ex) {
            displayErrorDialog(ex.getMessage());
        }
    }//GEN-LAST:event_resetZCoordinateButtonActionPerformed

    private void resetXCoordinateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetXCoordinateButtonActionPerformed
        try {
            this.backend.resetCoordinateToZero('X');
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
            this.backend.resetCoordinateToZero('Y');
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
        this.fileChooser = GcodeFileTypeFilter.getGcodeFileChooser(backend.getSettings().getLastOpenedFilename());
    }
        
    private void initProgram() {
        Localization.initialize(this.backend.getSettings().getLanguage());
        try {
            backend.applySettings(backend.getSettings());
        } catch (Exception e) {
            displayErrorDialog(e.getMessage());
        }
        
        this.setLocalLabels();
        this.checkScrollWindow();
        this.setTitle(Localization.getString("title") + " ("
                + Localization.getString("version") + " " + VERSION + ")");

        // Command History
        this.manualCommandHistory = new ArrayList<>();
        
        // Add keyboard listener for manual controls.
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
            .addKeyEventDispatcher(new KeyEventDispatcher() {
                @Override
                public boolean dispatchKeyEvent(KeyEvent e) {
                    // Check context.
                    if (((arrowMovementEnabled.isSelected()) &&
                            e.getID() == KeyEvent.KEY_PRESSED) &&
                            xPlusButton.isEnabled()) {
                        switch (e.getKeyCode()) {
                            case KeyEvent.VK_RIGHT:
                            case KeyEvent.VK_KP_RIGHT:
                            case KeyEvent.VK_NUMPAD6:
                                xPlusButtonActionPerformed(null);
                                e.consume();
                                return true;
                            case KeyEvent.VK_LEFT:
                            case KeyEvent.VK_KP_LEFT:
                            case KeyEvent.VK_NUMPAD4:
                                xMinusButtonActionPerformed(null);
                                e.consume();
                                return true;
                            case KeyEvent.VK_UP:
                            case KeyEvent.VK_KP_UP:
                            case KeyEvent.VK_NUMPAD8:
                                yPlusButtonActionPerformed(null);
                                e.consume();
                                return true;
                            case KeyEvent.VK_DOWN:
                            case KeyEvent.VK_KP_DOWN:
                            case KeyEvent.VK_NUMPAD2:                                                                                                                        
                                yMinusButtonActionPerformed(null);
                                e.consume();
                                return true;
                            case KeyEvent.VK_PAGE_UP:
                            case KeyEvent.VK_NUMPAD9:
                                zPlusButtonActionPerformed(null);
                                e.consume();
                                return true;
                            case KeyEvent.VK_PAGE_DOWN:
                            case KeyEvent.VK_NUMPAD3:
                                zMinusButtonActionPerformed(null);
                                e.consume();
                                return true;
                            case KeyEvent.VK_ADD:
                                increaseStepActionPerformed(null);
                                e.consume();
                                return true;
                            case KeyEvent.VK_SUBTRACT:
                                decreaseStepActionPerformed(null);
                                e.consume();
                                return true;
                            case KeyEvent.VK_DIVIDE:
                                divideStepActionPerformed(null);
                                e.consume();
                                return true;
                            case KeyEvent.VK_MULTIPLY:
                                multiplyStepActionPerformed(null);
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

    private double getStepSize() {
        try {
            this.stepSizeSpinner.commitEdit();
        } catch (ParseException e) {
            this.stepSizeSpinner.setValue(0.0);
        }
        BigDecimal bd = new BigDecimal(this.stepSizeSpinner.getValue().toString()).setScale(3, RoundingMode.HALF_EVEN);
        return bd.doubleValue();
        //return Double.parseDouble( this.stepSizeSpinner.getValue().toString() );
    }

    private void setStepSize(double val) {
        BigDecimal bd = new BigDecimal(val).setScale(3, RoundingMode.HALF_EVEN);
        val = bd.doubleValue();
        this.stepSizeSpinner.setValue(val);
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
        
        switch (backend.getControlState()) {
            case COMM_DISCONNECTED:
                this.updateManualControls(false);
                this.updateWorkflowControls(false);
                break;
            case COMM_IDLE:
                this.updateManualControls(true);
                this.updateWorkflowControls(true);
                break;
            case COMM_SENDING:
                // Workflow tab
                this.updateWorkflowControls(false);
                // Jogging commands
                this.updateManualControls(false);
        
                break;
            case COMM_SENDING_PAUSED:

                break;
            default:
                
        }
    }
    
    /**
     * Enable/disable jogging controls.
     */
    private void updateManualControls(boolean enabled) {
        this.arrowMovementEnabled.setEnabled(enabled);

        this.xMinusButton.setEnabled(enabled);
        this.xPlusButton.setEnabled(enabled);
        this.yMinusButton.setEnabled(enabled);
        this.yPlusButton.setEnabled(enabled);
        this.zMinusButton.setEnabled(enabled);
        this.zPlusButton.setEnabled(enabled);
        this.stepSizeLabel.setEnabled(enabled);
        this.stepSizeSpinner.setEnabled(enabled);
        this.inchRadioButton.setEnabled(enabled);
        this.mmRadioButton.setEnabled(enabled);
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
    
//    private void resetTimerLabels() {
//        // Reset labels
//        this.durationValueLabel.setText("00:00:00");
//        if (this.backend.isConnected()) {
//            if (this.backend.getSendDuration() < 0) {
//                this.remainingTimeValueLabel.setText("estimating...");
//            } else if (this.backend.getSendDuration() == 0) {
//                this.remainingTimeValueLabel.setText("--:--:--");
//            } else {
//                this.remainingTimeValueLabel.setText(Utils.formattedMillis(this.backend.getSendDuration()));
//            }
//        }
//    }
//
//    private void resetSentRowLabels(long numRows) {
//        // Reset labels
//        String totalRows =  String.valueOf(numRows);
//        resetTimerLabels();
//        this.sentRowsValueLabel.setText("0");
//        this.remainingRowsValueLabel.setText(totalRows);
//        this.rowsValueLabel.setText(totalRows);
//    }
    
    /**
     * Updates all text labels in the GUI with localized labels.
     */
    private void setLocalLabels() {
        this.arrowMovementEnabled.setText(Localization.getString("mainWindow.swing.arrowMovementEnabled"));
        this.browseButton.setText(Localization.getString("mainWindow.swing.browseButton"));
        this.cancelButton.setText(Localization.getString("mainWindow.swing.cancelButton"));
        this.commandLabel.setText(Localization.getString("mainWindow.swing.commandLabel"));
        this.controlContextTabbedPane.setTitleAt(0, Localization.getString("mainWindow.swing.controlContextTabbedPane.machineControl"));
        this.controlContextTabbedPane.setTitleAt(1, Localization.getString("mainWindow.swing.controlContextTabbedPane.macros"));
//        this.durationLabel.setText(Localization.getString("mainWindow.swing.durationLabel"));
        this.fileModePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(Localization.getString("mainWindow.swing.fileLabel")));
        this.keyboardMovementPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(Localization.getString("mainWindow.swing.keyboardMovementPanel")));
        this.firmwareSettingsMenu.setText(Localization.getString("mainWindow.swing.firmwareSettingsMenu"));
        this.grblConnectionSettingsMenuItem.setText(Localization.getString("mainWindow.swing.grblConnectionSettingsMenuItem"));
        this.grblFirmwareSettingsMenuItem.setText(Localization.getString("mainWindow.swing.grblFirmwareSettingsMenuItem"));
        this.helpButtonMachineControl.setText(Localization.getString("help"));
        this.settingsMenu.setText(Localization.getString("mainWindow.swing.settingsMenu"));
        this.bottomTabbedPane.setTitleAt(0, Localization.getString("mainWindow.swing.bottomTabbedPane.console"));
        this.bottomTabbedPane.setTitleAt(1, Localization.getString("mainWindow.swing.bottomTabbedPane.table"));
        this.pauseButton.setText(Localization.getString("mainWindow.swing.pauseButton"));
//        this.remainingRowsLabel.setText(Localization.getString("mainWindow.swing.remainingRowsLabel"));
//        this.remainingTimeLabel.setText(Localization.getString("mainWindow.swing.remainingTimeLabel"));
        this.resetCoordinatesButton.setText(Localization.getString("mainWindow.swing.resetCoordinatesButton"));
        this.returnToZeroButton.setText(Localization.getString("mainWindow.swing.returnToZeroButton"));
//        this.rowsLabel.setText(Localization.getString("mainWindow.swing.rowsLabel"));
        this.saveButton.setText(Localization.getString("save"));
        this.scrollWindowCheckBox.setText(Localization.getString("mainWindow.swing.scrollWindowCheckBox"));
        this.sendButton.setText(Localization.getString("mainWindow.swing.sendButton"));
//        this.sentRowsLabel.setText(Localization.getString("mainWindow.swing.sentRowsLabel"));
        this.showVerboseOutputCheckBox.setText(Localization.getString("mainWindow.swing.showVerboseOutputCheckBox"));
        this.showCommandTableCheckBox.setText(Localization.getString("mainWindow.swing.showCommandTableCheckBox"));
        this.softResetMachineControl.setText(Localization.getString("mainWindow.swing.softResetMachineControl"));
        this.stepSizeLabel.setText(Localization.getString("mainWindow.swing.stepSizeLabel"));
        this.visualizeButton.setText(Localization.getString("mainWindow.swing.visualizeButton"));
        this.macroPane.setToolTipText(Localization.getString("mainWindow.swing.macroInstructions"));
        this.inchRadioButton.setText(Localization.getString("mainWindow.swing.inchRadioButton"));
        this.mmRadioButton.setText(Localization.getString("mainWindow.swing.mmRadioButton"));
    }

    private void checkScrollWindow() {
        // Console output.
        DefaultCaret caret = (DefaultCaret)consoleTextArea.getCaret();
        if (scrollWindowCheckBox.isSelected()) {
          caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
          consoleTextArea.setCaretPosition(consoleTextArea.getDocument().getLength());
        } else {
            caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }
        
        // Command table.
        this.commandTable.setAutoWindowScroll(scrollWindowCheckBox.isSelected());
    }

    /** 
     * SerialCommunicatorListener implementation.
     */
    
    @Override
    public void fileStreamComplete(String filename, boolean success) {
        final String durationLabelCopy = sendStatusPanel.getDuration();
        if (success) {
            java.awt.EventQueue.invokeLater(new Runnable() { @Override public void run() {
                JOptionPane.showMessageDialog(new JFrame(),
                        Localization.getString("mainWindow.ui.jobComplete") + " " + durationLabelCopy,
                        Localization.getString("success"), JOptionPane.INFORMATION_MESSAGE);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {}

                // Stop the timer after a delay to make sure it is updated.
                timer.stop();
            }});
        } else {
            displayErrorDialog(Localization.getString("mainWindow.error.jobComplete"));
        }
    }
    
    @Override
    public void commandSkipped(GcodeCommand command) {
        commandSent(command);
    }
     
    @Override
    public void commandSent(final GcodeCommand command) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                // sent
                if (commandTableScrollPane.isEnabled()) {
                    commandTable.addRow(command);
                }
                //commandTable.updateRow(command);
            }});
    }
    
    @Override
    public void commandComment(String comment) {

    }
    
    @Override
    public void commandComplete(final GcodeCommand command) {
        //String gcodeString = command.getCommandString().toLowerCase();
        
        // update gui
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (commandTableScrollPane.isEnabled()) {
                    commandTable.updateRow(command);
                }
            }});
    }

    // TODO: Change verbose into an enum to toggle regular/verbose/error.
    @Override
    public void messageForConsole(final String msg, final Boolean verbose) {
        //final javax.swing.JTextArea consoleTextArea = this.consoleTextArea;
        //final javax.swing.JCheckBox showVerboseOutputCheckBox = this.showVerboseOutputCheckBox;
        //final javax.swing.JCheckBox scrollWindowCheckBox = this.scrollWindowCheckBox;

        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (!verbose || showVerboseOutputCheckBox.isSelected()) {
                    String verboseS = "[" + Localization.getString("verbose") + "]";
                    consoleTextArea.append((verbose ? verboseS : "") + msg);

                    if (consoleTextArea.isVisible() &&
                            scrollWindowCheckBox.isSelected()) {
                        consoleTextArea.setCaretPosition(consoleTextArea.getDocument().getLength());
                    }
                }
            }
        });
    }
    
    @Override
    public void statusStringListener(String state, Position machineCoord, Position workCoord) {

    }
    
    @Override
    public void postProcessData(int numRows) {
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
        if (evt.isFileChangeEvent() || evt.isStateChangeEvent()) {
            this.updateControls();
        }
        if (evt.isFileChangeEvent()) {
            switch(evt.getFileState()) {
                case FILE_LOADING:
                    processedGcodeFile = null;
                    gcodeFile = evt.getFile();
                    break;
                case FILE_LOADED:
                    processedGcodeFile = evt.getFile();
//                    try {
//                        try (GcodeStreamReader gsr = new GcodeStreamReader(backend.getProcessedGcodeFile())) {
//                            resetSentRowLabels(gsr.getNumRows());
//                        }
//                    } catch (IOException ex) {}
                    break;
                default:
                    break;
            }

            setVisualizerFile();
        }
    }

    // Generated variables.
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu PendantMenu;
    private javax.swing.JCheckBox arrowMovementEnabled;
    private javax.swing.JTabbedPane bottomTabbedPane;
    private javax.swing.JButton browseButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel commandLabel;
    private com.willwinder.universalgcodesender.uielements.GcodeTable commandTable;
    private javax.swing.JScrollPane commandTableScrollPane;
    private javax.swing.JTextField commandTextField;
    private javax.swing.JPanel commandsPanel;
    private com.willwinder.universalgcodesender.uielements.connection.ConnectionPanel connectionPanel;
    private javax.swing.JScrollPane consoleScrollPane;
    private javax.swing.JTextArea consoleTextArea;
    private javax.swing.JTabbedPane controlContextTabbedPane;
    private javax.swing.JPanel fileModePanel;
    private javax.swing.JMenu firmwareSettingsMenu;
    private javax.swing.JMenuItem grblConnectionSettingsMenuItem;
    private javax.swing.JMenuItem grblFirmwareSettingsMenuItem;
    private javax.swing.JButton helpButtonMachineControl;
    private javax.swing.JRadioButton inchRadioButton;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.ButtonGroup jogUnitsGroup;
    private javax.swing.JPanel keyboardMovementPanel;
    private javax.swing.JButton killAlarmLock;
    private javax.swing.ButtonGroup lineBreakGroup;
    private javax.swing.JPanel machineControlPanel;
    private com.willwinder.universalgcodesender.uielements.machinestatus.MachineStatusPanel machineStatusPanel;
    private com.willwinder.universalgcodesender.uielements.MacroActionPanel macroActionPanel;
    private javax.swing.JScrollPane macroPane;
    private com.willwinder.universalgcodesender.uielements.MacroPanel macroPanel;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JRadioButton mmRadioButton;
    private javax.swing.JPanel movementButtonPanel;
    private javax.swing.JButton pauseButton;
    private javax.swing.JButton performHomingCycleButton;
    private javax.swing.JButton requestStateInformation;
    private javax.swing.JButton resetCoordinatesButton;
    private javax.swing.JButton resetXButton;
    private javax.swing.JButton resetYButton;
    private javax.swing.JButton resetZButton;
    private javax.swing.JButton returnToZeroButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JCheckBox scrollWindowCheckBox;
    private javax.swing.JButton sendButton;
    private com.willwinder.universalgcodesender.uielements.SendStatusPanel sendStatusPanel;
    private javax.swing.JMenu settingsMenu;
    private javax.swing.JCheckBox showCommandTableCheckBox;
    private javax.swing.JCheckBox showVerboseOutputCheckBox;
    private javax.swing.JButton softResetMachineControl;
    private javax.swing.JMenuItem startPendantServerButton;
    private javax.swing.JLabel stepSizeLabel;
    private javax.swing.JSpinner stepSizeSpinner;
    private javax.swing.JMenuItem stopPendantServerButton;
    private javax.swing.JButton toggleCheckMode;
    private javax.swing.JButton visualizeButton;
    private javax.swing.JButton xMinusButton;
    private javax.swing.JButton xPlusButton;
    private javax.swing.JButton yMinusButton;
    private javax.swing.JButton yPlusButton;
    private javax.swing.JButton zMinusButton;
    private javax.swing.JButton zPlusButton;
    // End of variables declaration//GEN-END:variables

}
