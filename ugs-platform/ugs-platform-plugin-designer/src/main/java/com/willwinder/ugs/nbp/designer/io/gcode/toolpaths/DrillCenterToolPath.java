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
import com.willwinder.ugs.nbp.designer.io.gcode.path.Segment;
import com.willwinder.ugs.nbp.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.nbp.designer.model.Settings;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;

import java.awt.geom.Point2D;

/**
 * Drills a hole in the center of the given shape.
 *
 * @author Joacim Breiler
 */
public class DrillCenterToolPath extends AbstractToolPath {
    private final Cuttable source;

    public DrillCenterToolPath(Settings settings, Cuttable source) {
        super(settings);
        this.source = source;
    }

    private void addDepthSegment(GcodePath gcodePath, double depth) {
        gcodePath.addSegment(SegmentType.POINT, PartialPosition.builder(UnitUtils.Units.MM)
                .setZ(depth)
                .build());
    }

    private void addUpSegment(GcodePath gcodePath) {
        gcodePath.addSegment(SegmentType.MOVE, PartialPosition.builder(UnitUtils.Units.MM)
                .setZ(-getStartDepth())
                .build());
    }

    private PartialPosition getCenterPosition() {
        Point2D center = source.getCenter();
        return PartialPosition.builder(UnitUtils.Units.MM)
                .setX(center.getX())
                .setY(center.getY())
                .build();
    }

    @Override
    public void appendGcodePath(GcodePath gcodePath, Settings settings) {
        PartialPosition centerPosition = getCenterPosition();
        addSafeHeightSegmentTo(gcodePath, centerPosition,true);
        if (source.getSpindleSpeed() > 0) {
            gcodePath.addSegment(new Segment(SegmentType.SEAM, null, null, (int) Math.round(settings.getMaxSpindleSpeed() * (source.getSpindleSpeed() / 100d)), null));
        }
        addDepthSegment(gcodePath, -getStartDepth());

        double currentDepth = getStartDepth();
        while (currentDepth < getTargetDepth()) {
            currentDepth += settings.getDepthPerPass();
            if (currentDepth > getTargetDepth()) {
                currentDepth = getTargetDepth();
            }

            addDepthSegment(gcodePath, -currentDepth);

            if (currentDepth != 0) {
                addUpSegment(gcodePath);
            }
        }

        addSafeHeightSegment(gcodePath,null,true);
    }
}
