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
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.willwinder.ugs.nbp.designer.gcode.toolpaths.ToolPathUtils.*;

/**
 * @author Joacim Breiler
 */
public class SimplePath extends AbstractToolPath {
    private final PathGenerator source;


    private double offset;

    public SimplePath(PathGenerator source) {
        this.source = source;
    }

    @Override
    public GcodePath toGcodePath() {
        final List<GcodePath> sources = source.toGcodePath().splitAtSubpaths();

        // Sort the paths from smallest to largest, so the small (possibly inner) parts
        // get cut first.
        sources.sort(new GcodePathAreaComparator(sources));

        List<List<NumericCoordinate>> coordinateList = new ArrayList<>();
        for (GcodePath path : sources) {
            GcodePath sourcePath = path.toGcodePath();
            if (sourcePath.getSize() < 4) {
                continue;
            }

            LinearRing linearRing = pathToLinearRing(sourcePath);
            Polygon polygon = new Polygon(linearRing, new LinearRing[0], getGeometryFactory());
            List<Geometry> geometries = toGeometryList(polygon.buffer(offset));

            geometries.forEach(geometry -> {
                List<NumericCoordinate> geometryCoordinates = geometryToCoordinates(geometry);

                double currentDepth = 0;
                while (currentDepth < getTargetDepth()) {

                    currentDepth += getDepthPerPass();
                    if (currentDepth > getTargetDepth()) {
                        currentDepth = getTargetDepth();
                    }

                    final double depth = -currentDepth;
                    coordinateList.add(geometryCoordinates.stream()
                            .map(numericCoordinate -> new NumericCoordinate(numericCoordinate.get(Axis.X), numericCoordinate.get(Axis.Y), depth))
                            .collect(Collectors.toList()));
                }
            });
        }

        return toGcodePath(coordinateList);
    }

    public void setOffset(double offset) {
        this.offset = offset;
    }
}
