package com.willwinder.universalgcodesender.fx.component.jog;

import com.willwinder.universalgcodesender.fx.actions.LongPressMouseEventProxy;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;

public class DirectionalPadPane extends GridPane {

    List<JogButton> buttons = new ArrayList<>();

    public DirectionalPadPane() {
        setAlignment(Pos.CENTER);
        setHgap(0);
        setVgap(0);


        JogButton yPosButton = new JogButton(JogButtonEnum.BUTTON_YPOS);
        yPosButton.setStyle("-fx-border-width: 1 1 0 1; -fx-border-radius: 8 8 0 0");
        buttons.add(yPosButton);

        JogButton xNegButton = new JogButton(JogButtonEnum.BUTTON_XNEG);
        xNegButton.setStyle("-fx-border-width: 1 0 1 1; -fx-border-radius: 8 0 0 8");
        buttons.add(xNegButton);

        JogButton xPosButton = new JogButton(JogButtonEnum.BUTTON_XPOS);
        xPosButton.setStyle("-fx-border-width: 1 1 1 0; -fx-border-radius: 0 8 8 0");
        buttons.add(xPosButton);

        JogButton yNegButton = new JogButton(JogButtonEnum.BUTTON_YNEG);
        yNegButton.setStyle("-fx-border-width: 0 1 1 1; -fx-border-radius: 0 0 8 8");
        buttons.add(yNegButton);

        JogButton diagXnegYposButton = new JogButton(JogButtonEnum.BUTTON_DIAG_XNEG_YPOS);
        diagXnegYposButton.setStyle("-fx-border-radius: 50%; -fx-background-radius: 50%;");
        buttons.add(diagXnegYposButton);

        JogButton diagXnegYnegButton = new JogButton(JogButtonEnum.BUTTON_DIAG_XNEG_YNEG);
        diagXnegYnegButton.setStyle("-fx-border-radius: 50%; -fx-background-radius: 50%;");
        buttons.add(diagXnegYnegButton);

        JogButton diagXposYposButton = new JogButton(JogButtonEnum.BUTTON_DIAG_XPOS_YPOS);
        diagXposYposButton.setStyle("-fx-border-radius: 50%; -fx-background-radius: 50%;");
        buttons.add(diagXposYposButton);

        JogButton diagXposYnegButton = new JogButton(JogButtonEnum.BUTTON_DIAG_XPOS_YNEG);
        diagXposYnegButton.setStyle("-fx-border-radius: 50%; -fx-background-radius: 50%;");
        buttons.add(diagXposYnegButton);

        JogButton zPosButton = new JogButton(JogButtonEnum.BUTTON_ZPOS);
        GridPane.setMargin(zPosButton, new Insets(0, 0, 0, 30));
        buttons.add(zPosButton);

        JogButton zNegButton = new JogButton(JogButtonEnum.BUTTON_ZNEG);
        GridPane.setMargin(zNegButton, new Insets(0, 0, 0, 30));
        buttons.add(zNegButton);


        JogButton cancel = new JogButton(JogButtonEnum.BUTTON_CANCEL);
        cancel.setStyle("-fx-border-color: transparent");
        buttons.add(cancel);

        bindButtonSize(yPosButton);
        bindButtonSize(yNegButton);
        bindButtonSize(xNegButton);
        bindButtonSize(xPosButton);
        bindButtonSize(zPosButton);
        bindButtonSize(zNegButton);
        bindButtonSize(cancel);

        bindDiagonalButtonSize(diagXnegYposButton);
        bindDiagonalButtonSize(diagXnegYnegButton);
        bindDiagonalButtonSize(diagXposYposButton);
        bindDiagonalButtonSize(diagXposYnegButton);

        add(yPosButton, 1, 0);
        add(xNegButton, 0, 1);
        add(cancel, 1, 1);
        add(xPosButton, 2, 1);
        add(yNegButton, 1, 2);
        add(zPosButton, 3, 0);
        add(zNegButton, 3, 2);

        add(diagXnegYposButton, 0, 0);
        GridPane.setValignment(diagXnegYposButton, VPos.CENTER);
        GridPane.setHalignment(diagXnegYposButton, HPos.CENTER);

        add(diagXnegYnegButton, 0, 2);
        GridPane.setValignment(diagXnegYnegButton, VPos.CENTER);
        GridPane.setHalignment(diagXnegYnegButton, HPos.CENTER);

        add(diagXposYposButton, 2, 0);
        GridPane.setValignment(diagXposYposButton, VPos.CENTER);
        GridPane.setHalignment(diagXposYposButton, HPos.CENTER);

        add(diagXposYnegButton, 2, 2);
        GridPane.setValignment(diagXposYnegButton, VPos.CENTER);
        GridPane.setHalignment(diagXposYnegButton, HPos.CENTER);
    }

    private void bindDiagonalButtonSize(Button button) {
        button.prefWidthProperty().bind(widthProperty().divide(4).divide(1.5));
        button.prefHeightProperty().bind(widthProperty().divide(4).divide(1.5));
    }

    private void bindButtonSize(Region button) {
        button.prefWidthProperty().bind(widthProperty().divide(4));
        button.prefHeightProperty().bind(widthProperty().divide(4));
    }

    public void setOnAction(EventHandler<ActionEvent> eventHandler) {
        //buttons.forEach(button -> button.setOnAction(eventHandler));
    }

    public void setMouseListener(EventHandler<MouseEvent> mouseEventEventHandler) {
        buttons.forEach(button -> button.addEventHandler(MouseEvent.ANY, new LongPressMouseEventProxy(300, mouseEventEventHandler)));
    }
}
