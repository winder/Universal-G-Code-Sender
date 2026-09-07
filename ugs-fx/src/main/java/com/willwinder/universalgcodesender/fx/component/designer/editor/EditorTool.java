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

import com.willwinder.universalgcodesender.fx.component.visualizer.input.PointerEvent;
import javafx.scene.Cursor;
import javafx.scene.input.KeyEvent;

import java.awt.geom.Point2D;
import java.util.Optional;

/**
 * One of the designer's tools, driven by the {@link DesignEditor} with the primary button's
 * gestures. Positions are in design coordinates, millimeters on the work plane, with the
 * original event alongside for modifiers and click counts.
 */
public interface EditorTool {

    void onPressed(PointerEvent event, Point2D design);

    void onDragged(PointerEvent event, Point2D design);

    void onReleased(PointerEvent event, Point2D design);

    default void onMoved(PointerEvent event, Point2D design) {
    }

    default Optional<Cursor> cursorAt(PointerEvent event, Point2D design) {
        return Optional.empty();
    }

    /**
     * @return true when the key was used
     */
    default boolean onKeyPressed(KeyEvent event) {
        return false;
    }

    /**
     * Called when the tool becomes the current one.
     */
    default void activate() {
    }

    /**
     * Called when another tool takes over; any gesture in progress is abandoned.
     */
    default void deactivate() {
    }

    /**
     * Called when the selection changed while the tool is current.
     */
    default void onSelectionChanged() {
    }
}
