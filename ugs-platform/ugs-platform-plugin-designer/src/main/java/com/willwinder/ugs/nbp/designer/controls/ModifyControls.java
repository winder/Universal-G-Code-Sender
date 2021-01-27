package com.willwinder.ugs.nbp.designer.controls;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.logic.events.EntityEvent;
import com.willwinder.ugs.nbp.designer.selection.SelectionManager;

import java.awt.*;
import java.awt.geom.Point2D;

public class ModifyControls extends Control {

    private final Entity target;

    public ModifyControls(Entity parent, SelectionManager selectionManager) {
        super(parent, selectionManager);
        target = parent;

        addChild(new RotationControl(parent, selectionManager));
        addChild(new ResizeControl(parent, selectionManager, Location.TOP_LEFT));
        addChild(new ResizeControl(parent, selectionManager, Location.TOP_RIGHT));
        addChild(new ResizeControl(parent, selectionManager, Location.BOTTOM_LEFT));
        addChild(new ResizeControl(parent, selectionManager, Location.BOTTOM_RIGHT));
        addChild(new MoveControl(parent, selectionManager));
        target.addChild(this);
    }

    @Override
    public void drawShape(Graphics2D g) {

    }

    @Override
    public void setSize(Point2D s) {

    }

    @Override
    public Shape getShape() {
        return target.getBounds();
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {

    }
}
