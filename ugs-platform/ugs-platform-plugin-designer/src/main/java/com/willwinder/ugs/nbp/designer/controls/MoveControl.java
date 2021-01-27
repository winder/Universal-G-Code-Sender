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
import java.util.logging.Logger;

public class MoveControl extends Control {
    private static final Logger LOGGER = Logger.getLogger(MoveControl.class.getSimpleName());
    public static final int SIZE = 1;

    private Point2D startOffset = new Point2D.Double();

    public MoveControl(Entity parent, SelectionManager selectionManager) {
        super(parent, selectionManager);
        AffineTransform transform = new AffineTransform();
        transform.translate(-(SIZE/2d), -(SIZE/2d));
        setTransform(transform);
    }

    @Override
    public void setSize(Point2D s) {
    }

    @Override
    public Shape getShape() {
        java.awt.Rectangle bounds = getParent().getBounds();
        bounds.grow(SIZE, SIZE);
        return bounds;
    }

    @Override
    public void drawShape(Graphics2D g) {
        g.setStroke(new BasicStroke(1f));
        g.setColor(Colors.CONTROL_BORDER);

        Shape transformedShape = getParent().getGlobalTransform().createTransformedShape(getShape());
        g.draw(transformedShape);
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        if (entityEvent instanceof MouseEntityEvent && entityEvent.getShape() == this) {
            MouseEntityEvent mouseShapeEvent = (MouseEntityEvent) entityEvent;
            Point2D mousePosition = mouseShapeEvent.getCurrentMousePosition();

            getSelectionManager().getShapes().forEach(entity -> {
                Point2D movement = new Point2D.Double(mousePosition.getX() - entity.getPosition().getX() - startOffset.getX(), mousePosition.getY() - entity.getPosition().getY() - startOffset.getY());

                if (mouseShapeEvent.getType() == EntityEventType.MOUSE_PRESSED) {
                    startOffset = new Point2D.Double(mousePosition.getX() - entityEvent.getShape().getPosition().getX(), mousePosition.getY() - entityEvent.getShape().getPosition().getY());
                } else if (mouseShapeEvent.getType() == EntityEventType.MOUSE_DRAGGED) {
                    entity.move(movement);
                } else if (mouseShapeEvent.getType() == EntityEventType.MOUSE_RELEASED) {
                    LOGGER.info("Stopped moving " + entity.getPosition());
                }
            });
        }
    }
}
