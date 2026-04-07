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

import com.willwinder.ugs.designer.entities.entities.selection.SelectionEvent;
import com.willwinder.ugs.designer.entities.entities.selection.SelectionListener;
import com.willwinder.ugs.designer.entities.entities.selection.SelectionManager;
import com.willwinder.ugs.designer.io.ugsd.UgsDesignWriter;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.services.NotificationService;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;

public class CopyAction extends AbstractDesignAction implements SelectionListener {

    private final transient Controller controller;

    public CopyAction() {
        putValue("menuText", "Copy");
        putValue(NAME, "Copy");

        this.controller = ControllerFactory.getController();

        SelectionManager selectionManager = controller.getSelectionManager();
        selectionManager.addSelectionListener(this);
        setEnabled(!selectionManager.getSelection().isEmpty());
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        SelectionManager selectionManager = controller.getSelectionManager();
        setEnabled(!selectionManager.getSelection().isEmpty());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (controller.getSelectionManager().getSelection().isEmpty()) {
            LookupService.lookupOptional(NotificationService.class).ifPresent(s -> s.setStatusText(""));
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(null, null);
        } else {
            LookupService.lookupOptional(NotificationService.class).ifPresent(s -> s.setStatusText("Clipboard: " + controller.getSelectionManager().getSelection().size()));
            UgsDesignWriter writer = new UgsDesignWriter();
            String data = writer.serialize(controller.getSelectionManager().getChildren());

            Transferable content = new StringSelection(data);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(content, null);
        }
    }
}