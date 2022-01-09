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

import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.gcode.path.GcodePath;
import com.willwinder.universalgcodesender.model.PartialPosition;
import org.locationtech.jts.geom.Geometry;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.willwinder.ugs.nbp.designer.gcode.toolpaths.ToolPathUtils.*;

/**
 * @author Joacim Breiler
 */
public class SimplePath extends AbstractToolPath {
    private final Cuttable source;

    private double offset;

    public SimplePath(Cuttable source) {
        this.source = source;
    }

    @Override
    public GcodePath toGcodePath() {
        Geometry geometry = convertAreaToGeometry(new Area(source.getShape()), getGeometryFactory());
        Geometry bufferedGeometry = geometry.buffer(offset);
        List<Geometry> geometries = toGeometryList(bufferedGeometry);

        ArrayList<List<PartialPosition>> coordinateList = new ArrayList<>();
        geometries.forEach(g -> {
            List<PartialPosition> geometryCoordinates = geometryToCoordinates(g);

            double currentDepth = getStartDepth();
            while (currentDepth < getTargetDepth()) {

                currentDepth += getDepthPerPass();
                if (currentDepth > getTargetDepth()) {
                    currentDepth = getTargetDepth();
                }

                final double depth = -currentDepth;
                coordinateList.add(geometryCoordinates.stream()
                        .map(numericCoordinate -> PartialPosition.builder().copy(numericCoordinate).setZ(depth).build())
                        .collect(Collectors.toList()));
            }
        });

        return toGcodePath(coordinateList);
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }
}
