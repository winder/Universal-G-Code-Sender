/*
 * The current state of a gcode program.
 */
/*
    Copywrite 2016 Will Winder

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

import com.willwinder.universalgcodesender.gcode.util.Plane;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class GcodeState {
    public GcodeState copy() {
        GcodeState ret = new GcodeState();
        ret.plane = plane;
        ret.isMetric = isMetric;
        ret.inAbsoluteMode = inAbsoluteMode;
        ret.inAbsoluteIJKMode = inAbsoluteIJKMode;
        ret.lastGcodeCommand = lastGcodeCommand;
        if (currentPoint != null) {
            ret.currentPoint = new Point3d(currentPoint.x, currentPoint.y, currentPoint.z);
        }
        ret.speed = speed;
        ret.commandNumber = commandNumber;
        return ret;
    }

    // Current state
    public Plane plane;
    public boolean isMetric = true;
    public boolean inAbsoluteMode = true;
    public boolean inAbsoluteIJKMode = false;
    public String lastGcodeCommand = "";
    public Point3d currentPoint = null;
    public double speed = 0;
    //PointSegment currentPoint = null;
    public int commandNumber = 0;
}
