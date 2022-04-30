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
package com.willwinder.ugs.nbp.designer.gcode.path;

import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Position;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A tool path with segment steps.
 *
 * @author Calle Laakkonen
 * @author Joacim Breiler
 */
public class GcodePath implements PathGenerator {

    private List<Segment> segments;

    public GcodePath() {
        segments = new ArrayList<>();
    }

    /**
     * Add a new segment
     *
     * @param type  segment type
     * @param point segment coordinates
     */
    public void addSegment(SegmentType type, PartialPosition point) {
        segments.add(new Segment(type, point));
    }

    public List<Segment> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    /**
     * Get the size of the path
     *
     * @return path segment count
     */
    public int getSize() {
        return segments.size();
    }

    /**
     * Is this an empty path
     *
     * @return true if nothing has been added to the path yet
     */
    public boolean isEmpty() {
        return segments.isEmpty();
    }

    /**
     * @return this
     */
    public GcodePath toGcodePath() {
        return this;
    }
}
