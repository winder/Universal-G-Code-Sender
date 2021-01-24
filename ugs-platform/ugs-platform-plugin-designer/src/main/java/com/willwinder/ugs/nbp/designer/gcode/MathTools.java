package com.willwinder.ugs.nbp.designer.gcode;

import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;

/**
 * A collection of static functions.
 *
 * @author Calle Laakkonen
 */
public class MathTools {

    public final static DecimalFormatSymbols DECIMAL_FORMAT_SYMBOLS = DecimalFormatSymbols.getInstance();
    public final static DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.###", DECIMAL_FORMAT_SYMBOLS);

    static {
        // Makes sure decimal separator is '.' despite locale
        DECIMAL_FORMAT_SYMBOLS.setDecimalSeparator('.');

        // Make sure minus sign is not a UTF-8 character
        DECIMAL_FORMAT_SYMBOLS.setMinusSign('-');
    }

    static public String formatToString(double value) {
        return DECIMAL_FORMAT.format(value);
    }


    /**
     * Generates a list of points interpolating a quadratic bezier using the given number of segments
     *
     * @param origin the start position
     * @param destination the end position
     * @param control1 the control point
     * @param segments number of segments to make the bezier curve
     * @return a list of points for a line path.
     */
    public static List<Point2D> quadraticBezier(Point2D origin, Point2D destination, Point2D control1, int segments) {
        List<Point2D> pointsForReturn = new ArrayList<>();

        float t = 0;
        for (int i = 0; i < segments; i++) {

            // B(t) = (1 - t)2 * P0 + 2(1 - t) * t * P1 + t2 * P2, t ∈ [0,1]
            double x = Math.pow(1 - t, 2) * origin.getX() + 2.0f * (1 - t) * t * control1.getX() + t * t * destination.getX();
            double y = Math.pow(1 - t, 2) * origin.getY() + 2.0f * (1 - t) * t * control1.getY() + t * t * destination.getY();
            Point2D p = new Point2D.Double(x, y);
            t += 1.0f / segments;
            pointsForReturn.add(p);
        }
        pointsForReturn.add(destination);
        return pointsForReturn;
    }

    /**
     * Generates a list of points interpolating a cubic bezier using the given number of segments
     *
     * @param origin the start position
     * @param destination the end position
     * @param control1 the first control point
     * @param control2 the second control point
     * @param segments number of segments to make the bezier curve
     * @return a list of points for a line path.
     */
    public static List<Point2D> cubicBezier(Point2D origin, Point2D destination, Point2D control1, Point2D control2, int segments) {
        List<Point2D> pointsForReturn = new ArrayList<>();

        float t = 0;
        for (int i = 0; i < segments; i++) {

            // B(t) = (1 – t)3 * P0 + 3(1 – t)2 * t * P1 + 3(1-t) * t2 * P2 + t3 * P3
            double x = Math.pow(1 - t, 3) * origin.getX() + 3.0f * Math.pow(1 - t, 2) * t * control1.getX() + 3.0f * (1 - t) * t * t * control2.getX() + t * t * t * destination.getX();
            double y = Math.pow(1 - t, 3) * origin.getY() + 3.0f * Math.pow(1 - t, 2) * t * control1.getY() + 3.0f * (1 - t) * t * t * control2.getY() + t * t * t * destination.getY();
            Point2D p = new Point2D.Double(x, y);
            t += 1.0f / segments;
            pointsForReturn.add(p);
        }
        pointsForReturn.add(destination);
        return pointsForReturn;
    }
}
