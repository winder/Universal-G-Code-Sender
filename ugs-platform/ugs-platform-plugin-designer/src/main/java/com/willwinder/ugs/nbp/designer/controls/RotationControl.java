package com.willwinder.ugs.nbp.designer.controls;


import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.logic.events.MouseEntityEvent;
import com.willwinder.ugs.nbp.designer.logic.events.EntityEvent;
import com.willwinder.ugs.nbp.designer.logic.events.EntityEventType;
import com.willwinder.ugs.nbp.designer.selection.SelectionManager;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class RotationControl extends Control {
    public static final int SIZE = 6;
    private final Rectangle2D shape;
    private Point2D mousePoint;
    private double angle;

    public RotationControl(Entity parent, SelectionManager selectionManager) {
        super(parent, selectionManager);
        shape = new Rectangle2D.Double(0, 0, SIZE, SIZE);
        angle = parent.getRotation();

        updatePosition();
        parent.addListener(event -> {
            if (event.getType() == EntityEventType.RESIZED) {
                updatePosition();
            }
        });
    }

    private void updatePosition() {
        AffineTransform transform = new AffineTransform();
        transform.translate(getParent().getBounds().getWidth() / 2 - (SIZE / 2d), -(SIZE * 2d));
        setTransform(transform);
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
        g.setStroke(new BasicStroke(0));
        g.setColor(Color.GRAY);

        Shape transformedShape = getGlobalTransform().createTransformedShape(getShape());
        g.fill(transformedShape);
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        if (entityEvent instanceof MouseEntityEvent && entityEvent.getShape() == this) {
            MouseEntityEvent mouseShapeEvent = (MouseEntityEvent) entityEvent;
            Point2D centerPoint = getParent().getCenter();

            try {
                mousePoint = mouseShapeEvent.getCurrentMousePosition();
            } catch (Exception e) {
                e.printStackTrace();
            }

            angle = Math.round(calcRotationAngleInDegrees(centerPoint, mousePoint) / 5d) * 5d;
            getParent().setRotation(angle);
        }
    }


    /**
     * Calculates the angle from centerPt to targetPt in degrees.
     * The return should range from [0,360), rotating CLOCKWISE,
     * 0 and 360 degrees represents NORTH,
     * 90 degrees represents EAST, etc...
     * <p>
     * Assumes all points are in the same coordinate space.  If they are not,
     * you will need to call SwingUtilities.convertPointToScreen or equivalent
     * on all arguments before passing them  to this function.
     *
     * @param centerPt Point we are rotating around.
     * @param targetPt Point we want to calcuate the angle to.
     * @return angle in degrees.  This is the angle from centerPt to targetPt.
     */
    public static double calcRotationAngleInDegrees(Point2D centerPt, Point2D targetPt) {
        // calculate the angle theta from the deltaY and deltaX values
        // (atan2 returns radians values from [-PI,PI])
        // 0 currently points EAST.
        // NOTE: By preserving Y and X param order to atan2,  we are expecting
        // a CLOCKWISE angle direction.
        double theta = Math.atan2(targetPt.getY() - centerPt.getY(), targetPt.getX() - centerPt.getX());

        // rotate the theta angle clockwise by 90 degrees
        // (this makes 0 point NORTH)
        // NOTE: adding to an angle rotates it clockwise.
        // subtracting would rotate it counter-clockwise
        theta += Math.PI / 2.0;

        // convert from radians to degrees
        // this will give you an angle from [0->270],[-180,0]
        double angle = Math.toDegrees(theta);

        // convert to positive range [0-360)
        // since we want to prevent negative angles, adjust them now.
        // we can assume that atan2 will not return a negative value
        // greater than one partial rotation
        if (angle < 0) {
            angle += 360;
        }

        if (angle > 360) {
            angle -= 360;
        }

        return angle;
    }
}
