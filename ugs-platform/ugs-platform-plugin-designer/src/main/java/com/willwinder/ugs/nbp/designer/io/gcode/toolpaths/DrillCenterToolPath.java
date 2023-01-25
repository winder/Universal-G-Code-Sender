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
        PartialPosition centerPosition = getCenterPosition();
        GcodePath gcodePath = new GcodePath();
        addSafeHeightSegmentTo(gcodePath, centerPosition);

        double currentDepth = getStartDepth() - getDepthPerPass();
        while (currentDepth < getTargetDepth()) {
            currentDepth += getDepthPerPass();
            if (currentDepth > getTargetDepth()) {
                currentDepth = getTargetDepth();
            }

            final double depth = -currentDepth;
            addDepthSegment(gcodePath, depth);

            if (currentDepth != 0) {
                addDepthSegment(gcodePath, 0d);
            }
        }

        addSafeHeightSegment(gcodePath);
        return gcodePath;
    }

    private void addDepthSegment(GcodePath gcodePath, double depth) {
        gcodePath.addSegment(SegmentType.POINT, PartialPosition.builder()
                .setZ(depth)
                .setUnits(UnitUtils.Units.MM)
                .build());
    }

    private PartialPosition getCenterPosition() {
        Point2D center = source.getCenter();
        return PartialPosition.builder()
                .setX(center.getX())
                .setY(center.getY())
                .setUnits(UnitUtils.Units.MM)
                .build();
    }
}
