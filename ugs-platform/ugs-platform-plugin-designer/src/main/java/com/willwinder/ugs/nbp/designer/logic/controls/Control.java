package com.willwinder.ugs.nbp.designer.logic.controls;


import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.logic.events.ShapeListener;

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
    public Shape getRawShape() {
        return parent.getRawShape().getBounds();
    }
}
