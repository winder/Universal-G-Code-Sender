/*
 * Helper functions for visualizer routines.
 */
/*
    Copyright 2013-2022 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender.visualizer;

import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.gcode.util.PlaneFormatter;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.types.PointSegment;
import com.willwinder.universalgcodesender.utils.GUIHelpers;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author wwinder
 */
public class VisualizerUtils {
    private static final Logger LOGGER = Logger.getLogger(VisualizerUtils.class.getSimpleName());

    /**
     * Returns the maximum side dimension of a box containing two points.
     */
    public static double findMaxSide(Position min, Position max) {
        double x = Math.abs(min.x) + Math.abs(max.x);
        double y = Math.abs(min.y) + Math.abs(max.y);
        double z = Math.abs(min.z) + Math.abs(max.z);
        return Math.max(x, Math.max(y, z));
    }

    /**
     * Returns the aspect ratio from two points.
     */
    public static double findAspectRatio(Position min, Position max) {
        double x = Math.abs(min.x) + Math.abs(max.x);
        double y = Math.abs(min.y) + Math.abs(max.y);
        return x / y;
    }

    /**
     * Returns the center point on a line.
     */
    public static Position findCenter(Position min, Position max) {
        Position center = new Position(
                (min.x + max.x) / 2.0,
                (min.y + max.y) / 2.0,
                (min.z + max.z) / 2.0);
        return center;
    }

    /**
     * Find a factor to scale an object by so that it fits in the window.
     * The buffer factor is how much of a border to leave.
     */
    public static double findScaleFactor(double x, double y, Position min, Position max, double bufferFactor) {

        if (y == 0 || x == 0 || min == null || max == null) {
            return 1;
        }
        double xObj = Math.abs(min.x) + Math.abs(max.x);
        double yObj = Math.abs(min.y) + Math.abs(max.y);
        double windowRatio = x / y;
        double objRatio = xObj / yObj;
        if (windowRatio < objRatio) {
            return (1.0 / xObj) * windowRatio * bufferFactor;
        } else {
            return (1.0 / yObj) * bufferFactor;
        }
    }

    /**
     * Constructor to setup the GUI for this Component
     */
    public static ArrayList<String> readFiletoArrayList(String gCode) throws IOException {
        ArrayList<String> vect = new ArrayList<>();
        File gCodeFile = new File(gCode);
        try (FileInputStream fstream = new FileInputStream(gCodeFile)) {
            DataInputStream dis = new DataInputStream(fstream);
            BufferedReader fileStream = new BufferedReader(new InputStreamReader(dis));
            String line;
            while ((line = fileStream.readLine()) != null) {
                vect.add(line);
            }
        }

        return vect;
    }

    /**
     * Determine the ratio of mouse movement to model movement for panning operations on a single axis.
     *
     * @param objectMin     The lowest value on the axis from the model's size.
     * @param objectMax     The highest point on the axis from the model's size.
     * @param movementRange The length of the axis in the window displaying the model.
     * @return the ratio of the model size to the display size on that axis.
     */
    public static double getRelativeMovementMultiplier(double objectMin, double objectMax, int movementRange) {
        if (movementRange == 0)
            return 0;

        double objectAxis = Math.abs(objectMax - objectMin);

        return objectAxis / (double) movementRange;
    }

    /**
     * Helper to create a line segment with flags initialized.
     */
    private static LineSegment createLineSegment(Position a, Position b, PointSegment meta) {
        LineSegment ls = new LineSegment(a, b, meta.getLineNumber());
        ls.setIsArc(meta.isArc());
        ls.setIsFastTraverse(meta.isFastTraverse());
        ls.setIsZMovement(meta.isZMovement());
        ls.setIsRotation(meta.isRotation());
        return ls;
    }

    /**
     * Turns a point segment into one or more LineSegment. Arcs and rotations around axes are expanded
     */
    public static void addLinesFromPointSegment(final Position start, final PointSegment endSegment, double arcSegmentLength, List<LineSegment> ret) {
        // For a line segment list ALL arcs must be converted to lines.
        double minArcLength = 0;
        endSegment.convertToMetric();

        try {
            // start is null for the first iteration.
            if (start != null) {
                // Expand arc for graphics.
                if (endSegment.isArc()) {
                    expandArc(start, endSegment, arcSegmentLength, ret, minArcLength);
                } else if (endSegment.isRotation()) {
                    expandRotationalLineSegment(start, endSegment, ret);
                } else {
                    // Line
                    ret.add(createLineSegment(start, endSegment.point(), endSegment));
                }
            }
        } catch (Exception e) {
            String message = endSegment.getLineNumber() + ": " + e.getMessage();
            GUIHelpers.displayErrorDialog(message, true);
            LOGGER.log(Level.SEVERE, message, e);
        }
    }

    private static void expandArc(Position start, PointSegment endSegment, double arcSegmentLength, List<LineSegment> ret, double minArcLength) {
        List<Position> points =
                GcodePreprocessorUtils.generatePointsAlongArcBDring(
                        start, endSegment.point(), endSegment.center(), endSegment.isClockwise(),
                        endSegment.getRadius(), minArcLength, arcSegmentLength, new PlaneFormatter(endSegment.getPlaneState()));
        // Create line segments from points.
        if (points != null) {
            Position startPoint = start;
            for (Position nextPoint : points) {
                ret.add(createLineSegment(startPoint, nextPoint, endSegment));
                startPoint = nextPoint;
            }
        }
    }

