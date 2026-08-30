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

import com.willwinder.universalgcodesender.model.UnitUtils.Units;

/**
 * The motion limits of a machine, used by the {@link GcodeStatisticsCalculator} when estimating
 * how long a program will take to run.
 *
 * @param maxFeedRate       the maximum feed rate in units per minute
 * @param maxRapidRate      the maximum rapid rate in units per minute
 * @param acceleration      the maximum acceleration in units per second squared, or zero if it is
 *                          unknown. Without it a movement is assumed to be made at its target
 *                          velocity from end to end, which is far too optimistic for programs
 *                          built from short segments
 * @param junctionDeviation the junction deviation in units, deciding how fast a corner between two
 *                          movements may be taken. Zero makes the machine come to a stop in every
 *                          corner
 * @param units             the units that the limits are expressed in
 * @author Joacim Breiler
 */
public record MachineLimits(double maxFeedRate, double maxRapidRate, double acceleration,
                            double junctionDeviation, Units units) {

    /**
     * The junction deviation used by GRBL unless it has been configured with something else.
     */
    public static final double DEFAULT_JUNCTION_DEVIATION_MM = 0.01;

    /**
     * Creates limits for a machine where only the maximum rates are known, making the estimate
     * ignore how long the machine takes to accelerate up to those rates.
     *
     * @param maxFeedRate  the maximum feed rate in units per minute
     * @param maxRapidRate the maximum rapid rate in units per minute
     * @param units        the units that the rates are expressed in
     * @return the machine limits
     */
    public static MachineLimits withoutAcceleration(double maxFeedRate, double maxRapidRate, Units units) {
        return new MachineLimits(maxFeedRate, maxRapidRate, 0, 0, units);
    }

    public boolean hasAcceleration() {
        return acceleration > 0;
    }
}
