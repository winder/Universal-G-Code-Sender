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
package com.willwinder.universalgcodesender.fx.component;

import com.willwinder.universalgcodesender.fx.actions.Action;
import com.willwinder.universalgcodesender.fx.service.ShortcutService;
import com.willwinder.universalgcodesender.fx.stage.ShortcutDialog;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Window;

import java.util.Optional;

public class ActionShortcutTableCell extends TableCell<Action, Void> {
    private final Label label = new Label();
    private final Button setButton = new Button("Set");
    private final HBox container = new HBox(5);

    public ActionShortcutTableCell() {
        container.setAlignment(Pos.CENTER_LEFT);
        setButton.setOnAction(e -> {
            Window parent = getTableView().getScene().getWindow();
            Action item = getTableView().getItems().get(getIndex());
            ShortcutDialog shortcutDialog = new ShortcutDialog(parent, item);
            shortcutDialog.showAndWait();
        });
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setGraphic(null);
            return;
        }

        Action action = getTableView().getItems().get(getIndex());
        Optional<String> shortcut = ShortcutService.getShortcut(action.getId());

        container.getChildren().clear();
        Region spacer = new Region();

        if (shortcut.isEmpty()) {
            container.getChildren().add(spacer);
            container.getChildren().add(setButton);
        } else {
            label.setText(shortcut.get());
            container.getChildren().add(spacer);
            container.getChildren().add(label);
            container.getChildren().add(setButton);
        }
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox.setMargin(setButton, new Insets(0, 10, 0, 10));
        setGraphic(container);
    }
}
