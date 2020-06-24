package com.willwinder.ugs.designer.gui;


import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.ugs.designer.logic.controls.Control;
import com.willwinder.ugs.designer.logic.controls.ModifyControls;
import com.willwinder.ugs.designer.logic.events.MouseShapeEvent;
import com.willwinder.ugs.designer.logic.events.ShapeEvent;
import com.willwinder.ugs.designer.logic.events.ShapeEventType;
import com.willwinder.ugs.designer.entities.Ellipse;
import com.willwinder.ugs.designer.entities.Rectangle;
import com.willwinder.ugs.designer.entities.Entity;

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.List;

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

    private Entity newShape;
    private Control control;

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

    }

    public void mouseDragged(MouseEvent m) {

        mouseDelta.setLocation(m.getPoint().getX() - lastPos.getX(), m.getPoint().getY() - lastPos.getY());
        if (isDrawing && (newShape != null)) {
            Point2D position = newShape.getPosition();
            newShape.setSize(new Point2D.Double(m.getPoint().x - position.getX(), m.getPoint().y - position.getY()));
        }

        if (control != null) {
            control.notifyShapeEvent(new MouseShapeEvent(control, ShapeEventType.MOUSE_DRAGGED, startPos, m.getPoint()));
        } else if (c.getTool() == Tool.SELECT) {
            for (Entity s : c.getSelectionManager().getShapes()) {
                s.notifyShapeEvent(new MouseShapeEvent(s, ShapeEventType.MOUSE_DRAGGED, startPos, m.getPoint()));
            }
        }

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
        control = null;

        if (t == Tool.SELECT) {

            List<Entity> shapes = c.getDrawing().getShapesAt(startPos);


            control = shapes.stream().filter(shape -> shape instanceof Control && !(shape instanceof ModifyControls)).map(shape -> (Control) shape).findFirst().orElse(null);
            if (control != null) {
                control.notifyShapeEvent(new MouseShapeEvent(control, ShapeEventType.MOUSE_PRESSED, startPos, m.getPoint()));
            } else {
                Entity tmp = null;
                if (!shapes.isEmpty()) {
                    tmp = shapes.get(shapes.size() - 1);
                }

                if (tmp != null) {
                    tmp.notifyShapeEvent(new MouseShapeEvent(tmp, ShapeEventType.MOUSE_PRESSED, startPos, m.getPoint()));
                }

                if (((m.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == 0)
                        && !c.getSelectionManager().contains(tmp)) {

                    c.getSelectionManager().getShapes().forEach(shape -> {
                        shape.notifyShapeEvent(new ShapeEvent(shape, ShapeEventType.UNSELECTED));
                    });

                    c.getSelectionManager().empty();
                }

                if ((tmp != null) && (!c.getSelectionManager().contains(tmp))) {

                    // empty the selection before selecting a new shape if shift is
                    // not down

                    c.getSelectionManager().add(tmp);
                    tmp.notifyShapeEvent(new ShapeEvent(tmp, ShapeEventType.SELECTED));
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

        if (control != null) {
            control.notifyShapeEvent(new MouseShapeEvent(control, ShapeEventType.MOUSE_RELEASED, startPos, m.getPoint()));
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
        control = null;
    }

}
