/*
 * MainWindow.java
 *
 * Created on Jun 26, 2012, 3:04:38 PM
 */

/*
    Copywrite 2012-2015 Will Winder

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

import com.willwinder.universalgcodesender.types.Macro;
import com.willwinder.universalgcodesender.uielements.*;
import com.willwinder.universalgcodesender.utils.CommUtils;
import com.willwinder.universalgcodesender.utils.FirmwareUtils;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import com.willwinder.universalgcodesender.utils.Version;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.pendantui.PendantUI;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.visualizer.VisualizerWindow;
import com.willwinder.universalgcodesender.model.ControlStateEvent;
import com.willwinder.universalgcodesender.listeners.ControlStateListener;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.model.GUIBackend;
import com.willwinder.universalgcodesender.model.Utils.Units;
import org.jdesktop.layout.GroupLayout;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.DefaultCaret;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class MainWindow extends javax.swing.JFrame 
implements KeyListener, ControllerListener, ControlStateListener {
    private static final Logger logger = Logger.getLogger(MainWindow.class.getName());

    final private static String VERSION = Version.getVersion() + " / " + Version.getTimestamp();

    private PendantUI pendantUI;
    public Settings settings;
    
    BackendAPI backend;
    
    // My Variables
    private javax.swing.JFileChooser fileChooser;

    // TODO: Move command history box into a self contained object.
    private int commandNum = -1;
    private List<String> manualCommandHistory;

    // Other windows
    VisualizerWindow vw = null;
    
    // Duration timer
    private Timer timer;
    
    /** Creates new form MainWindow */
    public MainWindow(BackendAPI backend) {
        this.backend = backend;
        this.settings = SettingsFactory.loadSettings();
        if (settings.isShowNightlyWarning() && MainWindow.VERSION.contains("nightly")) {
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
        backend.addControlStateListener(this);
        
        arrowMovementEnabled.setSelected(settings.isManualModeEnabled());
        stepSizeSpinner.setValue(settings.getManualModeStepSize());
        boolean unitsAreMM = settings.getDefaultUnits().equals("mm");
        mmRadioButton.setSelected(unitsAreMM);
        inchRadioButton.setSelected(!unitsAreMM);
        fileChooser = new JFileChooser(settings.getLastOpenedFilename());
        commPortComboBox.setSelectedItem(settings.getPort());
        baudrateSelectionComboBox.setSelectedItem(settings.getPortRate());
        scrollWindowCheckBox.setSelected(settings.isScrollWindowEnabled());
        checkScrollWindow();
        showVerboseOutputCheckBox.setSelected(settings.isVerboseOutputEnabled());
        firmwareComboBox.setSelectedItem(settings.getFirmwareVersion());
        initMacroButtons(settings);

        setSize(settings.getMainWindowSettings().width, settings.getMainWindowSettings().height);
        setLocation(settings.getMainWindowSettings().xLocation, settings.getMainWindowSettings().yLocation);
//        mw.setSize(java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width, java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width);

        
        initFileChooser();
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (fileChooser.getSelectedFile() != null ) {
                    settings.setLastOpenedFilename(fileChooser.getSelectedFile().getAbsolutePath());
                }
                
                settings.setDefaultUnits(inchRadioButton.isSelected() ? "inch" : "mm");
                settings.setManualModeStepSize(getStepSize());
                settings.setManualModeEnabled(arrowMovementEnabled.isSelected());
                settings.setPort(commPortComboBox.getSelectedItem().toString());
                settings.setPortRate(baudrateSelectionComboBox.getSelectedItem().toString());
                settings.setScrollWindowEnabled(scrollWindowCheckBox.isSelected());
                settings.setVerboseOutputEnabled(showVerboseOutputCheckBox.isSelected());
                settings.setFirmwareVersion(firmwareComboBox.getSelectedItem().toString());

                SettingsFactory.saveSettings(settings);
                
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
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
         /* Create the form */
        GUIBackend backend = new GUIBackend();
        final MainWindow mw = new MainWindow(backend);
        
        /* Apply the settings to the MainWindow bofore showing it */
        mw.arrowMovementEnabled.setSelected(mw.settings.isManualModeEnabled());
        mw.stepSizeSpinner.setValue(mw.settings.getManualModeStepSize());
        boolean unitsAreMM = mw.settings.getDefaultUnits().equals("mm");
        mw.mmRadioButton.setSelected(unitsAreMM);
        mw.inchRadioButton.setSelected(!unitsAreMM);
        mw.fileChooser = new JFileChooser(mw.settings.getLastOpenedFilename());
        mw.commPortComboBox.setSelectedItem(mw.settings.getPort());
        mw.baudrateSelectionComboBox.setSelectedItem(mw.settings.getPortRate());
        mw.scrollWindowCheckBox.setSelected(mw.settings.isScrollWindowEnabled());
        mw.showVerboseOutputCheckBox.setSelected(mw.settings.isVerboseOutputEnabled());
        mw.firmwareComboBox.setSelectedItem(mw.settings.getFirmwareVersion());

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
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                if (mw.fileChooser.getSelectedFile() != null ) {
                    mw.settings.setLastOpenedFilename(mw.fileChooser.getSelectedFile().getAbsolutePath());
                }
                
                mw.settings.setDefaultUnits(mw.inchRadioButton.isSelected() ? "inch" : "mm");
                mw.settings.setManualModeStepSize(mw.getStepSize());
                mw.settings.setManualModeEnabled(mw.arrowMovementEnabled.isSelected());
                mw.settings.setPort(mw.commPortComboBox.getSelectedItem().toString());
                mw.settings.setPortRate(mw.baudrateSelectionComboBox.getSelectedItem().toString());
                mw.settings.setScrollWindowEnabled(mw.scrollWindowCheckBox.isSelected());
                mw.settings.setVerboseOutputEnabled(mw.showVerboseOutputCheckBox.isSelected());
                mw.settings.setFirmwareVersion(mw.firmwareComboBox.getSelectedItem().toString());
                SettingsFactory.saveSettings(mw.settings);
                
                if(mw.pendantUI!=null){
                	mw.pendantUI.stop();
                }
            }
        });
    }

    private  void initMacroButtons(Settings settings) {
        Integer lastMacroIndex = settings.getLastMacroIndex()+1;
        logger.info((lastMacroIndex) + " macros");
        for (int i = 0; i <= lastMacroIndex; i++) {
            JButton button = createMacroButton(i);
            JTextField textField = createMacroTextField(i);

            Macro macro = settings.getMacro(i);
            if (macro != null) {
                textField.setText(macro.getGcode());
                if (macro.getName() != null) {
                    button.setText(macro.getName());
                }
                if (macro.getDescription() != null) {
                    button.setToolTipText(macro.getDescription());
                }
            }
        }


        org.jdesktop.layout.GroupLayout macroPanelLayout = new org.jdesktop.layout.GroupLayout(macroPanel);

        GroupLayout.ParallelGroup parallelGroup = macroPanelLayout.createParallelGroup(GroupLayout.LEADING);

        GroupLayout.SequentialGroup sequentialGroup = macroPanelLayout.createSequentialGroup();
        parallelGroup.add(sequentialGroup);

        sequentialGroup.addContainerGap();
        GroupLayout.ParallelGroup parallelGroup1 = macroPanelLayout.createParallelGroup(GroupLayout.LEADING);
        sequentialGroup.add(parallelGroup1);

        for (int i = 0; i < customGcodeButtons.size(); i++) {
            GroupLayout.SequentialGroup group = macroPanelLayout.createSequentialGroup();
            group.add(customGcodeButtons.get(i), org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 49, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                    .add(customGcodeTextFields.get(i));
            parallelGroup1.add(group);
        }
//        sequentialGroup.addContainerGap();

        macroPanelLayout.setHorizontalGroup( parallelGroup );
        GroupLayout.SequentialGroup sequentialGroup1 = macroPanelLayout.createSequentialGroup();
        macroPanelLayout.setVerticalGroup(
                macroPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(sequentialGroup1
                                        .add(8, 8, 8)
//                                        .add(macroInstructions)
                        ));


        for (int i = 0; i < customGcodeButtons.size(); i++) {
//            sequentialGroup1.addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
            sequentialGroup1.add(macroPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                            .add(customGcodeButtons.get(i))
                            .add(customGcodeTextFields.get(i), org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE));
        }
//        sequentialGroup1.addContainerGap(76, Short.MAX_VALUE);

        macroPanel.setLayout(macroPanelLayout);
    }

    private JTextField createMacroTextField(int index) {
        JTextField textField = new MacroTextField(index, settings);

        customGcodeTextFields.add(textField);
        return textField;
    }

    private JButton createMacroButton(int i) {
        JButton button = new JButton(i+"");

        button.setEnabled(false);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                customGcodeButtonActionPerformed(evt);
            }
        });
        customGcodeButtons.add(button);
        return button;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lineBreakGroup = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jogUnitsGroup = new javax.swing.ButtonGroup();
        scrollWindowCheckBox = new javax.swing.JCheckBox();
        bottomTabbedPane = new javax.swing.JTabbedPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        consoleTextArea = new javax.swing.JTextArea();
        jScrollPane1 = new javax.swing.JScrollPane();
        commandTable = new com.willwinder.universalgcodesender.uielements.GcodeTable();
        controlContextTabbedPane = new javax.swing.JTabbedPane();
        commandsPanel = new javax.swing.JPanel();
        commandLabel = new javax.swing.JLabel();
        commandTextField = new javax.swing.JTextField();
        fileModePanel = new javax.swing.JPanel();
        sendButton = new javax.swing.JButton();
        pauseButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        visualizeButton = new javax.swing.JButton();
        browseButton = new javax.swing.JButton();
        fileTextField = new javax.swing.JTextField();
        fileLabel = new javax.swing.JLabel();
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
        machineControlPanel = new javax.swing.JPanel();
        resetCoordinatesButton = new javax.swing.JButton();
        returnToZeroButton = new javax.swing.JButton();
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
        performHomingCycleButton = new javax.swing.JButton();
        killAlarmLock = new javax.swing.JButton();
        toggleCheckMode = new javax.swing.JButton();
        helpButtonMachineControl = new javax.swing.JButton();
        softResetMachineControl = new javax.swing.JButton();
        requestStateInformation = new javax.swing.JButton();
        resetXButton = new javax.swing.JButton();
        resetYButton = new javax.swing.JButton();
        resetZButton = new javax.swing.JButton();
        macroPanel = new javax.swing.JPanel();
        macroScrollPane = new JScrollPane(macroPanel);
        macroInstructions = new javax.swing.JLabel();
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

        consoleTextArea.setColumns(20);
        consoleTextArea.setRows(5);
        consoleTextArea.setMaximumSize(new java.awt.Dimension(32767, 32767));
        consoleTextArea.setMinimumSize(new java.awt.Dimension(0, 0));
        jScrollPane2.setViewportView(consoleTextArea);

        bottomTabbedPane.addTab("Console", jScrollPane2);

        commandTable.setModel(new com.willwinder.universalgcodesender.uielements.GcodeTableModel());
        commandTable.setMaximumSize(new java.awt.Dimension(32767, 32767));
        commandTable.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(commandTable);

        bottomTabbedPane.addTab("Command Table", jScrollPane1);

        controlContextTabbedPane.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        controlContextTabbedPane.setMinimumSize(new java.awt.Dimension(395, 175));

        commandLabel.setText("Command");

        commandTextField.setEnabled(false);
        commandTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                commandTextFieldActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout commandsPanelLayout = new org.jdesktop.layout.GroupLayout(commandsPanel);
        commandsPanel.setLayout(commandsPanelLayout);
        commandsPanelLayout.setHorizontalGroup(
            commandsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(commandsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(commandsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(commandTextField)
                    .add(commandsPanelLayout.createSequentialGroup()
                        .add(commandLabel)
                        .add(0, 595, Short.MAX_VALUE)))
                .addContainerGap())
        );
        commandsPanelLayout.setVerticalGroup(
            commandsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(commandsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(commandLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(commandTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(143, Short.MAX_VALUE))
        );

        controlContextTabbedPane.addTab("Commands", commandsPanel);

        fileModePanel.setMinimumSize(new java.awt.Dimension(389, 150));

        sendButton.setText("Send");
        sendButton.setEnabled(false);
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendButtonActionPerformed(evt);
            }
        });

        pauseButton.setText("Pause");
        pauseButton.setEnabled(false);
        pauseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pauseButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        visualizeButton.setText("Visualize");
        visualizeButton.setEnabled(false);
        visualizeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                visualizeButtonActionPerformed(evt);
            }
        });

        browseButton.setText("Browse");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        fileTextField.setEnabled(false);

        fileLabel.setText("File");

        saveButton.setText("Save");
        saveButton.setEnabled(false);
        saveButton.setMaximumSize(new java.awt.Dimension(88, 29));
        saveButton.setMinimumSize(new java.awt.Dimension(88, 29));
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

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
                .addContainerGap()
                .add(fileRunPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, remainingRowsLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, remainingTimeLabel)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, sentRowsLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, rowsLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, durationLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fileRunPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(durationValueLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(remainingRowsValueLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(sentRowsValueLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(remainingTimeValueLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(rowsValueLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 56, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        fileRunPanelLayout.setVerticalGroup(
            fileRunPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(fileRunPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(fileRunPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(fileRunPanelLayout.createSequentialGroup()
                        .add(rowsValueLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sentRowsValueLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(remainingRowsValueLabel))
                    .add(fileRunPanelLayout.createSequentialGroup()
                        .add(rowsLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(sentRowsLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(remainingRowsLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fileRunPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(remainingTimeLabel)
                    .add(remainingTimeValueLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fileRunPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(durationValueLabel)
                    .add(durationLabel))
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout fileModePanelLayout = new org.jdesktop.layout.GroupLayout(fileModePanel);
        fileModePanel.setLayout(fileModePanelLayout);
        fileModePanelLayout.setHorizontalGroup(
            fileModePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(fileModePanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(fileTextField)
                .addContainerGap())
            .add(org.jdesktop.layout.GroupLayout.TRAILING, fileModePanelLayout.createSequentialGroup()
                .add(fileModePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(fileModePanelLayout.createSequentialGroup()
                        .add(2, 2, 2)
                        .add(sendButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(pauseButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 95, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(cancelButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(visualizeButton))
                    .add(fileModePanelLayout.createSequentialGroup()
                        .add(fileModePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(fileModePanelLayout.createSequentialGroup()
                                .add(8, 8, 8)
                                .add(fileLabel))
                            .add(fileModePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .add(fileRunPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fileModePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(fileModePanelLayout.createSequentialGroup()
                        .add(6, 6, 6)
                        .add(saveButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(browseButton))
                .add(8, 8, 8))
        );
        fileModePanelLayout.setVerticalGroup(
            fileModePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(fileModePanelLayout.createSequentialGroup()
                .add(fileLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fileTextField, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fileModePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, browseButton)
                    .add(fileModePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                        .add(sendButton)
                        .add(pauseButton)
                        .add(cancelButton)
                        .add(visualizeButton)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(fileModePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(fileModePanelLayout.createSequentialGroup()
                        .add(saveButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(0, 99, Short.MAX_VALUE))
                    .add(fileRunPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        controlContextTabbedPane.addTab("File Mode", fileModePanel);

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
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
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
            .add(arrowMovementEnabled)
            .add(movementButtonPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
            .add(keyboardMovementPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(keyboardMovementPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(keyboardMovementPanelLayout.createSequentialGroup()
                        .add(keyboardMovementPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(inchRadioButton)
                            .add(stepSizeLabel))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(stepSizeSpinner, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 70, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(mmRadioButton)))
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
                .add(inchRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(mmRadioButton)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(movementButtonPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        keyboardMovementPanelLayout.linkSize(new java.awt.Component[] {stepSizeLabel, stepSizeSpinner}, org.jdesktop.layout.GroupLayout.VERTICAL);

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

        toggleCheckMode.setText("$C");
        toggleCheckMode.setEnabled(false);
        toggleCheckMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleCheckModeActionPerformed(evt);
            }
        });

        helpButtonMachineControl.setText("Help");
        helpButtonMachineControl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpButtonMachineControlActionPerformed(evt);
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

        resetXButton.setText("Reset X Axis");
        resetXButton.setEnabled(false);
        resetXButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetXCoordinateButtonActionPerformed(evt);
            }
        });

        resetYButton.setText("Reset Y Axis");
        resetYButton.setEnabled(false);
        resetYButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetYCoordinateButtonActionPerformed(evt);
            }
        });

        resetZButton.setText("Reset Z Axis");
        resetZButton.setEnabled(false);
        resetZButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetZCoordinateButtonActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout machineControlPanelLayout = new org.jdesktop.layout.GroupLayout(machineControlPanel);
        machineControlPanel.setLayout(machineControlPanelLayout);
        machineControlPanelLayout.setHorizontalGroup(
            machineControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, machineControlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(machineControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(machineControlPanelLayout.createSequentialGroup()
                        .add(softResetMachineControl, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 159, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(resetZButton))
                    .add(machineControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                        .add(org.jdesktop.layout.GroupLayout.LEADING, machineControlPanelLayout.createSequentialGroup()
                            .add(requestStateInformation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 49, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(helpButtonMachineControl))
                        .add(org.jdesktop.layout.GroupLayout.LEADING, machineControlPanelLayout.createSequentialGroup()
                            .add(performHomingCycleButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 49, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(killAlarmLock, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 49, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                            .add(toggleCheckMode, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 49, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(machineControlPanelLayout.createSequentialGroup()
                        .add(resetCoordinatesButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 159, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(resetXButton))
                    .add(machineControlPanelLayout.createSequentialGroup()
                        .add(returnToZeroButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 159, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(resetYButton)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED, 162, Short.MAX_VALUE)
                .add(keyboardMovementPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        machineControlPanelLayout.setVerticalGroup(
            machineControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(machineControlPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(keyboardMovementPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(0, 0, Short.MAX_VALUE))
            .add(machineControlPanelLayout.createSequentialGroup()
                .add(machineControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(resetCoordinatesButton)
                    .add(resetXButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(machineControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(returnToZeroButton)
                    .add(resetYButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(machineControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(softResetMachineControl)
                    .add(resetZButton))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(machineControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(performHomingCycleButton)
                    .add(killAlarmLock)
                    .add(toggleCheckMode))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(machineControlPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(requestStateInformation)
                    .add(helpButtonMachineControl))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        controlContextTabbedPane.addTab("Machine Control", machineControlPanel);

        controlContextTabbedPane.addTab("Macros", macroScrollPane);

        connectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Connection"));
        connectionPanel.setMaximumSize(new java.awt.Dimension(247, 100));
        connectionPanel.setMinimumSize(new java.awt.Dimension(247, 100));
        connectionPanel.setName("Connection"); // NOI18N
        connectionPanel.setPreferredSize(new java.awt.Dimension(247, 100));

        commPortComboBox.setEditable(true);

        baudrateSelectionComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "2400", "4800", "9600", "19200", "38400", "57600", "115200" }));
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

        refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/refresh.gif"))); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        baudLabel.setText("Baud:");

        portLabel.setText("Port:");

        firmwareLabel.setText("Firmware:");

        firmwareComboBox.setEditable(true);

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
                .addContainerGap(25, Short.MAX_VALUE))
        );

        showVerboseOutputCheckBox.setText("Show verbose output");

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
                .addContainerGap(6, Short.MAX_VALUE))
        );

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
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                    .add(connectionPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .add(statusPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(scrollWindowCheckBox)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(showVerboseOutputCheckBox)
                        .addContainerGap())
                    .add(controlContextTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            .add(bottomTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(connectionPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 144, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(statusPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(layout.createSequentialGroup()
                        .add(controlContextTabbedPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 283, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(scrollWindowCheckBox)
                            .add(showVerboseOutputCheckBox))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(bottomTabbedPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE))
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

    private void commandTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_commandTextFieldActionPerformed
        final String str = this.commandTextField.getText().replaceAll("(\\r\\n|\\n\\r|\\r|\\n)", "");
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    backend.sendGcodeCommand(str);
                } catch (Exception ex) {
                    displayErrorDialog(ex.getMessage());
                }
            }
        });
        this.commandTextField.setText("");
        this.manualCommandHistory.add(str);
        this.commandNum = -1;
    }//GEN-LAST:event_commandTextFieldActionPerformed

    // TODO: Find out how to make these key* functions actions like the above.
    // TODO: Create custom text area that will do all this stuff without
    //       cluttering up the MainWindow class.
    @Override
    public void keyPressed(KeyEvent ke) {
        boolean pressed = false;
        
        if (ke.getKeyCode() == KeyEvent.VK_UP) {
            pressed = true;
            if (this.commandNum == 0 || this.manualCommandHistory.isEmpty()) {
                java.awt.Toolkit.getDefaultToolkit().beep();
            } else if (this.commandNum == -1) {
                this.commandNum = this.manualCommandHistory.size() -1;
            } else {
                this.commandNum--;
            }
        }
        else if (ke.getKeyCode() == KeyEvent.VK_DOWN) {
            pressed = true;
            if ((this.commandNum == -1) || this.commandNum == (this.manualCommandHistory.size() -1)) {
                java.awt.Toolkit.getDefaultToolkit().beep();
                return;
            } else {
                this.commandNum++;
            }
        }
        
        if (pressed && this.commandNum != -1) {
            String text = this.manualCommandHistory.get(this.commandNum);
            this.commandTextField.setText(text);
        }
    }
    
    @Override
    public void keyTyped(KeyEvent ke) {
        // Don't care about this one...
    }
    
    @Override
    public void keyReleased(KeyEvent ke) {
        // Or this one...
    }
    
    private void opencloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_opencloseButtonActionPerformed
        if( this.opencloseButton.getText().equalsIgnoreCase(Localization.getString("open")) ) {
            this.clearTable();
            this.sentRowsValueLabel.setText("0");

            String firmware = this.firmwareComboBox.getSelectedItem().toString();
            String port = commPortComboBox.getSelectedItem().toString();
            int baudRate = Integer.parseInt(baudrateSelectionComboBox.getSelectedItem().toString());
            
            try {
                this.backend.connect(firmware, port, baudRate);
                
                if (this.backend.getFile() != null) {
                    if (this.vw != null) {
                        vw.setGcodeFile(this.backend.getFile().getAbsolutePath());
                    }
                }
                
                // Let the command field grab focus.
                commandTextField.grabFocus();
            } catch (Exception e) {
                MainWindow.displayErrorDialog(e.getMessage());
            }
        } else {
            try {
                this.backend.disconnect();
            } catch (Exception e) {
                MainWindow.displayErrorDialog(e.getMessage());
            }
        }
    }//GEN-LAST:event_opencloseButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        loadPortSelector();
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void baudrateSelectionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_baudrateSelectionComboBoxActionPerformed
    }//GEN-LAST:event_baudrateSelectionComboBoxActionPerformed


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
        gcsd.setSpeedOverrideEnabled(settings.isOverrideSpeedSelected());
        gcsd.setSpeedOverridePercent((int) settings.getOverrideSpeedValue());
        gcsd.setMaxCommandLength(settings.getMaxCommandLength());
        gcsd.setTruncateDecimalLength(settings.getTruncateDecimalLength());
        gcsd.setSingleStepModeEnabled(settings.isSingleStepMode());
        gcsd.setRemoveAllWhitespace(settings.isRemoveAllWhitespace());
        gcsd.setStatusUpdatesEnabled(settings.isStatusUpdatesEnabled());
        gcsd.setStatusUpdatesRate(settings.getStatusUpdateRate());
        gcsd.setStateColorDisplayEnabled(settings.isDisplayStateColor());
        gcsd.setConvertArcsToLines(settings.isConvertArcsToLines());
        gcsd.setSmallArcThreshold(settings.getSmallArcThreshold());
        gcsd.setSmallArcSegmentLengthSpinner(settings.getSmallArcSegmentLength());
        gcsd.setselectedLanguage(settings.getLanguage());
        gcsd.setVisible(true);
        
        if (gcsd.saveChanges()) {
            settings.setOverrideSpeedSelected(gcsd.getSpeedOverrideEnabled());
            settings.setOverrideSpeedValue(gcsd.getSpeedOverridePercent());
            settings.setMaxCommandLength(gcsd.getMaxCommandLength());
            settings.setTruncateDecimalLength(gcsd.getTruncateDecimalLength());
            settings.setSingleStepMode(gcsd.getSingleStepModeEnabled());
            settings.setRemoveAllWhitespace(gcsd.getRemoveAllWhitespace());
            settings.setStatusUpdatesEnabled(gcsd.getStatusUpdatesEnabled());
            settings.setStatusUpdateRate(gcsd.getStatusUpdatesRate());
            settings.setDisplayStateColor(gcsd.getDisplayStateColor());
            settings.setConvertArcsToLines(gcsd.getConvertArcsToLines());
            settings.setSmallArcThreshold(gcsd.getSmallArcThreshold());
            settings.setSmallArcSegmentLength(gcsd.getSmallArcSegmentLength());
            settings.setLanguage(gcsd.getLanguage());
            
            try {
                backend.applySettings(settings);
            } catch (Exception e) {
                MainWindow.displayErrorDialog(e.getMessage());
            }

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
                fileTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                File gcodeFile = fileChooser.getSelectedFile();
                backend.setFile(gcodeFile);
                if (this.vw != null) {
                    vw.setGcodeFile(gcodeFile.getAbsolutePath());
                }
            } catch (Exception ex) {
                MainWindow.displayErrorDialog(ex.getMessage());
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

            vw.setMinArcLength(settings.getSmallArcThreshold());
            vw.setArcLength(settings.getSmallArcSegmentLength());
            if (this.fileTextField.getText().length() > 1) {
                vw.setGcodeFile(this.fileTextField.getText());
            }
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
            MainWindow.displayErrorDialog(e.getMessage());
        }
    }
    
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        cancelButtonActionPerformed();
    }//GEN-LAST:event_cancelButtonActionPerformed

    public void pauseButtonActionPerformed() {
        try {
            this.backend.pauseResume();
        } catch (Exception e) {
            MainWindow.displayErrorDialog(e.getMessage());
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
                        durationValueLabel.setText(Utils.formattedMillis(backend.getSendDuration()));
                        remainingTimeValueLabel.setText(Utils.formattedMillis(backend.getSendRemainingDuration()));
                        
                        //sentRowsValueLabel.setText(""+sentRows);
                        sentRowsValueLabel.setText(""+backend.getNumSentRows());
                        remainingRowsValueLabel.setText("" + backend.getNumRemainingRows());

                        if (backend.isSending()) {
                            if (vw != null) {
                                vw.setCompletedCommandNumber((int)backend.getNumSentRows());
                            }
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
            this.backend.send();
            this.resetSentRowLabels(backend.getNumRows());
            timer.start();
        } catch (Exception e) {
            timer.stop();
            logger.log(Level.INFO, "Exception in sendButtonActionPerformed.", e);
            MainWindow.displayErrorDialog(e.getMessage());
        }
        
    }//GEN-LAST:event_sendButtonActionPerformed

    private void grblFirmwareSettingsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_grblFirmwareSettingsMenuItemActionPerformed
        try {
            if (!this.backend.isConnected()) {
                MainWindow.displayErrorDialog(Localization.getString("mainWindow.error.noFirmware"));
            } else if (this.backend.getController() instanceof GrblController) {
                    GrblFirmwareSettingsDialog gfsd = new GrblFirmwareSettingsDialog(this, true, this.backend);
                    gfsd.setVisible(true);
            } else {
                MainWindow.displayErrorDialog(Localization.getString("mainWindow.error.notGrbl"));
            }
        } catch (Exception ex) {
                MainWindow.displayErrorDialog(ex.getMessage());
        }
    }//GEN-LAST:event_grblFirmwareSettingsMenuItemActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        //MainWindow.displayErrorDialog("Disabled for refactoring.");
        
        int returnVal = fileChooser.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                File newFile = fileChooser.getSelectedFile();
                AbstractController control = FirmwareUtils.getControllerFor(FirmwareUtils.GRBL);
                backend.applySettingsToController(settings, control);
                
                backend.preprocessAndExportToFile(newFile);
            } catch (FileNotFoundException ex) {
                MainWindow.displayErrorDialog(Localization.getString("mainWindow.error.openingFile")
                        + ": " + ex.getMessage());
            } catch (IOException e) {
                MainWindow.displayErrorDialog(Localization.getString("mainWindow.error.processingFile")
                        + ": "+e.getMessage());
            } catch (Exception e) {
                logger.log(Level.INFO, "Exception in saveButtonActionPerformed.", e);
                MainWindow.displayErrorDialog(Localization.getString("mainWindow.error.duringSave") +
                        ": " + e.getMessage());
            }
        }
        
    }//GEN-LAST:event_saveButtonActionPerformed

        private void startPendantServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startPendantServerButtonActionPerformed
	    this.pendantUI = new PendantUI(backend);
	    this.pendantUI.start();
	    this.startPendantServerButton.setEnabled(false);
	    this.stopPendantServerButton.setEnabled(true);
            this.backend.addControllerListener(pendantUI);
        }//GEN-LAST:event_startPendantServerButtonActionPerformed

        private void stopPendantServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopPendantServerButtonActionPerformed
	    this.pendantUI.stop();
	    this.startPendantServerButton.setEnabled(true);
	    this.stopPendantServerButton.setEnabled(false);
        }//GEN-LAST:event_stopPendantServerButtonActionPerformed

    private void resetZCoordinateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetZCoordinateButtonActionPerformed
        try {
            this.backend.resetCoordinateToZero('Z');
        } catch (Exception ex) {
            MainWindow.displayErrorDialog(ex.getMessage());
        }
    }//GEN-LAST:event_resetZCoordinateButtonActionPerformed

    private void resetYCoordinateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetYCoordinateButtonActionPerformed
        try {
            this.backend.resetCoordinateToZero('Y');
        } catch (Exception ex) {
            MainWindow.displayErrorDialog(ex.getMessage());
        }
    }//GEN-LAST:event_resetYCoordinateButtonActionPerformed

    private void resetXCoordinateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetXCoordinateButtonActionPerformed
        try {
            this.backend.resetCoordinateToZero('X');
        } catch (Exception ex) {
            MainWindow.displayErrorDialog(ex.getMessage());
        }
    }//GEN-LAST:event_resetXCoordinateButtonActionPerformed

    private void requestStateInformationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_requestStateInformationActionPerformed
        try {
            this.backend.requestParserState();
        } catch (Exception ex) {
            MainWindow.displayErrorDialog(ex.getMessage());
        }
    }//GEN-LAST:event_requestStateInformationActionPerformed

    private void softResetMachineControlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_softResetMachineControlActionPerformed
        try {
            this.backend.issueSoftReset();
        } catch (Exception ex) {
            MainWindow.displayErrorDialog(ex.getMessage());
        }
    }//GEN-LAST:event_softResetMachineControlActionPerformed

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

    private void toggleCheckModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toggleCheckModeActionPerformed
        try {
            this.backend.toggleCheckMode();
        } catch (Exception ex) {
            MainWindow.displayErrorDialog(ex.getMessage());
        }
    }//GEN-LAST:event_toggleCheckModeActionPerformed

    private void killAlarmLockActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_killAlarmLockActionPerformed
        try {
            this.backend.killAlarmLock();
        } catch (Exception ex) {
            MainWindow.displayErrorDialog(ex.getMessage());
        }
    }//GEN-LAST:event_killAlarmLockActionPerformed

    private void performHomingCycleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_performHomingCycleButtonActionPerformed
        try {
            this.backend.performHomingCycle();
        } catch (Exception ex) {
            MainWindow.displayErrorDialog(ex.getMessage());
        }
    }//GEN-LAST:event_performHomingCycleButtonActionPerformed

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
            MainWindow.displayErrorDialog(e.getMessage());
        }
    }
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

    private void returnToZeroButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_returnToZeroButtonActionPerformed
        try {
            backend.returnToZero();
        } catch (Exception ex) {
            MainWindow.displayErrorDialog(ex.getMessage());
        }

    }//GEN-LAST:event_returnToZeroButtonActionPerformed

    private void resetCoordinatesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetCoordinatesButtonActionPerformed
        try {
            this.backend.resetCoordinatesToZero();
        } catch (Exception ex) {
            MainWindow.displayErrorDialog(ex.getMessage());
        }
    }//GEN-LAST:event_resetCoordinatesButtonActionPerformed

    private void inchRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inchRadioButtonActionPerformed
    }//GEN-LAST:event_inchRadioButtonActionPerformed

    private void mmRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mmRadioButtonActionPerformed
    }//GEN-LAST:event_mmRadioButtonActionPerformed

    private void executeCustomGcode(String str)
    {
        if (str == null) {
            return;
        }

        str = str.replaceAll("(\\r\\n|\\n\\r|\\r|\\n)", "");
        final String[] parts = str.split(";");
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    for (String cmd : parts)
                    {
                        backend.sendGcodeCommand(cmd);
                    }
                } catch (Exception ex) {
                    displayErrorDialog(ex.getMessage());
                }
            }
        });
        
        for (String cmd : parts)
        {
            this.manualCommandHistory.add(cmd);
            this.commandNum = -1;
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
            MainWindow.displayErrorDialog(e.getMessage());
        }
        
        this.setLocalLabels();
        this.loadPortSelector();
        this.checkScrollWindow();
        this.loadFirmwareSelector();
        this.setTitle(Localization.getString("title") + " (" 
                + Localization.getString("version") + " " + VERSION + ")");
        
        // Command History
        this.manualCommandHistory = new ArrayList<>();
        this.commandTextField.addKeyListener(this);
        
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
                        }
                    }
                    
                    return false;
                }
            });
    }

    private double getStepSize() {
        BigDecimal bd = new BigDecimal(this.stepSizeSpinner.getValue().toString()).setScale(3, RoundingMode.HALF_EVEN);
        return bd.doubleValue();
        //return Double.parseDouble( this.stepSizeSpinner.getValue().toString() );
    }

    private void setStepSize(double val) {
        BigDecimal bd = new BigDecimal(val).setScale(3, RoundingMode.HALF_EVEN);
        val = bd.doubleValue();
        this.stepSizeSpinner.setValue(val);
    }
    
    private void setStatusColorForState(String state) {
        if (settings.isDisplayStateColor()) {
            java.awt.Color color = null; // default to a transparent background.
            if (state.equals(Localization.getString("mainWindow.status.alarm"))) {
                color = Color.RED;
            } else if (state.equals(Localization.getString("mainWindow.status.hold"))) {
                color = Color.YELLOW;
            } else if (state.equals(Localization.getString("mainWindow.status.queue"))) {
                color = Color.YELLOW;
            } else if (state.equals(Localization.getString("mainWindow.status.run"))) {
                color = Color.GREEN;
            } else {
                color = Color.WHITE;
            }

            this.activeStateLabel.setBackground(color);
            this.activeStateValueLabel.setBackground(color);
        } else {
            this.activeStateLabel.setBackground(null);
            this.activeStateValueLabel.setBackground(null);
        }
    }
    
    private void updateControls() {
        this.cancelButton.setEnabled(backend.canCancel());
        this.pauseButton.setEnabled(backend.canPause() || backend.isPaused());
        this.pauseButton.setText(backend.getPauseResumeText());
        this.sendButton.setEnabled(backend.canSend());
        
        boolean hasFile = backend.getFile() != null;
        if (hasFile) {
                this.saveButton.setEnabled(true);
                this.visualizeButton.setEnabled(true);
                this.fileTextField.setEnabled(false);
        }
        
        switch (backend.getControlState()) {
            case COMM_DISCONNECTED:
                this.updateConnectionControlsStateOpen(false);
                this.updateManualControls(false);
                this.updateWorkflowControls(false);
                this.updateCustomGcodeControls(false);
                this.setStatusColorForState("");
                break;
            case COMM_IDLE:
                this.updateConnectionControlsStateOpen(true);
                this.updateManualControls(true);
                this.updateWorkflowControls(true);
                this.updateCustomGcodeControls(true);
                break;
            case COMM_SENDING:
                // Workflow tab
                this.updateWorkflowControls(false);
                this.updateCustomGcodeControls(false);
                // Jogging commands
                this.updateManualControls(false);
        
                break;
            case COMM_SENDING_PAUSED:

                break;
            default:
                
        }
    }
    
    /**
     * Enable/disable connection frame based on connection state.
     */
    private void updateConnectionControlsStateOpen(boolean isOpen) {

        this.commPortComboBox.setEnabled(!isOpen);
        this.baudrateSelectionComboBox.setEnabled(!isOpen);
        this.refreshButton.setEnabled(!isOpen);
        this.commandTextField.setEnabled(isOpen);

        if (isOpen) {
            this.opencloseButton.setText(Localization.getString("close"));
        } else {
            this.opencloseButton.setText(Localization.getString("open"));
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
    
    private void updateCustomGcodeControls(boolean enabled) {
        for(JButton button : customGcodeButtons) {
            button.setEnabled(enabled);
        }
    }

    private void customGcodeButtonActionPerformed(java.awt.event.ActionEvent evt) {
        //This is probably totally wrong.  Need to get the button out of the event, and from there figure out the macro.
        Macro macro = settings.getMacro(Integer.parseInt(evt.getActionCommand()));
        executeCustomGcode(macro.getGcode());
    }

    private void resetTimerLabels() {
        // Reset labels
        this.durationValueLabel.setText("00:00:00");
        if (this.backend.getSendDuration() < 0) {
            this.remainingTimeValueLabel.setText("estimating...");
        } else if (this.backend.getSendDuration() == 0) {
            this.remainingTimeValueLabel.setText("--:--:--");
        } else {
            this.remainingTimeValueLabel.setText(Utils.formattedMillis(this.backend.getSendDuration()));
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
        this.arrowMovementEnabled.setText(Localization.getString("mainWindow.swing.arrowMovementEnabled"));
        this.baudLabel.setText(Localization.getString("mainWindow.swing.baudLabel"));
        this.browseButton.setText(Localization.getString("mainWindow.swing.browseButton"));
        this.cancelButton.setText(Localization.getString("mainWindow.swing.cancelButton"));
        this.cancelButton.setToolTipText("cancel fool!");
        this.commandLabel.setText(Localization.getString("mainWindow.swing.commandLabel"));
        this.connectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
                Localization.getString("mainWindow.swing.connectionPanel")));
        this.controlContextTabbedPane.setTitleAt(0, Localization.getString("mainWindow.swing.controlContextTabbedPane.commands"));
        this.controlContextTabbedPane.setTitleAt(1, Localization.getString("mainWindow.swing.controlContextTabbedPane.fileMode"));
        this.controlContextTabbedPane.setTitleAt(2, Localization.getString("mainWindow.swing.controlContextTabbedPane.machineControl"));
        this.controlContextTabbedPane.setTitleAt(3, Localization.getString("mainWindow.swing.controlContextTabbedPane.macros"));
        this.durationLabel.setText(Localization.getString("mainWindow.swing.durationLabel"));
        this.fileLabel.setText(Localization.getString("mainWindow.swing.fileLabel"));
        this.firmwareLabel.setText(Localization.getString("mainWindow.swing.firmwareLabel"));
        this.firmwareSettingsMenu.setText(Localization.getString("mainWindow.swing.firmwareSettingsMenu"));
        this.grblConnectionSettingsMenuItem.setText(Localization.getString("mainWindow.swing.grblConnectionSettingsMenuItem"));
        this.grblFirmwareSettingsMenuItem.setText(Localization.getString("mainWindow.swing.grblFirmwareSettingsMenuItem"));
        this.helpButtonMachineControl.setText(Localization.getString("help"));
        this.settingsMenu.setText(Localization.getString("mainWindow.swing.settingsMenu"));
        this.statusPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
                Localization.getString("mainWindow.swing.statusPanel")));
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
        this.softResetMachineControl.setText(Localization.getString("mainWindow.swing.softResetMachineControl"));
        this.stepSizeLabel.setText(Localization.getString("mainWindow.swing.stepSizeLabel"));
        this.visualizeButton.setText(Localization.getString("mainWindow.swing.visualizeButton"));
        this.workPositionLabel.setText(Localization.getString("mainWindow.swing.workPositionLabel"));
        this.macroInstructions.setText(Localization.getString("mainWindow.swing.macroInstructions"));
        this.inchRadioButton.setText(Localization.getString("mainWindow.swing.inchRadioButton"));
        this.mmRadioButton.setText(Localization.getString("mainWindow.swing.mmRadioButton"));
    }
    
    // Scans for comm ports and puts them in the comm port combo box.
    private void loadPortSelector() {
        commPortComboBox.removeAllItems();
        
        String[] portList = CommUtils.getSerialPortList();

        if (portList.length < 1) {
            if (settings.isShowSerialPortWarning()) {
                MainWindow.displayErrorDialog(Localization.getString("mainWindow.error.noSerialPort"));
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
            MainWindow.displayErrorDialog(Localization.getString("mainWindow.error.noFirmware"));
        } else {
            java.util.Iterator<String> iter = firmwareList.iterator();
            while ( iter.hasNext() ) {
                firmwareComboBox.addItem(iter.next());
            }
        }
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
    
    void clearTable() {
        this.commandTable.clear();
    }
        
    private static void displayErrorDialog(final String errorMessage) {
        java.awt.EventQueue.invokeLater(new Runnable() { @Override public void run() {
            JOptionPane.showMessageDialog(new JFrame(), errorMessage, 
                    Localization.getString("error"), JOptionPane.ERROR_MESSAGE);
        }});
    }
    
    /** 
     * SerialCommunicatorListener implementation.
     */
    
    @Override
    public void fileStreamComplete(String filename, boolean success) {
        // Stop the timer
        this.timer.stop();
        remainingTimeValueLabel.setText(Utils.formattedMillis(0));
        remainingRowsValueLabel.setText("" + backend.getNumRemainingRows());

        final String durationLabelCopy = this.durationValueLabel.getText();
        if (success) {
            java.awt.EventQueue.invokeLater(new Runnable() { @Override public void run() {
                JOptionPane.showMessageDialog(new JFrame(),
                        Localization.getString("mainWindow.ui.jobComplete") + " " + durationLabelCopy,
                        Localization.getString("success"), JOptionPane.INFORMATION_MESSAGE);
            }});
        } else {
            displayErrorDialog(Localization.getString("mainWindow.error.jobComplete"));
        }
    }
    
    @Override
    public void commandQueued(GcodeCommand command) {
        this.commandTable.addRow(command);
    }
     
    @Override
    public void commandSent(final GcodeCommand command) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                // sent
                commandTable.updateRow(command);
            }});
    }
    
    @Override
    public void commandComment(String comment) {
        latestCommentValueLabel.setText(comment);
    }
    
    @Override
    public void commandComplete(final GcodeCommand command) {
        //String gcodeString = command.getCommandString().toLowerCase();
        
        // update gui
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                commandTable.updateRow(command);
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
    public void statusStringListener(String state, Point3d machineCoord, Point3d workCoord) {
        this.activeStateValueLabel.setText( state );
        this.setStatusColorForState( state );
        
        if (machineCoord != null) {
            this.machinePositionXValueLabel.setText( Utils.formatter.format(machineCoord.x) + "" );
            this.machinePositionYValueLabel.setText( Utils.formatter.format(machineCoord.y) + "" );
            this.machinePositionZValueLabel.setText( Utils.formatter.format(machineCoord.z) + "" );
        }
        
        if (workCoord != null) {
            this.workPositionXValueLabel.setText( Utils.formatter.format(workCoord.x) + "" );
            this.workPositionYValueLabel.setText( Utils.formatter.format(workCoord.y) + "" );
            this.workPositionZValueLabel.setText( Utils.formatter.format(workCoord.z) + "" );
        }
    }
    
    @Override
    public void postProcessData(int numRows) {
    }
    
    
    @Override
    public void ControlStateEvent(ControlStateEvent evt) {
        switch (evt.getEventType()) {
            case STATE_CHANGED:
            case FILE_CHANGED:
                this.updateControls();
        }
    }

    // Generated variables.
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu PendantMenu;
    private javax.swing.JLabel activeStateLabel;
    private javax.swing.JLabel activeStateValueLabel;
    private javax.swing.JCheckBox arrowMovementEnabled;
    private javax.swing.JLabel baudLabel;
    private javax.swing.JComboBox baudrateSelectionComboBox;
    private javax.swing.JTabbedPane bottomTabbedPane;
    private javax.swing.JButton browseButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JComboBox commPortComboBox;
    private javax.swing.JLabel commandLabel;
    private com.willwinder.universalgcodesender.uielements.GcodeTable commandTable;
    private javax.swing.JTextField commandTextField;
    private javax.swing.JPanel commandsPanel;
    private javax.swing.JPanel connectionPanel;
    private javax.swing.JTextArea consoleTextArea;
    private javax.swing.JTabbedPane controlContextTabbedPane;
    private List<javax.swing.JButton> customGcodeButtons = new ArrayList<JButton>();
    private List<javax.swing.JTextField> customGcodeTextFields = new ArrayList<JTextField>();
    private javax.swing.JLabel durationLabel;
    private javax.swing.JLabel durationValueLabel;
    private javax.swing.JLabel fileLabel;
    private javax.swing.JPanel fileModePanel;
    private javax.swing.JPanel fileRunPanel;
    private javax.swing.JTextField fileTextField;
    private javax.swing.JComboBox firmwareComboBox;
    private javax.swing.JLabel firmwareLabel;
    private javax.swing.JMenu firmwareSettingsMenu;
    private javax.swing.JMenuItem grblConnectionSettingsMenuItem;
    private javax.swing.JMenuItem grblFirmwareSettingsMenuItem;
    private javax.swing.JButton helpButtonMachineControl;
    private javax.swing.JRadioButton inchRadioButton;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.ButtonGroup jogUnitsGroup;
    private javax.swing.JPanel keyboardMovementPanel;
    private javax.swing.JButton killAlarmLock;
    private javax.swing.JLabel latestCommentLabel;
    private javax.swing.JLabel latestCommentValueLabel;
    private javax.swing.ButtonGroup lineBreakGroup;
    private javax.swing.JPanel machineControlPanel;
    private javax.swing.JLabel machinePosition;
    private javax.swing.JLabel machinePositionXLabel;
    private javax.swing.JLabel machinePositionXValueLabel;
    private javax.swing.JLabel machinePositionYLabel;
    private javax.swing.JLabel machinePositionYValueLabel;
    private javax.swing.JLabel machinePositionZLabel;
    private javax.swing.JLabel machinePositionZValueLabel;
    private javax.swing.JLabel macroInstructions;
    private javax.swing.JPanel macroPanel;
    private javax.swing.JScrollPane macroScrollPane;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JRadioButton mmRadioButton;
    private javax.swing.JPanel movementButtonPanel;
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
    private javax.swing.JCheckBox showVerboseOutputCheckBox;
    private javax.swing.JButton softResetMachineControl;
    private javax.swing.JMenuItem startPendantServerButton;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JLabel stepSizeLabel;
    private javax.swing.JSpinner stepSizeSpinner;
    private javax.swing.JMenuItem stopPendantServerButton;
    private javax.swing.JButton toggleCheckMode;
    private javax.swing.JButton visualizeButton;
    private javax.swing.JLabel workPositionLabel;
    private javax.swing.JLabel workPositionXLabel;
    private javax.swing.JLabel workPositionXValueLabel;
    private javax.swing.JLabel workPositionYLabel;
    private javax.swing.JLabel workPositionYValueLabel;
    private javax.swing.JLabel workPositionZLabel;
    private javax.swing.JLabel workPositionZValueLabel;
    private javax.swing.JButton xMinusButton;
    private javax.swing.JButton xPlusButton;
    private javax.swing.JButton yMinusButton;
    private javax.swing.JButton yPlusButton;
    private javax.swing.JButton zMinusButton;
    private javax.swing.JButton zPlusButton;
    // End of variables declaration//GEN-END:variables

}
