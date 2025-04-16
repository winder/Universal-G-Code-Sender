package com.willwinder.universalgcodesender.fx.component;

import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import com.willwinder.universalgcodesender.fx.model.SettingsListItem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class SettingsListCell extends javafx.scene.control.ListCell<SettingsListItem> {
    private final ImageView icon = new ImageView();
    private final Label label = new Label();
    private final HBox hbox = new HBox(8, icon, label);

    public SettingsListCell() {
        icon.setFitWidth(24);
        icon.setFitHeight(24);
        label.setStyle("-fx-font-size: 1.2em");
        hbox.setPadding(new Insets(8, 8, 8, 8));
        hbox.setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    protected void updateItem(SettingsListItem item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            setText(null);
        } else {
            icon.setImage(getDeviceIcon(item));
            label.setText(item.text());
            setGraphic(hbox);
            setText(null);
        }
    }

    private Image getDeviceIcon(SettingsListItem settingsListItem) {
        return SvgLoader.loadIcon(settingsListItem.icon(), 24).orElse(null);
    }
}
