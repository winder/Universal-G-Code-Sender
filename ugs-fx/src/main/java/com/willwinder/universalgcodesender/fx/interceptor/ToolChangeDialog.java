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
package com.willwinder.universalgcodesender.fx.interceptor;

import com.willwinder.universalgcodesender.fx.component.ButtonBox;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.services.interceptor.InterceptorPrompt;
import com.willwinder.universalgcodesender.services.interceptor.InterceptorState;
import com.willwinder.universalgcodesender.services.interceptor.InterceptorStateEvent;
import com.willwinder.universalgcodesender.services.interceptor.ToolChangeInterceptor;
import com.willwinder.universalgcodesender.services.interceptor.UserResponse;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Objects;

/**
 * A modal dialog that guides the operator through a tool change. It shows a busy indicator while the machine
 * is moving, jog controls with actions for zeroing and probing Z while the tool is being changed, and a plain
 * confirmation for the remaining steps. The header, message and buttons are updated from the current
 * interceptor state.
 *
 * <p>The dialog is only shown once the machine has settled to idle, and its buttons are only enabled while
 * the machine is idle so the operator cannot continue while a move is still in progress.
 *
 * <p>All methods must be called on the JavaFX application thread.
 *
 * @author Joacim Breiler
 */
public class ToolChangeDialog extends Stage implements InterceptorDialog {
    private static final double MIN_WIDTH = 460;
    private static final double MIN_HEIGHT = 500;
    private static final double BUTTON_MIN_WIDTH = 108;

    private final BackendAPI backend;
    private final Label headerLabel = new Label();
    private final Label messageLabel = new Label();
    private final ProgressBar busyIndicator = new ProgressBar();
    private final ToolChangeJogPane jogPane;
    private final StackPane cardPane = new StackPane();
    private final ButtonBox buttonBox = new ButtonBox();

    private InterceptorState interceptorState = InterceptorState.INACTIVE;
    private InterceptorPrompt prompt;

