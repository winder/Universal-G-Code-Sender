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
package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.gui.StockSettingsPanel;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.universalgcodesender.uielements.DialogUtils;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class OpenStockSettingsAction implements ActionListener {
    private final Controller controller;

    public OpenStockSettingsAction(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        StockSettingsPanel stockSettingsPanel = new StockSettingsPanel(controller);
        Window parent = DialogUtils.getParentWindow(controller.getDrawing());

        if (DialogUtils.showModalDialog(parent, "Stock settings", stockSettingsPanel)) {
            double stockThickness = stockSettingsPanel.getStockThickness();
            ChangeStockSettingsAction changeStockSettingsAction = new ChangeStockSettingsAction(controller, stockThickness);
            changeStockSettingsAction.actionPerformed(null);
            controller.getUndoManager().addAction(changeStockSettingsAction);
        }
    }
}
