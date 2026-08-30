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

import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;

import java.time.Duration;
import java.util.List;

/**
 * Statistics for a gcode program as calculated by {@link GcodeStatisticsCalculator}. All distances
 * are given in millimeters unless a unit is requested.
 *
 * @param commandCount  the number of commands that were processed
 * @param rapidDistance the distance travelled by rapid movements (G0) in millimeters
 * @param feedDistance  the distance travelled by cutting movements (G1, G2, G3) in millimeters
 * @param rapidDuration the estimated time spent on rapid movements
 * @param feedDuration  the estimated time spent on cutting movements
 * @param checkpoints   how far into the program the machine is expected to be at a handful of rows
 *                      spread over the runtime, used to tell how far off the estimate is while the
 *                      program is running
 * @author Joacim Breiler
 */
public record GcodeStatistics(long commandCount, double rapidDistance, double feedDistance,
                              Duration rapidDuration, Duration feedDuration,
                              List<RuntimeCheckpoint> checkpoints) {

    public static final GcodeStatistics EMPTY =
            new GcodeStatistics(0, 0, 0, Duration.ZERO, Duration.ZERO, List.of());

    public GcodeStatistics {
        checkpoints = List.copyOf(checkpoints);
    }

    public double rapidDistance(Units units) {
        return rapidDistance * UnitUtils.scaleUnits(Units.MM, units);
    }

    public double feedDistance(Units units) {
        return feedDistance * UnitUtils.scaleUnits(Units.MM, units);
    }

    public double totalDistance() {
        return rapidDistance + feedDistance;
    }

    public double totalDistance(Units units) {
        return totalDistance() * UnitUtils.scaleUnits(Units.MM, units);
    }

    public Duration totalDuration() {
        return rapidDuration.plus(feedDuration);
    }
}
