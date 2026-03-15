package com.willwinder.ugs.nbp.designer.entities.cuttable;

import com.willwinder.universalgcodesender.i18n.Localization;

public enum ToolPathDirection {
    VERTICAL(Localization.getString("platform.plugin.designer.toolpath.direction.vertical")),
    HORIZONTAL(Localization.getString("platform.plugin.designer.toolpath.direction.horizontal"));

    private final String label;

    ToolPathDirection(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
