/*
    Copyright 2023 Will Winder

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
package com.willwinder.ugs.nbp.designer.logic;

import com.willwinder.ugs.nbp.designer.actions.SimpleUndoManager;
import com.willwinder.ugs.nbp.designer.actions.UndoManager;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;

/**
 * Creates and keeps a singleton instance of the design controller.
 *
 * @author Joacim Breiler
 */
public class ControllerFactory {
    private static Controller controller;

    private ControllerFactory() {
        // Can not be instanced
    }

    public static Controller getController() {
        if (controller == null) {
            controller = new Controller(new SelectionManager(), new SimpleUndoManager());
        }

        return controller;
    }

    public static UndoManager getUndoManager() {
        return getController().getUndoManager();
    }

    public static SelectionManager getSelectionManager() {
        return getController().getSelectionManager();
    }
}
