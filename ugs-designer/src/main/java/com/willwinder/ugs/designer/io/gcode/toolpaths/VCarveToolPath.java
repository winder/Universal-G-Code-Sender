/*
    Copyright 2026 Will Winder

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
import com.willwinder.ugs.designer.entities.cuttable.Direction;
import com.willwinder.ugs.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.operation.distance.IndexedFacetDistance;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

/**
 * Carves a shape with a V-shaped bit by running the tool along the centre line of the shape and
 * varying its height. A V-shaped bit cuts a wider line the deeper it is lowered, so following the
 * centre line at the depth where the bit is exactly as wide as the shape reproduces the shape
 * itself: strokes taper as they narrow and corners come to a point.
 * <p>
 * The target depth is the deepest the tool is allowed to go, which limits how wide a line the bit
 * can open up. Wherever the shape is wider than that, the centre line cannot describe it any more,
 * and the part that is out of reach is cleared as a flat bottomed pocket at the target depth
 * instead.
 *
 * @author Joacim Breiler
 */
public class VCarveToolPath extends AbstractToolPath {
    private final Cuttable source;

    public VCarveToolPath(Settings settings, Cuttable source) {
        super(settings);
        this.source = source;
    }

    @Override
    public void appendGcodePath(GcodePath gcodePath, Settings settings) {
        if (getTargetDepth() <= getStartDepth()) {
            return;
        }

        Geometry shape = ToolPathUtils.convertAreaToGeometry(new Area(source.getShape()), getGeometryFactory(), settings.getFlatnessPrecision());
        if (shape.isEmpty() || shape.getArea() <= 0) {
            return;
        }

        // How much wider the carved line gets for every millimeter the tool is lowered
        double widthPerDepth = Math.tan(Math.toRadians(settings.getVBitAngle() / 2d));
        double maxCarveRadius = (getTargetDepth() - getStartDepth()) * widthPerDepth;

        List<List<PartialPosition>> coordinateList = new ArrayList<>();
        addCentreLines(coordinateList, shape, settings, widthPerDepth, maxCarveRadius);
        addPocket(coordinateList, shape, settings, maxCarveRadius);

        addToGcodePath(gcodePath, coordinateList, source);
    }

    private void addCentreLines(List<List<PartialPosition>> coordinateList, Geometry shape, Settings settings, double widthPerDepth, double maxCarveRadius) {
        IndexedFacetDistance outlineDistance = new IndexedFacetDistance(shape.getBoundary());
        double resolution = calculateResolution(settings, widthPerDepth);

        for (LineString centreLine : MedialAxis.compute(shape, resolution)) {
            List<PartialPosition> carve = new ArrayList<>();
            for (Coordinate coordinate : centreLine.getCoordinates()) {
                double radius = outlineDistance.distance(ToolPathUtils.GEOMETRY_FACTORY.createPoint(coordinate));
                if (radius > maxCarveRadius) {
                    // Out of reach of the bit, so the pocket takes over from here
                    addCarve(coordinateList, carve);
                    carve = new ArrayList<>();
                    continue;
                }

                double depth = getStartDepth() + (radius / widthPerDepth);
                carve.add(new PartialPosition(coordinate.getX(), coordinate.getY(), -depth, UnitUtils.Units.MM));
            }
            addCarve(coordinateList, carve);
        }
    }

    /**
     * Clears what the bit could not open up wide enough to reach, at the target depth. The
     * outermost ring is the contour where the flank of the bit still just touches the outline of
     * the shape, and the rings inside it clear the flat bottom that is left within.
     */
    private void addPocket(List<List<PartialPosition>> coordinateList, Geometry shape, Settings settings, double maxCarveRadius) {
        Geometry core = shape.buffer(-maxCarveRadius);
        if (core.isEmpty()) {
            return;
        }

        // At the target depth the bit is only as wide as the cone it has opened up, unless it is
        // buried past its full diameter. Stepping over by less than the contours are accurate to
        // would only add passes that cannot clear anything, so that is as narrow as it gets.
        double stepOver = clampedStepOver(settings);
        double cuttingDiameter = Math.max(settings.getFlatnessPrecision() / stepOver,
                Math.min(settings.getToolDiameter(), maxCarveRadius * 2));
        List<Geometry> rings = ToolPathUtils.bufferAndCollectGeometries(core, 0, cuttingDiameter, stepOver, settings.getFlatnessPrecision());
        rings.sort(new GeometrySizeComparator());

        Geometry shell = core.buffer(-cuttingDiameter / 2d);
        ToolPathUtils.addGeometriesToCoordinatesList(shell, rings, coordinateList, getTargetDepth(), source.getDirection() == Direction.CLIMB);
    }

    /**
     * How finely to follow the centre line. Moving sideways by more than the tool step over would
     * engage more of the bit than intended, while moving down by more than the depth per pass
     * would turn the sloping wall into visible steps — so the tighter of the two wins.
     */
    private double calculateResolution(Settings settings, double widthPerDepth) {
        double sideways = settings.getToolDiameter() * clampedStepOver(settings);
        double downwards = settings.getDepthPerPass() * widthPerDepth;
        return Math.max(0.01, Math.min(sideways, downwards));
    }

    private static double clampedStepOver(Settings settings) {
        return Math.min(Math.max(0.01, Math.abs(settings.getToolStepOver())), 1.0);
    }

    private static void addCarve(List<List<PartialPosition>> coordinateList, List<PartialPosition> carve) {
        if (carve.size() > 1) {
            coordinateList.add(carve);
        }
    }
}
