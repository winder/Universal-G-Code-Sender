/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.ugs.designer.io.gcode.toolpaths;

import com.willwinder.ugs.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.designer.io.gcode.path.Segment;
import com.willwinder.ugs.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.designer.model.Settings;
import static com.willwinder.ugs.designer.utils.GeometryUtils.generateLineString;
import static com.willwinder.ugs.designer.utils.GeometryUtils.getOffsetRange;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPoint;

import java.awt.geom.Area;
import java.util.List;

/**
 * Fills a shape with a plotter by hatching it with parallel lines, drawn at the angle and the
 * spacing set on the shape. Every line is drawn with the pen down and reached with it up, leaving
 * it to the gcode writer to turn the pen segments into whatever the machine expects.
 *
 * @author Joacim Breiler
 */
public class PlotterFillToolPath extends AbstractToolPath {
    private final Cuttable source;

    /**
     * Where the pen was left by the previous line, used to pick which end of the next line to
     * start from
     */
    private PartialPosition penPosition;

    public PlotterFillToolPath(Settings settings, Cuttable source) {
        super(settings);
        this.source = source;
    }

    @Override
    public void appendGcodePath(GcodePath gcodePath, Settings settings) {
        double angle = source.getToolPathAngle();
        double spacing = source.getLineSpacing();

        getGeometries().forEach(geometry -> {
            Envelope envelope = geometry.getEnvelopeInternal();
            double[] offsetRange = getOffsetRange(envelope, angle);

            double currentOffset = offsetRange[0];
            while (currentOffset <= offsetRange[1]) {
                LineString lineString = generateLineString(envelope, currentOffset, angle);
                if (lineString != null) {
                    addLineIntersectionSegments(gcodePath, geometry, lineString);
                }
                currentOffset += spacing;
            }
        });

        gcodePath.addSegment(new Segment(SegmentType.PEN_UP, null));
    }

    /**
     * The area to hatch, held half a pen width inside the shape. The pen draws a line of its own
     * width centered on where it is moved, so hatching all the way out to the outline would leave
     * half of the outermost line outside the shape.
     * <p>
     * A shape narrower than the pen has nothing left once it is held inside, and is not drawn at
     * all rather than being drawn wider than it is.
     */
    private List<Geometry> getGeometries() {
        if (!ToolPathUtils.isClosedGeometry(source.getShape())) {
            return ToolPathUtils.convertShapeToGeometry(source.getShape(), getGeometryFactory(), settings.getFlatnessPrecision());
        }

        Geometry geometry = ToolPathUtils.convertAreaToGeometry(new Area(source.getShape()), getGeometryFactory(), settings.getFlatnessPrecision());
        Geometry inset = geometry.buffer(-settings.getPenWidth() / 2d);
        return inset.isEmpty() ? List.of() : List.of(inset);
    }

    /**
     * Draws the parts of one hatch line that fall inside the shape. Each line is drawn from
     * whichever of its ends the pen is already closest to, so the fill zig-zags across the shape
     * instead of returning to the same side before every line.
     */
    private void addLineIntersectionSegments(GcodePath gcodePath, Geometry geometry, LineString lineString) {
        Geometry intersection = geometry.intersection(lineString);

        // A line that only touches the shape in single points has nothing to draw between them
        if (intersection instanceof MultiPoint) {
            return;
        }

        List<PartialPosition> coordinates = ToolPathUtils.geometryToCoordinates(intersection).stream()
                .map(coordinate -> new PartialPosition(coordinate.getX(), coordinate.getY(), UnitUtils.Units.MM))
                .toList();

        for (int i = 0; i + 1 < coordinates.size(); i += 2) {
            PartialPosition start = coordinates.get(i);
            PartialPosition end = coordinates.get(i + 1);
            if (isCloserTo(end, start)) {
                start = coordinates.get(i + 1);
                end = coordinates.get(i);
            }

            gcodePath.addSegment(new Segment(SegmentType.PEN_UP, null));
            gcodePath.addSegment(SegmentType.MOVE, start);
            gcodePath.addSegment(new Segment(SegmentType.PEN_DOWN, null));
            gcodePath.addSegment(SegmentType.LINE, end, source.getFeedRate());
            penPosition = end;
        }
    }

    /**
     * If the pen would travel a shorter distance to reach the first position than the second one.
     * With the pen not yet placed anywhere either end is as good as the other.
     */
    private boolean isCloserTo(PartialPosition first, PartialPosition second) {
        if (penPosition == null) {
            return false;
        }

        return distanceTo(first) < distanceTo(second);
    }

    private double distanceTo(PartialPosition position) {
        return Math.hypot(position.getX() - penPosition.getX(), position.getY() - penPosition.getY());
    }
}
