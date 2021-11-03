/*
 * Created on Jun 28, 2004
 *
 */
package org.kabeja.dxf;

import org.kabeja.dxf.helpers.Point;
import org.kabeja.math.MathUtils;
import org.kabeja.math.ParametricPlane;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class DXFArc extends DXFEntity {
    private Point center;
    private double radius;
    private double start_angle;
    private double end_angle;
    private boolean counterclockwise = false;

    public DXFArc() {
        center = new Point();
    }

    /**
     * @return Returns the end_angle.
     */
    public double getEndAngle() {
        return end_angle;
    }

    /**
     * @param end_angle
     *            The end_angle to set.
     */
    public void setEndAngle(double end_angle) {
        this.end_angle = end_angle;
    }

    /**
     * @return Returns the radius.
     */
    public double getRadius() {
        return radius;
    }

    /**
     * @param radius
     *            The radius to set.
     */
    public void setRadius(double radius) {
        this.radius = radius;
    }

    /**
     * @return Returns the start_angle.
     */
    public double getStartAngle() {
        return start_angle;
    }

    /**
     * @param start_angle
     *            The start_angle to set.
     */
    public void setStartAngle(double start_angle) {
        this.start_angle = start_angle;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.kabeja.dxf.DXFEntity#updateViewPort()
     */
    public Bounds getBounds() {
        Bounds bounds = new Bounds();
        Point start = this.getStartPoint();
        Point end = this.getEndPoint();
        bounds.addToBounds(start);
        bounds.addToBounds(end);

        ParametricPlane plane = new ParametricPlane(this.getExtrusion());
        Point center = plane.getPoint(this.center.getX(), this.center.getY());
        int startQ = MathUtils.getQuadrant(start, center);
        int endQ = MathUtils.getQuadrant(end, center);

        if (endQ < startQ) {
            endQ += 4;
        }

        while (endQ > startQ) {
            switch (startQ) {
            case 0:
                bounds.addToBounds(center.getX(), center.getY() + radius,
                    center.getZ());

                break;

            case 1:
                bounds.addToBounds(center.getX() - radius, center.getY(),
                    center.getZ());

                break;

            case 2:
                bounds.addToBounds(center.getX(), center.getY() - radius,
                    center.getZ());

                break;

            case 3:
                bounds.addToBounds(center.getX() + radius, center.getY(),
                    center.getZ());
                endQ -= 4;
                startQ -= 4;

                break;
            }

            startQ++;
        }

        return bounds;
    }

    public void setCenterPoint(Point p) {
        this.center = p;
    }

    public Point getCenterPoint() {
        return center;
    }

    /**
     * Calculate the start point of the arc (defined by the start parameter)
     *
     * @return the start point
     */
    public Point getStartPoint() {
        double angle = this.start_angle;

        // if (this.start_angle < 0) {
        // angle += 360;
        // }
        return this.getPointAt(angle);
    }

    /**
     * Calculate the end point of the arc (defined by the end parameter)
     *
     * @return the end point
     */
    public Point getEndPoint() {
        double angle = this.end_angle;

        // if (this.end_angle < 0) {
        // angle += 360;
        // }
        return this.getPointAt(angle);
    }

    /**
     * Calculate a point of the arc
     *
     * @param angle
     *            in degree
     * @return Point on the circle
     */
    public Point getPointAt(double angle) {
        // the local part
        double x = this.radius * Math.cos(Math.toRadians(angle));
        double y = radius * Math.sin(Math.toRadians(angle));

        // the wcs part
        ParametricPlane plane = new ParametricPlane(this.getExtrusion());
        Point p = plane.getPoint(x + this.center.getX(), y +
                this.center.getY());

        return p;
    }

    /**
     *
     */
    public String getType() {
        return DXFConstants.ENTITY_TYPE_ARC;
    }

    public double getLength() {
        double alpha = this.getTotalAngle();

        return (alpha * Math.PI * this.radius) / 180.0;
    }

    public double getTotalAngle() {
        if (this.end_angle < this.start_angle) {
            return (360 + this.end_angle) - this.start_angle;
        } else {
            return Math.abs(this.end_angle - this.start_angle);
        }
    }

    public double getChordLength() {
        double s = 2 * this.radius * Math.sin(Math.toRadians(
                    this.getTotalAngle() / 2));

        return s;
    }

    public boolean isCounterClockwise() {
        return counterclockwise;
    }

    public void setCounterClockwise(boolean counterclockwise) {
        this.counterclockwise = counterclockwise;
    }
}
