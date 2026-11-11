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
package com.willwinder.universalgcodesender.fx.control;

import com.willwinder.universalgcodesender.fx.actions.Action;
import com.willwinder.universalgcodesender.fx.helper.Colors;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import javafx.animation.PauseTransition;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Polygon;
import javafx.util.Duration;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * A palette button for a group of tools, showing the tool of the group used last with a small
 * triangle in its corner. Clicking it selects that tool, while holding the mouse button down on
 * it, or a right click, opens a menu with every tool of the group. It is shown selected while
 * any of its tools is active, and it switches to a tool of the group that is selected some other
 * way, such as by its shortcut.
 */
public class ToolGroupButton extends ToggleButton {
    private static final Duration HOLD_DURATION = Duration.millis(100);
    private static final double TRIANGLE_PX = 5;
    private static final double TRIANGLE_INSET_PX = 4;

    private final List<Action> actions;
    private final int iconSize;
    private final ContextMenu menu = new ContextMenu();
    private final Map<Action, Tooltip> tooltips = new IdentityHashMap<>();
    private final PauseTransition hold = new PauseTransition(HOLD_DURATION);
    private Action current;
    private boolean openedByHold;

    public ToolGroupButton(List<Action> actions, int iconSize) {
        if (actions.isEmpty()) {
            throw new IllegalArgumentException("A tool group needs at least one tool");
        }
        this.actions = List.copyOf(actions);
        this.iconSize = iconSize;

        for (Action action : this.actions) {
            menu.getItems().add(new ActionMenuItem(action));
            tooltips.put(action, ActionTooltip.create(action));
            action.selectedProperty().addListener((_, _, selected) -> {
                if (selected) {
                    show(action);
                }
                updateSelected();
            });
        }
        show(this.actions.stream().filter(action -> action.selectedProperty().get()).findFirst().orElse(this.actions.getFirst()));
        updateSelected();

        setOnAction(event -> {
            current.handle(event);
            // Activating it toggled the button, but only the active tool decides whether it is selected
            updateSelected();
        });
        hold.setOnFinished(_ -> {
            openedByHold = true;
            disarm();
            showMenu();
        });
        addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
            openedByHold = false;
            if (event.getButton() == MouseButton.PRIMARY) {
                hold.playFromStart();
            } else {
                // Consumed so the button is not armed by any other button
                event.consume();
                if (event.getButton() == MouseButton.SECONDARY) {
                    showMenu();
                }
            }
        });
        // A press held long enough to open the menu does not also select the tool shown
        addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            hold.stop();
            if (openedByHold) {
                event.consume();
            }
        });
    }

    private void showMenu() {
        menu.show(this, Side.RIGHT, 0, 0);
    }

    private void show(Action action) {
        current = action;
        setTooltip(tooltips.get(action));
        StackPane graphic = new StackPane();
        SvgLoader.loadImageIcon(action.getIcon(), iconSize).ifPresent(graphic.getChildren()::add);
        Polygon triangle = new Polygon(TRIANGLE_PX, 0, TRIANGLE_PX, TRIANGLE_PX, 0, TRIANGLE_PX);
        triangle.setFill(Colors.BLACKISH);
        triangle.setTranslateX(TRIANGLE_INSET_PX);
        triangle.setTranslateY(TRIANGLE_INSET_PX);
        StackPane.setAlignment(triangle, Pos.BOTTOM_RIGHT);
        graphic.getChildren().add(triangle);
        setGraphic(graphic);
    }

    private void updateSelected() {
        setSelected(actions.stream().anyMatch(action -> action.selectedProperty().get()));
    }
}
