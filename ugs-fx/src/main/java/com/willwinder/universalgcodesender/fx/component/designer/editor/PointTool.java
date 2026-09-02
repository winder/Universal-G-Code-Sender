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

import com.willwinder.ugs.designer.entities.cuttable.Point;
import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.universalgcodesender.fx.component.visualizer.input.PointerEvent;
import javafx.scene.Cursor;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Optional;

/**
 * Places a drill point where the user clicks, snapped to the grid.
 */
public final class PointTool implements EditorTool {
    private final ToolContext context;

    public PointTool(ToolContext context) {
        this.context = context;
    }

    @Override
    public void onPressed(PointerEvent event, Point2D design) {
        Point2D snapped = context.snapper().snap(design);
        Point point = new Point(snapped.getX(), snapped.getY());
        context.controller().addEntity(point);
        context.selectTool(Tool.SELECT);
        context.selection().setSelection(List.of(point));
        context.render();
    }

    @Override
    public void onDragged(PointerEvent event, Point2D design) {
    }

    @Override
    public void onReleased(PointerEvent event, Point2D design) {
    }

    @Override
    public Optional<Cursor> cursorAt(PointerEvent event, Point2D design) {
        return Optional.of(Cursor.CROSSHAIR);
    }
}
