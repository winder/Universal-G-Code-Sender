/*
    Copyright 2023-2024 Will Winder

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
import static com.willwinder.ugs.designer.io.gcode.toolpaths.ToolPathUtils.addGeometriesToCoordinatesList;
import static com.willwinder.ugs.designer.io.gcode.toolpaths.ToolPathUtils.bufferAndCollectGeometries;
import static com.willwinder.ugs.designer.io.gcode.toolpaths.ToolPathUtils.convertAreaToGeometry;
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.universalgcodesender.model.PartialPosition;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Joacim Breiler
 */
public class PocketToolPath extends AbstractToolPath {
    private final Cuttable source;

    public PocketToolPath(Settings settings, Cuttable source) {
        super(settings);
        this.source = source;
    }

    @Override
    public void appendGcodePath(GcodePath gcodePath, Settings settings) {
        boolean clockwise = source.getDirection() == Direction.CLIMB;
        double stepOver = Math.min(Math.max(0.01, Math.abs(settings.getToolStepOver())), 1.0);
        Geometry pocket = convertAreaToGeometry(new Area(source.getShape()), getGeometryFactory(), settings.getFlatnessPrecision());

        List<AreaPath> areas = new ArrayList<>();
        for (int i = 0; i < pocket.getNumGeometries(); i++) {
            AreaPath area = toAreaPath(pocket.getGeometryN(i), settings, stepOver, clockwise);
            if (!area.isEmpty()) {
                areas.add(area);
            }
        }

        List<List<PartialPosition>> coordinateList = new ArrayList<>();
        inTravelOrder(areas, pocket.getEnvelopeInternal()).forEach(area -> coordinateList.addAll(area.runs()));

        addToGcodePath(gcodePath, coordinateList, source);
    }

    /**
     * The runs clearing one area of the pocket, one depth of cut at a time, so that the area is
     * finished before the tool moves on to the next one.
     */
    private AreaPath toAreaPath(Geometry area, Settings settings, double stepOver, boolean clockwise) {
        Geometry shell = area.buffer(-settings.getToolDiameter() / 2d);
        List<Geometry> geometries = bufferAndCollectGeometries(area, settings.getToolDiameter(), stepOver, settings.getFlatnessPrecision());

        List<List<PartialPosition>> runs = new ArrayList<>();
        addGeometriesToCoordinatesList(shell, geometries, runs, getStartDepth(), clockwise);

        double currentDepth = getStartDepth();
        while (currentDepth < getTargetDepth()) {
            currentDepth = Math.min(getTargetDepth(), currentDepth + settings.getDepthPerPass());
            addGeometriesToCoordinatesList(shell, geometries, runs, currentDepth, clockwise);
        }

        return new AreaPath(runs.stream().filter(run -> !run.isEmpty()).toList());
    }

    /**
     * Orders the areas along a Hilbert curve, which keeps areas lying close to each other next to each
     * other in the order and the rapid movements between them short.
     */
    private static List<AreaPath> inTravelOrder(List<AreaPath> areas, Envelope envelope) {
        HilbertPositionComparator comparator = new HilbertPositionComparator(envelope);

        List<AreaPath> ordered = new ArrayList<>(areas);
        ordered.sort(Comparator.comparingLong(area -> comparator.index(area.entry().getX(), area.entry().getY())));
        return ordered;
    }

    /**
     * The runs clearing one of the areas that the pocket is made up of.
     */
    private record AreaPath(List<List<PartialPosition>> runs) {
        boolean isEmpty() {
            return runs.isEmpty();
        }

        /**
         * Where the tool starts cutting the area
         */
        PartialPosition entry() {
            return runs.get(0).get(0);
        }
    }
}
