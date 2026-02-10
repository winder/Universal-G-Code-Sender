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

import com.willwinder.universalgcodesender.fx.exceptions.ProbeException;
import com.willwinder.universalgcodesender.fx.service.ProbeService;
import com.willwinder.universalgcodesender.fx.component.visualizer.models.ProbeZModel;
import com.willwinder.universalgcodesender.fx.dialog.ProbeConfirmDialog;
import com.willwinder.universalgcodesender.fx.settings.ProbeSettings;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

import java.util.prefs.Preferences;

public class ProbeZPane extends VBox {
    private static final String PREF_SKIP_PROBE_CHECK = "probe.z.skipProbeCheck";
    private static final Preferences PREFS = Preferences.userNodeForPackage(ProbeSettings.class);

    private final BackendAPI backend;
    private final Label status = new Label();
    private final Label description = new Label();
    private final ProbeService probeService;

    private final ProbeZModel model = new ProbeZModel();

    public ProbeZPane(BackendAPI backend) {
        super(10);
        this.backend = backend;
        this.probeService = new ProbeService(backend, ProbeSettings.getInstance());

        setStyle("-fx-padding: 10;");

        String result;
        try {
            result = Localization.getString("probe.action.z");
        } catch (Exception ignored) {
            result = "Probe and zero Z";
        }
        Button probe = new Button(result);
        probe.setPrefWidth(220);
        probe.setPrefHeight(38);
        probe.setOnAction(e -> onProbePressed());

        backend.addUGSEventListener(this::onUGSEvent);

        getChildren().addAll(
                probe,
                status,
                description
        );
    }

    public ProbeZModel getModel() {
        return model;
    }

    private void onProbePressed() {
        if (PREFS.getBoolean(PREF_SKIP_PROBE_CHECK, false)) {
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
        if (ugsEvent instanceof ControllerStatusEvent controllerStatusEvent) {
            // Update the existing description line (already present in your pane)
            double pos = controllerStatusEvent.getStatus().getMachineCoord().getZ()
                    + probeService.getSafeProbeZDistance().value().doubleValue();

            Platform.runLater(() -> description.setText(
                    "This will probe downwards " + probeService.getSafeProbeZDistance().value() + " "
                            + probeService.getSafeProbeZDistance().unit()
                            + " from the current position (" + pos + ")"
            ));
        }
    }


    private void runProbe() {
        if (backend == null || !backend.isConnected()) {
            status.setText("Not connected.");
            return;
        }
        if (!backend.isIdle()) {
            status.setText("Controller is busy.");
            return;
        }

        status.setText("Z probe started.");

        // Run the probe routine off the UI thread, but marshal UI updates onto the FX thread.
        ThreadHelper.invokeLater(() -> {
            try {
                probeService.probeZ();
                Platform.runLater(() ->
                        status.setText("Z probe completed.")
                );
            } catch (ProbeException ex) {
                Platform.runLater(() ->
                        status.setText("Could not start Z probe: " + ex.getMessage())
                );
                ex.printStackTrace();
            }
        });
    }
}