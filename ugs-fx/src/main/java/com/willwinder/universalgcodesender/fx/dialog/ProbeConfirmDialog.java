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
package com.willwinder.universalgcodesender.fx.dialog;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.fx.component.ButtonBox;
import com.willwinder.universalgcodesender.fx.helper.Colors;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Objects;

public class ProbeConfirmDialog extends Stage implements UGSEventListener {
    private final BackendAPI backend;
    private final Button acceptButton;
    private final Circle indicatorDot;
    private final Label indicatorText;

    private boolean isAccepted = false;

    public ProbeConfirmDialog(Window parent) {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this);
        initOwner(parent);
        initModality(Modality.APPLICATION_MODAL);
        setAlwaysOnTop(true);
        setTitle("Confirm start probing");

        Label instruction = new Label("To continue, test the probe now by touching it to confirm the signal is working.");
        instruction.setWrapText(true);

        indicatorDot = new Circle(6);
        indicatorText = new Label();
        HBox indicatorRow = new HBox(10, new Label("Probe signal:"), indicatorDot, indicatorText);
        indicatorRow.setAlignment(Pos.CENTER_LEFT);
        updateProbeIndicator(false);

        acceptButton = new Button("Start probing");
        acceptButton.setDefaultButton(true);
        acceptButton.setDisable(true);


        VBox content = new VBox(10, instruction, indicatorRow);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(10));

        ButtonBox buttonBar = new ButtonBox();
        ButtonBox.setButtonData(acceptButton, ButtonBox.ButtonData.OK_DONE);
        Button abortButton = new Button("Abort");
        ButtonBox.setButtonData(abortButton, ButtonBox.ButtonData.LEFT);
        buttonBar.getButtons().addAll(abortButton, acceptButton);

        BorderPane root = new BorderPane();
        root.setCenter(content);
        root.setBottom(buttonBar);

        Scene scene = new Scene(root, 300, 150);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/root.css")).toExternalForm());

        acceptButton.setOnAction(e -> {
            isAccepted = true;
            close();
        });

        abortButton.setOnAction(e -> {
            isAccepted = false;
            close();
        });

        setScene(scene);

        setupWindowListeners();
    }

    private void setupWindowListeners() {
        setOnShown(e -> {
            Window owner = getOwner();
            if (owner != null) {
                double centerX = owner.getX() + owner.getWidth() / 2;
                double centerY = owner.getY() + owner.getHeight() / 2;

                setX(centerX - getWidth() / 2);
                setY(centerY - getHeight() / 2);
            }
        });

        setOnCloseRequest(e -> backend.removeUGSEventListener(this));
    }


    @Override
    public void UGSEvent(UGSEvent event) {
        if (event instanceof ControllerStatusEvent controllerStatusEvent) {
            if (acceptButton.isDisable() && controllerStatusEvent.getStatus().getEnabledPins().probe()) {
                acceptButton.setDisable(false);
            }

            Platform.runLater(() -> updateProbeIndicator(controllerStatusEvent.getStatus().getEnabledPins().probe()));
        }
    }

    private void updateProbeIndicator(boolean touching) {
        if (touching) {
            indicatorDot.setFill(Colors.GREEN);
            indicatorText.setText("Touching");
        } else {
            indicatorDot.setFill(Colors.RED);
            indicatorText.setText("Not touching");
        }
    }

    public boolean isAccepted() {
        return isAccepted;
    }
}
