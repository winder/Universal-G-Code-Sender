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

import com.willwinder.ugs.designer.entities.cuttable.Text;
import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.universalgcodesender.fx.component.visualizer.input.PointerEvent;
import javafx.scene.Cursor;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.Optional;

/**
 * Places a text entity where the user clicks and opens the inline text editor on it.
 */
public final class TextTool implements EditorTool {
    private final ToolContext context;

    public TextTool(ToolContext context) {
        this.context = context;
    }

    @Override
    public void onPressed(PointerEvent event, Point2D design) {
        Point2D snapped = context.snapper().snap(design);
        Text text = new Text(snapped.getX(), snapped.getY());
        context.controller().addEntity(text);
        context.selectTool(Tool.SELECT);
        context.selection().setSelection(List.of(text));
        context.render();
        context.textEditor().accept(text);
    }

    @Override
    public void onDragged(PointerEvent event, Point2D design) {
    }

    @Override
    public void onReleased(PointerEvent event, Point2D design) {
    }

    @Override
    public Optional<Cursor> cursorAt(PointerEvent event, Point2D design) {
        return Optional.of(Cursor.TEXT);
    }
}
