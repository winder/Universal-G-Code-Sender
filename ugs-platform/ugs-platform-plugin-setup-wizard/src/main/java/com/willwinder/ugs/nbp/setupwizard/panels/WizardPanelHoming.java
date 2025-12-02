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
import com.willwinder.universalgcodesender.Capabilities;
import com.willwinder.universalgcodesender.CapabilitiesConstants;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Motor;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.FirmwareSettingEvent;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.DecimalFormat;
import net.miginfocom.swing.MigLayout;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.ImageUtilities;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * A wizard step panel for configuring homing on a controller
 *
 * @author Joacim Breiler
 */
public class WizardPanelHoming extends AbstractWizardPanel implements UGSEventListener {
    private JCheckBox checkboxEnableHoming;
    private JLabel labelHomingNotSupported;
    private JLabel labelHardLimitsNotEnabled;
    private JLabel labelDescription;
    private JLabel labelHomingDirection;
    private JLabel labelHomingMposMM;
    private JLabel labelHomingPullOffMM;
    private JLabel labelHomingInstructions;
    private JComboBox<String> comboBoxInvertDirectionX;
    private JComboBox<String> comboBoxInvertDirectionY;
    private JComboBox<String> comboBoxInvertDirectionZ;
    private JButton buttonHomeMachine;
    private JButton buttonAbort;
    private JSeparator separatorBottom;
    private JSeparator separatorMiddle;
    private JSeparator separatorMiddleNext;
    
    private JSeparator separatorTop;
    private JPanel pnlMposMMX;
    private JPanel pnlMposMMY;
    private JPanel pnlMposMMZ;
    private JTextField textfieldMposMMX;
    private JTextField textfieldMposMMY;
    private JTextField textfieldMposMMZ;
    
    private JPanel pnlPullOffMMX0;
    private JPanel pnlPullOffMMY0;
    private JPanel pnlPullOffMMZ0;
    private JTextField textfieldPullOffMMX0;
    private JTextField textfieldPullOffMMY0;
    private JTextField textfieldPullOffMMZ0;
    
    private JPanel pnlPullOffMMX1;
    private JPanel pnlPullOffMMY1;
    private JPanel pnlPullOffMMZ1;
    private JTextField textfieldPullOffMMX1;
    private JTextField textfieldPullOffMMY1;
    private JTextField textfieldPullOffMMZ1;
    
    private final DecimalFormat mPosMMDecimalFormat;
    private JButton btnApplyMPos;
    private JButton btnResetMPos;
    private JPanel pnlMPosButtons;
    private JPanel pnlSpacer;
    
    private JButton btnApplyPulloffMM;
    private JButton btnResetPulloffMM;
    private JPanel pnlPulloffMMSpacer;
    private JPanel pnlPulloffMMSpacerB;
    private JPanel pnlPulloffMMSpacerC;

    private JPanel pnlPulloffMMButtons;
        
