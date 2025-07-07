package com.willwinder.universalgcodesender.fx.component.settings;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.firmware.FirmwareSetting;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.model.BackendAPI;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;

public class FirmwareSettingTableCell extends TextFieldTableCell<FirmwareSetting, String> {

    private TextField textField;

    public FirmwareSettingTableCell() {
    }

    @Override
    public void startEdit() {
        super.startEdit();
        if (textField == null) {
            createTextField();
        }
        setGraphic(textField);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        textField.selectAll();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getItem());
        setContentDisplay(ContentDisplay.TEXT_ONLY);
        setStyle("");
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getItem());
                }
                setGraphic(textField);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            } else {
                setText(getItem());
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
        }
    }

    @Override
    public void commitEdit(String newValue) {
        BackendAPI backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);

        FirmwareSetting setting = getTableRow().getItem();
        try {
            FirmwareSetting updateFirmwareSetting = backendAPI.getController().getFirmwareSettings().setValue(setting.getKey(), newValue);

            // The value has not changed
            if (updateFirmwareSetting.getValue().equals(setting.getValue())) {
                textField.setStyle("-fx-border-color: red;");
                return;
            }
        } catch (FirmwareSettingsException e) {
            textField.setStyle("-fx-border-color: red;");
            return;
        }


        if (textField != null) {
            textField.setStyle("");
        }
        super.commitEdit(newValue);
    }

    private void createTextField() {
        textField = new TextField(getItem());
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        textField.setOnAction(e -> commitEdit(textField.getText()));
        textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                commitEdit(textField.getText());
            }
        });
    }
}