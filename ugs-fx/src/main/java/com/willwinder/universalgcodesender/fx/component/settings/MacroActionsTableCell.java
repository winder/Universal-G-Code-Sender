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
package com.willwinder.universalgcodesender.fx.component.settings;

import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import com.willwinder.universalgcodesender.fx.model.MacroAdapter;
import com.willwinder.universalgcodesender.fx.service.MacroRegistry;
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class MacroActionsTableCell extends TableCell<MacroAdapter, Void> {
    private final Button deleteButton;
    private final HBox container = new HBox(5);

    public MacroActionsTableCell() {
        container.setAlignment(Pos.CENTER_LEFT);
        deleteButton = new Button(Localization.getString("delete"), SvgLoader.loadImageIcon("icons/delete.svg", 16).orElse(null));
        deleteButton.setOnAction(e -> {
            MacroAdapter item = getTableView().getItems().get(getIndex());
            MacroRegistry.getInstance().removeMacro(item);
        });
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setGraphic(null);
            return;
        }

        container.getChildren().clear();
        Region spacer = new Region();

        container.getChildren().add(spacer);
        container.getChildren().add(deleteButton);

        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox.setMargin(deleteButton, new Insets(0, 10, 0, 10));
        setGraphic(container);
    }
}
