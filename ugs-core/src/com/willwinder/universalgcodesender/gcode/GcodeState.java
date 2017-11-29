/*
 * The current state of a gcode program.
 */
/*
    Copywrite 2016-2017 Will Winder

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
package com.willwinder.universalgcodesender.gcode;

import com.willwinder.universalgcodesender.gcode.util.Code;
import static com.willwinder.universalgcodesender.gcode.util.Code.G0;
import static com.willwinder.universalgcodesender.gcode.util.Code.G21;
import static com.willwinder.universalgcodesender.gcode.util.Code.G54;
import static com.willwinder.universalgcodesender.gcode.util.Code.G90;
import static com.willwinder.universalgcodesender.gcode.util.Code.G91_1;
import static com.willwinder.universalgcodesender.gcode.util.Code.G93;
import static com.willwinder.universalgcodesender.gcode.util.Code.G94;
import com.willwinder.universalgcodesender.gcode.util.Plane;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;

/**
 *
 * @author wwinder
 */
public class GcodeState {
    public GcodeState() {
        // GRBL initial state: [G0 G54 G17 G21 G90 G94 M0 M5 M9 T0 F0. S0.]
        this.currentMotionMode = G0;
        // TODO: Add WCS
        this.plane = Plane.XY;

        this.isMetric = true;
        this.units = G21;

        this.inAbsoluteMode = true;
        this.distanceMode = G90;
        // TODO: Feed mode
        this.speed = 0;
        this.spindleSpeed = 0;

        this.currentPoint = new Position(0, 0, 0, Units.MM);
    }

    public GcodeState copy() {
        GcodeState ret = new GcodeState();
        ret.currentMotionMode = currentMotionMode;
        ret.plane = plane;

        ret.inAbsoluteMode = inAbsoluteMode;
        ret.distanceMode = distanceMode;

        ret.inAbsoluteIJKMode = inAbsoluteIJKMode;
        ret.arcDistanceMode = arcDistanceMode;

        ret.feedMode = feedMode;

        ret.isMetric = isMetric;
        ret.units = units;

        ret.speed = speed;
        ret.spindleSpeed = spindleSpeed;

        ret.offset = offset;

        if (currentPoint != null) {
            ret.currentPoint = new Position(currentPoint.x, currentPoint.y, currentPoint.z, UnitUtils.Units.getUnits(units));
        }
        ret.commandNumber = commandNumber;
        return ret;
    }

    // Current state
    // group 1
    public Code currentMotionMode = null;

    // group 2
    public Plane plane;

    // group 3
    public boolean inAbsoluteMode = true;
    public Code distanceMode = G90;

    // group 4
    public boolean inAbsoluteIJKMode = false;
    public Code arcDistanceMode = G91_1;

    // group 5
    public Code feedMode = G94;
    public double speed = 0;

    // group 6
    public boolean isMetric = true;
    public Code units = G21;

    // group 12
    public Code offset = G54;

    // Misc
    public double spindleSpeed = 0;
    public Position currentPoint = null;
    public int commandNumber = 0;

    @Override
    public String toString() {
      String pattern = "metric: %b, motionMode: %s, plane: %s, absoluteMode: %b, ijkMode: %b, feed: %f, spindle: %f, point: %s";
      return String.format(pattern,
              isMetric, currentMotionMode, plane, inAbsoluteMode, inAbsoluteIJKMode, speed, spindleSpeed, currentPoint);

    }
}
