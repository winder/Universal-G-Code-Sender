package com.willwinder.ugs.nbp.designer.gui.tree;

import com.willwinder.ugs.nbp.designer.entities.Entity;

import javax.swing.tree.DefaultMutableTreeNode;

public class EntityTreeNode extends DefaultMutableTreeNode {

    public EntityTreeNode(Entity entity) {
        super(entity);
    }

    @Override
    public void setUserObject(Object userObject) {
        if (userObject instanceof Entity) {
            super.setUserObject(userObject);
        } else if (userObject instanceof String) {
            getUserObject().setName((String) userObject);
        } else {
            throw new RuntimeException("Invalid object type " + userObject.getClass().getSimpleName());
        }

    }

    public void setName(String name) {
        if (getUserObject() != null) {
            getUserObject().setName(name);
        }
    }

    public String getName() {
        if (getUserObject() != null) {
            return getUserObject().getName();
        }
        return null;
    }

    @Override
    public Entity getUserObject() {
        return (Entity) super.getUserObject();
    }
}
