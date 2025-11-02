/*
    Copyright 2021-2024 Will Winder

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
package com.willwinder.ugs.nbp.designer.io.gcode.toolpaths;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.nbp.designer.model.Settings;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.locationtech.jts.geom.Geometry;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joacim Breiler
 */
public class OutlineToolPath extends AbstractToolPath {
    private final Cuttable source;

    private double offset;

    public OutlineToolPath(Settings settings, Cuttable source) {
        super(settings);
        this.source = source;
    }

    private static void addGeometriesToCoordinateList(ArrayList<List<PartialPosition>> coordinateList, List<PartialPosition> geometryCoordinates, double depth) {
        coordinateList.add(geometryCoordinates.stream()
                .map(numericCoordinate -> PartialPosition.builder(numericCoordinate).setZ(-depth).build())
                .toList());
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }

    @Override
    protected void addSafeHeightSegment(GcodePath gcodePath, PartialPosition coordinate, boolean isFirst) {
        if (isFirst) {
            super.addSafeHeightSegment(gcodePath, coordinate, true);
        } else {
            // Outline Paths always Start and end in the same spot so its worthwhile to only climb a smaller amount
            double safeHeightToUse = settings.getSafeHeight() + (coordinate != null && coordinate.hasZ() ? coordinate.getZ() : -getStartDepth());
            PartialPosition safeHeightCoordinate = PartialPosition.from(Axis.Z, safeHeightToUse, UnitUtils.Units.MM);
            gcodePath.addSegment(SegmentType.MOVE, safeHeightCoordinate);
        }
    }

    @Override
    public void appendGcodePath(GcodePath gcodePath, Settings settings) {
        List<Geometry> geometries;
        if (ToolPathUtils.isClosedGeometry(source.getShape())) {
            Geometry geometry = ToolPathUtils.convertAreaToGeometry(new Area(source.getShape()), getGeometryFactory(), settings.getFlatnessPrecision());
            Geometry bufferedGeometry = geometry.buffer(offset);
            geometries = ToolPathUtils.toGeometryList(bufferedGeometry);
        } else {
            geometries = ToolPathUtils.convertShapeToGeometry(source.getShape(), getGeometryFactory(), settings.getFlatnessPrecision());
        }


        ArrayList<List<PartialPosition>> coordinateList = new ArrayList<>();
        geometries.forEach(g -> {
            List<PartialPosition> geometryCoordinates = ToolPathUtils.geometryToCoordinates(g);

            addGeometriesToCoordinateList(coordinateList, geometryCoordinates, getStartDepth());

            double currentDepth = getStartDepth();
            while (currentDepth < getTargetDepth()) {
                currentDepth += settings.getDepthPerPass();
                if (currentDepth > getTargetDepth()) {
                    currentDepth = getTargetDepth();
                }

                addGeometriesToCoordinateList(coordinateList, geometryCoordinates, currentDepth);
            }
        });

        addToGcodePath(gcodePath, coordinateList, source);
    }
}