    public WizardPanelHoming(BackendAPI backend) {
        super(backend, Localization.getString("platform.plugin.setupwizard.homing.title"));
        mPosMMDecimalFormat = new DecimalFormat("######0.000", Localization.dfs);
        initComponents();
        initLayout();
    }
    private JPanel makeQuickPanel(String aLabel, JTextField aTextField) {
        JPanel result = new JPanel();
        result.setLayout(new BorderLayout());
        result.add(new JLabel(aLabel),BorderLayout.WEST);
        result.add(aTextField,BorderLayout.CENTER);
        
        result.add(new JLabel("mm  "),BorderLayout.EAST);        
        return result;
    }
    private void initLayout() {
        JPanel panel = new JPanel(new MigLayout("wrap 4, fillx, inset 0, gap 0, hidemode 3"));
        panel.add(labelDescription, "gapbottom 10, spanx");
        panel.add(checkboxEnableHoming, "spanx");
        panel.add(labelHardLimitsNotEnabled, "spanx");
        panel.add(labelHomingNotSupported, "spanx");

        panel.add(separatorTop, "spanx, hmin 10, gaptop 10, grow");

        panel.add(labelHomingDirection, "spanx, gaptop 10, gapbottom 10");
        panel.add(comboBoxInvertDirectionX, "wmin 130, gapleft 4, gapright 4");
        panel.add(comboBoxInvertDirectionY, "wmin 130, gapleft 4, gapright 4");
        panel.add(comboBoxInvertDirectionZ, "wmin 130, gapleft 4, gapright 4");
        panel.add(pnlSpacer, "wmin 10");
        
        panel.add(separatorMiddle, "spanx, hmin 10, gaptop 4, grow");        
        
        panel.add(labelHomingMposMM, "spanx, gaptop 4, gapbottom 4");
        panel.add(pnlMposMMX, "wmin 130");
        panel.add(pnlMposMMY, "wmin 130");
        panel.add(pnlMposMMZ, "wmin 130");
        panel.add(pnlMPosButtons, "spanx, wmin 130");       
        
        panel.add(separatorMiddleNext, "spanx, hmin 10, gaptop 4, grow");        
        panel.add(labelHomingPullOffMM, "spanx, gaptop 4, gapbottom 4");
        panel.add(pnlPullOffMMX0, "wmin 130");
        panel.add(pnlPullOffMMX1, "wmin 130");
        panel.add(pnlPulloffMMSpacerB, "spanx, wmin 10");
        
        panel.add(pnlPullOffMMY0, "wmin 130");
        panel.add(pnlPullOffMMY1, "wmin 130");
        panel.add(pnlPulloffMMSpacerC, "spanx, wmin 10");
        
        panel.add(pnlPullOffMMZ0, "wmin 130");
        panel.add(pnlPullOffMMZ1, "wmin 130");        
        panel.add(pnlPulloffMMButtons, "wmin 150");        
        panel.add(pnlPulloffMMSpacer, "spanx, wmin 10");

        panel.add(separatorBottom, "spanx, hmin 10, gaptop 10, grow");                
        panel.add(labelHomingInstructions, "spanx, gaptop 10, gapbottom 10");
        panel.add(buttonHomeMachine, "wmin 130, hmin 32, gapleft 20, gapright 20");
        panel.add(buttonAbort, "wmin 130, hmin 32");        
        getPanel().add(panel, "grow");        
        setValid(true);
    }

    private void initComponents() {
        labelDescription = new JLabel("<html><body><p>" +
                Localization.getString("platform.plugin.setupwizard.homing.intro") +
                "</p></body></html>");

        checkboxEnableHoming = new JCheckBox(Localization.getString("platform.plugin.setupwizard.homing.enable"));
        checkboxEnableHoming.addActionListener(event -> {
            try {
                getBackend().getController().getFirmwareSettings().setHomingEnabled(checkboxEnableHoming.isSelected());
            } catch (FirmwareSettingsException e) {
                NotifyDescriptor nd = new NotifyDescriptor.Message("Couldn't enable/disable the hard limits settings: " + e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
        });

        labelHardLimitsNotEnabled = new JLabel("<html><body>" + Localization.getString("platform.plugin.setupwizard.homing.require-limit-switches") + "</body></html>", ImageUtilities.loadImageIcon("icons/information24.png", false), JLabel.LEFT);
        labelHardLimitsNotEnabled.setVisible(false);

        labelHomingNotSupported = new JLabel("<html><body>" + Localization.getString("platform.plugin.setupwizard.homing.not-available") + "</body></html>", ImageUtilities.loadImageIcon("icons/information24.png", false), JLabel.LEFT);
        labelHomingNotSupported.setVisible(false);

        separatorTop = new JSeparator(SwingConstants.HORIZONTAL);
        separatorTop.setVisible(false);
        separatorMiddle = new JSeparator(SwingConstants.HORIZONTAL);
        separatorMiddle.setVisible(false);
        
        separatorMiddleNext = new JSeparator(SwingConstants.HORIZONTAL);
        separatorMiddleNext.setVisible(false);
        
                
        labelHomingDirection = new JLabel("<html><body>" + Localization.getString("platform.plugin.setupwizard.homing.instruction1") + "</body></html>");
        labelHomingDirection.setVisible(false);
        initInvertComboBoxes();
        
        labelHomingMposMM = new JLabel("<html><body>" + Localization.getString("platform.plugin.setupwizard.homing.advanced1") + "</body></html>");
        labelHomingMposMM.setVisible(false);

        labelHomingPullOffMM = new JLabel("<html><body>" + Localization.getString("platform.plugin.setupwizard.homing.advanced2") + "</body></html>");
        labelHomingPullOffMM.setVisible(false);
                
        textfieldMposMMX = new JTextField("0.00");
        textfieldMposMMY = new JTextField("0.00");
        textfieldMposMMZ = new JTextField("0.00");
        textfieldMposMMX.addKeyListener(createMPosMMKeyListener());
        textfieldMposMMY.addKeyListener(createMPosMMKeyListener());
        textfieldMposMMZ.addKeyListener(createMPosMMKeyListener());
        
        btnApplyMPos = new JButton("Apply");
        btnApplyMPos.setEnabled(false);
        btnApplyMPos.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMachinePosSettings();
            }
        });
        btnResetMPos = new JButton("Reset");
        btnResetMPos.setEnabled(false);
        btnResetMPos.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                resetMachinePosSettings();
            }
        });                
        pnlMPosButtons = new JPanel();
        pnlMPosButtons.setLayout(new BorderLayout());
        pnlMPosButtons.add(btnApplyMPos, BorderLayout.WEST);
        pnlMPosButtons.add(new JPanel(), BorderLayout.CENTER);        
        pnlMPosButtons.add(btnResetMPos, BorderLayout.EAST);
        
        ///
        btnApplyPulloffMM = new JButton("Apply");
        btnApplyPulloffMM.setEnabled(false);
        btnApplyPulloffMM.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMotorPulloffSettings();
            }
        });
        btnResetPulloffMM = new JButton("Reset");
        btnResetPulloffMM.setEnabled(false);
        btnResetPulloffMM.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                resetMotorPulloffSettings();
            }
        });                
        pnlPulloffMMButtons = new JPanel();
        pnlPulloffMMButtons.setLayout(new BorderLayout());
        pnlPulloffMMButtons.add(btnApplyPulloffMM, BorderLayout.WEST);
        pnlPulloffMMButtons.add(new JPanel(), BorderLayout.CENTER);
        pnlPulloffMMButtons.add(btnResetPulloffMM, BorderLayout.EAST);
        pnlPulloffMMSpacer = new JPanel();
        pnlPulloffMMSpacerB = new JPanel();
        pnlPulloffMMSpacerC = new JPanel();
        /// 
        
        pnlMposMMX = makeQuickPanel("X:",textfieldMposMMX);
        pnlMposMMX.setVisible(false);        
        pnlMposMMY = makeQuickPanel("Y:",textfieldMposMMY);
        pnlMposMMY.setVisible(false);
        pnlMposMMZ = makeQuickPanel("Z:",textfieldMposMMZ);
        pnlMposMMZ.setVisible(false);
