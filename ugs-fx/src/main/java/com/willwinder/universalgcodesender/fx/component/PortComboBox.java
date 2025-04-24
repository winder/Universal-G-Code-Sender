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
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class PortComboBox extends ComboBox<IConnectionDevice> implements UGSEventListener {
    private final transient BackendAPI backend;
    private final ObservableList<IConnectionDevice> portList = FXCollections.observableArrayList();

    public PortComboBox(BackendAPI backend) {
        this.backend = backend;

        setItems(portList);
        setEditable(true);

        // Display port name as string
        setConverter(new StringConverter<>() {
            @Override
            public String toString(IConnectionDevice device) {
                return device != null ? device.getAddress() : "";
            }

            @Override
            public IConnectionDevice fromString(String string) {
                return portList.stream()
                        .filter(dev -> StringUtils.equals(dev.getAddress(), string))
                        .findFirst()
                        .orElse(null);
            }
        });

        setCellFactory(listView -> new PortComboBoxCell());


        // Handle selection changes
        valueProperty().addListener((obs, oldVal, newVal) -> onUpdate());

        // Set initial selection
        String currentPort = backend.getSettings().getPort();
        portList.setAll(getAvailablePorts());
        setValue(portList.stream()
                .filter(dev -> dev.getAddress().equals(currentPort))
                .findFirst()
                .orElse(null));

        // Register for backend events
        backend.addUGSEventListener(this);
    }


    private void onUpdate() {
        IConnectionDevice selectedItem = getValue();
        if (selectedItem == null) {
            return;
        }

        backend.getSettings().setPort(selectedItem.getAddress());
        selectedItem.getPort().ifPresent(port ->
                backend.getSettings().setPortRate(port.toString())
        );

    }

    public void refreshPorts() {
        List<? extends IConnectionDevice> availablePorts = getAvailablePorts();
        Platform.runLater(() -> {
            availablePorts.forEach(port -> {
                if (!portList.contains(port)) {
                    portList.add(port);
                }
            });

            portList.removeIf(port -> !availablePorts.contains(port));
        });
    }

    private List<? extends IConnectionDevice> getAvailablePorts() {
        return ConnectionFactory.getDevices(backend.getSettings().getConnectionDriver());
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof SettingChangedEvent) {
            String currentPort = backend.getSettings().getPort();
            Platform.runLater(() -> setValue(portList.stream()
                    .filter(dev -> StringUtils.equals(dev.getAddress(), currentPort))
                    .findFirst()
                    .orElse(null)));
        }
    }
}
