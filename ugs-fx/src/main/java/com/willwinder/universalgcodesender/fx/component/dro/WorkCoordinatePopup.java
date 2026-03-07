package com.willwinder.universalgcodesender.fx.component.dro;

import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;

import java.util.function.Consumer;

public class WorkCoordinatePopup extends Popup {
    private final TextField textField = new TextField();

    public WorkCoordinatePopup(String title, String initialValue, Consumer<String> onSubmit) {
        VBox root = new VBox(0);
        root.getStyleClass().add("work-coordinate-popup-root");
        root.getStylesheets().add(getClass().getResource("/styles/work-coordinate-popup.css").toExternalForm());

        Region arrow = new Region();
        arrow.getStyleClass().add("work-coordinate-popup-arrow");

        VBox bubble = new VBox(8);
        bubble.getStyleClass().add("work-coordinate-popup");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("work-coordinate-popup-title");

        textField.setText(initialValue);
        textField.getStyleClass().add("work-coordinate-popup-field");

        Button setButton = new Button("Set");
        setButton.getStyleClass().add("work-coordinate-popup-button");
        setButton.setDefaultButton(true);
        setButton.setMaxHeight(Double.MAX_VALUE);

        HBox actions = new HBox(8, textField, setButton);
        actions.setPadding(new Insets(0));
        actions.setFillHeight(true);
        HBox.setHgrow(textField, Priority.ALWAYS);

        bubble.getChildren().addAll(titleLabel, actions);
        root.getChildren().addAll(arrow, bubble);

        getContent().add(root);
        setAutoHide(true);
        setAutoFix(true);
        setHideOnEscape(true);

        Runnable submit = () -> {
            onSubmit.accept(textField.getText());
            hide();
        };

        setButton.setOnAction(e -> submit.run());
        textField.setOnAction(e -> submit.run());
        textField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                hide();
            }
        });
    }

    public void showBelow(Region anchor) {
        Bounds bounds = anchor.localToScreen(anchor.getBoundsInLocal());
        if (bounds == null) {
            return;
        }

        Window window = anchor.getScene().getWindow();
        show(window, bounds.getMinX(), bounds.getMaxY());

        textField.requestFocus();
        textField.selectAll();
    }
}