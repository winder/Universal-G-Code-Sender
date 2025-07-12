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
package com.willwinder.ugs.nbp.designer.io.gcode.toolpaths;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.io.gcode.path.GcodePath;
import static com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.ToolPathUtils.addGeometriesToCoordinatesList;
import static com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.ToolPathUtils.bufferAndCollectGeometries;
import static com.willwinder.ugs.nbp.designer.io.gcode.toolpaths.ToolPathUtils.convertAreaToGeometry;
import com.willwinder.ugs.nbp.designer.model.Settings;
import com.willwinder.universalgcodesender.model.PartialPosition;
import org.locationtech.jts.geom.Geometry;

import java.awt.geom.Area;
import java.util.ArrayList;
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
        double stepOver = Math.min(Math.max(0.01, Math.abs(settings.getToolStepOver())), 1.0);
        Geometry geometryCollection = convertAreaToGeometry(new Area(source.getShape()), getGeometryFactory(), settings.getFlatnessPrecision());
        Geometry shell = geometryCollection.buffer(-settings.getToolDiameter() / 2d);
        List<Geometry> geometries = bufferAndCollectGeometries(geometryCollection, settings.getToolDiameter(), stepOver);

        List<List<PartialPosition>> coordinateList = new ArrayList<>();
        addGeometriesToCoordinatesList(shell, geometries, coordinateList, getStartDepth());

        double currentDepth = getStartDepth();
        while (currentDepth < getTargetDepth()) {
            currentDepth += settings.getDepthPerPass();
            if (currentDepth > getTargetDepth()) {
                currentDepth = getTargetDepth();
            }

            addGeometriesToCoordinatesList(shell, geometries, coordinateList, currentDepth);
        }

        addToGcodePath(gcodePath, coordinateList, source);
    }
}
