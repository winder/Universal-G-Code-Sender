/*
    Copyright 2017 Will Winder

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
package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.gcode.GcodeParser.GcodeMeta;
import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.GcodeStats;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserUtils;
import com.willwinder.universalgcodesender.gcode.util.PlaneFormatter;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.types.PointSegment;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author wwinder
 */
public class Stats implements CommandProcessor, GcodeStats {
    private static Units defaultUnits = Units.MM;

    /** Cardinal angles (0, 90, 180, 270 degrees) where a circle reaches its axis-aligned extremes. */
    private static final double[] CARDINAL_ANGLES = {0, Math.PI / 2, Math.PI, 3 * Math.PI / 2};
    private static final double TWO_PI = 2 * Math.PI;
    private static final double EPSILON = 1e-9;

    private Position min = new Position(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, Units.MM);
    private Position max = new Position(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE, Units.MM);

    private long commandCount = 0;
    private Position previous = null;

    /** Step size used when sampling a rotary move so its swept cartesian extents are captured. */
    private static final double MAX_DEGREES_PER_STEP = 5;

    @Override
    public List<String> processCommand(String command, GcodeState state) throws GcodeParserException {
        Position c = state.currentPoint;
        if (c != null) {
            record(c, state.isMetric);
            if (isArcMotion(state.currentMotionMode) && previous != null && !isSamePoint(previous, c)) {
                expandArcBounds(command, state, previous);
            } else if (previous != null && hasRotationChange(previous, c)) {
                expandRotationBounds(previous, c, state.isMetric);
            }

            // Num commands
            commandCount++;
            previous = new Position(c);
        }

        return Collections.singletonList(command);
    }

    private void expandRotationBounds(Position start, Position end, boolean metric) {
        double startA = zeroIfNaN(start.a);
        double startB = zeroIfNaN(start.b);
        double startC = zeroIfNaN(start.c);
        double endA = zeroIfNaN(end.a);
        double endB = zeroIfNaN(end.b);
        double endC = zeroIfNaN(end.c);
        double maxDelta = Math.max(Math.abs(endA - startA), Math.max(Math.abs(endB - startB), Math.abs(endC - startC)));
        int steps = (int) Math.ceil(maxDelta / MAX_DEGREES_PER_STEP);

        for (int i = 1; i < steps; i++) {
            double t = (double) i / steps;
            Position sample = new Position(
                    lerp(start.x, end.x, t),
                    lerp(start.y, end.y, t),
                    lerp(start.z, end.z, t),
                    lerp(startA, endA, t),
                    lerp(startB, endB, t),
                    lerp(startC, endC, t),
                    metric ? Units.MM : Units.INCH);
            record(sample, metric);
        }
    }

    // An arc only reports its end point, so add the cardinal crossings (0/90/180/270 degrees from
    // its center) that fall within the swept angle, which is where it reaches its axis extremes.
    private void expandArcBounds(String command, GcodeState endState, Position start) {
        try {
            GcodeState startState = endState.copy();
            startState.currentPoint = new Position(start);

            PointSegment arc = findArc(GcodeParserUtils.processCommand(command, 0, startState, true));
            if (arc == null) {
                return;
            }

            PlaneFormatter plane = new PlaneFormatter(arc.getPlaneState());
            Position center = arc.center();
            Position end = arc.point();
            double radius = arc.getRadius();
            if (radius <= 0) {
                radius = Math.hypot(plane.axis0(start) - plane.axis0(center), plane.axis1(start) - plane.axis1(center));
            }

            double startAngle = GcodePreprocessorUtils.getAngle(center, start, plane);
            double endAngle = GcodePreprocessorUtils.getAngle(center, end, plane);

            for (double cardinal : CARDINAL_ANGLES) {
                if (!arcContainsAngle(startAngle, endAngle, cardinal, arc.isClockwise())) {
                    continue;
                }

                Position extreme = new Position(start);
                plane.setAxis0(extreme, plane.axis0(center) + radius * Math.cos(cardinal));
                plane.setAxis1(extreme, plane.axis1(center) + radius * Math.sin(cardinal));
                record(extreme, endState.isMetric);
            }
        } catch (GcodeParserException e) {
            // If the arc can't be parsed, fall back to the start/end points already recorded.
        }
    }

    private void record(Position rawPoint, boolean metric) {
        Position p = new Position(rawPoint.x, rawPoint.y, rawPoint.z,
                rawPoint.a, rawPoint.b, rawPoint.c, metric ? Units.MM : Units.INCH)
                .getPositionIn(defaultUnits);

        // Only fold rotations into cartesian coordinates when the point actually rotates. Doing it
        // unconditionally would turn undefined (NaN) axes into 0 and wrongly pull the bounds to the
        // origin for plain XYZ moves.
        if (p.hasRotation()) {
            p = p.getCartesian();
        }

        min.x = getMin(min.x, p.x);
        min.y = getMin(min.y, p.y);
        min.z = getMin(min.z, p.z);

        max.x = getMax(max.x, p.x);
        max.y = getMax(max.y, p.y);
        max.z = getMax(max.z, p.z);
    }

    private static boolean isArcMotion(Code motionMode) {
        return motionMode == Code.G2 || motionMode == Code.G3;
    }

    private static boolean isSamePoint(Position a, Position b) {
        return a.x == b.x && a.y == b.y && a.z == b.z;
    }

    private static boolean hasRotationChange(Position from, Position to) {
        return zeroIfNaN(from.a) != zeroIfNaN(to.a)
                || zeroIfNaN(from.b) != zeroIfNaN(to.b)
                || zeroIfNaN(from.c) != zeroIfNaN(to.c);
    }

    private static double lerp(double from, double to, double t) {
        return from + (to - from) * t;
    }

    private static double zeroIfNaN(double value) {
        return Double.isNaN(value) ? 0 : value;
    }

    private static PointSegment findArc(List<GcodeMeta> commands) {
        if (commands == null) {
            return null;
        }
        for (GcodeMeta meta : commands) {
            if (meta.point != null && meta.point.isArc()) {
                return meta.point;
            }
        }
        return null;
    }

    /**
     * @return true if {@code angle} is swept when travelling along the arc from {@code startAngle}
     * to {@code endAngle} in the given direction. All angles are in radians.
     */
    private static boolean arcContainsAngle(double startAngle, double endAngle, double angle, boolean clockwise) {
        double sweep = clockwise ? mod2pi(startAngle - endAngle) : mod2pi(endAngle - startAngle);
        if (sweep < EPSILON) {
            sweep = TWO_PI; // a full circle (start == end) reaches every extreme
        }
        double toAngle = clockwise ? mod2pi(startAngle - angle) : mod2pi(angle - startAngle);
        return toAngle <= sweep + EPSILON;
    }

    private static double mod2pi(double value) {
        double remainder = value % TWO_PI;
        return remainder < 0 ? remainder + TWO_PI : remainder;
    }

    private double getMin(double value1, double value2) {
        if (Double.isNaN(value1)) {
            return value2;
        } else if(Double.isNaN(value2)) {
            return value1;
        }
        return Math.min(value1, value2);
    }

    private double getMax(double value1, double value2) {
        if (Double.isNaN(value1)) {
            return value2;
        } else if(Double.isNaN(value2)) {
            return value1;
        }
        return Math.max(value1, value2);
    }

    @Override
    public String getHelp() {
        return "Caches program metrics, shouldn't be enabled or disabled.";
    }

    @Override
    public final Position getMin() {
        return min;
    }

    @Override
    public final Position getMax() {
        return max;
    }

    @Override
    public final long getCommandCount() {
        return commandCount;
    }
    
}
