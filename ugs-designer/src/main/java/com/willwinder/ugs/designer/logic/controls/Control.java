package com.willwinder.ugs.designer.logic.controls;


import com.willwinder.ugs.designer.logic.events.ShapeListener;
import com.willwinder.ugs.designer.entities.Entity;

import java.awt.*;


public abstract class Control extends Entity implements ShapeListener {

    public Entity getParent() {
        return parent;
    }

    private final Entity parent;

    public Control(Entity parent) {
        super();
        this.parent = parent;
        addListener(this);
        if( parent != null) {
            parent.addListener(this);
        }
    }

    @Override
    public void drawShape(Graphics2D g) {
        g.setStroke(new BasicStroke(1));
        g.setColor(Color.RED);
        g.draw(getBounds());
    }

    @Override
    public void destroy() {
        super.destroy();
        if( parent != null) {
            parent.removeListener(this);
        }
    }

    @Override
    public java.awt.Shape getRawShape() {
        return parent.getRawShape().getBounds();
    }
}
