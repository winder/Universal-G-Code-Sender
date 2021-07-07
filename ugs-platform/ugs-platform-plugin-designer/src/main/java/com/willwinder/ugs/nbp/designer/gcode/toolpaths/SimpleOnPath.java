/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.designer.gcode.toolpaths;

import com.willwinder.ugs.nbp.designer.gcode.path.*;

import java.util.Iterator;
import java.util.List;

/**
 * @author Joacim Breiler
 */
public class SimpleOnPath implements PathGenerator {
    private final PathGenerator source;
    private double depth;
    private double passDepth = 1;

    public SimpleOnPath(PathGenerator source) {
        this.source = source;
    }

    @Override
    public GcodePath toGcodePath() {
        final List<GcodePath> sources = source.toGcodePath().splitAtSubpaths();

        // Sort the paths from smallest to largest, so the small (possibly inner) parts
        // get cut first.
        sources.sort(new GcodePathAreaComparator(sources));

        GcodePath outlinePath = new GcodePath();
        for (GcodePath path : sources) {
            makePath(outlinePath, path);
        }
        return outlinePath;
    }

    private void makePath(GcodePath cut, GcodePath source) {
        final double targetDepth = 0 - depth + passDepth;
        double currentDepth = passDepth;
        while (currentDepth > targetDepth) {
            currentDepth -= passDepth;
            if (currentDepth < targetDepth) {
                currentDepth = targetDepth;
            }

            Iterator<Segment> segmentIterator = source.getSegments().iterator();
            Coordinate firstPoint = segmentIterator.next().getPoint().offset(new NumericCoordinate(null, null, currentDepth), false, true);
            cut.addSegment(SegmentType.POINT, firstPoint);

            while (segmentIterator.hasNext()) {
                Segment s = segmentIterator.next();
                cut.addSegment(s.type, s.point.offset(new NumericCoordinate(null, null, currentDepth), false, true));
            }
        }

    }

    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }

    public double getPassDepth() {
        return passDepth;
    }

    public void setPassDepth(double passDepth) {
        this.passDepth = passDepth;
    }

    public PathGenerator getSource() {
        return source;
    }
}
