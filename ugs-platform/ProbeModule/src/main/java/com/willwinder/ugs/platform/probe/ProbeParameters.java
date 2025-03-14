/*
    Copyright 2023 Will Winder

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
package com.willwinder.ugs.platform.probe;

import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.WorkCoordinateSystem;

/**
 * Parameters passed into the probe operations.
 */
public class ProbeParameters {
    public final double probeDiameter;
    public final double xSpacing;
    public final double ySpacing;
    public final double zSpacing;
    public final double xOffset;
    public final double yOffset;
    public final double zOffset;
    public final double holeDiameter;
    public final double feedRate;
    public final double feedRateSlow;

    /**
     * The distance to retract after first fast probe cycle before running the next slow probe cycle
     */
    public final double retractAmount;

    /**
     * Wait a time given in seconds before beginning slow probe after retracting. Compensates for slow touch probe response.
     */
    public final double delayAfterRetract;

    public final WorkCoordinateSystem wcsToUpdate;
    public final UnitUtils.Units units;

    // Results
    public final Position startPosition;
    public Position endPosition;

    public ProbeParameters(double diameter, Position start,
                           double xSpacing, double ySpacing, double zSpacing,
                           double xOffset, double yOffset, double zOffset,
                           double holeDiameter,
                           double feedRate, double feedRateSlow, double retractAmount, double delayAfterRetract,
                           UnitUtils.Units u, WorkCoordinateSystem wcs) {
        this.endPosition = null;
        this.probeDiameter = diameter;
        this.startPosition = start;
        this.xSpacing = xSpacing;
        this.ySpacing = ySpacing;
        this.zSpacing = zSpacing;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.zOffset = zOffset;
        this.holeDiameter = holeDiameter;
        this.feedRate = feedRate;
        this.feedRateSlow = feedRateSlow;
        this.retractAmount = retractAmount;
        this.delayAfterRetract = delayAfterRetract;
        this.units = u;
        this.wcsToUpdate = wcs;
    }
}
