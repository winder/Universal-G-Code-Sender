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
import com.willwinder.ugs.designer.gui.Colors;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;

/**
 * How dimensions look and feel in the editor, in the same colors as in the Swing designer.
 */
public final class DimensionStyle {
    public static final Color COLOR = toFx(Colors.DIMENSION);
    public static final Color LABEL_COLOR = toFx(Colors.DIMENSION_LABEL);

    /**
     * The color of a selected or hovered dimension.
     */
    public static final Color HIGHLIGHT_COLOR = toFx(Colors.DIMENSION_HIGHLIGHT);

    private DimensionStyle() {
    }

    private static Color toFx(java.awt.Color color) {
        return Color.rgb(color.getRed(), color.getGreen(), color.getBlue());
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
