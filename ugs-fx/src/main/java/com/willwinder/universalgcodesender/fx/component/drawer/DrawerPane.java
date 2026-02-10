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
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.animation.TranslateTransition;
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

public class DrawerPane extends BorderPane {

    private final ToggleGroup toggleGroup;
    private final VBox buttonBox;
    private final List<Drawer> drawers = new ArrayList<>();

    public DrawerPane() {
        getStylesheets().add(getClass().getResource("/styles/drawer-pane.css").toExternalForm());
        getStyleClass().add("drawer-pane");
        setMaxWidth(600);

        setMaxHeight(220);
        setPrefHeight(220);

        toggleGroup = new ToggleGroup();

        JobControlsDrawer jobControlsDrawer = new JobControlsDrawer();
        buttonBox = new VBox(5);
        buttonBox.getStyleClass().add("toggle-group");
        createAndAddButton(jobControlsDrawer, "icons/run.svg", Localization.getString("platform.window.sendstatus"));
        createAndAddButton(new MacrosDrawer(), "icons/robot.svg", Localization.getString("platform.menu.macros"));
        createAndAddButton(new ProbeDrawer(), "icons/probe.svg",  Localization.getString("settings.probe"));
        createAndAddButton(new TerminalDrawer(), "icons/terminal.svg",  Localization.getString("platform.window.serialconsole"));

        toggleGroup.selectToggle(toggleGroup.getToggles().get(0));

        setCenter(jobControlsDrawer);
        setLeft(buttonBox);

        VBox rightBox = new VBox(createCollapseButton());
        rightBox.setPadding(new Insets(5));
        setRight(rightBox);

        toggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> toggleDrawer(newToggle != null));

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

    private void toggleDrawer(boolean drawerVisible) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(300), this);
        if (drawerVisible) {
            tt.setToX(0);
        } else {
            tt.setToX(getWidth() - this.buttonBox.getWidth());
        }
        tt.play();
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
