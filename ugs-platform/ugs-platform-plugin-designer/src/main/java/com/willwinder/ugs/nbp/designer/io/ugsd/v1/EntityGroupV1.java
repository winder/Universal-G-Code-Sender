package com.willwinder.ugs.nbp.designer.io.ugsd.v1;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;

import java.util.List;

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
