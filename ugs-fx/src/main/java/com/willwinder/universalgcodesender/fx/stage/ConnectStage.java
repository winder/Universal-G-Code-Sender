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
package com.willwinder.universalgcodesender.fx.stage;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.connection.ConnectionDriver;
import com.willwinder.universalgcodesender.fx.component.ButtonBox;
import com.willwinder.universalgcodesender.fx.component.PortComboBox;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.BaudRateEnum;
import com.willwinder.universalgcodesender.utils.FirmwareUtils;
import com.willwinder.universalgcodesender.utils.RefreshThread;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ConnectStage extends Stage {
    private ComboBox<String> connectionDriverCombo;
    private PortComboBox addressCombo;
    private Button connectButton;
    private Button closeButton;
    private final BackendAPI backend;
    private ComboBox<String> firmwareCombo;
    private ComboBox<String> portRateCombo;
    private Label addressLabel;
    private Label portRateLabel;

    public ConnectStage(Window owner) {
        initModality(Modality.APPLICATION_MODAL);
        initOwner(owner);
        setTitle("Connect");

        backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        createComponents();
        createLayout();
        registerListeners();
    }

    private void registerListeners() {
        firmwareCombo.getSelectionModel().select(backend.getSettings().getFirmwareVersion());
        firmwareCombo.valueProperty().addListener((observable, oldValue, newValue) -> backend.getSettings().setFirmwareVersion(newValue));

        connectionDriverCombo.getSelectionModel().select(backend.getSettings().getConnectionDriver().getPrettyName());
        connectionDriverCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            backend.getSettings().setConnectionDriver(ConnectionDriver.prettyNameToEnum(newValue));
            updateAddressLabel();
            updatePortRateLabel();
        });


        portRateCombo.getSelectionModel().select(backend.getSettings().getPortRate());
        portRateCombo.valueProperty().addListener((observable, oldValue, newValue) -> backend.getSettings().setPortRate(newValue));

        closeButton.setOnAction(e -> close());
        connectButton.setOnAction(e -> onConnect());

        final RefreshThread refreshThread;
        refreshThread = new RefreshThread(this::refreshPorts, 3000);

        setOnShowing(event -> {
            double centerX = getOwner().getX() + getOwner().getWidth() / 2 - getWidth() / 2;
            double centerY = getOwner().getY() + getOwner().getHeight() / 2 - getHeight() / 2;
            setX(centerX);
            setY(centerY);

            refreshThread.start();
        });

        setOnCloseRequest(event -> refreshThread.interrupt());
    }

    private void onConnect() {
        try {
            backend.connect(firmwareCombo.getSelectionModel().getSelectedItem(), addressCombo.getSelectionModel().getSelectedItem().getAddress(), Integer.parseInt(portRateCombo.getSelectionModel().getSelectedItem()));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            close();
        }
    }

    private void createLayout() {
        GridPane grid = new GridPane();

        ColumnConstraints column1 = new ColumnConstraints();
        column1.setMinWidth(100);
        grid.getColumnConstraints().add(column1);

        ColumnConstraints column2 = new ColumnConstraints();
        column2.setMaxWidth(200);
        column2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().add(column2);

        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        grid.add(new Label(Localization.getString("mainWindow.swing.firmwareLabel")), 0, 0);
        grid.add(firmwareCombo, 1, 0);
        GridPane.setHgrow(firmwareCombo, Priority.ALWAYS);
        firmwareCombo.setMaxWidth(Double.MAX_VALUE);

        grid.add(new Label(Localization.getString("settings.connectionDriver")), 0, 1);
        grid.add(connectionDriverCombo, 1, 1);
        GridPane.setHgrow(connectionDriverCombo, Priority.ALWAYS);
        connectionDriverCombo.setMaxWidth(Double.MAX_VALUE);

        grid.add(addressLabel, 0, 2);
        grid.add(addressCombo, 1, 2);
        GridPane.setHgrow(addressCombo, Priority.ALWAYS);
        addressCombo.setMaxWidth(Double.MAX_VALUE);

        grid.add(portRateLabel, 0, 3);
        grid.add(portRateCombo, 1, 3);
        GridPane.setHgrow(portRateCombo, Priority.ALWAYS);
        portRateCombo.setMaxWidth(Double.MAX_VALUE);

        BorderPane root = new BorderPane();
        root.setCenter(grid);
        ButtonBox buttonBox = new ButtonBox();
        ButtonBox.setButtonData(closeButton, ButtonBox.ButtonData.CANCEL_CLOSE);
        ButtonBox.setButtonData(connectButton, ButtonBox.ButtonData.OK_DONE);
        buttonBox.getButtons().addAll(closeButton, connectButton);
        root.setBottom(buttonBox);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles/root.css").toExternalForm());
        setScene(scene);
        setWidth(350);
        setHeight(300);
        setResizable(false);
    }

    private void createComponents() {
        firmwareCombo = new ComboBox<>(FXCollections.observableArrayList(FirmwareUtils.getFirmwareList()));
        connectionDriverCombo = new ComboBox<>(FXCollections.observableArrayList(ConnectionDriver.getPrettyNames()));

        addressCombo = new PortComboBox(backend);
        addressLabel = new Label();
        addressLabel.setLabelFor(addressCombo);

        portRateCombo = new ComboBox<>(FXCollections.observableArrayList(BaudRateEnum.getAllBaudRates()));
        portRateLabel = new Label();
        portRateLabel.setLabelFor(portRateCombo);

        connectButton = new Button("Connect");
        connectButton.setDefaultButton(true);
        closeButton = new Button("Close");

        updateAddressLabel();
        updatePortRateLabel();
    }

    private void updateAddressLabel() {
        ConnectionDriver connectionDriver = backend.getSettings().getConnectionDriver();
        if (connectionDriver == ConnectionDriver.TCP || connectionDriver == ConnectionDriver.WS) {
            addressLabel.setText(Localization.getString("mainWindow.swing.hostLabel"));
        } else {
            addressLabel.setText(Localization.getString("mainWindow.swing.portLabel"));
        }
    }

    private void updatePortRateLabel() {
        ConnectionDriver connectionDriver = backend.getSettings().getConnectionDriver();
        if (connectionDriver == ConnectionDriver.TCP || connectionDriver == ConnectionDriver.WS) {
            portRateLabel.setText(Localization.getString("mainWindow.swing.portLabel"));
            portRateCombo.setEditable(true);
            portRateCombo.setItems(FXCollections.observableArrayList("23", "80"));
            portRateCombo.getSelectionModel().select("23");
            refreshPorts();
        } else {
            portRateLabel.setText(Localization.getString("mainWindow.swing.baudLabel"));
            portRateCombo.setEditable(false);
            portRateCombo.setItems(FXCollections.observableArrayList(BaudRateEnum.getAllBaudRates()));
            portRateCombo.getSelectionModel().select(BaudRateEnum.BAUD_RATE_115200.getBaudRate());
            refreshPorts();
        }
    }

    private void refreshPorts() {
        addressCombo.refreshPorts();
    }
}
