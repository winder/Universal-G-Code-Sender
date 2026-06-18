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

    @Override
    public List<String> processCommand(String command, GcodeState state) throws GcodeParserException {
        Position c = state.currentPoint;
        if (c != null) {
            recordPoint(c.x, c.y, c.z, state.isMetric);
            if (isArcMotion(state.currentMotionMode) && previous != null && !isSamePoint(previous, c)) {
                expandArcBounds(command, state, previous);
            }

            // Num commands
            commandCount++;
            previous = new Position(c);
        }

        return Collections.singletonList(command);
    }

    /**
     * Extends the bounding box to cover the full sweep of an arc, not just its end points. An arc
     * reaches an axis-aligned extreme wherever it crosses one of the cardinal directions (0/90/180/270
     * degrees) from its center, so we add those crossing points that fall within the swept angle.
     *
     * @param command  the gcode command describing the arc
     * @param endState the parser state after the command, used to re-derive the arc geometry
     * @param start    the point where the arc starts, in the gcode's units
     */
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
                recordPoint(extreme.x, extreme.y, extreme.z, endState.isMetric);
            }
        } catch (GcodeParserException e) {
            // If the arc can't be parsed, fall back to the start/end points already recorded.
        }
    }

    private void recordPoint(double x, double y, double z, boolean metric) {
        Position p = new Position(x, y, z, metric ? Units.MM : Units.INCH).getPositionIn(defaultUnits);

        // Update min
        min.x = getMin(min.x, p.x);
        min.y = getMin(min.y, p.y);
        min.z = getMin(min.z, p.z);

        // Update max
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
