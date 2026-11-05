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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BooleanSupplier;

/**
 * Lets an editor take over the delete action, for instance to delete the selected node of a
 * path rather than the whole path. The delete action asks the handlers first and only deletes
 * the selected entities when none of them handled it.
 */
public final class DeleteHandlers {
    private static final List<BooleanSupplier> HANDLERS = new CopyOnWriteArrayList<>();

    private DeleteHandlers() {
    }

    /**
     * @param handler deletes what it has selected and returns true, or returns false to leave
     *                the delete to the next handler or the default
     */
    public static void add(BooleanSupplier handler) {
        HANDLERS.add(handler);
    }

    public static void remove(BooleanSupplier handler) {
        HANDLERS.remove(handler);
    }

    /**
     * @return true when a handler performed the delete
     */
    public static boolean handle() {
        for (BooleanSupplier handler : HANDLERS) {
            if (handler.getAsBoolean()) {
                return true;
            }
        }
        return false;
    }
}
