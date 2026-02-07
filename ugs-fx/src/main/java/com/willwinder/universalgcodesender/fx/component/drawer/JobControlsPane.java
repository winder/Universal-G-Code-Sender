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
package com.willwinder.universalgcodesender.fx.component.drawer;


import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.fx.actions.Action;
import com.willwinder.universalgcodesender.fx.actions.OpenFileAction;
import com.willwinder.universalgcodesender.fx.actions.PauseAction;
import com.willwinder.universalgcodesender.fx.actions.StartAction;
import com.willwinder.universalgcodesender.fx.actions.StopAction;
import com.willwinder.universalgcodesender.fx.control.ActionButton;
import com.willwinder.universalgcodesender.fx.helper.Colors;
import com.willwinder.universalgcodesender.fx.service.ActionRegistry;
import com.willwinder.universalgcodesender.gcode.GcodeStats;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.model.events.StreamEvent;
import com.willwinder.universalgcodesender.model.events.StreamEventType;
import static com.willwinder.universalgcodesender.model.events.StreamEventType.STREAM_CANCELED;
import static com.willwinder.universalgcodesender.model.events.StreamEventType.STREAM_COMPLETE;
import static com.willwinder.universalgcodesender.model.events.StreamEventType.STREAM_STARTED;
import com.willwinder.universalgcodesender.utils.RefreshThread;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.Optional;

public class JobControlsPane extends VBox {
    private final Label fileNameLabel = new Label("No file loaded");
    private final Label fileInfoLabel = new Label("");
    private final Label progressLabel = new Label("00:00:00");
    private final Label timeLeftLabel = new Label("00:00:00");
    private final ProgressBar progressBar = new ProgressBar(0);
    private final Label progressBarLabel = new Label();
    private final BackendAPI backendAPI;
    private RefreshThread refreshThread;

    public JobControlsPane() {
        super(12);
        getStyleClass().add("job-card");
        getStylesheets().add(
                getClass().getResource("/styles/job-controls-pane.css").toExternalForm()
        );

        backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
        backendAPI.addUGSEventListener(this::onEvent);

        getChildren().addAll(
                createFileSection(),
                createProgressSection(),
                createControlSection()
        );
    }

    private HBox createFileSection() {
        fileNameLabel.getStyleClass().add("job-file-name");
        fileInfoLabel.getStyleClass().add("job-file-info");

        setGcodeStats(0, Position.ZERO, Position.ZERO);

        HBox section = new HBox(8);
        Optional<ActionButton> openButton = ActionRegistry.getInstance()
                .getAction(OpenFileAction.class.getCanonicalName())
                .map(action -> createActionButton(action, false, "job-file-open-button"));

        openButton.ifPresent(actionButton -> section.getChildren().add(actionButton));

        section.getChildren().add(new VBox(4, fileNameLabel, fileInfoLabel));
        section.getStyleClass().add("job-section");
        section.setAlignment(Pos.CENTER_LEFT);
        return section;
    }

    private VBox createProgressSection() {
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.getStyleClass().add("job-progress-bar");

        progressBarLabel.getStyleClass().add("job-progress-text");
        progressBarLabel.setMouseTransparent(true); // clicks go through

        StackPane progressStack = new StackPane(progressBar, progressBarLabel);
        StackPane.setAlignment(progressBarLabel, Pos.CENTER);

        HBox labelsRow = new HBox();
        labelsRow.setAlignment(Pos.CENTER_LEFT);
        labelsRow.setFillHeight(true);

        HBox.setHgrow(progressLabel, Priority.ALWAYS);
        progressLabel.setMaxWidth(Double.MAX_VALUE);
        progressLabel.setAlignment(Pos.CENTER_LEFT);
        timeLeftLabel.setAlignment(Pos.CENTER_RIGHT);
        labelsRow.getChildren().addAll(progressLabel, timeLeftLabel);

        VBox box = new VBox(6, progressStack, labelsRow);
        box.getStyleClass().add("job-section");

        return box;
    }

    private HBox createControlSection() {
        HBox box = new HBox(20);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("job-controls");

        ActionRegistry.getInstance().getAction(StartAction.class.getCanonicalName())
                .ifPresent(a -> box.getChildren().add(createActionButton(a, true, "job-controls-button")));
        ActionRegistry.getInstance().getAction(PauseAction.class.getCanonicalName())
                .ifPresent(a -> box.getChildren().add(createActionButton(a, true, "job-controls-button")));
        ActionRegistry.getInstance().getAction(StopAction.class.getCanonicalName())
                .ifPresent(a -> box.getChildren().add(createActionButton(a, true, "job-controls-button")));
        return box;
    }

    private void onStreamRefresh() {
        Platform.runLater(() -> {
            progressBar.setProgress((double) backendAPI.getNumSentRows() / backendAPI.getNumRows());
            progressLabel.setText(Utils.formattedMillis(backendAPI.getSendDuration()));
            timeLeftLabel.setText("Time left: " + Utils.formattedMillis(backendAPI.getSendRemainingDuration()));
            progressBarLabel.setText(String.format("%d / %d (%d%%)", backendAPI.getNumCompletedRows(), backendAPI.getNumRows(), (int) (backendAPI.getNumCompletedRows() * 100 / backendAPI.getNumRows())));
        });
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof FileStateEvent) {
            if (backendAPI.getGcodeFile() == null) {
                fileNameLabel.setText("No file loaded");
                fileInfoLabel.setText("");
                return;
            }

            fileNameLabel.setText(backendAPI.getGcodeFile().getName());
            GcodeStats stats = backendAPI.getGcodeStats();
            setGcodeStats(backendAPI.getNumRows(), stats.getMin(), stats.getMax());
        } else if (event instanceof StreamEvent streamEvent) {
            StreamEventType streamEventType = streamEvent.getType();
            if (streamEventType == STREAM_STARTED) {
                if (refreshThread != null) {
                    refreshThread.interrupt();
                }
                refreshThread = new RefreshThread(this::onStreamRefresh, 500);
                refreshThread.start();
            } else if (streamEventType == STREAM_CANCELED || streamEventType == STREAM_COMPLETE) {
                refreshThread.interrupt();
            }
        } else if (event instanceof ControllerStateEvent controllerStateEvent) {
            var state = controllerStateEvent.getState();
            if (state == ControllerState.RUN) {
                setProgressBarColor(Colors.GREEN);
            } else if (state == ControllerState.HOLD) {
                setProgressBarColor(Colors.ORANGE);
            } else if (state == ControllerState.ALARM) {
                setProgressBarColor(Colors.RED);
            } else {
                setProgressBarColor(Colors.BLUE);
            }
        }
    }

    private void setGcodeStats(long numberOfLines, Position min, Position max) {
        String info = String.format(
                "%,d lines · %.1f × %.1f mm",
                numberOfLines,
                max.getX() - min.getX(),
                max.getY() - min.getY()
        );

        fileInfoLabel.setText(info);
    }

    private void setProgressBarColor(Color color) {
        var style = "-fx-background-color: " + toWebHex(color) + ";";
        progressBar.lookup(".bar").setStyle(style);
    }

    public static String toWebHex(Color color) {
        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);
        return String.format("#%02X%02X%02X", r, g, b);
    }

    private static ActionButton createActionButton(Action action, boolean showText, String className) {
        ActionButton actionButton = new ActionButton(action, 24, showText);
        actionButton.getStyleClass().add(className);
        return actionButton;
    }
}
