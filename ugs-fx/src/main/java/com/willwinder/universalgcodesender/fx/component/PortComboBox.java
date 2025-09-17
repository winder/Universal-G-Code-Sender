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

import com.willwinder.universalgcodesender.connection.ConnectionFactory;
import com.willwinder.universalgcodesender.connection.IConnectionDevice;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class PortComboBox extends ComboBox<String> implements UGSEventListener {
    private final transient BackendAPI backend;
    private final ObservableList<String> portList = FXCollections.observableArrayList();

    public PortComboBox(BackendAPI backend) {
        this.backend = backend;

        setItems(portList);
        setEditable(true);
        setCellFactory(listView -> new PortComboBoxCell(this));

        // Handle selection changes
        valueProperty().addListener((obs, oldVal, newVal) -> onUpdate());

        // Set initial selection
        String currentPort = backend.getSettings().getPort();
        portList.setAll(getAvailablePorts().stream().map(IConnectionDevice::getAddress).toList());
        setValue(portList.stream()
                .filter(dev -> dev.equals(currentPort))
                .findFirst()
                .orElse(null));

        // Register for backend events
        backend.addUGSEventListener(this);
    }


    private void onUpdate() {
        String selectedItem = getValue();
        if (selectedItem == null) {
            return;
        }

        backend.getSettings().setPort(selectedItem);
        Optional<? extends IConnectionDevice> selectedPort = findConnectionDeviceByAddress(selectedItem);
        selectedPort.flatMap(IConnectionDevice::getPort)
                .ifPresent(port -> backend.getSettings().setPortRate(port.toString()));
    }

    public Optional<? extends IConnectionDevice> findConnectionDeviceByAddress(String address) {
        return getAvailablePorts().stream()
                .filter(p -> p.getAddress().equals(address))
                .findFirst();
    }

    public void refreshPorts() {
        Platform.runLater(() -> {
            List<? extends IConnectionDevice> availablePorts = getAvailablePorts();
            availablePorts.forEach(port -> {
                if (portList.stream()
                        .noneMatch(a -> StringUtils.equalsIgnoreCase(a, port.getAddress()))) {
                    portList.add(port.getAddress());
                }
            });

            portList.removeIf(port -> availablePorts.stream()
                    .filter(Objects::nonNull)
                    .noneMatch(a -> StringUtils.equalsIgnoreCase(a.getAddress(), port)));
        });
    }

    private List<? extends IConnectionDevice> getAvailablePorts() {
        return ConnectionFactory.getDevices(backend.getSettings().getConnectionDriver()).stream()
                .filter(Objects::nonNull)
                .filter(p -> p.getAddress() != null)
                .toList();
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof SettingChangedEvent) {
            String currentPort = backend.getSettings().getPort();
            Platform.runLater(() -> setValue(portList.stream()
                    .filter(dev -> StringUtils.equals(dev, currentPort))
                    .findFirst()
                    .orElse(null)));
        }
    }
}
