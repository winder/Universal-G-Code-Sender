package com.willwinder.ugs.nbp.designer.gcode.toolpaths;

import com.willwinder.ugs.nbp.designer.gcode.path.*;

import java.util.Iterator;
import java.util.List;

public class SimpleOutline implements PathGenerator {
    private final PathGenerator source;
    private double depth;
    private double passDepth = 1;

    public SimpleOutline(PathGenerator source) {
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
