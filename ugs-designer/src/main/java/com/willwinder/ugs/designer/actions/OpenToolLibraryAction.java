/*
    Copyright 2026 Damian Nikodem

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

import com.willwinder.ugs.designer.gui.toollibrary.ToolLibraryDialog;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.universalgcodesender.uielements.DialogUtils;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OpenToolLibraryAction implements ActionListener {
    private final Controller controller;

    public OpenToolLibraryAction(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Window parent = DialogUtils.getParentWindow(controller.getDrawing());
        ToolLibraryDialog.show(parent, controller.getSettings().getPreferredUnits());
    }
}
