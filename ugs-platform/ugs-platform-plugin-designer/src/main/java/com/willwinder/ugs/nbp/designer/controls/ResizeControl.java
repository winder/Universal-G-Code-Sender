package com.willwinder.ugs.nbp.designer.controls;


import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.gui.Colors;
import com.willwinder.ugs.nbp.designer.logic.events.MouseEntityEvent;
import com.willwinder.ugs.nbp.designer.logic.events.EntityEvent;
import com.willwinder.ugs.nbp.designer.logic.events.EntityEventType;
import com.willwinder.ugs.nbp.designer.selection.SelectionManager;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class ResizeControl extends Control {
    public static final int SIZE = 7;
    private final Rectangle shape;
    private final Location location;

    public ResizeControl(Entity parent, SelectionManager selectionManager, Location location) {
        super(parent, selectionManager);
        this.location = location;
        shape = new Rectangle(0, 0, SIZE, SIZE);

        updatePosition();

        parent.addListener((event) -> {
            if (event.getType() == EntityEventType.RESIZED) {
                updatePosition();
            }
        });
    }

    private void updatePosition() {
        Point2D topLeft = new Point2D.Double(-MoveControl.SIZE - (SIZE / 2d), -MoveControl.SIZE - (SIZE / 2d));
        Point2D bottomRight = new Point2D.Double(getParent().getBounds().getWidth() + MoveControl.SIZE - (SIZE / 2d), getParent().getBounds().getHeight() + MoveControl.SIZE - (SIZE / 2d));

        AffineTransform affineTransform = new AffineTransform();
        if (location == Location.TOP_LEFT) {
            affineTransform.translate(topLeft.getX(), topLeft.getY());
        } else if (location == Location.TOP_RIGHT) {
            affineTransform.translate(bottomRight.getX(), topLeft.getY());
        } else if (location == Location.BOTTOM_LEFT) {
            affineTransform.translate(topLeft.getX(), bottomRight.getY());
        } else if (location == Location.BOTTOM_RIGHT) {
            affineTransform.translate(bottomRight.getX(), bottomRight.getY());
        }

        setTransform(affineTransform);
    }

    @Override
    public Shape getShape() {
        return shape;
    }

    @Override
    public void setSize(Point2D s) {

    }

    @Override
    public void drawShape(Graphics2D g) {
        Shape transformedShape = getGlobalTransform().createTransformedShape(shape);

        g.setStroke(new BasicStroke(1));
        g.setColor(Colors.CONTROL_HANDLE);
        g.fill(transformedShape);
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        if (entityEvent instanceof MouseEntityEvent && entityEvent.getShape() == this) {
            MouseEntityEvent mouseShapeEvent = (MouseEntityEvent) entityEvent;
            Point2D mousePosition = mouseShapeEvent.getCurrentMousePosition();
            double deltaX = mousePosition.getX() - getParent().getPosition().getX();
            double deltaY = mousePosition.getY() - getParent().getPosition().getY();

            if (location == Location.TOP_LEFT) {
                getParent().move(new Point2D.Double(deltaX, deltaY));
                Point size = getParent().getSize();
                getParent().setSize(new Point2D.Double(size.x - deltaX, size.y - deltaY));
            }
        }
    }
}
