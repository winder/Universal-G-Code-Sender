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


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A toolpath.
 * <p>Paths can be divided into two types: numeric or symbolic. A numeric path contains
 * only numeric coordinates, while a symbolic path may contain a mixture of numeric and symbolic coordinates.
 * Some path manipulation functions can only be used on numeric paths.
 */
public class GcodePath implements PathGenerator {

    private List<Segment> segments;

    public GcodePath() {
        segments = new ArrayList<>();
    }

    private GcodePath(List<Segment> segments) {
        this.segments = segments;
    }

    /**
     * Add a new segment
     *
     * @param type  segment type
     * @param point segment coordinates
     */
    public void addSegment(SegmentType type, Coordinate point) {
        segments.add(new Segment(type, point));
    }

    /**
     * Add a new labeled segment
     *
     * @param type
     * @param point
     * @param label
     */
    public void addSegment(SegmentType type, Coordinate point, String label) {
        segments.add(new Segment(type, point, label));
    }

    public List<Segment> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    /**
     * Split this path at the seams.
     *
     * @return new paths
     */
    public List<GcodePath> splitAtSeams() {
        List<GcodePath> subpaths = new ArrayList<>();
        GcodePath sp = new GcodePath();
        for (Segment s : segments) {
            if (s.type == SegmentType.SEAM) {
                if (!sp.isEmpty())
                    subpaths.add(sp);
                sp = new GcodePath();
            } else
                sp.segments.add(s);
        }
        if (!sp.isEmpty())
            subpaths.add(sp);
        return subpaths;
    }

    /**
     * Split this path at move commands.
     *
     * @return new paths
     */
    public List<GcodePath> splitAtSubpaths() {
        List<GcodePath> subpaths = new ArrayList<>();
        GcodePath sp = new GcodePath();
        for (Segment s : segments) {
            if (s.type == SegmentType.MOVE) {
                if (!sp.isEmpty()) {
                    if (sp.segments.get(0).getType() == SegmentType.SEAM) {
                        // No use starting a path with a seam
                        sp.segments.remove(0);
                    }
                    if (!sp.isEmpty())
                        subpaths.add(sp);
                    sp = new GcodePath();
                }
            }

            sp.segments.add(s);
        }
        if (!sp.isEmpty()) {
            if (sp.segments.get(0).getType() == SegmentType.SEAM) {
                // No use starting a path with a seam
                sp.segments.remove(0);
            }
            if (!sp.isEmpty())
                subpaths.add(sp);
        }
        return subpaths;
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
     * Is this a closed path? I.e. are the XY coordinates of the first and last point the same?
     *
     * @return true if path is closed
     */
    public boolean isClosed() {
        Segment first = segments.get(0);
        Segment last = segments.get(segments.size() - 1);

        return first.point.get(Axis.X) == last.point.get(Axis.X) && first.point.get(Axis.Y) == last.point.get(Axis.Y);
    }


    /**
     * @return this
     */
    public GcodePath toGcodePath() {
        return this;
    }
}
