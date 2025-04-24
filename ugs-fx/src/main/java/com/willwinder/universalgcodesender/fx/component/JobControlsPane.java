package com.willwinder.universalgcodesender.fx.component;

import com.willwinder.ugs.nbp.core.actions.PauseAction;
import com.willwinder.ugs.nbp.core.actions.StopAction;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.fx.actions.Action;
import com.willwinder.universalgcodesender.fx.actions.ActionRegistry;
import com.willwinder.universalgcodesender.fx.actions.StartAction;
import com.willwinder.universalgcodesender.fx.control.ActionButton;
import com.willwinder.universalgcodesender.fx.helper.Colors;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.BackendAPI;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class JobControlsPane extends VBox {
    private final Label loadedFileLabel;
    private final Label progressLabel;
    private final BackendAPI backendAPI;
    private final ProgressBar progressBar;
    private RefreshThread refreshThread;

    public JobControlsPane() {
        super(10);
        getStylesheets().add(getClass().getResource("/styles/job-controls-pane.css").toExternalForm());

        loadedFileLabel = new Label("No file loaded");
        loadedFileLabel.setStyle("-fx-font-weight: bold;");
        getChildren().add(loadedFileLabel);

        progressLabel = new Label("");
        getChildren().add(progressLabel);

        progressBar = new ProgressBar(0);
        progressBar.setProgress(0);
        progressBar.setMaxWidth(200);
        progressBar.setPadding(new Insets(0, 10, 0, 10));

        getChildren().add(progressBar);

        HBox hbox = new HBox();
        hbox.setStyle("-fx-spacing: 20; -fx-alignment: center;");
        ActionRegistry.getInstance().getAction(StartAction.class.getCanonicalName()).ifPresent(action -> hbox.getChildren().add(createActionButton(action)));
        ActionRegistry.getInstance().getAction(PauseAction.class.getCanonicalName()).ifPresent(action -> hbox.getChildren().add(createActionButton(action)));
        ActionRegistry.getInstance().getAction(StopAction.class.getCanonicalName()).ifPresent(action -> hbox.getChildren().add(createActionButton(action)));
        getChildren().add(hbox);

        setAlignment(Pos.CENTER);
        backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
        backendAPI.addUGSEventListener(this::onEvent);
    }

    private void onStreamRefresh() {
        Platform.runLater(() -> {
            progressBar.setProgress((double) backendAPI.getNumSentRows() / backendAPI.getNumRows());
            progressLabel.setText(Utils.formattedMillis(backendAPI.getSendRemainingDuration()));
        });
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof FileStateEvent) {
            loadedFileLabel.setText(backendAPI.getGcodeFile() == null ? "No file loaded" : backendAPI.getGcodeFile().getName());
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

    private static ActionButton createActionButton(Action action) {
        return new ActionButton(action, 24, false);
    }
}
