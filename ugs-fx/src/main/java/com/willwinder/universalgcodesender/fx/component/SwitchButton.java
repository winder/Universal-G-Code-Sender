package com.willwinder.universalgcodesender.fx.component;

import javafx.scene.layout.Region;

import java.util.Objects;

public class SwitchButton extends javafx.scene.control.ToggleButton {
    public SwitchButton() {
        super();
        getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/switch-button.css")).toExternalForm());
        getStyleClass().add("switch-button");

        Region thumb = new Region();
        thumb.getStyleClass().add("thumb");
        setGraphic(thumb);
    }
}
