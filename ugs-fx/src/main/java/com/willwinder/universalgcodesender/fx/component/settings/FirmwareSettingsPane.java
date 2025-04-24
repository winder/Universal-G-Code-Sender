package com.willwinder.universalgcodesender.fx.component.settings;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.firmware.FirmwareSetting;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.FirmwareSettingEvent;
import com.willwinder.universalgcodesender.utils.SettingsComparator;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

public class FirmwareSettingsPane extends BorderPane {
    private final ObservableList<FirmwareSetting> firmwareSettings = FXCollections.observableArrayList();
    private TableView<FirmwareSetting> tableView;

    public FirmwareSettingsPane() {
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        addTitleSection();
        addFirmwareSettingsTable();

        backend.addUGSEventListener(this::onEvent);

        if (backend.isConnected()) {
            firmwareSettings.addAll(backend.getController().getFirmwareSettings().getAllSettings());
            tableView.sort();
        }
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof FirmwareSettingEvent firmwareSettingEvent) {
            FirmwareSetting newFirmwareSetting = firmwareSettingEvent.getFirmwareSetting();
            replaceOrAddSetting(newFirmwareSetting);
        }
    }

    private void replaceOrAddSetting(FirmwareSetting newFirmwareSetting) {
        Platform.runLater(() -> {
            String key = newFirmwareSetting.getKey();
            firmwareSettings.removeIf(oldFirmwareSetting -> oldFirmwareSetting.getKey().equalsIgnoreCase(key));
            firmwareSettings.add(newFirmwareSetting);
            tableView.sort();
        });
    }

    private void addFirmwareSettingsTable() {
        tableView = new TableView<>();

        TableColumn<FirmwareSetting, String> keyCol = new TableColumn<>("Setting");
        keyCol.setComparator(new SettingsComparator());
        keyCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey()));
        keyCol.setEditable(false);
        keyCol.setPrefWidth(40);

        TableColumn<FirmwareSetting, String> valueCol = new TableColumn<>("Value");
        valueCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue()));
        valueCol.setEditable(true);
        valueCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue()));
        valueCol.setCellFactory(column -> new FirmwareSettingTableCell());

        TableColumn<FirmwareSetting, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getShortDescription()));
        valueCol.setMaxWidth(150);
        descriptionCol.setEditable(false);
        descriptionCol.setSortable(false);

        tableView.setEditable(true);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.getColumns().addAll(keyCol, valueCol, descriptionCol);
        tableView.setItems(firmwareSettings);
        tableView.getSortOrder().add(keyCol);
        tableView.sort();
        setCenter(tableView);
    }

    private void addTitleSection() {
        Label title = new Label(Localization.getString("settings.firmware"));
        title.setPadding(new Insets(0, 0, 15, 0));
        title.setFont(Font.font(20));
        setTop(title);
    }
}
