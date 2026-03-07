package com.willwinder.universalgcodesender.fx.component.dro;

import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Duration;
import org.apache.commons.lang3.StringUtils;


public class HighlightableLabel extends HBox {
    private static final Duration HIGHLIGHT_TIME = Duration.millis(500);
    private final Label label;
    private final PauseTransition highlightTimer;

    public HighlightableLabel(String text, Node icon) {
        getStyleClass().add("highlightable-label");

        setAlignment(Pos.CENTER_LEFT);
        setSpacing(10);
        setMaxWidth(Double.MAX_VALUE);

        managedProperty().bind(visibleProperty());

        label = new Label(text);
        HBox.setHgrow(label, Priority.ALWAYS);

        getChildren().add(icon);
        getChildren().add(label);

        highlightTimer = new PauseTransition(HIGHLIGHT_TIME);
        highlightTimer.setOnFinished(e -> getStyleClass().removeAll("highlight"));
        label.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!StringUtils.equals(oldValue, newValue)) {
                flashHighlight();
            }
        });
    }


    private void flashHighlight() {
        getStyleClass().add("highlight");
        highlightTimer.playFromStart();
    }

    public void setText(String text) {
        label.setText(text);
    }
}
