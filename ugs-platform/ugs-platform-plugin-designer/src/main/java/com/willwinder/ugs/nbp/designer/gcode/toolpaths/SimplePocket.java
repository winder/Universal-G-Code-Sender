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
import java.util.Comparator;
import java.util.List;

import static com.willwinder.ugs.nbp.designer.gcode.toolpaths.ToolPathUtils.*;

/**
 * @author Joacim Breiler
 */
public class SimplePocket extends AbstractToolPath {
    private final Cuttable source;

    /**
     * How much should the tool cut for each pass. Should be larger than 0 and smaller than 1.
     * 0.1 would cut 10% of the tool diameter for each pass and 1 would cut 100% of the tool diameter.
     */
    private double stepOver = 0.3;

    public SimplePocket(Cuttable source) {
        this.source = source;
    }

    @Override
    public GcodePath toGcodePath() {
        List<List<PartialPosition>> coordinateList = new ArrayList<>();

        Geometry geometryCollection = convertAreaToGeometry(new Area(source.getShape()), getGeometryFactory());
        for (int i = 0; i < geometryCollection.getNumGeometries(); i++) {
            List<Geometry> geometries = bufferAndCollectGeometries(geometryCollection.getGeometryN(i));
            double currentDepth = getStartDepth();
            while (currentDepth < getTargetDepth()) {
                currentDepth += getDepthPerPass();
                if (currentDepth > getTargetDepth()) {
                    currentDepth = getTargetDepth();
                }

                List<List<PartialPosition>> geometriesCoordinates = geometriesToCoordinates(geometries, currentDepth);
                coordinateList.addAll(geometriesCoordinates);
            }
        }

        return toGcodePath(coordinateList);
    }

    private List<Geometry> bufferAndCollectGeometries(Geometry geometry) {
        List<Geometry> geometries = new ArrayList<>();
        double buffer = getToolDiameter() / 2d;
        while (true) {
            Geometry bufferedGeometry = geometry.buffer(-buffer);
            List<Geometry> bufferedGeometries = toGeometryList(bufferedGeometry);
            if(bufferedGeometries.isEmpty()) {
                geometries.sort(Comparator.comparingDouble(o -> o.getEnvelope().getArea()));
                return geometries;
            }

            geometries.addAll(bufferedGeometries);
            buffer = buffer + (getToolDiameter() * stepOver);
        }
    }

    public void setStepOver(double stepOver) {
        this.stepOver = Math.min(Math.max(0.01, Math.abs(stepOver)), 1.0);
    }
}
