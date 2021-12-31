/*
    Copyright 2016-2018 Will Winder

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
package com.willwinder.universalgcodesender.actions;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.uielements.firmware.FirmwareSettingsDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static com.willwinder.universalgcodesender.utils.GUIHelpers.displayErrorDialog;

/**
 * @author wwinder
 */
public class ConfigureFirmwareAction extends AbstractAction {
    private final BackendAPI backend;

    public ConfigureFirmwareAction(BackendAPI backend) {
        this.backend = backend;
        this.backend.addUGSEventListener(this::onEvent);
        setEnabled(canConfigureFirmware());
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof ControllerStateEvent) {
            EventQueue.invokeLater(() ->
                    setEnabled(canConfigureFirmware()));
        }
    }

    private boolean canConfigureFirmware() {
        return this.backend.isConnected() && this.backend.isIdle();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (!this.backend.isConnected()) {
                displayErrorDialog(Localization.getString("controller.log.notconnected"));
            } else if (this.backend.getController().getCapabilities().hasFirmwareSettings()) {
                FirmwareSettingsDialog gfsd =
                        new FirmwareSettingsDialog(new JFrame(), true, this.backend);
                gfsd.setVisible(true);
            }
            // Add additional firmware setting windows here.
            else {
                displayErrorDialog(Localization.getString("mainWindow.error.noFirmware"));
            }
        } catch (Exception ex) {
            displayErrorDialog(ex.getMessage());
        }
    }
}
