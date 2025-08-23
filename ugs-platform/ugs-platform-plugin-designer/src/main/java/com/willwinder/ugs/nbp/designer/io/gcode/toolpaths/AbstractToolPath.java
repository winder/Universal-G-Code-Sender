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
import com.willwinder.ugs.nbp.designer.io.gcode.path.PathGenerator;
import com.willwinder.ugs.nbp.designer.io.gcode.path.Segment;
import com.willwinder.ugs.nbp.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.nbp.designer.model.Settings;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.List;

public abstract class AbstractToolPath implements PathGenerator {

    protected final Settings settings;
    private final GeometryFactory geometryFactory = new GeometryFactory();
    /**
     * The depth to start from in millimeters
     */
    private double startDepth = 0;
    /**
     * The depth that we are targeting for in millimeters
     */
    private double targetDepth = 0;

    protected AbstractToolPath(Settings settings) {
        this.settings = settings;
    }

    public double getStartDepth() {
        return startDepth;
    }

    public void setStartDepth(double startDepth) {
        this.startDepth = startDepth;
    }

    public double getTargetDepth() {
        return targetDepth;
    }

    public void setTargetDepth(double targetDepth) {
        this.targetDepth = targetDepth;
    }

    protected void addSafeHeightSegment(GcodePath gcodePath, PartialPosition coordinate, boolean isFirst) {
        double safeHeight = settings.getSafeHeight();

        // If the start depth is negative we need to add it to the safe height to clear the material
        if (startDepth < 0) {
            safeHeight = safeHeight - startDepth;
        }

        PartialPosition safeHeightCoordinate = PartialPosition.from(Axis.Z, safeHeight, UnitUtils.Units.MM);
        gcodePath.addSegment(SegmentType.MOVE, safeHeightCoordinate);
    }

    protected void addSafeHeightSegmentTo(GcodePath gcodePath, PartialPosition coordinate, boolean isFirst) {
        addSafeHeightSegment(gcodePath,coordinate, isFirst);
        gcodePath.addSegment(SegmentType.MOVE, new PartialPosition(coordinate.getX(), coordinate.getY(), UnitUtils.Units.MM));
        if (!isFirst) {
            gcodePath.addSegment(SegmentType.MOVE, PartialPosition.from(Axis.Z, -getStartDepth(), UnitUtils.Units.MM));
        } else {
            addSafeHeightSegment(gcodePath,coordinate, isFirst);
        }
    }

    public GeometryFactory getGeometryFactory() {
        return geometryFactory;
    }

    protected void addToGcodePath(GcodePath gcodePath, List<List<PartialPosition>> coordinateList, Cuttable source) {
        if (!coordinateList.isEmpty()) {
            if (source.getSpindleSpeed() > 0) {
                gcodePath.addSegment(new Segment(SegmentType.SEAM, null, null, (int) Math.round(settings.getMaxSpindleSpeed() * (source.getSpindleSpeed() / 100d)), null));
            }
            
            coordinateList.forEach(cl -> {
                if (!cl.isEmpty()) {
                    addSafeHeightSegmentTo(gcodePath, cl.get(0), coordinateList.get(0) == cl);
                            
                    gcodePath.addSegment(SegmentType.POINT, cl.get(0));
                    cl.forEach(c -> gcodePath.addSegment(SegmentType.LINE, c, source.getFeedRate()));
                }
            });

            addSafeHeightSegment(gcodePath, null,true);
        }
    }


    public GcodePath toGcodePath() {
        GcodePath gcodePath = new GcodePath();
        appendGcodePath(gcodePath, settings);
        return gcodePath;
    }
}
