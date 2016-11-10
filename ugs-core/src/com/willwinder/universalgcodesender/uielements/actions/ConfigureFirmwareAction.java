/*
    Copywrite 2016 Will Winder

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
package com.willwinder.universalgcodesender.uielements.actions;

import com.willwinder.universalgcodesender.GrblController;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.GrblFirmwareSettingsDialog;
import static com.willwinder.universalgcodesender.utils.GUIHelpers.displayErrorDialog;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JFrame;

/**
 *
 * @author wwinder
 */
public class ConfigureFirmwareAction extends AbstractAction {
    private final BackendAPI backend;

    public ConfigureFirmwareAction(BackendAPI backend) {
        this.backend = backend;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (!this.backend.isConnected()) {
                displayErrorDialog(Localization.getString("controller.log.notconnected"));
            } else if (this.backend.getController().getClass().equals(GrblController.class)) {
                    GrblFirmwareSettingsDialog gfsd = 
                            new GrblFirmwareSettingsDialog(new JFrame(), true, this.backend);
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
