package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.gui.controls.Control;
import com.willwinder.ugs.nbp.designer.gui.entities.AbstractEntity;
import com.willwinder.ugs.nbp.designer.gui.entities.Ellipse;
import com.willwinder.ugs.nbp.designer.gui.entities.Entity;
import com.willwinder.ugs.nbp.designer.gui.entities.Rectangle;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.Tool;
import com.willwinder.ugs.nbp.designer.gui.entities.EventType;

import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MouseListener listens to the mouse events in a drawing and modifies the
 * Drawing through a DrawingController
 *
 * @author Alex Lagerstedt
 */

public class MouseListener extends MouseAdapter {

    private boolean isDrawing;
    private Controller controller;
    private Point2D startPos;
    private Point2D lastPos;

    private Point2D mouseDelta;

    private AbstractEntity newShape;
    private List<Control> controls;

    /**
     * Constructs a new MouseListener
     *
     * @param controller the DrawingController through which the modifications will be
     *          done
     */
    public MouseListener(Controller controller) {
        this.controller = controller;
        this.newShape = null;
        this.mouseDelta = new Point2D.Double(0, 0);
        this.controls = Collections.emptyList();
    }

    public void mouseDragged(MouseEvent m) {
        Point2D relativeMousePoint = toRelativePoint(m);

        mouseDelta.setLocation(relativeMousePoint.getX() - lastPos.getX(), relativeMousePoint.getY() - lastPos.getY());
        if (isDrawing && (newShape != null)) {
            Point2D position = newShape.getPosition();
            newShape.setSize(new Dimension(Double.valueOf(relativeMousePoint.getX() - position.getX()).intValue(), Double.valueOf(relativeMousePoint.getY() - position.getY()).intValue()));
        }

        if (!controls.isEmpty()) {
            Control control = controls.get(0);
            control.onEvent(new MouseEntityEvent(control, EventType.MOUSE_DRAGGED, startPos, relativeMousePoint));
        }

        controller.getDrawing().repaint();
        lastPos = relativeMousePoint;
    }

    public void mouseMoved(MouseEvent m) {
        lastPos = toRelativePoint(m);
    }

    private Point2D toRelativePoint(MouseEvent m) {
        try {
            return controller.getDrawing().getTransform().inverseTransform(m.getPoint(), null);
            //return m.getPoint();
        } catch (Exception e) {
            throw new RuntimeException("Could not transform mouse position", e);
        }
    }

    public void mousePressed(MouseEvent m) {
        Point2D relativeMousePoint = toRelativePoint(m);
        startPos = relativeMousePoint;

        Tool t = controller.getTool();
        isDrawing = true;
        controls = Collections.emptyList();

        if (t == Tool.SELECT) {


            controls = controller.getSelectionManager()
                    .getControls()
                    .stream()
                    .filter(c -> c.isWithin(relativeMousePoint)).collect(Collectors.toList());

            if (!controls.isEmpty()) {
                Control control = controls.get(0);
                control.onEvent(new MouseEntityEvent(control, EventType.MOUSE_PRESSED, startPos, relativeMousePoint));
            } else {
                Entity entity = controller.getDrawing().getEntitiesAt(startPos)
                        .stream()
                        .min((e1, e2) -> (int) ((e1.getBounds().getWidth() * e1.getBounds().getWidth()) - (e2.getBounds().getWidth() * e2.getBounds().getWidth())))
                        .orElse(null);

                if (((m.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == 0)
                        && !controller.getSelectionManager().isSelected(entity)) {
                    controller.getSelectionManager().clearSelection();
                }

                if (entity != null && !controller.getSelectionManager().isSelected(entity)) {
                    controller.getSelectionManager().addSelection(entity);
                }
            }

            controller.getDrawing().repaint();

        } else if (t == Tool.RECTANGLE) {
            newShape = new Rectangle(startPos.getX(), startPos.getY());
        } else if (t == Tool.CIRCLE) {
            newShape = new Ellipse(startPos.getX(), startPos.getY());
        }


        if (newShape != null) {
            controller.addEntity(newShape);
        }

    }

    public void mouseReleased(MouseEvent m) {
        Point2D relativeMousePoint = toRelativePoint(m);
        isDrawing = false;
        newShape = null;

        if (!controls.isEmpty()) {
            Control control = controls.get(0);
            control.onEvent(new MouseEntityEvent(control, EventType.MOUSE_RELEASED, startPos, relativeMousePoint));
        }

        controls = Collections.emptyList();
    }
}
