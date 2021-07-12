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

import com.willwinder.ugs.nbp.designer.gcode.path.Axis;
import com.willwinder.ugs.nbp.designer.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.gcode.path.NumericCoordinate;
import com.willwinder.ugs.nbp.designer.gcode.path.PathGenerator;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joacim Breiler
 */
public class SimplePocket extends AbstractToolPath {
    private final PathGenerator source;

    /**
     * How much should the tool cut for each pass. Should be larger than 0 and smaller than 1.
     * 0.1 would cut 10% of the tool diameter for each pass and 1 would cut 100% of the tool diameter.
     */
    private double stepOver = 0.3;


    public SimplePocket(PathGenerator source) {
        this.source = source;
    }

    @Override
    public GcodePath toGcodePath() {
        LinearRing linearRing = pathToLinearRing(source.toGcodePath());
        Polygon polygon = new Polygon(linearRing, new LinearRing[0], getGeometryFactory());

        List<List<NumericCoordinate>> coordinateList = new ArrayList<>();
        double currentDepth = 0;
        while (currentDepth < getTargetDepth()) {
            currentDepth += getDepthPerPass();
            if(currentDepth > getTargetDepth()) {
                currentDepth = getTargetDepth();
            }

            double buffer = getToolDiameter() / 2d;
            while (true) {
                final double depth = currentDepth;
                List<NumericCoordinate> bufferedCoordinates = geometryToCoordinates(simplifyGeometry(polygon.buffer(-buffer)))
                        .stream()
                        .map(coordinate -> new NumericCoordinate(coordinate.get(Axis.X), coordinate.get(Axis.Y), -depth))
                        .collect(Collectors.toList());

                if (bufferedCoordinates.isEmpty() || bufferedCoordinates.size() <= 1) {
                    break;
                }

                coordinateList.add(bufferedCoordinates);
                buffer = buffer + (getToolDiameter() * stepOver);
            }
        }

        return toGcodePath(coordinateList);
    }

    public void setStepOver(double stepOver) {
        this.stepOver = Math.min(Math.max(0.01, Math.abs(stepOver)), 1.0);
    }
}
