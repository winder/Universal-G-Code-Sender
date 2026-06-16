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
package com.willwinder.universalgcodesender.fx.actions;

import com.willwinder.ugs.designer.entities.entities.selection.SelectionEvent;
import com.willwinder.ugs.designer.entities.entities.selection.SelectionListener;
import com.willwinder.ugs.designer.entities.entities.selection.SelectionManager;
import com.willwinder.ugs.designer.io.ugsd.UgsDesignWriter;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.services.NotificationService;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

public class DesignCopyAction extends AbstractDesignEditAction implements SelectionListener {
    public static final String ICON_BASE = "icons/copy.svg";

    public DesignCopyAction() {
        super(Localization.getString("platform.designer.copy"), ICON_BASE);
        SelectionManager selectionManager = controller.getSelectionManager();
        selectionManager.addSelectionListener(this);
        enabledProperty().set(!selectionManager.getSelection().isEmpty());
    }

    @Override
    protected void performAction() {
        SelectionManager selectionManager = controller.getSelectionManager();
        if (selectionManager.getSelection().isEmpty()) {
            LookupService.lookupOptional(NotificationService.class).ifPresent(s -> s.setStatusText(""));
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(null, null);
            return;
        }

        LookupService.lookupOptional(NotificationService.class)
                .ifPresent(s -> s.setStatusText("Clipboard: " + selectionManager.getSelection().size()));
        String data = new UgsDesignWriter().serialize(selectionManager.getChildren());
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(data), null);
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        setEnabledLater(!controller.getSelectionManager().getSelection().isEmpty());
    }
}