///
        textfieldPullOffMMX0 = new JTextField("0.00");
        textfieldPullOffMMX1 = new JTextField("0.00");
        textfieldPullOffMMY0 = new JTextField("0.00");
        textfieldPullOffMMY1 = new JTextField("0.00");
        textfieldPullOffMMZ0 = new JTextField("0.00");
        textfieldPullOffMMZ1 = new JTextField("0.00");
        pnlPullOffMMX0 = makeQuickPanel("X0:", textfieldPullOffMMX0);
        pnlPullOffMMX1 = makeQuickPanel("X1:", textfieldPullOffMMX1);
        pnlPullOffMMY0 = makeQuickPanel("Y0:", textfieldPullOffMMY0);
        pnlPullOffMMY1 = makeQuickPanel("Y1:", textfieldPullOffMMY1);
        pnlPullOffMMZ0 = makeQuickPanel("Z0:", textfieldPullOffMMZ0);
        pnlPullOffMMZ1 = makeQuickPanel("Z1:", textfieldPullOffMMZ1);
        textfieldPullOffMMX0.addKeyListener(createKeyMotorPulloffListener());
        textfieldPullOffMMX1.addKeyListener(createKeyMotorPulloffListener());
        textfieldPullOffMMY0.addKeyListener(createKeyMotorPulloffListener());
        textfieldPullOffMMY1.addKeyListener(createKeyMotorPulloffListener());
        textfieldPullOffMMZ0.addKeyListener(createKeyMotorPulloffListener());
        textfieldPullOffMMZ1.addKeyListener(createKeyMotorPulloffListener());
        ///
        
