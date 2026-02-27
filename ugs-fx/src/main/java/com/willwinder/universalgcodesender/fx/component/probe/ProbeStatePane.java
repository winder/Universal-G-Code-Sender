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

import com.willwinder.universalgcodesender.fx.service.probe.ProbeEvent;
import com.willwinder.universalgcodesender.fx.service.probe.ProbeStep;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Flow;

/**
 * Displays probe job progress:
 * - Initializes all steps from {@link ProbeEvent.JobCreated}
 * - Shows a spinner for the running step
 * - Shows a green check for completed steps
 * - Shows a red X for failed steps
 */
public class ProbeStatePane extends VBox implements Flow.Subscriber<ProbeEvent> {
    private enum StepStatus {PENDING, WORKING, COMPLETED, FAILED}

    private static final class StepRow {
        private final ProbeStep step;
        private StepStatus status = StepStatus.PENDING;
        private StepPill pill;

        private StepRow(ProbeStep step) {
            this.step = step;
        }
    }

    private final ObservableList<StepRow> rows = FXCollections.observableArrayList();
    private final Map<ProbeStep, StepRow> rowByStep = new LinkedHashMap<>();

    private final FlowPane pillsPane = new FlowPane();

    public ProbeStatePane() {
        super(8);

        getStyleClass().add("probe-state-pane");

        pillsPane.getStyleClass().add("probe-step-pills");
        pillsPane.setHgap(8);
        pillsPane.setVgap(8);

        // Horizontal pills; wrap to next line if the pane gets narrow.
        pillsPane.setPrefWrapLength(420);

        getChildren().addAll(pillsPane);
    }

    public void reset() {
        Platform.runLater(() -> {
            rows.clear();
            rowByStep.clear();
            pillsPane.getChildren().clear();
        });
    }

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(ProbeEvent item) {
        if (item == null) return;

        Platform.runLater(() -> {
            if (item instanceof ProbeEvent.JobCreated jobCreated) {
                rows.clear();
                rowByStep.clear();
                pillsPane.getChildren().clear();

                for (ProbeStep step : jobCreated.steps()) {
                    StepRow row = new StepRow(step);
                    StepPill pill = new StepPill();
                    row.pill = pill;

                    applyStatusToPill(pill, row.status, step.label());

                    rows.add(row);
                    rowByStep.put(step, row);
                    pillsPane.getChildren().add(pill);
                }
            } else if (item instanceof ProbeEvent.StepStarted stepStarted) {
                setStatus(stepStarted.step(), StepStatus.WORKING);
            } else if (item instanceof ProbeEvent.StepCompleted stepCompleted) {
                setStatus(stepCompleted.step(), StepStatus.COMPLETED);
            } else if (item instanceof ProbeEvent.StepFailed stepFailed) {
                setStatus(stepFailed.step(), StepStatus.FAILED);
            }
        });
    }

    private void setStatus(ProbeStep step, StepStatus status) {
        StepRow row = rowByStep.get(step);
        if (row == null) return;

        row.status = status;
        if (row.pill != null) {
            applyStatusToPill(row.pill, status, row.step.label());
        }
    }

    private void applyStatusToPill(StepPill pill, StepStatus status, String text) {
        pill.label.setText(text);

        pill.getStyleClass().removeAll("pending", "working", "ok", "fail");

        // Default state
        pill.spinner.setVisible(false);
        pill.spinner.setManaged(false);
        pill.icon.setVisible(true);
        pill.icon.setManaged(true);

        switch (status) {
            case PENDING -> {
                pill.icon.setText("•");
                pill.getStyleClass().add("pending");
            }
            case WORKING -> {
                pill.spinner.setVisible(true);
                pill.spinner.setManaged(true);
                pill.icon.setVisible(false);
                pill.icon.setManaged(false);
                pill.getStyleClass().add("working");
            }
            case COMPLETED -> {
                pill.icon.setText("✓");
                pill.getStyleClass().add("ok");
            }
            case FAILED -> {
                pill.icon.setText("✕");
                pill.getStyleClass().add("fail");
            }
        }
    }

    @Override
    public void onError(Throwable throwable) {
        // UI component: let the parent decide how/where to display error text.
        // We intentionally do not mutate step status here.
    }

    @Override
    public void onComplete() {
        // No-op
    }

    private static final class StepPill extends HBox {
        private final ProgressIndicator spinner = new ProgressIndicator();
        private final Label icon = new Label();
        private final Label label = new Label();

        private StepPill() {
            super(6);
            getStyleClass().add("probe-step-pill");
            setAlignment(Pos.CENTER_LEFT);

            spinner.setPrefSize(12, 12);
            spinner.setMinSize(12, 12);
            spinner.setMaxSize(12, 12);

            icon.getStyleClass().add("probe-step-icon");
            label.getStyleClass().add("probe-step-label");

            getChildren().addAll(spinner, icon, label);

            // Default visual state
            spinner.setVisible(false);
            spinner.setManaged(false);
        }
    }
}