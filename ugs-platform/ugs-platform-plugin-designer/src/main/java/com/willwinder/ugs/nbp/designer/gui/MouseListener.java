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

import com.willwinder.ugs.nbp.designer.actions.AddAction;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.entities.controls.Control;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Ellipse;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.Tool;
import com.willwinder.ugs.nbp.designer.model.Size;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * MouseListener listens to the mouse events in a drawing and modifies the
 * Drawing through a DrawingController
 *
 * @author Alex Lagerstedt
 * @author Joacim Breiler
 */
public class MouseListener extends MouseAdapter {

    private boolean isDrawing;
    private Controller controller;
    private Point2D startPos;
    private Point2D lastPos;
    private Point2D mouseDelta;
    private Entity newShape;
    private Set<Control> hoveredControls;

    /**
     * Constructs a new MouseListener
     *
     * @param controller the DrawingController through which the modifications will be
     *                   done
     */
    public MouseListener(Controller controller) {
        this.controller = controller;
        this.newShape = null;
        this.mouseDelta = new Point2D.Double(0, 0);
        this.hoveredControls = new HashSet<>();
    }

    @Override
    public void mouseDragged(MouseEvent m) {
        Point2D relativeMousePoint = toRelativePoint(m);
        boolean shiftPressed = (m.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0;
        boolean ctrlPressed = (m.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0;
        boolean altPressed = (m.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0;

        mouseDelta.setLocation(relativeMousePoint.getX() - lastPos.getX(), relativeMousePoint.getY() - lastPos.getY());
        if (isDrawing && (newShape != null)) {
            Point2D position = newShape.getPosition();
            newShape.setSize(new Size(relativeMousePoint.getX() - position.getX(), relativeMousePoint.getY() - position.getY()));
        }

        if (!hoveredControls.isEmpty()) {
            Control control = hoveredControls.iterator().next();
            control.onEvent(new MouseEntityEvent(control, EventType.MOUSE_DRAGGED, startPos, relativeMousePoint, shiftPressed, ctrlPressed, altPressed));
        }

        controller.getDrawing().repaint();
        lastPos = relativeMousePoint;
    }

    @Override
    public void mouseMoved(MouseEvent m) {
        Point2D relativeMousePoint = toRelativePoint(m);
        boolean shiftPressed = (m.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0;
        boolean ctrlPressed = (m.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0;
        boolean altPressed = (m.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0;

        List<Control> allControls = controller.getSelectionManager().getControls();
        allControls.forEach(control -> {
            boolean within = control.isWithin(relativeMousePoint);
            boolean alreadyHovered = hoveredControls.contains(control);
            if (!within && alreadyHovered) {
                hoveredControls.remove(control);
                control.onEvent(new MouseEntityEvent(control, EventType.MOUSE_OUT, relativeMousePoint, relativeMousePoint, shiftPressed, ctrlPressed, altPressed));
            } else if (within && !alreadyHovered) {
                hoveredControls.add(control);
                control.onEvent(new MouseEntityEvent(control, EventType.MOUSE_IN, relativeMousePoint, relativeMousePoint, shiftPressed, ctrlPressed, altPressed));
            }
        });
        lastPos = toRelativePoint(m);
        controller.getDrawing().repaint();
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
        Point2D relativeMousePoint = toRelativePoint(m);
        boolean shiftPressed = (m.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0;
        boolean ctrlPressed = (m.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0;
        boolean altPressed = (m.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0;
        startPos = relativeMousePoint;

        Tool t = controller.getTool();
        isDrawing = true;

        if (t == Tool.SELECT) {
            if (!hoveredControls.isEmpty() && !shiftPressed) {
                Control control = hoveredControls.iterator().next();
                control.onEvent(new MouseEntityEvent(control, EventType.MOUSE_PRESSED, startPos, relativeMousePoint, shiftPressed, ctrlPressed, altPressed));
            } else {
                Entity entity = controller.getDrawing().getEntitiesAt(startPos)
                        .stream()
                        .min((e1, e2) -> (int) ((e1.getBounds().getWidth() * e1.getBounds().getWidth()) - (e2.getBounds().getWidth() * e2.getBounds().getWidth())))
                        .orElse(null);


                if (entity != null) {
                    if ((m.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) {
                        controller.getSelectionManager().toggleSelection(entity);
                    } else {
                        controller.getSelectionManager().clearSelection();
                        controller.getSelectionManager().addSelection(entity);
                    }
                } else {
                    if (!shiftPressed) {
                        controller.getSelectionManager().clearSelection();
                    }
                }
            }

            controller.getDrawing().repaint();

        } else if (t == Tool.RECTANGLE) {
            newShape = new Rectangle(startPos.getX(), startPos.getY());
        } else if (t == Tool.CIRCLE) {
            newShape = new Ellipse(startPos.getX(), startPos.getY());
        }


        if (newShape != null) {
            AddAction addAction = new AddAction(controller, newShape);
            addAction.actionPerformed(new ActionEvent(this, 0, ""));
            controller.addEntity(newShape);
        }

    }

    @Override
    public void mouseReleased(MouseEvent m) {
        Point2D relativeMousePoint = toRelativePoint(m);
        boolean shiftPressed = (m.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0;
        boolean ctrlPressed = (m.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0;
        boolean altPressed = (m.getModifiersEx() & InputEvent.ALT_DOWN_MASK) != 0;
        isDrawing = false;
        newShape = null;

        if (!hoveredControls.isEmpty() && startPos != null) {
            Control control = hoveredControls.iterator().next();
            control.onEvent(new MouseEntityEvent(control, EventType.MOUSE_RELEASED, startPos, relativeMousePoint, shiftPressed, ctrlPressed, altPressed));
        }
    }
}
