package com.willwinder.ugs.nbp.designer.logic.events;

import com.willwinder.ugs.nbp.designer.gui.entities.Entity;

public class EntityEvent {
    private final Entity target;
    private EventType type;

    public EntityEvent(Entity target, EventType type) {
        this.target = target;
        this.type = type;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public Entity getTarget() {
        return target;
    }
}