    public ToolChangeDialog(Window owner, BackendAPI backend) {
        this.backend = backend;
        this.jogPane = new ToolChangeJogPane(backend);

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle(Localization.getString("toolchange.title"));
        setOnCloseRequest(Event::consume);
        setMinWidth(MIN_WIDTH);
        setMinHeight(MIN_HEIGHT);

        headerLabel.setFont(Font.font(headerLabel.getFont().getFamily(), FontWeight.BOLD, headerLabel.getFont().getSize() + 4));
        headerLabel.setWrapText(true);
        messageLabel.setWrapText(true);
        busyIndicator.setMaxWidth(Double.MAX_VALUE);
        StackPane.setAlignment(busyIndicator, Pos.TOP_CENTER);

        VBox body = new VBox(12, headerLabel, messageLabel, cardPane);
        body.setPadding(new Insets(16));
        VBox.setVgrow(cardPane, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setCenter(body);
        root.setBottom(buttonBox);

        Scene scene = new Scene(root, MIN_WIDTH, MIN_HEIGHT);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/root.css")).toExternalForm());
        setScene(scene);
        setOnShown(event -> centerOnOwner());
    }

    @Override
    public void handleEvent(InterceptorStateEvent event) {
        interceptorState = event.getState();
        if (interceptorState == InterceptorState.INACTIVE) {
            hide();
            return;
        }

        prompt = event.getPrompt().orElse(null);
        refresh();
    }

    @Override
    public void handleControllerState(ControllerStateEvent event) {
        refresh();
    }

    private void refresh() {
        boolean machineIdle = backend.getControllerState() == ControllerState.IDLE;
        String stepId = prompt == null ? null : prompt.stepId();

        headerLabel.setText(headerFor(interceptorState, stepId));
        messageLabel.setText(bodyFor(interceptorState, stepId));

        boolean busy = interceptorState == InterceptorState.PENDING
                || interceptorState == InterceptorState.RUNNING
                || interceptorState == InterceptorState.RESUMING;
        boolean jogStep = interceptorState == InterceptorState.WAITING_FOR_USER
                && ToolChangeInterceptor.STEP_CHANGE_TOOL.equals(stepId);

        showCard(busy ? busyIndicator : jogStep ? jogPane : null);
        if (jogStep) {
            jogPane.updateEnabledState();
        }

        rebuildButtons(machineIdle);

        // Only show the dialog once the machine has settled to idle, so it does not pop up while the machine
        // is still finishing the commands that were sent before the interception.
        if (!isShowing() && machineIdle) {
            show();
        }
    }

    private void showCard(Node card) {
        if (card == null) {
            cardPane.getChildren().clear();
        } else {
            cardPane.getChildren().setAll(card);
        }
    }

    private void rebuildButtons(boolean machineIdle) {
        buttonBox.getButtons().clear();

        if (interceptorState == InterceptorState.WAITING_FOR_USER && prompt != null) {
            if (prompt.hasOption(UserResponse.ABORT)) {
                addButton(Localization.getString("toolchange.abort"), ButtonBar.ButtonData.LEFT, () -> backend.getInterceptorService().abort(), machineIdle);
            }
            if (prompt.hasOption(UserResponse.SKIP)) {
                addButton(Localization.getString("toolchange.skip"), ButtonBar.ButtonData.OTHER, () -> backend.getInterceptorService().provideUserResponse(UserResponse.SKIP), machineIdle);
            }
            if (prompt.hasOption(UserResponse.CONTINUE)) {
                Button continueButton = addButton(Localization.getString("toolchange.continue"), ButtonBar.ButtonData.OK_DONE, () -> backend.getInterceptorService().provideUserResponse(UserResponse.CONTINUE), machineIdle);
                continueButton.setDefaultButton(true);
            }
        } else if (interceptorState == InterceptorState.FAILED) {
            addButton(Localization.getString("toolchange.abort"), ButtonBar.ButtonData.LEFT, () -> backend.getInterceptorService().abort(), true);
        }
    }

    private Button addButton(String text, ButtonBar.ButtonData buttonData, Runnable action, boolean enabled) {
        Button button = new Button(text);
        button.setFocusTraversable(false);
        button.setDisable(!enabled);
        button.setMinWidth(BUTTON_MIN_WIDTH);
        button.setOnAction(event -> {
            disableButtons();
            action.run();
        });
        ButtonBar.setButtonData(button, buttonData);
        buttonBox.getButtons().add(button);
        return button;
    }

    private void disableButtons() {
        buttonBox.getButtons().forEach(button -> button.setDisable(true));
    }

    private void centerOnOwner() {
        Window owner = getOwner();
        if (owner == null) {
            return;
        }
        setX(owner.getX() + (owner.getWidth() - getWidth()) / 2);
        setY(owner.getY() + (owner.getHeight() - getHeight()) / 2);
    }

    private String bodyFor(InterceptorState state, String stepId) {
        return switch (state) {
            case PENDING -> "The machine is finishing the current commands.";
            case RUNNING -> "Please wait…";
            case WAITING_FOR_USER -> messageFor(stepId);
            case RESUMING -> "Restoring the machine state…";
            case FAILED -> "The tool change could not be completed.";
            case INACTIVE -> "";
        };
    }

    private String messageFor(String stepId) {
        if (stepId == null) {
            return "";
        }

        return switch (stepId) {
            case ToolChangeInterceptor.STEP_CHANGE_TOOL ->
                    String.format(Localization.getString("toolchange.change.message"), currentToolNumber());
            case ToolChangeInterceptor.STEP_CONTINUE -> Localization.getString("toolchange.resume.message");
            default -> "";
        };
    }

    private int currentToolNumber() {
        GcodeState state = backend.getGcodeState();
        return state == null ? 0 : state.toolNumber;
    }

    private static String headerFor(InterceptorState state, String stepId) {
        if (state == InterceptorState.WAITING_FOR_USER && stepId != null) {
            return switch (stepId) {
                case ToolChangeInterceptor.STEP_CHANGE_TOOL -> Localization.getString("toolchange.change.title");
                case ToolChangeInterceptor.STEP_CONTINUE -> Localization.getString("toolchange.resume.title");
                default -> "Action required";
            };
        }

        return switch (state) {
            case PENDING, RUNNING -> "Waiting for the machine to become idle";
            case WAITING_FOR_USER -> "Action required";
            case RESUMING -> "Resuming the job";
            case FAILED -> "Tool change failed";
            case INACTIVE -> "";
        };
    }
}
