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

import com.willwinder.universalgcodesender.fx.component.jog.JogButton;
import com.willwinder.universalgcodesender.fx.component.jog.JogButtonEnum;
import com.willwinder.universalgcodesender.fx.component.probe.ProbeStatePane;
import com.willwinder.universalgcodesender.fx.dialog.ProbeConfirmDialog;
import com.willwinder.universalgcodesender.fx.helper.Colors;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import com.willwinder.universalgcodesender.fx.service.probe.ProbeService;
import com.willwinder.universalgcodesender.fx.settings.ProbeSettings;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;

/**
 * The pane shown when the operator needs to change the tool. It provides jog controls for positioning the
 * machine together with actions for zeroing and probing the Z axis.
 *
 * <p>The jog buttons are the same as in the jog pane, so a short click steps the machine and a long press
 * jogs continuously until released.
 *
 * @author Joacim Breiler
 */
class ToolChangeJogPane extends VBox {
    private static final int JOG_BUTTON_SIZE = 64;
    private static final int ACTION_BUTTON_HEIGHT = 40;
    private static final int ICON_SIZE = 20;

    private final BackendAPI backend;
    private final ProbeService probeService;
    private final ProbeStatePane probeStatePane = new ProbeStatePane();
    private final GridPane jogPad;
    private final Button zeroZButton;
    private final Button probeZButton;

    ToolChangeJogPane(BackendAPI backend) {
        super(14);
        this.backend = backend;
        this.probeService = new ProbeService(backend, ProbeSettings.getInstance());
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(8, 0, 0, 0));

        jogPad = createJogPad();
        zeroZButton = createActionButton(Localization.getString("mainWindow.swing.resetCoordinatesButton") + " Z", "icons/resetzero.svg");
        zeroZButton.setOnAction(event -> zeroZ());
        probeZButton = createActionButton(Localization.getString("probe.action.z"), "icons/probe.svg");
        probeZButton.setOnAction(event -> probeZ());

        HBox actions = new HBox(10, zeroZButton, probeZButton);
        actions.setAlignment(Pos.CENTER);
        HBox.setHgrow(zeroZButton, Priority.ALWAYS);
        HBox.setHgrow(probeZButton, Priority.ALWAYS);

        getChildren().addAll(jogPad, actions, probeStatePane);
        updateEnabledState();
    }

    private GridPane createJogPad() {
        GridPane pad = new GridPane();
        pad.setAlignment(Pos.CENTER);
        pad.setHgap(6);
        pad.setVgap(6);

        pad.add(createJogButton(JogButtonEnum.BUTTON_YPOS), 1, 0);
        pad.add(createJogButton(JogButtonEnum.BUTTON_XNEG), 0, 1);
        pad.add(createJogButton(JogButtonEnum.BUTTON_XPOS), 2, 1);
        pad.add(createJogButton(JogButtonEnum.BUTTON_YNEG), 1, 2);

        JogButton zPosButton = createJogButton(JogButtonEnum.BUTTON_ZPOS);
        GridPane.setMargin(zPosButton, new Insets(0, 0, 0, 24));
        pad.add(zPosButton, 3, 0);

        JogButton zNegButton = createJogButton(JogButtonEnum.BUTTON_ZNEG);
        GridPane.setMargin(zNegButton, new Insets(0, 0, 0, 24));
        pad.add(zNegButton, 3, 2);
        return pad;
    }

    private JogButton createJogButton(JogButtonEnum buttonEnum) {
        JogButton button = new JogButton(buttonEnum);
        button.setMinSize(JOG_BUTTON_SIZE, JOG_BUTTON_SIZE);
        button.setPrefSize(JOG_BUTTON_SIZE, JOG_BUTTON_SIZE);
        button.setFocusTraversable(false);
        return button;
    }

    private Button createActionButton(String text, String iconUrl) {
        Button button = new Button(text);
        button.setFocusTraversable(false);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setMinHeight(ACTION_BUTTON_HEIGHT);
        button.setGraphicTextGap(8);
        SvgLoader.loadImageIcon(iconUrl, ICON_SIZE, Colors.BLACKISH).ifPresent(button::setGraphic);
        return button;
    }

    void updateEnabledState() {
        ControllerState state = backend.getControllerState();
        boolean canJog = backend.isConnected() && (state == ControllerState.IDLE || state == ControllerState.JOG);
        jogPad.setDisable(!canJog);
        zeroZButton.setDisable(!backend.isIdle());
        probeZButton.setDisable(!backend.isIdle());
    }

    private void zeroZ() {
        try {
            backend.resetCoordinateToZero(Axis.Z);
        } catch (Exception e) {
            GUIHelpers.displayErrorDialog(e.getLocalizedMessage());
        }
    }

    private void probeZ() {
        if (!ProbeSettings.getSkipProbeCheck()) {
            Window owner = getScene() == null ? null : getScene().getWindow();
            ProbeConfirmDialog confirmDialog = new ProbeConfirmDialog(owner);
            confirmDialog.showAndWait();
            if (!confirmDialog.isAccepted()) {
                return;
            }
        }

        if (!backend.isConnected() || !backend.isIdle()) {
            return;
        }

        probeStatePane.reset();
        probeService.probeZ(probeStatePane);
    }
}
