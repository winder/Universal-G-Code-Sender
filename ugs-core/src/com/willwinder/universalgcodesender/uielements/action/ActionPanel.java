package com.willwinder.universalgcodesender.uielements.action;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.uielements.MacroActionPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

import static com.willwinder.universalgcodesender.utils.GUIHelpers.displayErrorDialog;

public class ActionPanel extends JPanel implements UGSEventListener {

    private final BackendAPI backend;

    private final JButton resetCoordinatesButton = new JButton(Localization.getString("mainWindow.swing.resetCoordinatesButton"));
    private final JButton returnToZeroButton = new JButton(Localization.getString("mainWindow.swing.returnToZeroButton"));
    private final JButton softResetMachineControl = new JButton("Soft Reset");
    private final JButton resetXButton = new JButton("Reset X Axis");
    private final JButton resetYButton = new JButton("Reset Y Axis");
    private final JButton resetZButton = new JButton("Reset Z Axis");
    private final JButton performHomingCycleButton = new JButton("$H");
    private final JButton requestStateInformation = new JButton("$G");
    private final JButton killAlarmLock = new JButton("$X");
    private final JButton toggleCheckMode = new JButton("$C");
    private final JButton helpButtonMachineControl = new JButton(Localization.getString("help"));

    private final MacroActionPanel macroActionPanel;

    /**
     * No-Arg constructor to make this control work in the UI builder tools
     * @deprecated Use constructor with BackendAPI.
     */
    @Deprecated
    public ActionPanel() {
        this(null);
    }

    public ActionPanel(BackendAPI backend) {
        this.backend = backend;
        if (backend != null) {
            backend.addUGSEventListener(this);
        }

        macroActionPanel = new MacroActionPanel(backend);

        initComponents();
    }

    private void initComponents() {

        helpButtonMachineControl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpButtonMachineControlActionPerformed(evt);
            }
        });

        resetYButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetYCoordinateButtonActionPerformed(evt);
            }
        });

        softResetMachineControl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                softResetMachineControlActionPerformed(evt);
            }
        });

        requestStateInformation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                requestStateInformationActionPerformed(evt);
            }
        });

        returnToZeroButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                returnToZeroButtonActionPerformed(evt);
            }
        });


        resetCoordinatesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetCoordinatesButtonActionPerformed(evt);
            }
        });

        performHomingCycleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                performHomingCycleButtonActionPerformed(evt);
            }
        });

        killAlarmLock.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                killAlarmLockActionPerformed(evt);
            }
        });

        toggleCheckMode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toggleCheckModeActionPerformed(evt);
            }
        });

        resetXButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetXCoordinateButtonActionPerformed(evt);
            }
        });

        resetZButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetZCoordinateButtonActionPerformed(evt);
            }
        });


        MigLayout layout = new MigLayout("fill, wrap 3", "[fill, sg 1][fill, sg 1][fill, grow]");
        setLayout(layout);

        String constraints = "sg 1";
        add(resetCoordinatesButton, constraints);
        add(resetXButton, constraints);
        add(macroActionPanel, "spany, growy, top, left");
        add(returnToZeroButton, constraints);
        add(resetYButton, constraints);
        add(softResetMachineControl, constraints);
        add(resetZButton, constraints);
        add(performHomingCycleButton, constraints);
        add(killAlarmLock, constraints);
        add(requestStateInformation, constraints);
        add(toggleCheckMode, constraints);
        add(helpButtonMachineControl, constraints+", span 2, right");
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt.isStateChangeEvent()) {
            updateControls();
        }
    }

    private void updateControls() {
        switch (backend.getControlState()) {
            case COMM_DISCONNECTED:
                this.updateWorkflowControls(false);
                break;
            case COMM_IDLE:
                this.updateWorkflowControls(true);
                break;
            case COMM_SENDING:
                this.updateWorkflowControls(false);
                break;
            case COMM_SENDING_PAUSED:
                break;
            default:
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
}
