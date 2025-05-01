package com.willwinder.universalgcodesender.fx.component.jog;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

public class VerticalPad extends GridPane {

    List<JogButton> buttons = new ArrayList<>();

    public VerticalPad() {
        JogButton upButton = new JogButton(JogButtonEnum.BUTTON_ZPOS);
        upButton.setStyle("-fx-border-width: 1 1 0 1; -fx-border-radius: 8 8 0 0");
        buttons.add(upButton);

        JogButton downButton = new JogButton(JogButtonEnum.BUTTON_ZNEG);
        downButton.setStyle("-fx-border-width: 0 1 1 1; -fx-border-radius: 0 0 8 8");
        buttons.add(downButton);

        Pane center = new Pane();
        center.setStyle("-fx-background-color: white");

        setAlignment(Pos.CENTER);
        setHgap(0);
        setVgap(0);

        bindButtonSize(upButton);
        bindButtonSize(downButton);

        add(upButton, 0, 0);
        add(downButton, 0, 2);
        add(center, 0, 1);
    }

    private void bindButtonSize(Button button) {
        button.prefWidthProperty().bind(widthProperty().divide(3));
        button.prefHeightProperty().bind(widthProperty());
    }
}
