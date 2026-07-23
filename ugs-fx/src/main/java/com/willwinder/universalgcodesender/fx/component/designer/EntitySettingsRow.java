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

import javafx.beans.property.DoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import static com.willwinder.universalgcodesender.fx.component.designer.EntitySettingsPanel.SPACING;

/**
 * A settings row tailored for the {@link EntitySettingsPanel}. All rows sharing the same
 * {@code labelWidth} property keep their labels the same width, growing to fit the longest
 * label so the controls line up in a single column. The control is kept within a fixed
 * min/max width range.
 */
public class EntitySettingsRow extends HBox {
    private static final double CONTROL_MIN_WIDTH = 80;
    private static final double CONTROL_MAX_WIDTH = 160;

    public EntitySettingsRow(DoubleProperty labelWidth, String title, Region control) {
        super(SPACING);
        setAlignment(Pos.CENTER_LEFT);
        setMinHeight(26);

        Label label = new Label(title);
        label.setAlignment(Pos.CENTER_LEFT);

        // Grow the shared width to fit this label, then keep every label at that shared width so
        // all rows line up on the longest label. The natural width can only be measured once the
        // label has a skin and its CSS font is resolved, i.e. after it has been laid out, so we
        // observe its width rather than measuring at construction time (where it would be 0).
        label.minWidthProperty().bind(labelWidth);
        label.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double naturalWidth = label.prefWidth(-1);
            if (naturalWidth > labelWidth.get()) {
                labelWidth.set(naturalWidth);
            }
        });

        control.setMinWidth(CONTROL_MIN_WIDTH);
        control.setPrefWidth(CONTROL_MAX_WIDTH);
        control.setMaxWidth(CONTROL_MAX_WIDTH);
        HBox.setHgrow(control, Priority.ALWAYS);

        getChildren().addAll(label, control);
    }
}
