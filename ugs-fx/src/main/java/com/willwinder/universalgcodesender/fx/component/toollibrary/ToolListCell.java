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
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * A row in the tool list: the slot number, the shape icon and the name.
 */
public class ToolListCell extends ListCell<ToolDefinition> {
    private final Label toolNumber = new Label();
    private final Label name = new Label();
    private final HBox row = new HBox(8);

    public ToolListCell() {
        toolNumber.setMinWidth(28);
        toolNumber.setAlignment(Pos.CENTER_RIGHT);
        toolNumber.setStyle("-fx-font-size: 0.75em; -fx-font-weight: bold; -fx-opacity: 0.7;");
        HBox.setHgrow(name, Priority.ALWAYS);
        row.setAlignment(Pos.CENTER_LEFT);
    }

    @Override
    protected void updateItem(ToolDefinition tool, boolean empty) {
        super.updateItem(tool, empty);
        if (empty || tool == null) {
            setText(null);
            setGraphic(null);
            return;
        }
        toolNumber.setText(tool.hasToolNumber() ? "T" + tool.getToolNumber() : "");
        name.setText(tool.getName() == null ? tool.getId() : tool.getName());
        EndmillShape shape = tool.getShape() == null ? EndmillShape.CUSTOM : tool.getShape();
        row.getChildren().setAll(toolNumber, ToolShapeIcons.icon(shape), name);
        setText(null);
        setGraphic(row);
    }
}
