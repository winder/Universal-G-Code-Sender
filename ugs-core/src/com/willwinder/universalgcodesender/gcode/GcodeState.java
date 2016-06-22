/*
 * The current state of a gcode program.
 */
package com.willwinder.universalgcodesender.gcode;

import com.willwinder.universalgcodesender.types.PointSegment;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class GcodeState {
    public GcodeState copy() {
        GcodeState ret = new GcodeState();
        ret.isMetric = isMetric;
        ret.inAbsoluteMode = inAbsoluteMode;
        ret.inAbsoluteIJKMode = inAbsoluteIJKMode;
        ret.lastGcodeCommand = lastGcodeCommand;
        ret.currentPoint = new Point3d(currentPoint.x, currentPoint.y, currentPoint.z);
        ret.speed = speed;
        ret.commandNumber = commandNumber;
        return ret;
    }

    // Current state
    public boolean isMetric = true;
    public boolean inAbsoluteMode = true;
    public boolean inAbsoluteIJKMode = false;
    public String lastGcodeCommand = "";
    public Point3d currentPoint = null;
    public double speed = 0;
    //PointSegment currentPoint = null;
    public int commandNumber = 0;
}
