package com.willwinder.ugs.nbp.designer.io.gcode.toolpaths;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.io.gcode.path.SegmentType;
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

    public DrillCenterToolPath(Cuttable source) {
        this.source = source;
    }

    @Override
    public GcodePath toGcodePath() {
        Point2D center = source.getCenter();
        PartialPosition centerPosition = PartialPosition.builder()
                .setX(center.getX())
                .setY(center.getY())
                .setUnits(UnitUtils.Units.MM)
                .build();

        GcodePath gcodePath = new GcodePath();
        addSafeHeightSegmentTo(gcodePath, centerPosition);

        double currentDepth = getStartDepth() - getDepthPerPass();
        while (currentDepth < getTargetDepth()) {
            currentDepth += getDepthPerPass();
            if (currentDepth > getTargetDepth()) {
                currentDepth = getTargetDepth();
            }

            final double depth = -currentDepth;
            gcodePath.addSegment(SegmentType.POINT, PartialPosition.builder()
                    .copy(centerPosition)
                    .setZ(depth)
                    .build());

            gcodePath.addSegment(SegmentType.POINT, PartialPosition.builder()
                    .copy(centerPosition)
                    .setZ(0d)
                    .build());
        }

        addSafeHeightSegment(gcodePath);
        return gcodePath;
    }
}
