package com.willwinder.ugs.designer.logic.events;


import com.willwinder.ugs.designer.entities.Entity;

public class ShapeEvent {
    private final Entity shape;
    private ShapeEventType type;

    public ShapeEvent(Entity shape, ShapeEventType type) {
        this.shape = shape;
        this.type = type;
    }

    public ShapeEventType getType() {
        return type;
    }

    public void setType(ShapeEventType type) {
        this.type = type;
    }

    public Entity getShape() {
        return shape;
    }
}
