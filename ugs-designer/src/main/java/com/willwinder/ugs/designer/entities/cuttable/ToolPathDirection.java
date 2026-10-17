package com.willwinder.ugs.designer.entities.cuttable;

import com.willwinder.universalgcodesender.i18n.Localization;

/**
 * The direction that surfacing passes travel in. The angle is measured clockwise from the
 * positive X axis, like in {@code GeometryUtils.generateLineString} which lays out the passes.
 */
public enum ToolPathDirection {
    LEFT_RIGHT(Localization.getString("platform.plugin.designer.toolpath.direction.leftRight"), 0),
    RIGHT_LEFT(Localization.getString("platform.plugin.designer.toolpath.direction.rightLeft"), 180),
    TOP_BOTTOM(Localization.getString("platform.plugin.designer.toolpath.direction.topBottom"), 90),
    BOTTOM_TOP(Localization.getString("platform.plugin.designer.toolpath.direction.bottomTop"), 270);

    private final String label;
    private final double angle;

    ToolPathDirection(String label, double angle) {
        this.label = label;
        this.angle = angle;
    }

    public String getLabel() {
        return label;
    }

    public double getAngle() {
        return angle;
    }

    public boolean isVertical() {
        return this == TOP_BOTTOM || this == BOTTOM_TOP;
    }
}
