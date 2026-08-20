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
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.locationtech.jts.geom.Geometry;

import java.awt.geom.Area;
import java.util.List;

/**
 * Draws the outline of a shape with a plotter, following the line itself instead of offsetting for
 * a tool. The pen is only ever down or up, so the path carries no depths and leaves it to the gcode
 * writer to turn the pen segments into whatever the machine expects.
 *
 * @author Joacim Breiler
 */
public class PlotterToolPath extends AbstractToolPath {
    private final Cuttable source;

    public PlotterToolPath(Settings settings, Cuttable source) {
        super(settings);
        this.source = source;
    }

    @Override
    public void appendGcodePath(GcodePath gcodePath, Settings settings) {
        List<Geometry> geometries = getGeometries();
        if (geometries.isEmpty()) {
            return;
        }

        geometries.forEach(geometry -> addGeometrySegments(gcodePath, geometry));
        gcodePath.addSegment(new Segment(SegmentType.PEN_UP, null));
    }

    private List<Geometry> getGeometries() {
        if (ToolPathUtils.isClosedGeometry(source.getShape())) {
            Geometry geometry = ToolPathUtils.convertAreaToGeometry(new Area(source.getShape()), getGeometryFactory(), settings.getFlatnessPrecision());
            return ToolPathUtils.toGeometryList(geometry);
        }

        return ToolPathUtils.convertShapeToGeometry(source.getShape(), getGeometryFactory(), settings.getFlatnessPrecision());
    }

    private void addGeometrySegments(GcodePath gcodePath, Geometry geometry) {
        // The pen segments are what moves the pen, so the drawn path itself is kept to the XY plane
        List<PartialPosition> coordinates = ToolPathUtils.geometryToCoordinates(geometry).stream()
                .map(coordinate -> new PartialPosition(coordinate.getX(), coordinate.getY(), UnitUtils.Units.MM))
                .toList();
        if (coordinates.isEmpty()) {
            return;
        }

        gcodePath.addSegment(new Segment(SegmentType.PEN_UP, null));
        gcodePath.addSegment(SegmentType.MOVE, coordinates.get(0));
        gcodePath.addSegment(new Segment(SegmentType.PEN_DOWN, null));
        toMotionSegments(coordinates, source.getFeedRate()).forEach(gcodePath::addSegment);
    }
}
