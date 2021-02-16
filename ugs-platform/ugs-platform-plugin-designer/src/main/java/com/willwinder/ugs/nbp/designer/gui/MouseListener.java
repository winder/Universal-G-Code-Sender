package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.gui.controls.Control;
import com.willwinder.ugs.nbp.designer.gui.entities.AbstractEntity;
import com.willwinder.ugs.nbp.designer.gui.entities.Ellipse;
import com.willwinder.ugs.nbp.designer.gui.entities.Entity;
import com.willwinder.ugs.nbp.designer.gui.entities.Rectangle;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.Tool;
import com.willwinder.ugs.nbp.designer.logic.events.EventType;
import com.willwinder.ugs.nbp.designer.logic.events.MouseEntityEvent;

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

    boolean isDrawing;
    private Controller c;
    private Point2D startPos;
    private Point2D lastPos;

    private Point2D mouseDelta;

    private AbstractEntity newShape;
    private List<Control> controls;

    /**
     * Constructs a new MouseListener
     *
     * @param c the DrawingController through which the modifications will be
     *          done
     */
    public MouseListener(Controller c) {
        this.c = c;
        this.newShape = null;
        this.mouseDelta = new Point2D.Double(0, 0);
        this.controls = Collections.emptyList();
    }

    public void mouseDragged(MouseEvent m) {

        mouseDelta.setLocation(m.getPoint().getX() - lastPos.getX(), m.getPoint().getY() - lastPos.getY());
        if (isDrawing && (newShape != null)) {
            Point2D position = newShape.getPosition();
            newShape.setSize(new Dimension(Double.valueOf(m.getPoint().x - position.getX()).intValue(), Double.valueOf(m.getPoint().y - position.getY()).intValue()));
        }

        if (!controls.isEmpty()) {
            Control control = controls.get(0);
            control.onEvent(new MouseEntityEvent(control, EventType.MOUSE_DRAGGED, startPos, m.getPoint()));
        } /*else if (c.getTool() == Tool.SELECT) {
            for (Entity s : c.getSelectionManager().getShapes()) {
                s.notifyShapeEvent(new MouseShapeEvent(s, ShapeEventType.MOUSE_DRAGGED, startPos, m.getPoint()));
            }
        }*/

        c.getDrawing().repaint();
        lastPos = m.getPoint();
    }

    public void mouseMoved(MouseEvent m) {
        lastPos = m.getPoint();
    }

    public void mousePressed(MouseEvent m) {
        startPos = lastPos;

        Tool t = c.getTool();
        isDrawing = true;
        controls = Collections.emptyList();

        if (t == Tool.SELECT) {


            controls = c.getSelectionManager()
                    .getControls()
                    .stream()
                    .filter(c -> c.isWithin(m.getPoint())).collect(Collectors.toList());

            if (!controls.isEmpty()) {
                Control control = controls.get(0);
                control.onEvent(new MouseEntityEvent(control, EventType.MOUSE_PRESSED, startPos, m.getPoint()));
            } else {
                Entity entity = c.getDrawing().getEntitiesAt(startPos)
                        .stream()
                        .min((e1, e2) -> (int) ((e1.getBounds().getWidth() * e1.getBounds().getWidth()) - (e2.getBounds().getWidth() * e2.getBounds().getWidth())))
                        .orElse(null);

                if (((m.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == 0)
                        && !c.getSelectionManager().contains(entity)) {
                    c.getSelectionManager().removeAll();
                }

                if (entity != null && !c.getSelectionManager().contains(entity)) {
                    c.getSelectionManager().add(entity);
                }
            }

            c.getDrawing().repaint();

        } else if (t == Tool.RECTANGLE) {
            newShape = new Rectangle(startPos.getX(), startPos.getY());
        } else if (t == Tool.CIRCLE) {
            newShape = new Ellipse(startPos.getX(), startPos.getY());
        }


        if (newShape != null) {
            c.addShape(newShape);
        }

    }

    public void mouseReleased(MouseEvent m) {
        isDrawing = false;
        newShape = null;

        if (!controls.isEmpty()) {
            Control control = controls.get(0);
            control.onEvent(new MouseEntityEvent(control, EventType.MOUSE_RELEASED, startPos, m.getPoint()));
        }

        /*else if (c.getTool() == Tool.SELECT) {
            c.getSelection().getShapes().forEach(s -> {
                s.notifyShapeEvent(new MouseShapeEvent(s, ShapeEventType.MOUSE_RELEASED, startPos, m.getPoint()));
            });

            Point total = new Point(m.getPoint().x - startPos.x, m.getPoint().y - startPos.y);
            if ((total.x != 0) || (total.y != 0)) {
                c.recordMovement(total);
            }

        }*/
        controls = Collections.emptyList();
    }

}
