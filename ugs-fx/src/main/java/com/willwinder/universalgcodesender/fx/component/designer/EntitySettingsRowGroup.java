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
package com.willwinder.universalgcodesender.fx.component.designer;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Groups several {@link EntitySettingsRow} vertically and places a single trailing component
 * directly to the right of the rows, vertically centered across them. The row box is sized to its
 * content (rather than filling the width) so the trailing component sits next to the fields instead
 * of being pushed to the far right. Used, for example, to place an aspect-ratio lock next to the
 * width and height rows.
 */
public class EntitySettingsRowGroup extends HBox {
    private static final double TRAILING_COLUMN_WIDTH = 56;

    public EntitySettingsRowGroup(Node trailing, Region... rows) {
        super(10);
        setAlignment(Pos.CENTER_LEFT);

        VBox rowBox = new VBox(6, rows);
        getChildren().add(rowBox);

        if (trailing != null) {
            HBox trailingColumn = new HBox(trailing);
            trailingColumn.setAlignment(Pos.CENTER_LEFT);
            trailingColumn.setMinWidth(TRAILING_COLUMN_WIDTH);
            trailingColumn.setPrefWidth(TRAILING_COLUMN_WIDTH);
            trailingColumn.setMaxWidth(TRAILING_COLUMN_WIDTH);
            // Fill the full group height so a trailing component that opts in (maxHeight set) can
            // stretch across all rows; components that keep their own max height stay centered.
            trailingColumn.setMaxHeight(Double.MAX_VALUE);
            getChildren().add(trailingColumn);
        }
    }
}