//Sets the machine position after homing and limit switch pull-off in millimeters. If you want the machine position to be zero at the limit switch, set this to zero. Keep in mind the homing direction you choose this number.
        separatorBottom = new JSeparator(SwingConstants.HORIZONTAL);
        separatorBottom.setVisible(false);

        labelHomingInstructions = new JLabel("<html><body><p>" +
                Localization.getString("platform.plugin.setupwizard.homing.instruction2") +
                "</p></body></html>");
        labelHomingInstructions.setVisible(false);
        initButtons();
    }
    private KeyListener createMPosMMKeyListener() {
        return new KeyAdapter() {
            
            @Override
            public void keyReleased(KeyEvent e) {
                updateMachinePositionTextfields();
            }
        };
    }
    private KeyListener createKeyMotorPulloffListener() {
        return new KeyAdapter() {
            
            @Override
            public void keyReleased(KeyEvent e) {
                updateMotorPulloffTextfields();
            }
        };
    }
        
    private void updateMachinePositionTextfields() {
        double xVal = 0;
        double yVal = 0;
        double zVal = 0;
        boolean canApply = true;
        boolean canReset = false;
        textfieldMposMMX.setBackground(Color.white);
        textfieldMposMMY.setBackground(Color.white);
        textfieldMposMMZ.setBackground(Color.white);

        try {
            xVal = Double.parseDouble(textfieldMposMMX.getText());
            textfieldMposMMX.setForeground(Color.black);
        } catch (NumberFormatException ex) {
            textfieldMposMMX.setForeground(Color.red);
            canApply = false;
            canReset = true;
        }
        try {
            yVal = Double.parseDouble(textfieldMposMMY.getText());
            textfieldMposMMY.setForeground(Color.black);
        } catch (NumberFormatException ex) {
            textfieldMposMMY.setForeground(Color.red);
            canApply = false;
            canReset = true;
        }        
        try {
            zVal = Double.parseDouble(textfieldMposMMZ.getText());
            textfieldMposMMZ.setForeground(Color.black);                    
        } catch (NumberFormatException ex) {
            textfieldMposMMZ.setForeground(Color.red);
            canApply = false;
            canReset = true;
        }  
        
        if (canApply) {
            IFirmwareSettings firmwareSettings = getBackend().getController().getFirmwareSettings();
            try {
                boolean shouldApply = false;
                if ( xVal != firmwareSettings.getMposMM(Axis.X) ) {
                    shouldApply = true;
                    textfieldMposMMX.setBackground(Color.yellow);
                } 
                if ( yVal != firmwareSettings.getMposMM(Axis.Y)) {
                    shouldApply = true;
                    textfieldMposMMY.setBackground(Color.yellow);
                } 
                if ( zVal != firmwareSettings.getMposMM(Axis.Z)) {
                    shouldApply = true;
                    textfieldMposMMZ.setBackground(Color.yellow);
                } 
                if (!shouldApply) {
                    canApply = false;
                } else {
                    canReset = true;
                }
                
            } catch ( Exception ex) {
                ex.printStackTrace();
            }

        }
        btnApplyMPos.setEnabled(canApply);
        btnResetMPos.setEnabled(canReset);
    }
    private double parseAndColor(JTextField aTextfield) {
        double result = 0;
        aTextfield.setBackground(Color.white);
        try {
            result = Double.parseDouble(aTextfield.getText());
            aTextfield.setForeground(Color.black);
        } catch (NumberFormatException ex) {
            aTextfield.setForeground(Color.red);
            result = Double.NaN;
        }
        return result;
    }
    private void updateMotorPulloffTextfields() {
        double xVal0;
        double yVal0;
        double zVal0;
        double xVal1;
        double yVal1;
        double zVal1;        
        boolean canApply = true;
        boolean canReset = false;
                
        if ( Double.isNaN(xVal0 = parseAndColor(textfieldPullOffMMX0))) {
            canApply = false;
            canReset = true;         
        }
        if ( Double.isNaN(xVal1 = parseAndColor(textfieldPullOffMMX1))) {
            canApply = false;
            canReset = true;         
        }
        if ( Double.isNaN(yVal0 = parseAndColor(textfieldPullOffMMY0))) {
            canApply = false;
            canReset = true;         
        }
        if ( Double.isNaN(yVal1 = parseAndColor(textfieldPullOffMMY1))) {
            canApply = false;
            canReset = true;         
        }
        if ( Double.isNaN(zVal0 = parseAndColor(textfieldPullOffMMZ0))) {
            canApply = false;
            canReset = true;         
        }
        if ( Double.isNaN(zVal1 = parseAndColor(textfieldPullOffMMZ1))) {
            canApply = false;
            canReset = true;         
        }
        
        if (canApply) {
            IFirmwareSettings firmwareSettings = getBackend().getController().getFirmwareSettings();
            try {
                boolean shouldApply = false;
                if ( xVal0 != firmwareSettings.getPulloffMM(Axis.X, Motor.M0) ) {
                    shouldApply = true;
                    textfieldPullOffMMX0.setBackground(Color.yellow);
                } 
                if ( xVal1 != firmwareSettings.getPulloffMM(Axis.X, Motor.M1) ) {
                    shouldApply = true;
                    textfieldPullOffMMX1.setBackground(Color.yellow);
                }                 
                if ( yVal0 != firmwareSettings.getPulloffMM(Axis.Y, Motor.M0) ) {
                    shouldApply = true;
                    textfieldPullOffMMY0.setBackground(Color.yellow);
                } 
                if ( yVal1 != firmwareSettings.getPulloffMM(Axis.Y, Motor.M1) ) {
                    shouldApply = true;
                    textfieldPullOffMMY1.setBackground(Color.yellow);
                }                 
                if ( zVal0 != firmwareSettings.getPulloffMM(Axis.Z, Motor.M0) ) {
                    shouldApply = true;
                    textfieldPullOffMMZ0.setBackground(Color.yellow);
                } 
                if ( zVal1 != firmwareSettings.getPulloffMM(Axis.Z, Motor.M1) ) {
                    shouldApply = true;
                    textfieldPullOffMMZ1.setBackground(Color.yellow);
                }                 

                if (!shouldApply) {
                    canApply = false;
                } else {
                    canReset = true;
                }
                
            } catch ( Exception ex) {
                ex.printStackTrace();
            }

        }
        btnApplyPulloffMM.setEnabled(canApply);
        btnResetPulloffMM.setEnabled(canReset);
    }    
    private void resetMachinePosSettings() {
        try {
            getBackend().getController().getFirmwareSettings().refreshFirmwareSettings(); 
            IFirmwareSettings firmwareSettings = getBackend().getController().getFirmwareSettings();
            textfieldMposMMX.setText(mPosMMDecimalFormat.format(firmwareSettings.getMposMM(Axis.X)));
            textfieldMposMMY.setText(mPosMMDecimalFormat.format(firmwareSettings.getMposMM(Axis.Y)));
            textfieldMposMMZ.setText(mPosMMDecimalFormat.format(firmwareSettings.getMposMM(Axis.Z)));
        } catch (Exception e) {
             e.printStackTrace();
        }
        updateMachinePositionTextfields();
        
    }
    private void sendMachinePosSettings() {
        double xVal = 0;
        double yVal = 0;
        double zVal = 0;
        boolean canApply = true;
        
        try {
            xVal = Double.parseDouble(textfieldMposMMX.getText());
            yVal = Double.parseDouble(textfieldMposMMY.getText());
            zVal = Double.parseDouble(textfieldMposMMZ.getText());        
        } catch (NumberFormatException ex) {
            canApply = false;
        }
        if (canApply) {
            try {
                getBackend().getController().getFirmwareSettings().setMposMM(Axis.X, xVal);
                getBackend().getController().getFirmwareSettings().setMposMM(Axis.Y, yVal);
                getBackend().getController().getFirmwareSettings().setMposMM(Axis.Z, zVal);
                getBackend().getController().getFirmwareSettings().refreshFirmwareSettings();                
            } catch (Exception e) {
                
            }
            
        }
        updateMachinePositionTextfields();
    }
    private void sendMotorPulloffSettings() {
        double xVal0=0;
        double yVal0=0;
        double zVal0=0;
        double xVal1=0;
        double yVal1=0;
        double zVal1=0; 
        boolean canApply = true;
        try {
            xVal0 = Double.parseDouble(textfieldPullOffMMX0.getText());
            xVal1 = Double.parseDouble(textfieldPullOffMMX1.getText());            
            yVal0 = Double.parseDouble(textfieldPullOffMMY0.getText());
            yVal1 = Double.parseDouble(textfieldPullOffMMY1.getText());            
            zVal0 = Double.parseDouble(textfieldPullOffMMZ0.getText());
            zVal1 = Double.parseDouble(textfieldPullOffMMZ1.getText());            
            
        } catch (NumberFormatException ex) {
            canApply = false;
        }
        if (canApply) {
            try {
                getBackend().getController().getFirmwareSettings().setPulloffMM(Axis.X, Motor.M0, xVal0);
                getBackend().getController().getFirmwareSettings().setPulloffMM(Axis.X, Motor.M1, xVal1);
                getBackend().getController().getFirmwareSettings().setPulloffMM(Axis.Y, Motor.M0, yVal0);
                getBackend().getController().getFirmwareSettings().setPulloffMM(Axis.Y, Motor.M1, yVal1);
                getBackend().getController().getFirmwareSettings().setPulloffMM(Axis.Z, Motor.M0, zVal0);
                getBackend().getController().getFirmwareSettings().setPulloffMM(Axis.Z, Motor.M1, zVal1);
                
                getBackend().getController().getFirmwareSettings().refreshFirmwareSettings();                
            } catch (Exception e) {
                
            }            
        }
    }
    private void resetMotorPulloffSettings() {
        try {
            getBackend().getController().getFirmwareSettings().refreshFirmwareSettings(); 
            IFirmwareSettings firmwareSettings = getBackend().getController().getFirmwareSettings();
            textfieldPullOffMMX0.setText(mPosMMDecimalFormat.format(firmwareSettings.getPulloffMM(Axis.X,Motor.M0)));
            textfieldPullOffMMX1.setText(mPosMMDecimalFormat.format(firmwareSettings.getPulloffMM(Axis.X,Motor.M1)));
            textfieldPullOffMMY0.setText(mPosMMDecimalFormat.format(firmwareSettings.getPulloffMM(Axis.Y,Motor.M0)));
            textfieldPullOffMMY1.setText(mPosMMDecimalFormat.format(firmwareSettings.getPulloffMM(Axis.Y,Motor.M1)));
            textfieldPullOffMMZ0.setText(mPosMMDecimalFormat.format(firmwareSettings.getPulloffMM(Axis.Z,Motor.M0)));
            textfieldPullOffMMZ1.setText(mPosMMDecimalFormat.format(firmwareSettings.getPulloffMM(Axis.Z,Motor.M1)));
                        
        } catch (Exception e) {
             e.printStackTrace();
        }
        updateMotorPulloffTextfields();        
    }
    private void initButtons() {
        buttonHomeMachine = new JButton(Localization.getString("platform.plugin.setupwizard.homing.try-homing"));
        buttonHomeMachine.setVisible(false);
        buttonHomeMachine.addActionListener(event -> {
            try {
                getBackend().performHomingCycle();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        buttonAbort = new JButton(Localization.getString("platform.plugin.setupwizard.homing.abort"));
        buttonAbort.setVisible(false);
        buttonAbort.addActionListener(event -> {
            try {
                getBackend().cancel();
                getBackend().issueSoftReset();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void initInvertComboBoxes() {
        comboBoxInvertDirectionX = createInvertComboBox(Axis.X);
        comboBoxInvertDirectionY = createInvertComboBox(Axis.Y);
        comboBoxInvertDirectionZ = createInvertComboBox(Axis.Z);
        pnlSpacer = new JPanel();
    }

    private JComboBox<String> createInvertComboBox(Axis axis) {
        JComboBox<String> result = new JComboBox<>();
        result.setVisible(false);
        result.addItem("+" + axis.name());
        result.addItem("-" + axis.name());
        result.addActionListener(event -> {
            IController controller = getBackend().getController();
            if (controller != null) {
                try {
                    controller.getFirmwareSettings().setHomingDirectionInverted(axis, result.getSelectedIndex() == 1);
                } catch (FirmwareSettingsException e) {
                    NotifyDescriptor nd = new NotifyDescriptor.Message("Unexpected error while updating setting: " + e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                }
            }
        });
        return result;
    }

    @Override
    public void initialize() {
        getBackend().addUGSEventListener(this);
        refreshControls();
    }

    private void refreshControls() {
        ThreadHelper.invokeLater(() -> {
                    try {
                        if (getBackend().getController() != null &&
                                getBackend().getController().getCapabilities().hasHoming() &&
                                getBackend().getController().getCapabilities().hasHardLimits() &&
                                getBackend().getController().getFirmwareSettings().isHardLimitsEnabled()) {

                            IFirmwareSettings firmwareSettings = getBackend().getController().getFirmwareSettings();
                            Capabilities capabilties = getBackend().getController().getCapabilities();
                            
                            checkboxEnableHoming.setSelected(firmwareSettings.isHomingEnabled());
                            checkboxEnableHoming.setVisible(true);

                            labelHomingDirection.setVisible(firmwareSettings.isHomingEnabled());
                            comboBoxInvertDirectionX.setVisible(firmwareSettings.isHomingEnabled());
                            comboBoxInvertDirectionX.setSelectedIndex(firmwareSettings.isHomingDirectionInverted(Axis.X) ? 1 : 0);

                            comboBoxInvertDirectionY.setVisible(firmwareSettings.isHomingEnabled());
                            comboBoxInvertDirectionY.setSelectedIndex(firmwareSettings.isHomingDirectionInverted(Axis.Y) ? 1 : 0);

                            comboBoxInvertDirectionZ.setVisible(firmwareSettings.isHomingEnabled());
                            comboBoxInvertDirectionZ.setSelectedIndex(firmwareSettings.isHomingDirectionInverted(Axis.Z) ? 1 : 0);
                            pnlSpacer.setVisible(firmwareSettings.isHomingEnabled());
                            
                            labelHomingMposMM.setVisible(firmwareSettings.isHomingEnabled() && getBackend().getController().getCapabilities().hasAdvancedHoming());
                            pnlMposMMX.setVisible(labelHomingMposMM.isVisible());
                            pnlMposMMY.setVisible(labelHomingMposMM.isVisible());
                            pnlMposMMZ.setVisible(labelHomingMposMM.isVisible());
                            pnlMPosButtons.setVisible(labelHomingMposMM.isVisible());
                            
                            labelHomingPullOffMM.setVisible(labelHomingMposMM.isVisible());
                            
                            textfieldMposMMX.setText(mPosMMDecimalFormat.format(firmwareSettings.getMposMM(Axis.X)));
                            textfieldMposMMY.setText(mPosMMDecimalFormat.format(firmwareSettings.getMposMM(Axis.Y)));
                            textfieldMposMMZ.setText(mPosMMDecimalFormat.format(firmwareSettings.getMposMM(Axis.Z)));
                            
                            textfieldPullOffMMX0.setText(mPosMMDecimalFormat.format(firmwareSettings.getPulloffMM(Axis.X,Motor.M0)));
                            textfieldPullOffMMX1.setText(mPosMMDecimalFormat.format(firmwareSettings.getPulloffMM(Axis.X,Motor.M1)));
                            textfieldPullOffMMY0.setText(mPosMMDecimalFormat.format(firmwareSettings.getPulloffMM(Axis.Y,Motor.M0)));
                            textfieldPullOffMMY1.setText(mPosMMDecimalFormat.format(firmwareSettings.getPulloffMM(Axis.Y,Motor.M1)));
                            textfieldPullOffMMZ0.setText(mPosMMDecimalFormat.format(firmwareSettings.getPulloffMM(Axis.Z,Motor.M0)));
                            textfieldPullOffMMZ1.setText(mPosMMDecimalFormat.format(firmwareSettings.getPulloffMM(Axis.Z,Motor.M1)));                                                                                   
                            
                            pnlPullOffMMX0.setVisible(labelHomingMposMM.isVisible() && capabilties.hasAxis(Axis.X));
                            pnlPullOffMMX1.setVisible(labelHomingMposMM.isVisible() && capabilties.hasCapability(CapabilitiesConstants.DUAL_X_AXIS));
                            pnlPullOffMMY0.setVisible(labelHomingMposMM.isVisible() && capabilties.hasAxis(Axis.Y));
                            pnlPullOffMMY1.setVisible(labelHomingMposMM.isVisible() && capabilties.hasCapability(CapabilitiesConstants.DUAL_Y_AXIS));
                            pnlPullOffMMZ0.setVisible(labelHomingMposMM.isVisible() && capabilties.hasAxis(Axis.Z));
                            pnlPullOffMMZ1.setVisible(labelHomingMposMM.isVisible() && capabilties.hasCapability(CapabilitiesConstants.DUAL_Z_AXIS));
                            
                            pnlPulloffMMButtons.setVisible(labelHomingMposMM.isVisible());  
                            pnlPulloffMMSpacer.setVisible(labelHomingMposMM.isVisible());  
                            pnlPulloffMMSpacerB.setVisible(labelHomingMposMM.isVisible());  
                            pnlPulloffMMSpacerC.setVisible(labelHomingMposMM.isVisible());  
                            labelHomingNotSupported.setVisible(false);
                            labelHardLimitsNotEnabled.setVisible(false);

                            labelHomingInstructions.setVisible(firmwareSettings.isHomingEnabled());
                            buttonHomeMachine.setVisible(firmwareSettings.isHomingEnabled());
                            buttonAbort.setVisible(firmwareSettings.isHomingEnabled());
                            separatorTop.setVisible(firmwareSettings.isHomingEnabled());
                            separatorMiddle.setVisible(firmwareSettings.isHomingEnabled() && getBackend().getController().getCapabilities().hasAdvancedHoming()); 
                            separatorMiddleNext.setVisible(separatorMiddle.isVisible());
                            separatorBottom.setVisible(firmwareSettings.isHomingEnabled());
                        } else if (getBackend().getController() != null &&
                                getBackend().getController().getCapabilities().hasHoming() &&
                                !getBackend().getController().getFirmwareSettings().isHardLimitsEnabled()) {
                            checkboxEnableHoming.setVisible(false);
                            comboBoxInvertDirectionX.setVisible(false);
                            comboBoxInvertDirectionY.setVisible(false);
                            comboBoxInvertDirectionZ.setVisible(false);
                            pnlSpacer.setVisible(false);
                            labelHomingNotSupported.setVisible(false);
                            labelHardLimitsNotEnabled.setVisible(true);
                            labelHomingDirection.setVisible(false);
                            labelHomingMposMM.setVisible(false);
                            labelHomingPullOffMM.setVisible(false);
                            pnlMposMMX.setVisible(false);
                            pnlMposMMY.setVisible(false);
                            pnlMposMMZ.setVisible(false);
                            pnlPullOffMMX0.setVisible(false);
                            pnlPullOffMMX1.setVisible(false);
                            pnlPullOffMMY0.setVisible(false);
                            pnlPullOffMMY1.setVisible(false);
                            pnlPullOffMMZ0.setVisible(false);
                            pnlPullOffMMZ1.setVisible(false); 
                            pnlPulloffMMButtons.setVisible(false);
                            pnlPulloffMMSpacer.setVisible(false);
                            pnlPulloffMMSpacerB.setVisible(false);
                            pnlPulloffMMSpacerC.setVisible(false);
                            labelHomingInstructions.setVisible(false);
                            buttonHomeMachine.setVisible(false);
                            buttonAbort.setVisible(false);
                            separatorTop.setVisible(false);
                            separatorMiddle.setVisible(false);
                            separatorMiddleNext.setVisible(false);
                            separatorBottom.setVisible(false);
                        } else {
                            checkboxEnableHoming.setVisible(false);
                            comboBoxInvertDirectionX.setVisible(false);
                            comboBoxInvertDirectionY.setVisible(false);
                            comboBoxInvertDirectionZ.setVisible(false);
                            pnlSpacer.setVisible(false);
                            labelHomingNotSupported.setVisible(true);
                            labelHardLimitsNotEnabled.setVisible(false);
                            labelHomingDirection.setVisible(false);
                            labelHomingMposMM.setVisible(false);
                            labelHomingPullOffMM.setVisible(false);
                            pnlMposMMX.setVisible(false);
                            pnlMposMMY.setVisible(false);
                            pnlMposMMZ.setVisible(false);        
                            pnlPullOffMMX0.setVisible(false);
                            pnlPullOffMMX1.setVisible(false);
                            pnlPullOffMMY0.setVisible(false);
                            pnlPullOffMMY1.setVisible(false);
                            pnlPullOffMMZ0.setVisible(false);
                            pnlPullOffMMZ1.setVisible(false); 
                            pnlPulloffMMButtons.setVisible(false);  
                            pnlPulloffMMSpacer.setVisible(false);  
                            pnlPulloffMMSpacerB.setVisible(false);
                            pnlPulloffMMSpacerC.setVisible(false);
                            labelHomingInstructions.setVisible(false);
                            buttonHomeMachine.setVisible(false);
                            buttonAbort.setVisible(false);
                            separatorTop.setVisible(false);
                            separatorMiddle.setVisible(false);
                            separatorMiddleNext.setVisible(false);
                            separatorBottom.setVisible(false);
                        }
                    } catch (FirmwareSettingsException e) {
                        NotifyDescriptor nd = new NotifyDescriptor.Message("Couldn't fetch firmware settings: " + e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
                        DialogDisplayer.getDefault().notify(nd);
                    }
                },
                200);
    }

    @Override
    public boolean isEnabled() {
        return getBackend().isConnected() &&
                getBackend().getController().getCapabilities().hasSetupWizardSupport();
    }

    @Override
    public void destroy() {
        getBackend().removeUGSEventListener(this);
    }

    @Override
    public void UGSEvent(UGSEvent event) {
        if (event instanceof FirmwareSettingEvent) {
            refreshControls();
        }
    }
}
