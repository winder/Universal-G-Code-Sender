package com.willwinder.ugs.designer.logic.controls;




import com.willwinder.ugs.designer.logic.events.MouseShapeEvent;
import com.willwinder.ugs.designer.logic.events.ShapeEvent;
import com.willwinder.ugs.designer.logic.events.ShapeEventType;
import com.willwinder.ugs.designer.entities.Entity;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class RotationControl extends Control {
    public static final int SIZE = 10;
    private final Rectangle2D shape;
    private Point2D mousePoint;
    private double angle;

    public RotationControl(Entity parent) {
        super(parent);
        shape = new Rectangle2D.Double(0, 0, SIZE, SIZE);
        angle = parent.getRotation();
        updatePosition();
    }

    private void updatePosition() {
        shape.setFrame(getParent().getBounds().getX(), getParent().getBounds().getY(), SIZE, SIZE);

        AffineTransform affineTransform = new AffineTransform();
        affineTransform.rotate((angle / 180d) * Math.PI, getParent().getCenter().x, getParent().getCenter().y);
        affineTransform.translate((getParent().getBounds().getWidth() / 2) - (SIZE / 2), -(SIZE * 3));
        setTransform(affineTransform);
    }

    @Override
    public java.awt.Shape getShape() {
        return getTransform().createTransformedShape(shape);
    }

    @Override
    public void setSize(Point2D s) {

    }

    @Override
    public void drawShape(Graphics2D g) {
        updatePosition();

        g.setStroke(new BasicStroke(0));
        g.setColor(Color.GRAY);
        g.fill(getShape());
    }

    @Override
    public void onShapeEvent(ShapeEvent shapeEvent) {
        if( shapeEvent.getType() == ShapeEventType.RESIZED) {
            updatePosition();
        } else if(shapeEvent instanceof MouseShapeEvent && shapeEvent.getShape() == this) {
            MouseShapeEvent mouseShapeEvent = (MouseShapeEvent) shapeEvent;
            Point2D centerPoint = getParent().getCenter();

            try {
                mousePoint = mouseShapeEvent.getCurrentMousePosition();
            } catch (Exception e) {
                e.printStackTrace();
            }

            angle = calcRotationAngleInDegrees(centerPoint, mousePoint);
            getParent().setRotation(angle);
            updatePosition();

        }
    }


    /**
     * Calculates the angle from centerPt to targetPt in degrees.
     * The return should range from [0,360), rotating CLOCKWISE,
     * 0 and 360 degrees represents NORTH,
     * 90 degrees represents EAST, etc...
     *
     * Assumes all points are in the same coordinate space.  If they are not,
     * you will need to call SwingUtilities.convertPointToScreen or equivalent
     * on all arguments before passing them  to this function.
     *
     * @param centerPt   Point we are rotating around.
     * @param targetPt   Point we want to calcuate the angle to.
     * @return angle in degrees.  This is the angle from centerPt to targetPt.
     */
    public static double calcRotationAngleInDegrees(Point2D centerPt, Point2D targetPt)
    {
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
        theta += Math.PI/2.0;

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

        if ( angle > 360 ) {
            angle -= 360;
        }

        return angle;
    }
}
