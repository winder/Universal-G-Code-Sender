package com.willwinder.universalgcodesender.fx.component;

import com.willwinder.universalgcodesender.connection.IConnectionDevice;
import com.willwinder.universalgcodesender.connection.JSerialCommConnectionDevice;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class PortComboBoxCell extends ListCell<IConnectionDevice> {
    private final ImageView icon = new ImageView();
    private final Label addressLabel = new Label();
    private final Label descriptionLabel = new Label();
    private final Label manufacturerLabel = new Label();
    private final VBox textBox = new VBox(addressLabel, descriptionLabel, manufacturerLabel);
    private final HBox hbox = new HBox(8, icon, textBox);

    public PortComboBoxCell() {
        icon.setFitWidth(24);
        icon.setFitHeight(24);
        addressLabel.setStyle("-fx-font-weight: bold;");
        textBox.setSpacing(2);
        hbox.setPadding(new Insets(5, 5, 5, 5));
        hbox.setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    protected void updateItem(IConnectionDevice item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            setText(null);
        } else {
            icon.setImage(getDeviceIcon(item));
            addressLabel.setText(item.getAddress());
            descriptionLabel.setText(item.getDescription().orElse(null));
            manufacturerLabel.setText(item.getManufacturer().orElse(null));
            setGraphic(hbox);
            setText(null);
        }
    }

    private Image getDeviceIcon(IConnectionDevice device) {
        if (device instanceof JSerialCommConnectionDevice) {
            return SvgLoader.loadIcon("icons/usb.svg", 24).orElse(null);
        }
        return SvgLoader.loadIcon("resources/icons/device.svg", 24).orElse(null);
    }
}