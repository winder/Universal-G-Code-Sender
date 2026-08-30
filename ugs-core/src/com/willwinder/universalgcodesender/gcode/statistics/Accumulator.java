/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.gcode.statistics;

import com.willwinder.universalgcodesender.gcode.GcodeParser.GcodeMeta;
import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserUtils;
import com.willwinder.universalgcodesender.gcode.util.PlaneFormatter;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.types.PointSegment;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Walks through the commands of a gcode program one at a time, adding up how far the machine
 * travels and how long each movement takes, and hands out the result as {@link GcodeStatistics}.
 * <p>
 * A movement can only be timed once the following one is known, as that decides how much the
 * machine has to brake before the corner, so one movement is always kept pending.
 *
 * @author Joacim Breiler
 */
class Accumulator {

    private static final double TWO_PI = 2 * Math.PI;
    private static final double EPSILON = 1e-9;
    private static final double SECONDS_PER_MINUTE = 60;
    private static final double NANOS_PER_SECOND = 1_000_000_000d;
    private static final double MILLIS_PER_MINUTE = 60_000d;

    /**
     * How much of the estimated runtime there is between the checkpoints of a program. A shorter
     * interval lets a running program notice sooner that it is running at another pace than
     * estimated, at the cost of a steadier estimate.
     */
    private static final long CHECKPOINT_INTERVAL_MILLIS = 60_000;

    private final double maxFeedRate;
    private final double maxRapidRate;
    private final double acceleration;
    private final double junctionDeviation;

    private final List<RuntimeCheckpoint> checkpoints = new ArrayList<>();
    private GcodeState state = createInitialState();
    private int lineNumber = 0;
    private long commandCount = 0;
    private double rapidDistance = 0;
    private double feedDistance = 0;
    private double rapidMinutes = 0;
    private double feedMinutes = 0;
    private Move pendingMove = null;
    private long nextCheckpointMillis = CHECKPOINT_INTERVAL_MILLIS;

    /**
     * Creates an accumulator for a machine with the given limits.
     *
     * @param limits the motion limits of the machine, expressed in millimeters
     */
    Accumulator(MachineLimits limits) {
        this.maxFeedRate = limits.maxFeedRate();
        this.maxRapidRate = limits.maxRapidRate();
        this.acceleration = limits.acceleration();
        this.junctionDeviation = limits.junctionDeviation();
    }

    void add(String command) throws GcodeParserException {
        List<GcodeMeta> metaList = GcodeParserUtils.processCommand(command, ++lineNumber, state, true);
        if (metaList == null) {
            return;
        }

        commandCount++;
        for (GcodeMeta meta : metaList) {
            if (meta.point != null) {
                accumulate(state.currentPoint, meta);
            }
            if (meta.state != null) {
                state = meta.state;
            }
        }

        if (elapsedMillis() >= nextCheckpointMillis) {
            addCheckpoint();
        }
    }

    GcodeStatistics getStatistics() {
        // The last movement has nothing to brake for other than the end of the program
        timePendingMove(0);

        // A checkpoint at the end of the program makes the estimate defined all the way there
        addCheckpoint();

        return new GcodeStatistics(commandCount, rapidDistance, feedDistance,
                toDuration(rapidMinutes), toDuration(feedMinutes), checkpoints);
    }

    private void accumulate(Position startPoint, GcodeMeta meta) {
        Position start = startPoint.getPositionIn(Units.MM);
        Position end = meta.point.point().getPositionIn(Units.MM);

        boolean isRapid = meta.code == Code.G0;
        double distance = meta.point.isArc() ? arcLength(start, end, meta.point) : lineLength(start, end);
        double[] direction = direction(start, end, distance);
        if (distance <= 0) {
            distance = rotationLength(start, end);
            direction = null;
        }
        if (distance <= 0) {
            return;
        }

        if (isRapid) {
            rapidDistance += distance;
        } else {
            feedDistance += distance;
        }

        // An inverse time movement takes as long as it is told to, and the machines acceleration
        // has no say in it
        if (!hasAcceleration() || state.feedMode == Code.G93) {
            addMinutes(isRapid, isRapid ? distance / maxRapidRate : feedMinutes(distance, meta.state));
            return;
        }

        double targetVelocity = (isRapid ? maxRapidRate : feedRate(meta.state)) / SECONDS_PER_MINUTE;
        queueMove(new Move(distance, targetVelocity, direction, isRapid));
    }

