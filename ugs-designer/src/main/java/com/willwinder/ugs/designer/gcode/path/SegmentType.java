package com.willwinder.ugs.designer.gcode.path;

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
    LINE("G01"),

    /**
     * Clockwise arc
     */
    CWARC("G02"),

    /**
     * Counter clockwise arc
     */
    CCWARC("G03"),

    /**
     * Rapid to location (usually through safe height)
     */
    MOVE("G00");

    public final String gcode;

    SegmentType(String gc) {
        gcode = gc;
    }
}
