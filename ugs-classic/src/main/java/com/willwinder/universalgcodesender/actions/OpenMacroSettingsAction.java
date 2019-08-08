/*
    Copyright 2019 Will Winder

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

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.MacroSettingsDialog;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import java.awt.event.ActionEvent;

import static com.willwinder.universalgcodesender.utils.GUIHelpers.displayErrorDialog;

/**
 * An action for opening the macro settings dialog
 *
 * @author Joacim Breiler
 */
public class OpenMacroSettingsAction extends AbstractAction {
    private final BackendAPI backend;

    public OpenMacroSettingsAction(BackendAPI backend) {
        this.backend = backend;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            MacroSettingsDialog macroSettingsDialog = new MacroSettingsDialog(new JFrame(), true, this.backend);
            macroSettingsDialog.setVisible(true);
            macroSettingsDialog.invalidate();
        } catch (Exception ex) {
            displayErrorDialog(ex.getMessage());
        }
    }
}
