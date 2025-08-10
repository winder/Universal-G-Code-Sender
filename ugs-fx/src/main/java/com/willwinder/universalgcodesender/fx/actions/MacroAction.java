/*
    Copyright 2025 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.actions;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.MacroHelper;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.types.Macro;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import org.openide.util.Exceptions;

import java.awt.EventQueue;

public class MacroAction extends BaseAction {
    private final Macro macro;
    private final BackendAPI backend;

    public MacroAction(Macro macro) {
        this.macro = macro;
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this::onEvent);
        enabledProperty().set(backend.isConnected() && backend.isIdle());
        titleProperty().set(macro.getName());
        labelProperty().set(macro.getName());
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof ControllerStateEvent) {
            enabledProperty().set(backend.isConnected() && backend.isIdle());
        }
    }

    @Override
    public void handleAction(javafx.event.ActionEvent event) {
        if (macro == null || macro.getGcode() == null) {
            return;
        }

        EventQueue.invokeLater(() -> {
            try {
                MacroHelper.executeCustomGcode(macro.getGcode(), backend);
            } catch (Exception ex) {
                GUIHelpers.displayErrorDialog(ex.getMessage());
                Exceptions.printStackTrace(ex);
            }
        });
    }

    @Override
    public String getId() {
        return MacroAction.class.getSimpleName() + "_" + macro.getUuid();
    }
}
