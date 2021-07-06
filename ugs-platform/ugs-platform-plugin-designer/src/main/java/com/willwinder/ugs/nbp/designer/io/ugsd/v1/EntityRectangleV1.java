package com.willwinder.ugs.nbp.designer.io.ugsd.v1;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;

import java.awt.geom.Point2D;

public class EntityRectangleV1 extends CuttableEntityV1 {
    private double x;
    private double y;
    private double width;
    private double height;
    private double rotation;

    public EntityRectangleV1() {
        super(EntityTypeV1.RECTANGLE);
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public double getRotation() {
        return rotation;
    }

    @Override
    public Entity toInternal() {
        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(width);
        rectangle.setHeight(height);
        rectangle.setPosition(new Point2D.Double(x, y));
        applyCommonAttributes(rectangle);
        return rectangle;
    }
}
