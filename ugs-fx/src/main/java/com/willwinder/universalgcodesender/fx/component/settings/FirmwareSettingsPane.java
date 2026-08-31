/*
    Copyright 2025-2026 Joacim Breiler

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

import com.willwinder.universalgcodesender.firmware.FirmwareSetting;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingGrouper;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.FirmwareSettingEvent;
import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.utils.SettingsComparator;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

import java.util.ArrayList;
import java.util.List;

/**
 * Presents the firmware settings of the connected controller. The settings are grouped in the
 * sections that the controller reports, with a heading for each group. Sorting the table by one of
 * its columns presents the settings as a plain list instead, as the headings only make sense while
 * the settings are kept in their grouped order.
 */
public class FirmwareSettingsPane extends BorderPane {
    private static final String STYLE_CLASS = "firmware-settings";
    private static final PseudoClass GROUP_HEADING = PseudoClass.getPseudoClass("group-heading");

    private final List<FirmwareSetting> firmwareSettings = new ArrayList<>();
    private final TreeItem<FirmwareSetting> root = new TreeItem<>(new FirmwareSetting("", ""));
    private final BackendAPI backend;

    private TreeTableView<FirmwareSetting> tableView;
    private Label notConnectedLabel;
    private boolean rebuildScheduled;

    public FirmwareSettingsPane() {
        backend = LookupService.lookup(BackendAPI.class);

        addTitleSection();
        addFirmwareSettingsTable();
        addNotConnectedLabel();

        backend.addUGSEventListener(this::onEvent);

        if (backend.isConnected()) {
            setCenter(tableView);
            loadSettings();
        } else {
            setCenter(notConnectedLabel);
        }
    }

    private void addNotConnectedLabel() {
        notConnectedLabel = new Label(Localization.getString("controller.log.notconnected"));
        notConnectedLabel.setStyle("-fx-font-size: 1.4em");
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof ControllerStateEvent) {
            Platform.runLater(this::onControllerStateChanged);
        } else if (event instanceof FirmwareSettingEvent firmwareSettingEvent) {
            FirmwareSetting setting = firmwareSettingEvent.getFirmwareSetting();
            Platform.runLater(() -> replaceOrAddSetting(setting));
        }
    }

    private void onControllerStateChanged() {
        if (backend.isConnected() && getCenter() != tableView) {
            setCenter(tableView);
        } else if (!backend.isConnected() && getCenter() != notConnectedLabel) {
            setCenter(notConnectedLabel);
        }
    }

    private void loadSettings() {
        firmwareSettings.clear();
        firmwareSettings.addAll(backend.getController().getFirmwareSettings().getAllSettings());
        rebuildRows();
    }

    private void replaceOrAddSetting(FirmwareSetting newFirmwareSetting) {
        String key = newFirmwareSetting.getKey();
        firmwareSettings.removeIf(oldFirmwareSetting -> oldFirmwareSetting.getKey().equalsIgnoreCase(key));
        firmwareSettings.add(newFirmwareSetting);
        scheduleRebuild();
    }

    /**
     * Settings are reported one at a time when connecting, coalesce them into a single rebuild.
     */
    private void scheduleRebuild() {
        if (rebuildScheduled) {
            return;
        }

        rebuildScheduled = true;
        Platform.runLater(() -> {
            rebuildScheduled = false;
            rebuildRows();
        });
    }

    private void rebuildRows() {
        List<String> groupNames = getGroupNames();
        root.getChildren().clear();

        if (!tableView.getSortOrder().isEmpty()) {
            FirmwareSettingGrouper.groupedOrder(firmwareSettings, groupNames)
                    .forEach(setting -> root.getChildren().add(new TreeItem<>(setting)));
            return;
        }

        FirmwareSettingGrouper.group(firmwareSettings, groupNames).forEach(group -> {
            TreeItem<FirmwareSetting> parent = root;
            if (group.hasName()) {
                parent = new TreeItem<>(new FirmwareSetting(group.name(), ""));
                parent.setExpanded(true);
                root.getChildren().add(parent);
            }

            TreeItem<FirmwareSetting> groupNode = parent;
            group.settings().forEach(setting -> groupNode.getChildren().add(new TreeItem<>(setting)));
        });
    }

    private List<String> getGroupNames() {
        if (!backend.isConnected() || backend.getController() == null) {
            return List.of();
        }

        return backend.getController().getFirmwareSettings().getGroupNames();
    }

    private void addFirmwareSettingsTable() {
        tableView = new TreeTableView<>(root);
        tableView.getStyleClass().add(STYLE_CLASS);
        tableView.setShowRoot(false);
        root.setExpanded(true);

        TreeTableColumn<FirmwareSetting, String> keyCol = new TreeTableColumn<>(Localization.getString("setting"));
        keyCol.setComparator(new SettingsComparator());
        keyCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue().getKey()));
        keyCol.setEditable(false);
        keyCol.setPrefWidth(80);

        TreeTableColumn<FirmwareSetting, String> valueCol = new TreeTableColumn<>(Localization.getString("value"));
        valueCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue().getValue()));
        valueCol.setCellFactory(column -> new FirmwareSettingTableCell());
        valueCol.setEditable(true);
        valueCol.setPrefWidth(80);

        TreeTableColumn<FirmwareSetting, String> descriptionCol = new TreeTableColumn<>(Localization.getString("description"));
        descriptionCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue().getShortDescription()));
        descriptionCol.setEditable(false);
        descriptionCol.setSortable(false);

        tableView.setEditable(true);
        tableView.setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        tableView.getColumns().addAll(keyCol, valueCol, descriptionCol);
        tableView.setRowFactory(view -> createRow());

        // Sorting can not keep the group headings in place, present a plain list instead
        tableView.getSortOrder().addListener((javafx.collections.ListChangeListener<TreeTableColumn<FirmwareSetting, ?>>) change -> rebuildRows());
    }

    private TreeTableRow<FirmwareSetting> createRow() {
        TreeTableRow<FirmwareSetting> row = new TreeTableRow<>() {
            @Override
            protected void updateItem(FirmwareSetting item, boolean empty) {
                super.updateItem(item, empty);
                updateGroupHeadingState(this);
            }
        };

        row.treeItemProperty().addListener((observable, oldValue, newValue) -> updateGroupHeadingState(row));
        return row;
    }

    private static void updateGroupHeadingState(TreeTableRow<FirmwareSetting> row) {
        boolean isGroupHeading = !row.isEmpty() && FirmwareSettingRows.isGroupHeading(row.getTreeItem());
        row.pseudoClassStateChanged(GROUP_HEADING, isGroupHeading);
    }

    private void addTitleSection() {
        Label title = new Label(Localization.getString("settings.firmware"));
        title.setPadding(new Insets(0, 0, 15, 0));
        title.setFont(Font.font(20));
        setTop(title);
    }
}
