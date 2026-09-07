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
package com.willwinder.universalgcodesender.fx.component.visualizer.input;

import javafx.scene.Cursor;
import javafx.scene.input.KeyEvent;

import java.util.Optional;

/**
 * Receives the visualizer's input from an {@link InputRouter}. Handlers are asked in order and
 * the first one that consumes an event ends the dispatch.
 *
 * <p>A press that is consumed makes the handler the owner of the gesture: every drag and the
 * release that follow go to it alone, whatever is under the cursor by then.
 */
public interface InputHandler {

    /**
     * @return true to own the gesture that this press starts
     */
    default boolean onPressed(PointerEvent event) {
        return false;
    }

    /**
     * Only called on the owner of the current gesture.
     *
     * @param pressed the event that started the gesture
     */
    default void onDragged(PointerEvent event, PointerEvent pressed) {
    }

    /**
     * Only called on the owner of the current gesture.
     *
     * @param pressed the event that started the gesture
     */
    default void onReleased(PointerEvent event, PointerEvent pressed) {
    }

    /**
     * The cursor moved without a button held.
     *
     * @return true when consumed
     */
    default boolean onMoved(PointerEvent event) {
        return false;
    }

    /**
     * @return true when consumed
     */
    default boolean onScroll(ScrollInput scroll) {
        return false;
    }

    /**
     * @return true when consumed
     */
    default boolean onKeyPressed(KeyEvent event) {
        return false;
    }

    /**
     * @return true when consumed
     */
    default boolean onKeyReleased(KeyEvent event) {
        return false;
    }

    /**
     * A context menu was requested at the position, a right click without a drag.
     *
     * @return true when consumed
     */
    default boolean onContextMenu(PointerEvent event) {
        return false;
    }

    /**
     * The cursor to show over the given position, or empty to leave the choice to the next
     * handler.
     */
    default Optional<Cursor> cursorAt(PointerEvent event) {
        return Optional.empty();
    }
}
