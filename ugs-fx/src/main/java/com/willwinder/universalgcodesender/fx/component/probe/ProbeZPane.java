/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.component.probe;

import com.willwinder.universalgcodesender.fx.component.visualizer.models.ProbeZModel;
import com.willwinder.universalgcodesender.fx.dialog.ProbeConfirmDialog;
import com.willwinder.universalgcodesender.fx.service.probe.ProbeService;
import com.willwinder.universalgcodesender.fx.settings.ProbeSettings;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.Objects;

public class ProbeZPane extends BorderPane {

    private final BackendAPI backend;
    private final ProbeService probeService;

    private final ProbeZModel model;
    private final Button probeButton;

    private final ProbeStatePane probeStatePane = new ProbeStatePane();

    public ProbeZPane(BackendAPI backend) {
        getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/probe-pane.css")).toExternalForm());
        getStyleClass().add("probe-pane");

        this.backend = backend;
        this.probeService = new ProbeService(backend, ProbeSettings.getInstance());
        this.model = new ProbeZModel(probeService);

        backend.addUGSEventListener(this::onUGSEvent);
        probeButton = createProbeButton(backend);

        Label title = new Label();
        title.getStyleClass().add("probe-title");
        title.setText("Probing");

        setTop(title);

        VBox content = new VBox(10, probeStatePane);
        content.setAlignment(Pos.CENTER);
        setCenter(content);

        HBox box = new HBox(20, probeButton);
        box.setAlignment(Pos.CENTER);
        setBottom(box);
    }

    private Button createProbeButton(BackendAPI backend) {
        final Button probeButton;
        String result;
        try {
            result = Localization.getString("probe.action.z");
        } catch (Exception ignored) {
            result = "Probe and zero Z";
        }
        probeButton = new Button(result);
        probeButton.setPrefWidth(220);
        probeButton.setPrefHeight(38);
        probeButton.setOnAction(e -> onProbePressed());
        probeButton.setDisable(!backend.isIdle());
        return probeButton;
    }

    public ProbeZModel getModel() {
        return model;
    }

    private void onProbePressed() {
        if (ProbeSettings.getSkipProbeCheck()) {
            runProbe();
            return;
        }

        Window owner = (getScene() != null) ? getScene().getWindow() : null;
        ProbeConfirmDialog probeConfirmDialog = new ProbeConfirmDialog(owner);
        probeConfirmDialog.showAndWait();
        if (probeConfirmDialog.isAccepted()) {
            runProbe();
        }
    }

    private void onUGSEvent(UGSEvent ugsEvent) {
        if (ugsEvent instanceof ControllerStatusEvent) {
            Platform.runLater(() -> probeButton.setDisable(!backend.isIdle()));
        }
    }

    private void runProbe() {
        if (backend == null || !backend.isConnected() || !backend.isIdle()) {
            return;
        }

        probeStatePane.reset();
        probeService.probeZ(probeStatePane);
    }
}