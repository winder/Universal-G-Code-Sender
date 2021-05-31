package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EventType;

import java.awt.geom.Point2D;

public class MouseEntityEvent extends EntityEvent {

    private final Point2D currentMousePosition;
    private final Point2D startMousePosition;

    public MouseEntityEvent(Entity entity, EventType type, Point2D startMousePosition, Point2D currentMousePosition) {
        super(entity, type);
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
}
