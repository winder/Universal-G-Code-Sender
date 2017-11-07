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
import com.willwinder.universalgcodesender.gcode.util.Plane;
import javax.vecmath.Point3d;

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
        this.inAbsoluteMode = true;
        // TODO: Feed mode
        this.speed = 0;
        this.spindleSpeed = 0;

        this.currentPoint = new Point3d(0, 0, 0);
    }

    public GcodeState copy() {
        GcodeState ret = new GcodeState();
        ret.currentMotionMode = currentMotionMode;
        ret.plane = plane;
        ret.inAbsoluteMode = inAbsoluteMode;
        ret.inAbsoluteIJKMode = inAbsoluteIJKMode;
        ret.speed = speed;
        ret.isMetric = isMetric;
        ret.spindleSpeed = spindleSpeed;

        if (currentPoint != null) {
            ret.currentPoint = new Point3d(currentPoint.x, currentPoint.y, currentPoint.z);
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
    // group 4
    public boolean inAbsoluteIJKMode = false;
    // group 5
    public double speed = 0;
    // group 6
    public boolean isMetric = true;
    // group 12 (WCS)?

    // Misc
    public double spindleSpeed = 0;
    public Point3d currentPoint = null;
    public int commandNumber = 0;
}
