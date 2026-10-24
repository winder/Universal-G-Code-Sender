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

import com.willwinder.ugs.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.designer.entities.selection.SelectionListener;
import com.willwinder.universalgcodesender.fx.dialog.NestDialog;
import com.willwinder.universalgcodesender.fx.helper.WindowHelper;
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.stage.Window;

/**
 * Opens the nest dialog that packs the selected objects within an area. The dialog previews the
 * result in the design while it is open, so it runs on the JavaFX thread that owns the designer
 * model. There has to be more than one object to pack: several selected objects, or a group with
 * several objects in it.
 */
public class DesignNestAction extends AbstractDesignEditAction implements SelectionListener {
    public static final String ICON_BASE = "icons/nest.svg";

    public DesignNestAction() {
        super(Localization.getString("platform.designer.nest"), ICON_BASE);
        controller.getSelectionManager().addSelectionListener(this);
        enabledProperty().set(hasObjectsToNest());
    }

    @Override
    public void handleAction(ActionEvent event) {
        Window owner = WindowHelper.resolveWindow(event);
        Platform.runLater(() -> new NestDialog(owner, controller).showAndWait());
    }

    @Override
    protected void performAction() {
        handleAction(null);
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        setEnabledLater(hasObjectsToNest());
    }

    private boolean hasObjectsToNest() {
        return NestDialog.targets(controller).size() > 1;
    }
}