    private void queueMove(Move move) {
        if (pendingMove != null) {
            double corner = cornerVelocity(pendingMove.getDirection(), move.getDirection());
            move.setEntryVelocity(timePendingMove(
                    Math.min(corner, Math.min(pendingMove.getTargetVelocity(), move.getTargetVelocity()))));
        }
        pendingMove = move;
    }

    // Times the pending movement now that it is known how fast it may leave, and returns the
    // velocity it actually leaves at
    private double timePendingMove(double requestedExitVelocity) {
        if (pendingMove == null) {
            return 0;
        }

        Move move = pendingMove;
        pendingMove = null;

        double entryVelocity = move.getEntryVelocity();
        double reachable = 2 * acceleration * move.getLength();
        double exitVelocity = Math.max(
                Math.min(requestedExitVelocity, Math.sqrt(entryVelocity * entryVelocity + reachable)),
                Math.sqrt(Math.max(0, entryVelocity * entryVelocity - reachable)));

        double seconds = seconds(move.getLength(), entryVelocity, move.getTargetVelocity(), exitVelocity);
        addMinutes(move.isRapid(), seconds / SECONDS_PER_MINUTE);
        return exitVelocity;
    }

    // The time for a trapezoidal velocity profile that enters at one velocity, accelerates towards
    // the target velocity for as long as the movement is long enough, and brakes down to the exit
    // velocity. A movement that is too short to reach the target velocity gets a triangular profile.
    private double seconds(double length, double entryVelocity, double targetVelocity, double exitVelocity) {
        double peakVelocity = Math.min(targetVelocity,
                Math.sqrt((2 * acceleration * length + entryVelocity * entryVelocity + exitVelocity * exitVelocity) / 2));
        peakVelocity = Math.max(peakVelocity, Math.max(entryVelocity, exitVelocity));
        if (peakVelocity <= 0) {
            return 0;
        }

        double accelerateDistance = (peakVelocity * peakVelocity - entryVelocity * entryVelocity) / (2 * acceleration);
        double brakeDistance = (peakVelocity * peakVelocity - exitVelocity * exitVelocity) / (2 * acceleration);
        double cruiseDistance = Math.max(0, length - accelerateDistance - brakeDistance);

        return (peakVelocity - entryVelocity) / acceleration
                + cruiseDistance / peakVelocity
                + (peakVelocity - exitVelocity) / acceleration;
    }

    // The highest velocity that the corner between the two movements may be taken at, using the
    // same junction deviation model as the controller. Two movements in the same direction have no
    // limit, and a full reversal has to come to a stop.
    private double cornerVelocity(double[] previous, double[] next) {
        if (previous == null || next == null || junctionDeviation <= 0) {
            return 0;
        }

        double cosTheta = -(previous[0] * next[0] + previous[1] * next[1] + previous[2] * next[2]);
        if (cosTheta <= -1 + EPSILON) {
            return Double.MAX_VALUE;
        }
        if (cosTheta >= 1 - EPSILON) {
            return 0;
        }

        double sinHalfTheta = Math.sqrt(0.5 * (1 - cosTheta));
        return Math.sqrt(acceleration * junctionDeviation * sinHalfTheta / (1 - sinHalfTheta));
    }

    private boolean hasAcceleration() {
        return acceleration > 0;
    }

    private double feedRate(GcodeState state) {
        double feedRate = state.feedRate * UnitUtils.scaleUnits(state.getUnits(), Units.MM);
        if (state.feedMode == Code.G95) {
            feedRate *= state.spindleSpeed;
        }

        if (feedRate <= 0) {
            return maxFeedRate;
        }
        return Math.min(feedRate, maxFeedRate);
    }

