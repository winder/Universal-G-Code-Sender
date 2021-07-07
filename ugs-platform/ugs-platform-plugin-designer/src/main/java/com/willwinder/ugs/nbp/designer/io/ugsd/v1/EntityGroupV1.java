/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.designer.io.ugsd.v1;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;

import java.util.List;

/**
 * @author Joacim Breiler
 */
public class EntityGroupV1 extends EntityV1 {

    private List<EntityV1> children;

    public EntityGroupV1() {
        super(EntityTypeV1.GROUP);
    }

    public void setChildren(List<EntityV1> children) {
        this.children = children;
    }

    public List<EntityV1> getChildren() {
        return children;
    }

    @Override
    public Entity toInternal() {
        EntityGroup entityGroup = new EntityGroup();
        children.stream().map(EntityV1::toInternal).forEach(entityGroup::addChild);
        entityGroup.setName(getName());
        return entityGroup;
    }
}
