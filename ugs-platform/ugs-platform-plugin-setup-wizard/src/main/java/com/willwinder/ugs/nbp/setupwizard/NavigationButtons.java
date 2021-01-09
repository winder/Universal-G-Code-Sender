/*
    Copyright 2018 Will Winder

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
package com.willwinder.ugs.nbp.setupwizard;

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import javax.swing.JButton;
import java.awt.Dimension;
import java.awt.Insets;

/**
 * Handles the navigation buttons. It will check that the button wouldn't move the machine
 * beyond the machine soft limits, those buttons will be disabled.
 */
public class NavigationButtons {
    private final BackendAPI backendAPI;
    private double stepSize;
    private double feedRate;
    private JButton buttonXpos;
    private JButton buttonXneg;
    private JButton buttonYpos;
    private JButton buttonYneg;
    private JButton buttonZpos;
    private JButton buttonZneg;

    public NavigationButtons(BackendAPI backendAPI, double stepSize, int feedRate) {
        this.backendAPI = backendAPI;
        this.stepSize = stepSize;
        this.feedRate = feedRate;
        initButtons();
    }

    public JButton getButtonXpos() {
        return buttonXpos;
    }

    public JButton getButtonXneg() {
        return buttonXneg;
    }

    public JButton getButtonYpos() {
        return buttonYpos;
    }

    public JButton getButtonYneg() {
        return buttonYneg;
    }

    public JButton getButtonZpos() {
        return buttonZpos;
    }

    public JButton getButtonZneg() {
        return buttonZneg;
    }

    private void initButtons() {
        buttonXneg = createJogButton("X-");
        buttonXneg.addActionListener(event -> moveMachine(-1, 0, 0));

        buttonXpos = createJogButton("X+");
        buttonXpos.addActionListener(event -> moveMachine(1, 0, 0));

        buttonYneg = createJogButton("Y-");
        buttonYneg.addActionListener(event -> moveMachine(0, -1, 0));

        buttonYpos = createJogButton("Y+");
        buttonYpos.addActionListener(event -> moveMachine(0, 1, 0));

        buttonZneg = createJogButton("Z-");
        buttonZneg.addActionListener(event -> moveMachine(0, 0, -1));

        buttonZpos = createJogButton("Z+");
        buttonZpos.addActionListener(event -> moveMachine(0, 0, 1));
    }

    private JButton createJogButton(String text) {
        JButton button = new JButton(text);
        button.setMargin(new Insets(0, 0, 0, 0));
        button.setMinimumSize(new Dimension(44, 36));
        return button;
    }

    private void moveMachine(int x, int y, int z) {
        try {
            IController controller = backendAPI.getController();
            if (controller.getControllerStatus().getState() == ControllerState.ALARM) {
                WizardUtils.killAlarm(backendAPI);
            } else {
                controller.jogMachine(new PartialPosition(x * stepSize, y * stepSize, z * stepSize, UnitUtils.Units.MM), feedRate);
            }
        } catch (Exception e) {
            NotifyDescriptor nd = new NotifyDescriptor.Message("Unexpected error while moving the machine: " + e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }

    public void refresh(Position machineCoord) {
        if (backendAPI.getController() == null || backendAPI.getController().getFirmwareSettings() == null) {
            return;
        }

        try {
            IFirmwareSettings firmwareSettings = backendAPI.getController().getFirmwareSettings();
            updateNavigationButton(firmwareSettings, Axis.X, machineCoord, buttonXpos, buttonXneg);
            updateNavigationButton(firmwareSettings, Axis.Y, machineCoord, buttonYpos, buttonYneg);
            updateNavigationButton(firmwareSettings, Axis.Z, machineCoord, buttonZpos, buttonZneg);
        } catch (FirmwareSettingsException ignored) {
            // Never mind
        }
    }

    private void updateNavigationButton(IFirmwareSettings firmwareSettings, Axis axis, Position machineCoord, JButton buttonPos, JButton buttonNeg) throws FirmwareSettingsException {
        // If soft limits isn't enabled we will just enable all buttons
        if (!firmwareSettings.isSoftLimitsEnabled()) {
            buttonPos.setEnabled(true);
            buttonNeg.setEnabled(true);
            return;
        }

        double softLimit = firmwareSettings.getSoftLimit(axis);
        if (machineCoord.get(axis) - stepSize < -softLimit) {
            buttonNeg.setEnabled(false);
        } else {
            buttonNeg.setEnabled(true);
        }

        if (machineCoord.get(axis) + stepSize > 0) {
            buttonPos.setEnabled(false);
        } else {
            buttonPos.setEnabled(true);
        }
    }

    public void setStepSize(double stepSize) {
        this.stepSize = stepSize;
        refresh(backendAPI.getMachinePosition());
    }
}
