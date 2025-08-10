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

import com.willwinder.universalgcodesender.fx.actions.Action;
import com.willwinder.universalgcodesender.fx.component.ButtonBox;
import com.willwinder.universalgcodesender.fx.service.ShortcutService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Optional;

public class ShortcutDialog {

    private final Stage dialog;
    private final Label shortcutLabel = new Label();
    private final Button acceptButton = new Button("Accept");

    private String newShortcut = null;

    public ShortcutDialog(Window parent, Action action) {
        dialog = new Stage();
        dialog.initOwner(parent);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setAlwaysOnTop(true);
        dialog.setTitle("Set Shortcut");

        Label instruction = new Label("Press the new shortcut...");
        Optional<String> currentShortcut = ShortcutService.getShortcut(action.getId());
        shortcutLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        shortcutLabel.setText(currentShortcut.orElse(""));

        acceptButton.setDisable(currentShortcut.isEmpty());

        VBox content = new VBox(10, instruction, shortcutLabel);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(10));

        // ButtonBar at bottom
        ButtonBox buttonBar = new ButtonBox();
        ButtonBox.setButtonData(acceptButton, ButtonBox.ButtonData.OK_DONE);
        Button clearButton = new Button("Clear");
        ButtonBox.setButtonData(clearButton, ButtonBox.ButtonData.LEFT);
        buttonBar.getButtons().addAll(clearButton, acceptButton);

        BorderPane root = new BorderPane();
        root.setCenter(content);
        root.setBottom(buttonBar);

        Scene scene = new Scene(root, 300, 150);
        scene.getStylesheets().add(getClass().getResource("/styles/root.css").toExternalForm());

        // Capture key presses and update label
        scene.setOnKeyPressed(e -> {
            StringBuilder sb = new StringBuilder();
            if (e.isControlDown()) sb.append("CTRL+");
            if (e.isShiftDown()) sb.append("SHIFT+");
            if (e.isAltDown()) sb.append("ALT+");
            if (e.isMetaDown()) sb.append("META+");
            sb.append(e.getCode().getName().toUpperCase());

            newShortcut = sb.toString();
            shortcutLabel.setText(newShortcut);
            acceptButton.setDisable(false);

            e.consume();
        });

        acceptButton.setOnAction(e -> {
            if (newShortcut != null && !newShortcut.isBlank()) {
                ShortcutService.setShortcut(action.getId(), newShortcut);
            }
            dialog.close();
        });

        clearButton.setOnAction(e -> {
            newShortcut = null;
            shortcutLabel.setText("");
            acceptButton.setDisable(true);
            ShortcutService.removeShortcut(action.getId());
        });

        dialog.setScene(scene);

        dialog.setOnShown(e -> {
            Window owner = dialog.getOwner();
            if (owner != null) {
                double centerX = owner.getX() + owner.getWidth() / 2;
                double centerY = owner.getY() + owner.getHeight() / 2;

                dialog.setX(centerX - dialog.getWidth() / 2);
                dialog.setY(centerY - dialog.getHeight() / 2);
            }
        });
    }

    /**
     * Shows the dialog and waits for it to be closed.
     */
    public void showAndWait() {
        dialog.showAndWait();
    }
}