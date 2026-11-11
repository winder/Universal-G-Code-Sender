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

import com.willwinder.universalgcodesender.fx.component.ButtonBox;
import com.willwinder.universalgcodesender.fx.component.SettingsRow;
import com.willwinder.universalgcodesender.fx.control.UnitTextField;
import com.willwinder.universalgcodesender.fx.helper.WindowHelper;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitValue;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Objects;
import java.util.Optional;

/**
 * Asks for the distance a constraint should keep, centered over its owner window. The result is
 * empty when the dialog is cancelled or accepted without changing the distance, so the exact
 * distance is kept rather than the rounded one the field shows.
 */
public class ConstraintDistanceDialog extends Stage {
    private final UnitTextField distance;
    private final String shownDistance;
    private Double result;

    public ConstraintDistanceDialog(Window owner, double currentDistance) {
        distance = new UnitTextField(new UnitValue(Unit.MM, currentDistance), Unit.MM);
        distance.setMaxWidth(Double.MAX_VALUE);
        shownDistance = distance.getText();
        if (owner != null) {
            initOwner(owner);
        }
        initModality(Modality.APPLICATION_MODAL);
        setTitle(Localization.getString("designer.constraint.distance.title"));

        setScene(createScene());
        setWidth(380);
        setResizable(false);
        setOnShowing(_ -> WindowHelper.centerOnOwner(this));
        setOnShown(_ -> Platform.runLater(() -> {
            distance.requestFocus();
            distance.selectAll();
        }));
    }

    /**
     * Shows the dialog and waits for it to close.
     *
     * @return the entered distance, or empty if it was cancelled or the distance left unchanged
     */
    public Optional<Double> showAndWaitForDistance() {
        showAndWait();
        return Optional.ofNullable(result);
    }

    private Scene createScene() {
        VBox form = new VBox(4, new SettingsRow(Localization.getString("designer.constraint.distance"), distance));
        form.setPadding(new Insets(16));

        Button cancel = new Button(Localization.getString("mainWindow.swing.cancelButton"));
        cancel.setCancelButton(true);
        cancel.setOnAction(_ -> close());
        Button ok = new Button(Localization.getString("mainWindow.swing.okButton"));
        ok.setDefaultButton(true);
        ok.setOnAction(_ -> onOk());
        ButtonBox buttons = new ButtonBox();
        ButtonBox.setButtonData(cancel, ButtonBox.ButtonData.CANCEL_CLOSE);
        ButtonBox.setButtonData(ok, ButtonBox.ButtonData.OK_DONE);
        buttons.getButtons().addAll(cancel, ok);

        BorderPane root = new BorderPane(form);
        root.setBottom(buttons);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/root.css")).toExternalForm());
        return scene;
    }

    private void onOk() {
        if (!distance.getText().trim().equals(shownDistance)) {
            result = distance.getValue();
        }
        close();
    }
}
