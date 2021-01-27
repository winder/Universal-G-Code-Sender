package com.willwinder.ugs.nbp.designer.logic.events;


import com.willwinder.ugs.nbp.designer.entities.Entity;

public class EntityEvent {
    private final Entity shape;
    private EntityEventType type;

    public EntityEvent(Entity shape, EntityEventType type) {
        this.shape = shape;
        this.type = type;
    }

    public EntityEventType getType() {
        return type;
    }

    public void setType(EntityEventType type) {
        this.type = type;
    }

    public Entity getShape() {
        return shape;
    }
}
