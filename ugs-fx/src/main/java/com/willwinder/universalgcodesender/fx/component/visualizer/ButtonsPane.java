package com.willwinder.universalgcodesender.fx.component.visualizer;

import com.willwinder.universalgcodesender.fx.helper.Colors;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ButtonsPane extends VBox {
    public ButtonsPane() {
        setMaxWidth(60);
        setAlignment(Pos.CENTER_RIGHT);
        setMaxHeight(60);
        setSpacing(5);

        createAndAddButton("icons/cnc.svg", "test1");
        createAndAddButton("icons/open.svg", "test2");
        createAndAddButton("icons/open.svg", "test3");
        createAndAddButton("icons/open.svg", "test4");
    }

    private void createAndAddButton(String icon, String tooltipText) {
        ToggleButton button = new ToggleButton();
        button.getStyleClass().add("visualizer-toggle-button");
        SvgLoader.loadImageIcon(icon, 32, Colors.BLACKISH).ifPresent(button::setGraphic);
        button.setOnAction(event -> System.out.println(tooltipText));

        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setShowDelay(Duration.millis(100));
        button.setTooltip(tooltip);

        getChildren().add(button);
    }
}
