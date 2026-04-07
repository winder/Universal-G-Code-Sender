/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.ugs.designer.actions;

import com.willwinder.ugs.designer.gui.ToolSettingsPanel;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.universalgcodesender.uielements.DialogUtils;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OpenToolSettingsAction implements ActionListener {
    private final Controller controller;

    public OpenToolSettingsAction(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ToolSettingsPanel toolSettingsPanel = new ToolSettingsPanel(controller);
        Window parent = DialogUtils.getParentWindow(controller.getDrawing());

        if (DialogUtils.showModalDialog(parent, "Tool settings", toolSettingsPanel)) {
            ChangeToolSettingsAction changeToolSettingsAction =
                    new ChangeToolSettingsAction(controller, toolSettingsPanel.getSettings());
            changeToolSettingsAction.actionPerformed(null);
            controller.getUndoManager().addAction(changeToolSettingsAction);
        }
    }
}
