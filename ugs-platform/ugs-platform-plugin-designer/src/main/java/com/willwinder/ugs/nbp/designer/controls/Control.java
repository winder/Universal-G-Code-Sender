package com.willwinder.ugs.nbp.designer.controls;


import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.logic.events.EntityListener;
import com.willwinder.ugs.nbp.designer.selection.SelectionManager;

import java.awt.*;


public abstract class Control extends Entity implements EntityListener {

    private final SelectionManager selectionManager;

    public Entity getParent() {
        return parent;
    }

    private final Entity parent;

    public Control(Entity parent, SelectionManager selectionManager) {
        super();
        this.parent = parent;
        this.selectionManager = selectionManager;
        this.addListener(this);
    }

    @Override
    public void drawShape(Graphics2D g) {
        g.setStroke(new BasicStroke(1));
        g.setColor(Color.RED);
        Shape transformedShape = getGlobalTransform().createTransformedShape(getBounds());
        g.draw(transformedShape);
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }
}
