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

import com.google.gson.annotations.Expose;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;

import java.io.Serializable;

/**
 * @author Joacim Breiler
 */
public class EntityV1 implements Serializable {

    @Expose
    private String name;

    @Expose
    private EntityTypeV1 type;


    public EntityV1() {
    }

    public EntityV1(EntityTypeV1 type) {
        this.type = type;
    }



    public EntityTypeV1 getType() {
        return type;
    }

    public void setType(EntityTypeV1 type) {
        this.type = type;
    }

    protected void applyCommonAttributes(Entity entity) {
        entity.setName(name);
    }

    public Entity toInternal() {
        return new EntityGroup();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
