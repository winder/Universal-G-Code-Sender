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

import com.willwinder.ugs.designer.entities.cuttable.Path;
import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.universalgcodesender.fx.component.visualizer.input.PointerEvent;
import javafx.scene.Cursor;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Optional;

/**
 * Drags out a straight line, creating a two point path and switching to the vertex tool so
 * more points can be added right away, as the legacy designer does.
 */
public final class LineTool implements EditorTool {
    private static final double MIN_LENGTH = 0.1;

    private final ToolContext context;
    private Point2D start;

    public LineTool(ToolContext context) {
        this.context = context;
    }

    @Override
    public void onPressed(PointerEvent event, Point2D design) {
        start = context.snapper().snap(design);
    }

    @Override
    public void onDragged(PointerEvent event, Point2D design) {
        if (start == null) {
            return;
        }
        Point2D end = context.snapper().snap(design);
        context.state().setPreview(new Line2D.Double(start, end));
        context.render();
    }

    @Override
    public void onReleased(PointerEvent event, Point2D design) {
        if (start == null) {
            return;
        }
        Point2D end = context.snapper().snap(design);
        Point2D from = start;
        start = null;
        context.state().setPreview(null);
        if (from.distance(end) >= MIN_LENGTH) {
            Path path = new Path();
            path.moveTo(from.getX(), from.getY());
            path.lineTo(end.getX(), end.getY());
            context.controller().addEntity(path);
            context.selectTool(Tool.VERTEX);
            context.selection().setSelection(List.of(path));
        }
        context.render();
    }

    @Override
    public Optional<Cursor> cursorAt(PointerEvent event, Point2D design) {
        return Optional.of(Cursor.CROSSHAIR);
    }

    @Override
    public void deactivate() {
        start = null;
        context.state().setPreview(null);
    }
}
