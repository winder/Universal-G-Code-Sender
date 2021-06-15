package com.willwinder.ugs.nbp.designer.gcode.path;

import com.willwinder.universalgcodesender.gcode.util.Code;

/**
 * Segment types
 */
public enum SegmentType {
    /**
     * A marker for splitting the path
     */
    SEAM("---"),

    /**
     * Drill down at this point
     */
    POINT("???"),

    /**
     * Move in a straight line
     */
    LINE(Code.G1.name()),

    /**
     * Clockwise arc
     */
    CWARC(Code.G2.name()),

    /**
     * Counter clockwise arc
     */
    CCWARC(Code.G3.name()),

    /**
     * Rapid to location (usually through safe height)
     */
    MOVE(Code.G0.name());

    public final String gcode;

    SegmentType(String gc) {
        gcode = gc;
    }
}
