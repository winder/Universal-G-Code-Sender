/*
 * This file is part of JGCGen.
 *
 * JGCGen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JGCGen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JGCGen.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.designer.io.gcode.path;

import com.willwinder.universalgcodesender.model.PartialPosition;

import java.awt.geom.Point2D;

/**
 * Path segment
 *
 * @author Calle Laakkonen
 * @author Joacim Breiler
 */
public final class Segment {
    /**
     * The type of the segment
     */
    public final SegmentType type;
    /**
     * The segment point. This can be null when type is SEAM
     */
    public final PartialPosition point;

    /**
     * The segment label. This is usually with SEAM to identify subpaths,
     * but can be used with points too as general purpose comments.
     */
    public final String label;

    /**
     * The current spindle speed
     */
    private final Integer spindleSpeed;

    /**
     * The current feed speed
     */
    private final Integer feedSpeed;

    /**
     * The absolute center of the arc for {@link SegmentType#CWARC} and {@link SegmentType#CCWARC}
     * segments, null for all other segment types. It is stored as an absolute position instead of
     * as incremental I/J offsets so that the segment stays valid no matter which position precedes
     * it, leaving the conversion to the gcode writer.
     */
    private final Point2D arcCenter;

    public Segment(SegmentType type, PartialPosition point, String label, Integer spindleSpeed, Integer feedSpeed, Point2D arcCenter) {
        if (type.isArc() && arcCenter == null) {
            throw new IllegalArgumentException("A " + type + " segment requires an arc center");
        }
        if (!type.isArc() && arcCenter != null) {
            throw new IllegalArgumentException("A " + type + " segment can not have an arc center");
        }
        if (type.isArc() && (point == null || !point.hasX() || !point.hasY())) {
            throw new IllegalArgumentException("A " + type + " segment requires a point with an X and Y, as an arc can not be written without them");
        }

        this.type = type;
        this.point = point;
        this.label = label;
        this.spindleSpeed = spindleSpeed;
        this.feedSpeed = feedSpeed;
        this.arcCenter = arcCenter == null ? null : new Point2D.Double(arcCenter.getX(), arcCenter.getY());
    }

    public Segment(SegmentType type, PartialPosition point, String label, Integer spindleSpeed, Integer feedSpeed) {
        this(type, point, label, spindleSpeed, feedSpeed, null);
    }

    /**
     * Creates an arc segment ending at the given point, curving around the given absolute center.
     *
     * @param type      either {@link SegmentType#CWARC} or {@link SegmentType#CCWARC}
     * @param point     the position where the arc ends
     * @param arcCenter the absolute center that the arc curves around
     * @param feedSpeed the feed rate to cut the arc with
     */
    public static Segment arc(SegmentType type, PartialPosition point, Point2D arcCenter, Integer feedSpeed) {
        if (!type.isArc()) {
            throw new IllegalArgumentException("A " + type + " segment is not an arc");
        }

        return new Segment(type, point, null, null, feedSpeed, arcCenter);
    }

    public Segment(SegmentType type, PartialPosition point) {
        this(type, point, null);
    }

    public Segment(String label) {
        this(SegmentType.SEAM, null, label);
    }
    public Segment(SegmentType type, PartialPosition point, String label) {
        this(type, point, label, null, null);
    }

    /**
     * Get the segment comment/label
     *
     * @return
     */
    public String getLabel() {
        return label;
    }

    /**
     * Get the type of the segment
     *
     * @return segment type
     */
    public SegmentType getType() {
        return type;
    }

    /**
     * Get the segment point
     *
     * @return point
     * @throws NullPointerException if segment has no point
     */
    public PartialPosition getPoint() {
        if (point == null)
            throw new NullPointerException(type + " segment has no point!");

        return point;
    }

    /**
     * Get the absolute center that an arc segment curves around
     *
     * @return the arc center, or null if this is not an arc segment
     */
    public Point2D getArcCenter() {
        return arcCenter == null ? null : new Point2D.Double(arcCenter.getX(), arcCenter.getY());
    }

    public Integer getSpindleSpeed() {
        return spindleSpeed;
    }

    public Integer getFeedSpeed() {
        return feedSpeed;
    }
}
