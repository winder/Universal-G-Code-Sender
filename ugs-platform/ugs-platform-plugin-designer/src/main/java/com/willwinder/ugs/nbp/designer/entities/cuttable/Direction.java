package com.willwinder.ugs.nbp.designer.entities.cuttable;

import com.willwinder.universalgcodesender.i18n.Localization;

public enum Direction {
    CLIMB(Localization.getString("platform.plugin.designer.direction.climb")), // clockwise if starting from center
    CONVENTIONAL(Localization.getString("platform.plugin.designer.direction.conventional")) // counter clockwise if starting from center
    ;

    private final String label;

    Direction(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
