package com.willwinder.ugs.nbp.designer.logic.events;


import com.willwinder.ugs.nbp.designer.entities.Entity;

import java.awt.geom.Point2D;

public class MouseEntityEvent extends EntityEvent {

    private final Point2D currentMousePosition;
    private final Point2D startMousePosition;

    public MouseEntityEvent(Entity shape, EntityEventType type, Point2D startMousePosition, Point2D currentMousePosition) {
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
