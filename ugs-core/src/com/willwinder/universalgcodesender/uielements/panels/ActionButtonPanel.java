/*
    Copyright 2016 Will Winder

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
package com.willwinder.universalgcodesender.uielements.panels;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import static com.willwinder.universalgcodesender.utils.GUIHelpers.displayErrorDialog;

public class ActionButtonPanel extends JPanel implements UGSEventListener {

    private final BackendAPI backend;

    private final JButton resetCoordinatesButton = new JButton(Localization.getString("mainWindow.swing.resetCoordinatesButton"));
    private final JButton returnToZeroButton = new JButton(Localization.getString("mainWindow.swing.returnToZeroButton"));
    private final JButton softResetMachineControl = new JButton(Localization.getString("mainWindow.swing.softReset"));
    private final JButton performHomingCycleButton = new JButton(Localization.getString("mainWindow.swing.homeMachine"));
    private final JButton requestStateInformation = new JButton(Localization.getString("mainWindow.swing.getState"));
    private final JButton killAlarmLock = new JButton(Localization.getString("mainWindow.swing.alarmLock"));
    private final JButton toggleCheckMode = new JButton(Localization.getString("mainWindow.swing.checkMode"));
    private final JButton helpButtonMachineControl = new JButton(Localization.getString("help"));

    /**
     * No-Arg constructor to make this control work in the UI builder tools
     *
     * @deprecated Use constructor with BackendAPI.
     */
    @Deprecated
    public ActionButtonPanel() {
        this(null);
    }

    public ActionButtonPanel(BackendAPI backend) {
        this.backend = backend;
        if (backend != null) {
            backend.addUGSEventListener(this);
        }

        initComponents();
        updateControls();
    }

    private void initComponents() {

        helpButtonMachineControl.addActionListener(this::helpButtonMachineControl);

        softResetMachineControl.addActionListener(this::softResetMachineControl);

        requestStateInformation.addActionListener(this::requestStateInformation);

        returnToZeroButton.addActionListener(this::returnToZeroButton);

        resetCoordinatesButton.addActionListener(this::resetCoordinatesButton);

        performHomingCycleButton.addActionListener(this::performHomingCycleButton);

        killAlarmLock.addActionListener(this::killAlarmLock);

        toggleCheckMode.addActionListener(this::toggleCheckMode);

        MigLayout layout = new MigLayout("fill, wrap 2, inset 5, gap 2", "[50%][50%]");
        setLayout(layout);

        String constraints = "grow, wmin 110, hmin 28, gap 0, sg 1";
        add(resetCoordinatesButton, constraints);
        add(returnToZeroButton, constraints);
        add(softResetMachineControl, constraints);
        add(performHomingCycleButton, constraints);
        add(killAlarmLock, constraints);
        add(requestStateInformation, constraints);
        add(toggleCheckMode, constraints + ", wrap");
        add(helpButtonMachineControl, constraints + ", span 2");
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof ControllerStateEvent) {
            updateControls();
        }
    }

    private void updateControls() {
        this.updateWorkflowControls(backend.isIdle());
    }

    private void updateWorkflowControls(boolean enabled) {
        this.resetCoordinatesButton.setEnabled(enabled);

        boolean hasReturnToZero = backend.isConnected() && backend.getController().getCapabilities().hasReturnToZero();
        this.returnToZeroButton.setEnabled(enabled && hasReturnToZero);

        boolean hasHoming = backend.isConnected() && backend.getController().getCapabilities().hasHoming();
        this.performHomingCycleButton.setEnabled(enabled && hasHoming);

        this.softResetMachineControl.setEnabled(enabled);
        this.killAlarmLock.setEnabled(enabled);

        boolean hasCheckMode = backend.isConnected() && backend.getController().getCapabilities().hasCheckMode();
        this.toggleCheckMode.setEnabled(enabled && hasCheckMode);

        this.requestStateInformation.setEnabled(enabled);
    }

    private void killAlarmLock(java.awt.event.ActionEvent evt) {
        try {
            this.backend.killAlarmLock();
        } catch (Exception ex) {
            displayErrorDialog(ex.getMessage());
        }
    }

    private void performHomingCycleButton(java.awt.event.ActionEvent evt) {
        try {
            this.backend.performHomingCycle();
        } catch (Exception ex) {
            displayErrorDialog(ex.getMessage());
        }
    }

    private void resetCoordinatesButton(java.awt.event.ActionEvent evt) {
        try {
            this.backend.resetCoordinatesToZero();
        } catch (Exception ex) {
            displayErrorDialog(ex.getMessage());
        }
    }

    private void toggleCheckMode(java.awt.event.ActionEvent evt) {
        try {
            this.backend.toggleCheckMode();
        } catch (Exception ex) {
            displayErrorDialog(ex.getMessage());
        }
    }

    private void returnToZeroButton(java.awt.event.ActionEvent evt) {
        try {
            backend.returnToZero();
        } catch (Exception ex) {
            displayErrorDialog(ex.getMessage());
        }
    }

    private void requestStateInformation(java.awt.event.ActionEvent evt) {
        try {
            this.backend.requestParserState();
        } catch (Exception ex) {
            displayErrorDialog(ex.getMessage());
        }
    }

    private void softResetMachineControl(java.awt.event.ActionEvent evt) {
        try {
            this.backend.issueSoftReset();
        } catch (Exception ex) {
            displayErrorDialog(ex.getMessage());
        }
    }

    private void helpButtonMachineControl(java.awt.event.ActionEvent evt) {
        StringBuilder message = new StringBuilder()
                .append(Localization.getString("mainWindow.resetZero")).append("\n")
                .append(Localization.getString("mainWindow.returnToZero")).append("\n")
                .append(Localization.getString("mainWindow.softReset")).append("\n")
                .append(Localization.getString("mainWindow.homing")).append("\n")
                .append(Localization.getString("mainWindow.alarmLock")).append("\n")
                .append(Localization.getString("mainWindow.checkMode")).append("\n")
                .append(Localization.getString("mainWindow.getState")).append("\n")
                .append(Localization.getString("mainWindow.helpKeyboard")).append("\n")
                .append(Localization.getString("mainWindow.helpKeyPlusMinus")).append("\n")
                .append(Localization.getString("mainWindow.helpKeyDivMul")).append("\n")
                .append(Localization.getString("mainWindow.helpKeyZero")).append("\n");

        JOptionPane.showMessageDialog(new JFrame(),
                message,
                Localization.getString("mainWindow.helpDialog"),
                JOptionPane.INFORMATION_MESSAGE);
    }
}
