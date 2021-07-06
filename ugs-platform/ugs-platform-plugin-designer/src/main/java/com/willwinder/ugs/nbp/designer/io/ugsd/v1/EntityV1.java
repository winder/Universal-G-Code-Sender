package com.willwinder.ugs.nbp.designer.io.ugsd.v1;

import com.google.gson.annotations.Expose;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;

import java.io.Serializable;

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