    private double feedMinutes(double distance, GcodeState state) {
        if (state.feedMode == Code.G93) {
            return state.feedRate > 0 ? 1 / state.feedRate : 0;
        }
        return distance / feedRate(state);
    }

    private void addMinutes(boolean isRapid, double minutes) {
        if (isRapid) {
            rapidMinutes += minutes;
        } else {
            feedMinutes += minutes;
        }
    }

    private void addCheckpoint() {
        long elapsed = elapsedMillis();
        if (!checkpoints.isEmpty() && checkpoints.get(checkpoints.size() - 1).row() == lineNumber) {
            checkpoints.set(checkpoints.size() - 1, new RuntimeCheckpoint(lineNumber, elapsed));
        } else {
            checkpoints.add(new RuntimeCheckpoint(lineNumber, elapsed));
        }
        nextCheckpointMillis = elapsed + CHECKPOINT_INTERVAL_MILLIS;
    }

    private long elapsedMillis() {
        return Math.round((rapidMinutes + feedMinutes) * MILLIS_PER_MINUTE);
    }

    private Duration toDuration(double minutes) {
        double seconds = minutes * SECONDS_PER_MINUTE;
        if (!Double.isFinite(seconds) || seconds <= 0) {
            return Duration.ZERO;
        }
        return Duration.ofNanos(Math.round(seconds * NANOS_PER_SECOND));
    }

    private static GcodeState createInitialState() {
        GcodeState state = new GcodeState();
        state.currentPoint = new Position(Position.ZERO);
        return state;
    }

    private static double lineLength(Position start, Position end) {
        double deltaX = zeroIfNaN(end.x) - zeroIfNaN(start.x);
        double deltaY = zeroIfNaN(end.y) - zeroIfNaN(start.y);
        double deltaZ = zeroIfNaN(end.z) - zeroIfNaN(start.z);
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    private static double arcLength(Position start, Position end, PointSegment arc) {
        PlaneFormatter plane = new PlaneFormatter(arc.getPlaneState());
        Position center = arc.center().getPositionIn(Units.MM);
        double radius = Math.hypot(plane.axis0(start) - plane.axis0(center), plane.axis1(start) - plane.axis1(center));
        double sweep = sweep(
                GcodePreprocessorUtils.getAngle(center, start, plane),
                GcodePreprocessorUtils.getAngle(center, end, plane),
                arc.isClockwise());

        return Math.hypot(radius * sweep, plane.linear(end) - plane.linear(start));
    }

    // A rotation is measured in degrees but is fed at the same rate as a linear movement, so its
    // swept angle is used as the travelled distance.
    private static double rotationLength(Position start, Position end) {
        double deltaA = zeroIfNaN(end.a) - zeroIfNaN(start.a);
        double deltaB = zeroIfNaN(end.b) - zeroIfNaN(start.b);
        double deltaC = zeroIfNaN(end.c) - zeroIfNaN(start.c);
        return Math.sqrt(deltaA * deltaA + deltaB * deltaB + deltaC * deltaC);
    }

    private static double[] direction(Position start, Position end, double length) {
        if (length <= 0) {
            return null;
        }
        return new double[]{
                (zeroIfNaN(end.x) - zeroIfNaN(start.x)) / length,
                (zeroIfNaN(end.y) - zeroIfNaN(start.y)) / length,
                (zeroIfNaN(end.z) - zeroIfNaN(start.z)) / length};
    }

    private static double sweep(double startAngle, double endAngle, boolean clockwise) {
        double sweep = clockwise ? mod2pi(startAngle - endAngle) : mod2pi(endAngle - startAngle);
        return sweep < EPSILON ? TWO_PI : sweep;
    }

    private static double mod2pi(double value) {
        double remainder = value % TWO_PI;
        return remainder < 0 ? remainder + TWO_PI : remainder;
    }

    private static double zeroIfNaN(double value) {
        return Double.isNaN(value) ? 0 : value;
    }
}
