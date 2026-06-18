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
package com.willwinder.universalgcodesender.fx.component.drawer;

import com.willwinder.universalgcodesender.fx.helper.Colors;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import com.willwinder.universalgcodesender.fx.settings.Settings;
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DrawerPane extends BorderPane {

    private final ToggleGroup toggleGroup;
    private final VBox buttonBox;
    private final List<Drawer> drawers = new ArrayList<>();
    private final TranslateTransition slideTransition = new TranslateTransition(Duration.millis(300), this);

    public DrawerPane() {
        getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/drawer-pane.css")).toExternalForm());
        getStyleClass().add("drawer-pane");
        setMaxWidth(600);

        setMaxHeight(220);
        setPrefHeight(220);

        toggleGroup = new ToggleGroup();

        buttonBox = new VBox(5);
        buttonBox.getStyleClass().add("toggle-group");
        createAndAddButton(new JobControlsDrawer(), "icons/run.svg", Localization.getString("platform.window.sendstatus"));
        createAndAddButton(new MacrosDrawer(), "icons/robot.svg", Localization.getString("platform.menu.macros"));
        createAndAddButton(new ProbeDrawer(), "icons/probe.svg",  Localization.getString("settings.probe"));
        createAndAddButton(new TerminalDrawer(), "icons/terminal.svg",  Localization.getString("platform.window.serialconsole"));

        setLeft(buttonBox);

        VBox rightBox = new VBox(createCollapseButton());
        rightBox.setPadding(new Insets(5));
        setRight(rightBox);

        restoreState();
        toggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            boolean expanded = newToggle != null;
            Settings.getInstance().drawerExpandedProperty().set(expanded);
            if (newToggle != null) {
                Settings.getInstance().drawerSelectedIndexProperty()
                        .set(toggleGroup.getToggles().indexOf(newToggle));
            }
            updateDrawerOffset(expanded, true);
        });
    }

    private void restoreState() {
        int index = Settings.getInstance().drawerSelectedIndexProperty().get();
        if (index < 0 || index >= drawers.size()) {
            index = 0;
        }

        Drawer drawer = drawers.get(index);
        setCenter(drawer);

        drawers.forEach(d -> d.setActive(false));
        boolean expanded = Settings.getInstance().drawerExpandedProperty().get();
        if (expanded) {
            drawer.setActive(true);
            toggleGroup.selectToggle(toggleGroup.getToggles().get(index));
        } else {
            updateDrawerOffset(false, false);
        }
    }

    private void updateDrawerOffset(boolean expanded, boolean animate) {
        slideTransition.stop();
        translateXProperty().unbind();

        if (expanded) {
            slideOrSet(0, animate, null);
        } else {
            DoubleBinding collapsedOffset = widthProperty().subtract(buttonBox.widthProperty());
            slideOrSet(collapsedOffset.get(), animate, () -> translateXProperty().bind(collapsedOffset));
        }
    }

    private void slideOrSet(double targetX, boolean animate, Runnable onFinished) {
        if (animate) {
            slideTransition.setToX(targetX);
            slideTransition.setOnFinished(onFinished == null ? null : event -> onFinished.run());
            slideTransition.play();
        } else {
            setTranslateX(targetX);
            if (onFinished != null) {
                onFinished.run();
            }
        }
    }

    private Button createCollapseButton() {
        Button collapseButton = new Button("", SvgLoader.loadImageIcon("icons/caret-double-right.svg", 24).orElse(null));
        collapseButton.getStyleClass().add("collapse-button");
        collapseButton.setOnAction(event -> toggleGroup.selectToggle(null));
        Tooltip tooltip = new Tooltip(Localization.getString("close"));
        tooltip.setShowDelay(Duration.millis(100));
        collapseButton.setTooltip(tooltip);
        return collapseButton;
    }

    private void createAndAddButton(Drawer node, String icon, String tooltipText) {
        drawers.add(node);

        ToggleButton button = new ToggleButton();
        button.getStyleClass().add("toggle-button");
        SvgLoader.loadImageIcon(icon, 32, Colors.BLACKISH).ifPresent(button::setGraphic);
        button.setOnAction(event -> {
            drawers.forEach(d -> d.setActive(false));
            setCenter(node);
            node.setActive(true);
        });

        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setShowDelay(Duration.millis(100));
        button.setTooltip(tooltip);

        button.setToggleGroup(toggleGroup);
        buttonBox.getChildren().add(button);
    }
}
