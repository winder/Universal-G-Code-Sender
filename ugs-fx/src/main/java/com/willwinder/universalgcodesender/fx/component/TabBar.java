package com.willwinder.universalgcodesender.fx.component;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

import java.util.Objects;

public class TabBar extends HBox {

    private final ToggleGroup toggleGroup = new ToggleGroup();
    private final ObjectProperty<ToggleButton> selectedTab = new SimpleObjectProperty<>();

    public TabBar() {
        getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/tab-bar.css")).toExternalForm());
        getStyleClass().add("tab-bar");
        setSpacing(8);
        toggleGroup.selectedToggleProperty().addListener((obs, old, val) -> {
            selectedTab.set((ToggleButton) val);
        });
    }

    public void addTab(String title) {
        ToggleButton tab = new ToggleButton(title);
        tab.setToggleGroup(toggleGroup);
        tab.getStyleClass().add("tab-button");
        getChildren().add(tab);

        if (toggleGroup.getSelectedToggle() == null) {
            tab.setSelected(true);
        }
    }

    public ObjectProperty<ToggleButton> selectedTabProperty() {
        return selectedTab;
    }
}