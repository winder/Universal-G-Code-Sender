/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.component.designer.editor;

import com.willwinder.ugs.designer.constraint.Constraint;
import com.willwinder.ugs.designer.constraint.DistanceConstraint;
import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;

/**
 * How dimensions look and feel in the editor, in the colors chosen in the design settings.
 */
public final class DimensionStyle {

    private DimensionStyle() {
    }

    /**
     * The color of the dimension's lines, or of a selected or hovered dimension.
     */
    public static Color color(boolean highlighted) {
        VisualizerSettings settings = VisualizerSettings.getInstance();
        return Color.web((highlighted ? settings.colorDesignConstraintHighlightProperty() : settings.colorDesignConstraintProperty()).get());
    }

    /**
     * The color of the dimension's label, or of a selected or hovered dimension's.
     */
    public static Color labelColor(boolean highlighted) {
        VisualizerSettings settings = VisualizerSettings.getInstance();
        return Color.web((highlighted ? settings.colorDesignConstraintHighlightProperty() : settings.colorDesignConstraintLabelProperty()).get());
    }

    /**
     * The cursor for dragging the dimension across its line, or for picking a constraint that
     * can not be dragged, such as an alignment.
     */
    public static Cursor dragCursor(Constraint constraint) {
        if (!(constraint instanceof DistanceConstraint distance)) {
            return Cursor.HAND;
        }
        return distance.getFrame().isAcrossVertical() ? Cursor.V_RESIZE : Cursor.H_RESIZE;
    }
}
