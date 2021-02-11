package com.willwinder.ugs.nbp.designer.controls;


import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.gui.Colors;
import com.willwinder.ugs.nbp.designer.logic.events.EntityEvent;
import com.willwinder.ugs.nbp.designer.logic.events.EventType;
import com.willwinder.ugs.nbp.designer.logic.events.MouseEntityEvent;
import com.willwinder.ugs.nbp.designer.selection.SelectionManager;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.logging.Logger;

public class ResizeControl extends AbstractControl {
    public static final int SIZE = 8;
    private static final Logger LOGGER = Logger.getLogger(ResizeControl.class.getSimpleName());
    private final Location location;
    private final Rectangle2D shape;
    private AffineTransform transform;
    private Point2D.Double startOffset = new Point2D.Double();
    ;

    public ResizeControl(Entity target, SelectionManager selectionManager, Location location) {
        super(target, selectionManager);
        this.location = location;
        this.shape = new Rectangle2D.Double(0, 0, SIZE, SIZE);
    }

    @Override
    public Shape getShape() {
        return transform.createTransformedShape(getRelativeShape());
    }

    @Override
    public Shape getRelativeShape() {
        return shape;
    }

    private void updatePosition() {
        // Create transformation for where to position the controller in relative space
        AffineTransform transform = getTarget().getGlobalTransform();
        Rectangle bounds = getTarget().getRelativeShape().getBounds();
        transform.translate(bounds.getX(), bounds.getY());

        double halfSize = SIZE / 2d;
        if (location == Location.TOP_RIGHT) {
            transform.translate(bounds.getWidth(), 0);
        } else if (location == Location.BOTTOM_LEFT) {
            transform.translate(0, bounds.getHeight());
        } else if (location == Location.BOTTOM_RIGHT) {
            transform.translate(bounds.getWidth(), bounds.getHeight());
        }

        // Transform the position from relative space to real space
        Point2D center = new Point2D.Double();
        transform.transform(new Point2D.Double(0, 0), center);

        this.transform = new AffineTransform();
        this.transform.translate(center.getX() - halfSize, center.getY() - halfSize);
    }

    @Override
    public void setSize(Dimension s) {

    }

    @Override
    public AffineTransform getGlobalTransform() {
        return this.transform;
    }

    @Override
    public void render(Graphics2D g) {
        updatePosition();
        g.setStroke(new BasicStroke(1));
        g.setColor(Colors.CONTROL_HANDLE);
        g.fill(getShape());
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        if (entityEvent instanceof MouseEntityEvent && entityEvent.getTarget() == this) {
            MouseEntityEvent mouseShapeEvent = (MouseEntityEvent) entityEvent;
            Point2D mousePosition = mouseShapeEvent.getCurrentMousePosition();

            Entity target = getTarget();
            Point2D deltaMovement = new Point2D.Double(mousePosition.getX() - target.getPosition().getX() - startOffset.getX(), mousePosition.getY() - target.getPosition().getY() - startOffset.getY());
            if (mouseShapeEvent.getType() == EventType.MOUSE_PRESSED) {
                startOffset = new Point2D.Double(mousePosition.getX() - target.getPosition().getX(), mousePosition.getY() - target.getPosition().getY());
            } else if (mouseShapeEvent.getType() == EventType.MOUSE_DRAGGED) {
                if (location == Location.TOP_LEFT) {
                    target.move(deltaMovement);
                    Dimension size = getTarget().getSize();
                    target.setSize(new Dimension(Double.valueOf(size.getWidth() - deltaMovement.getX()).intValue(), Double.valueOf(size.getHeight() - deltaMovement.getY()).intValue()));
                }
            } else if (mouseShapeEvent.getType() == EventType.MOUSE_RELEASED) {
                LOGGER.info("Stopped moving " + target.getPosition());
            }
        }
    }
}
