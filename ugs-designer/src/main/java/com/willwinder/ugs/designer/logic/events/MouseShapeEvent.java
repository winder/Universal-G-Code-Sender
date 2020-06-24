package com.willwinder.ugs.designer.logic.events;


import com.willwinder.ugs.designer.entities.Entity;

import java.awt.geom.Point2D;

public class MouseShapeEvent extends ShapeEvent {

    private final Point2D currentMousePosition;
    private final Point2D startMousePosition;

    public MouseShapeEvent(Entity shape, ShapeEventType type, Point2D startMousePosition, Point2D currentMousePosition) {
        super(shape, type);
        this.currentMousePosition = currentMousePosition;
        this.startMousePosition = startMousePosition;
    }

    public Point2D getCurrentMousePosition() {
        return currentMousePosition;
    }

    public Point2D getStartMousePosition() {
        return startMousePosition;
    }

    public Point2D getMovementDelta() {
        return new Point2D.Double(startMousePosition.getX() - currentMousePosition.getX(), startMousePosition.getY() - currentMousePosition.getY());
    }

    @Override
    public String toString() {
        return "MouseShapeEvent{" +
                "event=" + getType() +
                "startMousePosition=" + startMousePosition +
                "currentMousePosition=" + currentMousePosition +
                '}';
    }
}
