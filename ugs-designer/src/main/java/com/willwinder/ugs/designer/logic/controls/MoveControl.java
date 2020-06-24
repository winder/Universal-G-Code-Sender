package com.willwinder.ugs.designer.logic.controls;



import com.willwinder.ugs.designer.gui.Colors;
import com.willwinder.ugs.designer.logic.events.MouseShapeEvent;
import com.willwinder.ugs.designer.logic.events.ShapeEvent;
import com.willwinder.ugs.designer.logic.events.ShapeEventType;
import com.willwinder.ugs.designer.entities.Entity;


import java.awt.*;
import java.awt.geom.Point2D;

public class MoveControl extends Control {

    private Point2D startOffset = new Point2D.Double();

    public MoveControl(Entity parent) {
        super(parent);
    }

    @Override
    public void setSize(Point2D s) {
    }

    @Override
    public java.awt.Shape getShape() {
        return getParent().getBoundingBox();
    }

    @Override
    public void drawShape(Graphics2D g) {
        g.setStroke(new BasicStroke(1f));
        g.setColor(Colors.LINE);
        g.draw(getShape());
    }

    @Override
    public void onShapeEvent(ShapeEvent shapeEvent) {
        if (shapeEvent instanceof MouseShapeEvent && shapeEvent.getShape() == this) {
            MouseShapeEvent mouseShapeEvent = (MouseShapeEvent) shapeEvent;
            Point2D mousePosition = mouseShapeEvent.getCurrentMousePosition();

            Point2D movement = new Point2D.Double(mousePosition.getX() - getParent().getPosition().getX() - startOffset.getX(), mousePosition.getY() - getParent().getPosition().getY() - startOffset.getY());

            if (mouseShapeEvent.getType() == ShapeEventType.MOUSE_PRESSED) {
                startOffset = new Point2D.Double(mousePosition.getX() - getParent().getPosition().getX(), mousePosition.getY() - getParent().getPosition().getY());
            } else if (mouseShapeEvent.getType() == ShapeEventType.MOUSE_DRAGGED) {
                System.out.println("Stopped moving " + getParent().getPosition());
                getParent().move(movement);
            } else if (mouseShapeEvent.getType() == ShapeEventType.MOUSE_RELEASED) {
                System.out.println("Stopped moving " + getParent().getPosition());
            }
        }
    }
}
