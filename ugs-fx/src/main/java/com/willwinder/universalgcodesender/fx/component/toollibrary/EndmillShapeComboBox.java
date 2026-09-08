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
package com.willwinder.universalgcodesender.fx.component.toollibrary;

import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;

/**
 * Picks an endmill shape, showing each with its icon.
 */
public class EndmillShapeComboBox extends ComboBox<EndmillShape> {

    public EndmillShapeComboBox() {
        super(FXCollections.observableArrayList(EndmillShape.values()));
        setValue(EndmillShape.UPCUT);
        setMaxWidth(Double.MAX_VALUE);
        setCellFactory(list -> new ShapeCell());
        setButtonCell(new ShapeCell());
    }

    public EndmillShape getSelectedShape() {
        return getValue() == null ? EndmillShape.UPCUT : getValue();
    }

    private static class ShapeCell extends ListCell<EndmillShape> {
        @Override
        protected void updateItem(EndmillShape shape, boolean empty) {
            super.updateItem(shape, empty);
            if (empty || shape == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(shape.getDisplayName());
                setGraphic(ToolShapeIcons.icon(shape));
            }
        }
    }
}
