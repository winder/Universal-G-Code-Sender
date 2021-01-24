package com.willwinder.ugs.nbp.designer.logic.controls;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.gui.Colors;
import com.willwinder.ugs.nbp.designer.logic.events.MouseShapeEvent;
import com.willwinder.ugs.nbp.designer.logic.events.ShapeEvent;
import com.willwinder.ugs.nbp.designer.logic.events.ShapeEventType;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.logging.Logger;

public class MoveControl extends Control {
    private static final Logger LOGGER = Logger.getLogger(MoveControl.class.getSimpleName());

    private Point2D startOffset = new Point2D.Double();
    private Point2D startPosition = new Point2D.Double();

    public MoveControl(Entity parent) {
        super(parent);
    }

    @Override
    public void setSize(Point2D s) {
    }

    @Override
    public Shape getShape() {
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
                startPosition = getParent().getPosition();
                startOffset = new Point2D.Double(mousePosition.getX() - getParent().getPosition().getX(), mousePosition.getY() - getParent().getPosition().getY());
            } else if (mouseShapeEvent.getType() == ShapeEventType.MOUSE_DRAGGED) {
                getParent().move(movement);
            } else if (mouseShapeEvent.getType() == ShapeEventType.MOUSE_RELEASED) {
                LOGGER.info("Stopped moving " + startPosition + " -> " + getParent().getPosition());
            }
        }
    }
}
