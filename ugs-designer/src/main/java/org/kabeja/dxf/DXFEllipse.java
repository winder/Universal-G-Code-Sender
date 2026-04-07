/*
 * Created on Jul 15, 2004
 *
 */
package org.kabeja.dxf;

import org.kabeja.dxf.helpers.Point;
import org.kabeja.dxf.helpers.Vector;
import org.kabeja.math.MathUtils;
import org.kabeja.math.ParametricPlane;


/**
 * @author <a href="mailto:simon.mieth@gmx.de>Simon Mieth</a>
 *
 */
public class DXFEllipse extends DXFEntity {
    public static final double DEFAULT_END_PARAMETER = Math.PI * 2;
    public static final double DEFAULT_START_PARAMETER = 0.0;
    public static final int INTEGRATION_STEPS = 15;
    private double ratio = 1.0;
    private double startParameter = DEFAULT_START_PARAMETER;
    private double endParameter = DEFAULT_END_PARAMETER;
    private Point center = new Point();
    private Vector majorAxisDirection = new Vector();
    private boolean counterclockwise;

    /**
     *
     */
    public DXFEllipse() {
        center = new Point();
    }

    public Bounds getBounds() {
        double alpha = this.getRotationAngle();
        Bounds bounds = new Bounds();

        ParametricPlane plane = new ParametricPlane(this.center,
                this.getExtrusion().getDirectionX(),
                this.getExtrusion().getDirectionY(),
                this.getExtrusion().getNormal());

        if ((this.startParameter == DEFAULT_START_PARAMETER) &&
                (this.endParameter == DEFAULT_END_PARAMETER) && (alpha == 0.0)) {
            double length = this.getHalfMajorAxisLength();

            bounds.addToBounds(plane.getPoint(length, length));
            bounds.addToBounds(plane.getPoint(-length, -length));
        } else {
            int n = 40;

            // we walking over the the ellipse or elliptical arc
            double h = (this.endParameter - this.startParameter) / n;

            double start = this.startParameter;
            double major = this.getHalfMajorAxisLength();
            double minor = major * this.ratio;

            Vector minorAxis = MathUtils.crossProduct(this.getExtrusion()
                                                          .getNormal(),
                    this.getMajorAxisDirection());
            minorAxis = MathUtils.scaleVector(minorAxis, this.ratio);

            for (int i = 0; i <= n; i++) {
                Vector v1 = MathUtils.scaleVector(this.getMajorAxisDirection(),
                        Math.cos(start));
                Vector v2 = MathUtils.scaleVector(minorAxis, Math.sin(start));

                //				double x = major * Math.cos(start);
                //				double y = minor * Math.sin(start);
                double x = v1.getX() + v2.getX();
                double y = v1.getY() + v2.getY();

                //				if (alpha != 0.0) {
                //					double lx = x;
                //					x = lx * Math.cos(alpha) - y * Math.sin(alpha);
                //					y = lx * Math.sin(alpha) + y * Math.cos(alpha);
                //				}
                Point p = plane.getPoint(x, y);

                bounds.addToBounds(p);

                start += h;
            }
        }

        return bounds;
    }

    public Point getCenterPoint() {
        return center;
    }

    public void setCenterPoint(Point center) {
        this.center = center;
    }

    public Vector getMajorAxisDirection() {
        return majorAxisDirection;
    }

    public void setMajorAxisDirection(Vector d) {
        this.majorAxisDirection = d;
    }

    public double getEndParameter() {
        return endParameter;
    }

    public void setEndParameter(double endParameter) {
        if (endParameter < 0) {
            this.endParameter = (Math.PI * 2) + endParameter;
        } else {
            this.endParameter = endParameter;
        }
    }

    public double getRatio() {
        return ratio;
    }

    public void setRatio(double ratio) {
        this.ratio = ratio;
    }

    public double getStartParameter() {
        return startParameter;
    }

    public void setStartParameter(double startParameter) {
        if (startParameter < 0) {
            this.startParameter = (Math.PI * 2) + startParameter;
        } else {
            this.startParameter = startParameter;
        }
    }

    public String getType() {
        return DXFConstants.ENTITY_TYPE_ELLIPSE;
    }

    public double getHalfMajorAxisLength() {
        return this.majorAxisDirection.getLength();
    }

    public Point getLocalPointAt(double para) {
        Point p = new Point();
        double major = getHalfMajorAxisLength();
        double minor = major * this.ratio;
        double x = major * Math.cos(para);
        double y = minor * Math.sin(para);
        double alpha = this.getRotationAngle();

        if (alpha != 0.0) {
            double lx = x;
            x = (lx * Math.cos(alpha)) - (y * Math.sin(alpha));
            y = (lx * Math.sin(alpha)) + (y * Math.cos(alpha));
        }

        p.setX(x);
        p.setY(y);
        p.setZ(0.0);

        return p;
    }

    /**
     * Calculate a Point in world coordinates for the given parameter
     * on the ellipse.
     * @param para in double (between 0.0 and 2*PI)
     * @return the point of the ellipse in world coordinates
     */
    public Point getPointAt(double para) {
        ParametricPlane plane = new ParametricPlane(this.center,
                this.getExtrusion().getDirectionX(),
                this.getExtrusion().getDirectionY(),
                this.getExtrusion().getNormal());
        Vector minorAxis = MathUtils.crossProduct(this.getExtrusion().getNormal(),
                this.getMajorAxisDirection());
        minorAxis = MathUtils.scaleVector(minorAxis, this.ratio);

        Vector v1 = MathUtils.scaleVector(this.getMajorAxisDirection(),
                Math.cos(para));
        Vector v2 = MathUtils.scaleVector(minorAxis, Math.sin(para));
        double x = v1.getX() + v2.getX();
        double y = v1.getY() + v2.getY();
        Point p = plane.getPoint(x, y);

        return p;
    }

    public Point getLocalStartPoint() {
        return this.getLocalPointAt(this.startParameter);
    }

    public Point getLocalEndPoint() {
        return this.getLocalPointAt(this.endParameter);
    }

    public double getRotationAngle() {
        return MathUtils.getAngle(DXFConstants.DEFAULT_X_AXIS_VECTOR,
            majorAxisDirection);
    }

    public double getLength() {
        int n = INTEGRATION_STEPS;
        double h = (this.endParameter - this.startParameter) / n;

        double a = this.getHalfMajorAxisLength();
        double b = a * this.ratio;
        double start = this.startParameter;

        double end = 0.0;
        double length = 0.0;

        // length = integral (sqrt((major*sin(t))^2+(minor*cos(t))^2))
        for (int i = 0; i < n; i++) {
            double center = (h / 2) + start;
            end = start + h;

            double w1 = Math.sqrt(Math.pow(a * Math.sin(start), 2) +
                    Math.pow(b * Math.cos(start), 2));
            double w2 = Math.sqrt(Math.pow(a * Math.sin(center), 2) +
                    Math.pow(b * Math.cos(center), 2));
            double w3 = Math.sqrt(Math.pow(a * Math.sin(end), 2) +
                    Math.pow(b * Math.cos(end), 2));
            // SIMPSON where (h/2)/3 is h/6 here
            length += ((w1 + (4 * w2) + w3) * (h / 6));
            start = end;
        }

        return length;
    }

    public boolean isCounterClockwise() {
        return counterclockwise;
    }

    public void setCounterClockwise(boolean counterclockwise) {
        this.counterclockwise = counterclockwise;
    }
}
