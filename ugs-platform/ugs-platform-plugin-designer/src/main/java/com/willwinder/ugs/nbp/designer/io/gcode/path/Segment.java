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
package com.willwinder.ugs.nbp.designer.io.gcode.path;

import com.willwinder.universalgcodesender.model.PartialPosition;

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

    public Segment(SegmentType type, PartialPosition point, String label, Integer spindleSpeed, Integer feedSpeed) {
        this.type = type;
        this.point = point;
        this.label = label;
        this.spindleSpeed = spindleSpeed;
        this.feedSpeed = feedSpeed;
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

    public Integer getSpindleSpeed() {
        return spindleSpeed;
    }

    public Integer getFeedSpeed() {
        return feedSpeed;
    }
}
