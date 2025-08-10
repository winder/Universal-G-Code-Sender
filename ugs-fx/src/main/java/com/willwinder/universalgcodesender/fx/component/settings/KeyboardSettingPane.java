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

import com.willwinder.universalgcodesender.fx.actions.Action;
import com.willwinder.universalgcodesender.fx.service.ActionRegistry;
import com.willwinder.universalgcodesender.fx.component.ActionIconTableCell;
import com.willwinder.universalgcodesender.fx.component.ActionShortcutTableCell;
import com.willwinder.universalgcodesender.fx.service.ShortcutService;
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class KeyboardSettingPane extends VBox {

    public KeyboardSettingPane() {
        setSpacing(20);
        addTitleSection();

        TableView<Action> table = new TableView<>();
        TableColumn<Action, Void> iconColumn = new TableColumn<>();
        iconColumn.setMaxWidth(30);
        iconColumn.setCellFactory(col -> new ActionIconTableCell());

        TableColumn<Action, String> titleColumn = new TableColumn<>(Localization.getString("settings.keyboard.action"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Action, String> categoryColumn = new TableColumn<>(Localization.getString("settings.keyboard.category"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Action, Void> shortcutColumn = new TableColumn<>(Localization.getString("settings.keyboard.shortcut"));
        shortcutColumn.setMaxWidth(Double.MAX_VALUE);
        shortcutColumn.setCellFactory(col -> new ActionShortcutTableCell());

        table.getColumns().addAll(iconColumn, titleColumn, shortcutColumn);
        table.setItems(FXCollections.observableArrayList(ActionRegistry.getInstance().getActions()));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        getChildren().add(table);

        VBox.setVgrow(table, Priority.ALWAYS);

        ShortcutService.getShortcuts().addListener((MapChangeListener<String, String>) change -> table.refresh());
    }

    private void addTitleSection() {
        Label title = new Label(Localization.getString("settings.keyboard"));
        title.setFont(Font.font(20));
        getChildren().add(title);
    }
}