    public static void expandRotationalLineSegment(Position start, PointSegment endSegment, List<LineSegment> ret) {
        double maxDegreesPerStep = 5;
        double deltaX = defaultZero(endSegment.point().x) - defaultZero(start.x);
        double deltaY = defaultZero(endSegment.point().y) - defaultZero(start.y);
        double deltaZ = defaultZero(endSegment.point().z) - defaultZero(start.z);
        double deltaA = defaultZero(endSegment.point().a) - defaultZero(start.a);
        double deltaB = defaultZero(endSegment.point().b) - defaultZero(start.b);
        double deltaC = defaultZero(endSegment.point().c) - defaultZero(start.c);
        double steps = Math.max(Math.abs(deltaA), Math.max(Math.abs(deltaB), Math.abs(deltaC))) / maxDegreesPerStep;

        Position startPoint = start;
        for (int i = 0; i < steps; i++) {
            Position end = new Position(endSegment.point());
            if (deltaX != 0) {
                end.setX(defaultZero(start.x) + ((deltaX / steps) * i));
            }
            if (deltaY != 0) {
                end.setY(defaultZero(start.y) + ((deltaY / steps) * i));
            }
            if (deltaZ != 0) {
                end.setZ(defaultZero(start.z) + ((deltaZ / steps) * i));
            }
            if (deltaA != 0) {
                end.setA(defaultZero(start.a) + ((deltaA / steps) * i));
            }
            if (deltaB != 0) {
                end.setB(defaultZero(start.b) + ((deltaB / steps) * i));
            }
            if (deltaC != 0) {
                end.setC(defaultZero(start.c) + ((deltaC / steps) * i));
            }
            ret.add(createLineSegment(startPoint, end, endSegment));
            startPoint = end;
        }

        ret.add(createLineSegment(startPoint, endSegment.point(), endSegment));
    }

    /**
     * If a double value is NaN this method will return a zero.
     *
     * @param value a value that is either a double or a Double.NaN
     * @return a zero or a double.
     */
    private static double defaultZero(double value) {
        return Double.isNaN(value) ? 0 : value;
    }

    private static double sinIfNotZero(double angle) {
        return angle == 0 ? 0.0 : Math.sin(Math.toRadians(angle));
    }

    private static double cosIfNotZero(double angle) {
        return angle == 0 ? 0.0 : Math.cos(Math.toRadians(angle));
    }

    public static LineSegment toCartesian(LineSegment p) {
        Position start = new Position(p.getStart().x, p.getStart().y, p.getStart().z);
        Position end = new Position(p.getEnd().x, p.getEnd().y, p.getEnd().z);

        if (p.getStart().hasRotation() || p.getEnd().hasRotation()) {
            start = toCartesian(p.getStart());
            end = toCartesian(p.getEnd());
        }

        // TODO: Somehow figure out how to optimize the way Position, Point3d, PointSegment and LineSegment are used.
        LineSegment next = new LineSegment(start, end, p.getLineNumber());
        next.setIsArc(p.isArc());
        next.setIsFastTraverse(p.isFastTraverse());
        next.setIsRotation(p.isFastTraverse());
        next.setIsZMovement(p.isZMovement());
        next.setSpeed(p.getSpeed());

        return next;
    }

    /**
     * Converts a position with rotations on either X, Y or Z axes to a cartesian coordinate.
     *
     * @param position the position to convert
     * @return the new position
     */
    public static Position toCartesian(Position position) {
        // There are no rotations happening in the position
        if (!position.hasRotation()) {
            return position;
        }

        Position result = new Position(position.x, position.y, position.z, 0, 0, 0, position.getUnits());
        double sx = position.x;
        double sy = position.y;
        double sz = position.z;
        double sa = defaultZero(position.a);
        double sb = defaultZero(position.b);
        double sc = defaultZero(position.c);
        double sSinA = sinIfNotZero(sa);
        double sCosA = cosIfNotZero(sa);
        double sSinB = sinIfNotZero(sb);
        double sCosB = cosIfNotZero(sb);
        double sSinC = sinIfNotZero(sc);
        double sCosC = cosIfNotZero(sc);

        // X-Axis rotation
        // x1 = x0
        // y1 = y0cos(u) − z0sin(u)
        // z1 = y0sin(u) + z0cos(u)
        if (sa != 0) {
            result.y = sy * sCosA - sz * sSinA;
            result.z = sy * sSinA + sz * sCosA;
        }

        // Y-Axis rotation
        // x2 = x1cos(v) + z1sin(v)
        // y2 = y1
        // z2 = − x1sin(v) + z1cos(v)
        if (sb != 0) {
            result.x = sx * sCosB + sz * sSinB;
            result.z = -1 * sx * sSinB + sz * sCosB;
        }

        // Z-Axis rotation
        // x3 = x2cos(w) − y2sin(w)
        // y3 = x2sin(w) + y2cos(w)
        // z3 = z2
        if (sc != 0) {
            result.x = sx * sCosC - sy * sSinC;
            result.y = sx * sSinC + sy * sCosC;
        }
        return result;
    }

    public enum Color {
        RED(255, 100, 100),
        BLUE(0, 255, 255),
        PURPLE(242, 0, 255),
        YELLOW(237, 255, 0),
        OTHER_YELLOW(234, 212, 7),
        GREEN(33, 255, 0),
        WHITE(255, 255, 255),
        GRAY(80, 80, 80),
        BLACK(0, 0, 0);

        final byte[] rgb;

        Color(int r, int g, int b) {
            rgb = new byte[]{(byte) r, (byte) g, (byte) b};
        }

        public byte[] getBytes() {
            return rgb;
        }
    }

}
