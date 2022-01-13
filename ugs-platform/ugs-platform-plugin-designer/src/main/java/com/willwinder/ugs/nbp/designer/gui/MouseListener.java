/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.designer.gui;

import com.google.common.collect.Sets;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.entities.controls.Control;
import com.willwinder.ugs.nbp.designer.logic.Controller;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * MouseListener listens to the mouse events in a drawing and modifies the
 * Drawing through a controllers
 *
 * @author Alex Lagerstedt
 * @author Joacim Breiler
 */
public class MouseListener extends MouseAdapter {

    private final Controller controller;
    private final Set<Control> hoveredControls = Sets.newConcurrentHashSet();
    private Point2D startPos;
    private Point2D lastPos;
    private Control selectedControl;

    /**
     * Constructs a new MouseListener
     *
     * @param controller the DrawingController through which the modifications will be
     *                   done
     */
    public MouseListener(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void mouseDragged(MouseEvent m) {
        lastPos = toRelativePoint(m);
        boolean shiftPressed = (m.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0;
        boolean ctrlPressed = (m.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0;
        boolean altPressed = (m.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0;

        if (selectedControl != null) {
            selectedControl.onEvent(new MouseEntityEvent(selectedControl, EventType.MOUSE_DRAGGED, startPos, lastPos, shiftPressed, ctrlPressed, altPressed));
            controller.getDrawing().repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent m) {
        lastPos = toRelativePoint(m);
        boolean shiftPressed = (m.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0;
        boolean ctrlPressed = (m.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0;
        boolean altPressed = (m.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0;


        List<Control> allControls = controller.getDrawing().getControls();
        allControls.forEach(control -> {
            boolean within = control.isWithin(lastPos);

            boolean alreadyHovered = hoveredControls.contains(control);
            if (!within && alreadyHovered) {
                hoveredControls.remove(control);
                control.onEvent(new MouseEntityEvent(control, EventType.MOUSE_OUT, lastPos, lastPos, shiftPressed, ctrlPressed, altPressed));
            } else if (within && !alreadyHovered) {
                hoveredControls.add(control);

                control.onEvent(new MouseEntityEvent(control, EventType.MOUSE_IN, lastPos, lastPos, shiftPressed, ctrlPressed, altPressed));
                control.getHoverCursor().ifPresent(controller::setCursor);
            } else {
                control.onEvent(new MouseEntityEvent(control, EventType.MOUSE_MOVED, lastPos, lastPos, shiftPressed, ctrlPressed, altPressed));
            }
        });

        updateCursor();
    }

    private void updateCursor() {
        hoveredControls.stream()
                .map(Control::getHoverCursor)
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.of(Cursor.getDefaultCursor()))
                .ifPresent(controller::setCursor);
    }

    private Point2D toRelativePoint(MouseEvent m) {
        try {
            return controller.getDrawing().getTransform().inverseTransform(m.getPoint(), new Point2D.Double());
        } catch (Exception e) {
            throw new RuntimeException("Could not transform mouse position", e);
        }
    }

    @Override
    public void mousePressed(MouseEvent m) {
        startPos = toRelativePoint(m);
        boolean shiftPressed = (m.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0;
        boolean ctrlPressed = (m.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0;
        boolean altPressed = (m.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0;

        controller.getDrawing().getControls().stream()
                .filter(control -> control.isWithin(lastPos))
                .findFirst()
                .ifPresent(control -> {
                    selectedControl = control;
                    control.onEvent(new MouseEntityEvent(control, EventType.MOUSE_PRESSED, startPos, startPos, shiftPressed, ctrlPressed, altPressed));
                    controller.getDrawing().repaint();
                });
    }

    @Override
    public void mouseReleased(MouseEvent m) {
        lastPos = toRelativePoint(m);
        boolean shiftPressed = (m.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0;
        boolean ctrlPressed = (m.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0;
        boolean altPressed = (m.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0;

        if (selectedControl != null) {
            selectedControl.onEvent(new MouseEntityEvent(selectedControl, EventType.MOUSE_RELEASED, startPos, lastPos, shiftPressed, ctrlPressed, altPressed));
            selectedControl = null;
            controller.getDrawing().repaint();
        }
    }

    public Set<Control> getHoveredControls() {
        return hoveredControls;
    }
}
