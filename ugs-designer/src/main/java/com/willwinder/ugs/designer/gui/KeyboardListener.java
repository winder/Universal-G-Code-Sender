/*
    Copyright 2022 Will Winder

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
package com.willwinder.ugs.designer.gui;

import com.willwinder.ugs.designer.actions.MoveAction;
import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.EventType;
import com.willwinder.ugs.designer.entities.controls.Control;
import com.willwinder.ugs.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.designer.logic.Controller;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Optional;

/**
 * @author Joacim Breiler
 */
public class KeyboardListener implements KeyListener {

    private static final double DEFAULT_MOVE_STEP = 1.0;

    private final Controller controller;
    private final MouseListener mouseListener;

    /**
     * Constructs a new KeyboardListener
     *
     * @param controller    the controller
     * @param mouseListener a mouse listener to fetch hovered controls from
     */
    public KeyboardListener(Controller controller, MouseListener mouseListener) {
        this.controller = controller;
        this.mouseListener = mouseListener;
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
        mouseListener.getHoveredControls().forEach(control -> control.onEvent(new KeyboardEntityEvent(control, EventType.KEY_TYPED, keyEvent)));
        updateCursor();
    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        if (moveSelection(keyEvent)) {
            keyEvent.consume();
            return;
        }
        mouseListener.getHoveredControls().forEach(control -> control.onEvent(new KeyboardEntityEvent(control, EventType.KEY_PRESSED, keyEvent)));
        updateCursor();
    }

    private boolean moveSelection(KeyEvent keyEvent) {
        if (!controller.getDrawing().isFocusOwner()) {
            return false;
        }

        SelectionManager selectionManager = controller.getSelectionManager();
        if (selectionManager.isEmpty()) {
            return false;
        }

        double step = controller.getDrawing().getSnapToGrid();
        if (step <= 0) {
            step = DEFAULT_MOVE_STEP;
        }

        Point2D deltaMovement = switch (keyEvent.getKeyCode()) {
            case KeyEvent.VK_LEFT -> new Point2D.Double(-step, 0);
            case KeyEvent.VK_RIGHT -> new Point2D.Double(step, 0);
            case KeyEvent.VK_UP -> new Point2D.Double(0, step);
            case KeyEvent.VK_DOWN -> new Point2D.Double(0, -step);
            default -> null;
        };

        if (deltaMovement == null) {
            return false;
        }

        List<Entity> entities = List.copyOf(selectionManager.getSelection());
        MoveAction moveAction = new MoveAction(entities, deltaMovement);
        moveAction.execute();
        controller.getUndoManager().addAction(moveAction);
        controller.getDrawing().repaint();
        return true;
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        mouseListener.getHoveredControls().forEach(control -> control.onEvent(new KeyboardEntityEvent(control, EventType.KEY_RELEASED, keyEvent)));
        updateCursor();
    }

    private void updateCursor() {
        mouseListener.getHoveredControls().stream()
                .map(Control::getHoverCursor)
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.of(Cursor.getDefaultCursor()))
                .ifPresent(controller::setCursor);
    }
}
