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

import com.willwinder.ugs.designer.actions.AddAction;
import com.willwinder.ugs.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.designer.gui.clipart.Clipart;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.universalgcodesender.fx.dialog.InsertClipartDialog;
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Window;

/**
 * Opens the clipart dialog and inserts the chosen clipart into the design. The dialog and the model
 * mutation both run on the JavaFX thread, which owns the designer model in the FX visualizer.
 */
public class DesignClipartAction extends BaseAction {
    public static final String ICON_BASE = "icons/clipart.svg";

    public DesignClipartAction() {
        super(Localization.getString("platform.designer.clipart"), Localization.getString("platform.designer.clipart"), Localization.getString("actions.category.designer"), ICON_BASE);
    }

    @Override
    public void handleAction(ActionEvent event) {
        Window owner = resolveWindow(event);
        Platform.runLater(() -> {
            InsertClipartDialog dialog = new InsertClipartDialog(owner);
            dialog.showAndWait();
            dialog.getSelectedClipart().ifPresent(this::insertClipart);
        });
    }

    private void insertClipart(Clipart clipart) {
        Controller controller = ControllerFactory.getController();
        Cuttable cuttable = clipart.getCuttable();
        new AddAction(controller, cuttable).actionPerformed(null);
        controller.getSelectionManager().addSelection(cuttable);
        controller.setTool(Tool.SELECT);
    }

    private static Window resolveWindow(ActionEvent event) {
        if (event != null && event.getSource() instanceof Node node && node.getScene() != null) {
            return node.getScene().getWindow();
        }
        return Window.getWindows().stream()
                .filter(Window::isFocused)
                .findFirst()
                .orElse(null);
    }
}
