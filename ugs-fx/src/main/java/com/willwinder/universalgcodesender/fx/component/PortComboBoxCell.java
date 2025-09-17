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

import com.willwinder.universalgcodesender.connection.DefaultConnectionDevice;
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

public class PortComboBoxCell extends ListCell<String> {
    private final ImageView icon = new ImageView();
    private final Label addressLabel = new Label();
    private final Label descriptionLabel = new Label();
    private final Label manufacturerLabel = new Label();
    private final VBox textBox = new VBox(addressLabel, descriptionLabel, manufacturerLabel);
    private final HBox hbox = new HBox(8, icon, textBox);
    private final PortComboBox portComboBox;

    public PortComboBoxCell(PortComboBox portComboBox) {
        this.portComboBox = portComboBox;
        icon.setFitWidth(24);
        icon.setFitHeight(24);
        addressLabel.setStyle("-fx-font-weight: bold;");
        textBox.setSpacing(2);
        hbox.setPadding(new Insets(5, 5, 5, 5));
        hbox.setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setGraphic(null);
            setText(null);
            return;
        }

        portComboBox.findConnectionDeviceByAddress(item)
                .ifPresentOrElse(this::setFromConnectionDevice,
                        () -> setFromConnectionDevice(new DefaultConnectionDevice(item)));
    }

    private void setFromConnectionDevice(IConnectionDevice connectionDevice) {
        icon.setImage(getDeviceIcon(connectionDevice));
        addressLabel.setText(connectionDevice.getAddress());
        descriptionLabel.setText(connectionDevice.getDescription().orElse(null));
        manufacturerLabel.setText(connectionDevice.getManufacturer().orElse(null));
        setGraphic(hbox);
        setText(null);
    }

    private Image getDeviceIcon(IConnectionDevice device) {
        if (device instanceof JSerialCommConnectionDevice) {
            return SvgLoader.loadIcon("icons/usb.svg", 24).orElse(null);
        }
        return SvgLoader.loadIcon("resources/icons/device.svg", 24).orElse(null);
    }
}