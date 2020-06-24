package com.willwinder.ugs.designer.logic.controls;

import com.willwinder.ugs.designer.logic.events.ShapeEvent;
import com.willwinder.ugs.designer.entities.Entity;


import java.awt.*;
import java.awt.geom.Point2D;

public class ModifyControls extends Control {

    private final Entity target;

    public ModifyControls(Entity parent) {
        super(parent);
        target = parent;

        add(new RotationControl(parent));
        //add(new ResizeControl(shape, Location.LEFT));
        //controls.add(new ResizeControl(shape, Location.TOP));
        //controls.add(new ResizeControl(shape, Location.BOTTOM));
        //controls.add(new ResizeControl(shape, Location.RIGHT));
        add(new MoveControl(parent));
        target.add(this);
    }

    @Override
    public void drawShape(Graphics2D g) {

    }

    @Override
    public void setSize(Point2D s) {

    }

    @Override
    public java.awt.Shape getShape() {
        return target.getBounds();
    }

    @Override
    public java.awt.Shape getRawShape() {
        return target.getRawShape().getBounds();
    }

    @Override
    public void onShapeEvent(ShapeEvent shapeEvent) {

    }
}
