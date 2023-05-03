/*
    Copyright 2023 Will Winder

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
import com.willwinder.universalgcodesender.model.PartialPosition;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.ToolPathUtils.convertAreaToGeometry;
import static com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.ToolPathUtils.geometryToCoordinates;
import static com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.ToolPathUtils.toPartialPosition;

/**
 * @author Joacim Breiler
 */
public class PocketToolPath extends AbstractToolPath {
    public static final double DISTANCE_TOLERANCE = 0.1;
    private final Cuttable source;

    /**
     * How much should the tool cut for each pass. Should be larger than 0 and smaller than 1.
     * 0.1 would cut 10% of the tool diameter for each pass and 1 would cut 100% of the tool diameter.
     */
    private double stepOver = 0.3;

    public PocketToolPath(Cuttable source) {
        this.source = source;
    }

    @Override
    public GcodePath toGcodePath() {
        Geometry geometryCollection = convertAreaToGeometry(new Area(source.getShape()), getGeometryFactory());
        Geometry shell = geometryCollection.buffer(-getToolDiameter() / 2d);
        List<Geometry> geometries = bufferAndCollectGeometries(geometryCollection);

        List<List<PartialPosition>> coordinateList = new ArrayList<>();
        double currentDepth = getStartDepth() - getDepthPerPass();
        while (currentDepth < getTargetDepth()) {
            currentDepth += getDepthPerPass();
            if (currentDepth > getTargetDepth()) {
                currentDepth = getTargetDepth();
            }

            addGeometriesToCoordinatesList(shell, geometries, coordinateList, currentDepth);
        }

        return toGcodePath(coordinateList);
    }

    private void addGeometriesToCoordinatesList(Geometry shell, List<Geometry> geometries, List<List<PartialPosition>> coordinateList, double currentDepth) {
        Geometry previousGeometry = null;
        List<PartialPosition> geometryLine = new ArrayList<>();
        for (int x = 0; x < geometries.size(); x++) {
            Geometry geometry = geometries.get(x);

            if (x > 0) {
                PartialPosition fromPosition = ToolPathUtils.toPartialPosition(previousGeometry.getCoordinates()[0], currentDepth);
                int newStartIndex = ToolPathUtils.findNearestCoordinateIndex(geometry.getCoordinates(), new Coordinate(fromPosition.getX(), fromPosition.getY(), fromPosition.getZ()));

                if (geometry instanceof LinearRing) {
                    geometry = rotateCoordinates((LinearRing) geometry, newStartIndex);
                }

                Coordinate firstCoordinate = geometry.getCoordinates()[0];
                PartialPosition nextPosition = toPartialPosition(firstCoordinate, currentDepth);

                LineString lineString = ToolPathUtils.createLineString(fromPosition, nextPosition);
                if (shell.crosses(lineString)) {
                    coordinateList.add(geometryLine);
                    geometryLine = new ArrayList<>();
                }
            }

            geometryLine.addAll(geometryToCoordinates(geometry, currentDepth));
            previousGeometry = geometry;
        }

        if (!geometryLine.isEmpty()) {
            coordinateList.add(geometryLine);
        }
    }

    private LinearRing rotateCoordinates(LinearRing nextGeometry, int newStartIndex) {
        Coordinate[] geomCoordinates = nextGeometry.getCoordinates();
        Coordinate[] newCoordinates = new Coordinate[geomCoordinates.length];
        int newIndex = 0;
        for (int coordIndex = newStartIndex; coordIndex < newCoordinates.length; coordIndex++) {
            newCoordinates[newIndex] = geomCoordinates[coordIndex];
            newIndex++;
        }

        for (int coordIndex = 1; coordIndex < newStartIndex; coordIndex++) {
            newCoordinates[newIndex] = geomCoordinates[coordIndex];
            newIndex++;
        }

        newCoordinates[newCoordinates.length - 1] = geomCoordinates[newStartIndex];
        nextGeometry = ToolPathUtils.createLinearRing(newCoordinates);
        return nextGeometry;
    }

    private List<Geometry> bufferAndCollectGeometries(Geometry geometry) {
        double buffer = getToolDiameter() / 2d;
        List<Geometry> geometries = bufferAndCollectGeometries(geometry, buffer);
        geometries.sort(new GeometrySizeComparator());
        return geometries;
    }

    private List<Geometry> bufferAndCollectGeometries(Geometry geometry, double buffer) {
        Geometry bufferedGeometry = geometry.buffer(-buffer);
        if (bufferedGeometry.getNumGeometries() <= 0 || bufferedGeometry.isEmpty()) {
            return Collections.emptyList();
        }

        List<Geometry> result = new ArrayList<>();
        for (int i = 0; i < bufferedGeometry.getNumGeometries(); i++) {
            Geometry geom = bufferedGeometry.getGeometryN(i);
            result.addAll(bufferAndCollectGeometries(geom, getToolDiameter() * stepOver));

            if (geom instanceof Polygon) {
                Polygon polygon = (Polygon) geom;
                result.add(DouglasPeuckerSimplifier.simplify(polygon.getExteriorRing(), DISTANCE_TOLERANCE));
                for (int j = 0; j < polygon.getNumInteriorRing(); j++) {
                    result.add(DouglasPeuckerSimplifier.simplify(polygon.getInteriorRingN(j), DISTANCE_TOLERANCE));
                }
            } else {
                result.add(DouglasPeuckerSimplifier.simplify(geom, DISTANCE_TOLERANCE));
            }
        }

        return result;
    }

    public void setStepOver(double stepOver) {
        this.stepOver = Math.min(Math.max(0.01, Math.abs(stepOver)), 1.0);
    }
}
