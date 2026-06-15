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
package com.willwinder.universalgcodesender.fx.component.visualizer.designer;

import com.willwinder.ugs.designer.actions.ToolClipartAction;
import com.willwinder.ugs.designer.actions.ToolDrawCircleAction;
import com.willwinder.ugs.designer.actions.ToolDrawLineAction;
import com.willwinder.ugs.designer.actions.ToolDrawPointAction;
import com.willwinder.ugs.designer.actions.ToolDrawRectangleAction;
import com.willwinder.ugs.designer.actions.ToolDrawTextAction;
import com.willwinder.ugs.designer.actions.ToolImportAction;
import com.willwinder.ugs.designer.actions.ToolSelectAction;
import com.willwinder.ugs.designer.actions.TraceImageAction;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerEventType;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.util.Duration;

import javax.swing.Action;
import javax.swing.SwingUtilities;
import java.util.EnumMap;
import java.util.Map;

/**
 * A toolbox of drawing actions (rectangle, ellipse, point, text, line) and import actions
 * (import file, clipart, trace image) laid out as flowing buttons, mirroring the legacy
 * {@code ToolBox.createToolDropDownButton}. Reuses the legacy Swing actions, dispatching
 * them on the Swing thread, and keeps the active tool button highlighted.
 */
public class DesignToolbar extends FlowPane {
    private static final int ICON_SIZE = 24;

    private final Controller controller;
    private final ToggleGroup toolGroup = new ToggleGroup();
    private final Map<Tool, ToggleButton> toolButtons = new EnumMap<>(Tool.class);

    public DesignToolbar() {
        getStyleClass().add("design-toolbar");
        setHgap(4);
        setVgap(4);
        setPadding(new Insets(8));

        this.controller = ControllerFactory.getController();

        addToolButton(Tool.SELECT, new ToolSelectAction(), ToolSelectAction.SMALL_ICON_PATH, "Select and move shapes");
        addToolButton(Tool.RECTANGLE, new ToolDrawRectangleAction(), ToolDrawRectangleAction.SMALL_ICON_PATH, "Draw rectangle");
        addToolButton(Tool.CIRCLE, new ToolDrawCircleAction(), ToolDrawCircleAction.ICON_SMALL_PATH, "Draw ellipse");
        addToolButton(Tool.POINT, new ToolDrawPointAction(), ToolDrawPointAction.SMALL_ICON_PATH, "Draw point");
        addToolButton(Tool.TEXT, new ToolDrawTextAction(), ToolDrawTextAction.SMALL_ICON_PATH, "Draw text");
        addToolButton(Tool.LINE, new ToolDrawLineAction(), ToolDrawLineAction.SMALL_ICON_PATH, "Draw line");

        addActionButton(new ToolImportAction(), ToolImportAction.SMALL_ICON_PATH, "Import file");
        addActionButton(new ToolClipartAction(), ToolClipartAction.SMALL_ICON_PATH, "Insert clipart");
        addActionButton(new TraceImageAction(), TraceImageAction.SMALL_ICON_PATH, "Trace image");

        controller.addListener(this::onControllerEvent);
        syncSelectedTool();
    }

    private void addToolButton(Tool tool, Action action, String iconPath, String tooltip) {
        ToggleButton button = new ToggleButton();
        button.setToggleGroup(toolGroup);
        decorate(button, iconPath, tooltip);
        button.setOnAction(e -> runAction(action));
        toolButtons.put(tool, button);
        getChildren().add(button);
    }

    private void addActionButton(Action action, String iconPath, String tooltip) {
        Button button = new Button();
        decorate(button, iconPath, tooltip);
        button.setOnAction(e -> runAction(action));
        getChildren().add(button);
    }

    private void decorate(ButtonBase button, String iconPath, String tooltip) {
        SvgLoader.loadImageIcon(iconPath, ICON_SIZE).ifPresent(button::setGraphic);
        Tooltip tip = new Tooltip(tooltip);
        tip.setShowDelay(Duration.millis(100));
        button.setTooltip(tip);
    }

    private void runAction(Action action) {
        SwingUtilities.invokeLater(() -> action.actionPerformed(null));
    }

    private void onControllerEvent(ControllerEventType event) {
        if (event == ControllerEventType.TOOL_SELECTED) {
            Platform.runLater(this::syncSelectedTool);
        }
    }

    private void syncSelectedTool() {
        // Null for tools without a button (e.g. ZOOM, VERTEX), which simply clears the selection.
        toolGroup.selectToggle(toolButtons.get(controller.getTool()));
    }
}
