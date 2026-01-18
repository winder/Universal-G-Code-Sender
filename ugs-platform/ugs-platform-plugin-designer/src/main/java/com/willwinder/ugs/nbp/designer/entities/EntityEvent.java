/*
    Copyright 2021-2026 Joacim Breiler

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.nbp.designer.entities;

import java.util.Optional;

/**
 * @author Joacim Breiler
 */
public class EntityEvent {
    private final Entity target;
    private final EventType type;
    private final Entity parent;

    public EntityEvent(Entity target, EventType type) {
        this(target, null, type);
    }

    public EntityEvent(Entity entity, Entity parent, EventType eventType) {
        this.target = entity;
        this.type = eventType;
        this.parent = parent;
    }

    public EventType getType() {
        return type;
    }

    public Entity getTarget() {
        return target;
    }

    public Optional<Entity> getParent() {
        return Optional.ofNullable(parent);
    }

    @Override
    public String toString() {
        return EntityEvent.class.getSimpleName() + " " + type + " " + target + " " + parent;
    }
}
