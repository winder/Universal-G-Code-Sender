package com.willwinder.ugs.nbp.designer.gcode.path;

/**
 * Path segment
 */
public final class Segment {
    /**
     * The type of the segment
     */
    public final SegmentType type;
    /**
     * The segment point. This can be null when type is SEAM
     */
    public final Coordinate point;
    /**
     * The segment label. This is usually with SEAM to identify subpaths,
     * but can be used with points too as general purpose comments.
     */
    public final String label;

    public Segment(SegmentType type, Coordinate point) {
        this.type = type;
        this.point = point;
        this.label = null;
    }
    public Segment(SegmentType type, Coordinate point, String label) {
        this.type = type;
        this.point = point;
        this.label = label;
    }

    @Override
    public String toString() {
        if (label == null)
            return type.name() + " " + point;
        else
            return type.name() + " " + point + '(' + label + ')';
    }

    /**
     * Get the segment comment/label
     *
     * @return
     */
    public final String getLabel() {
        return label;
    }

    /**
     * Get the type of the segment
     *
     * @return segment type
     */
    public final SegmentType getType() {
        return type;
    }

    /**
     * Get the segment point
     *
     * @return point
     * @throws NullPointerException if segment has no point
     */
    public final Coordinate getPoint() {
        if (point == null)
            throw new NullPointerException(type + " segment has no point!");

        return point;
    }
}
