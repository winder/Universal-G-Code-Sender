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
package com.willwinder.universalgcodesender.fx.actions;

import com.willwinder.ugs.designer.actions.UndoManager;
import com.willwinder.ugs.designer.actions.UndoManagerListener;
import com.willwinder.universalgcodesender.i18n.Localization;

public class DesignRedoAction extends AbstractDesignEditAction implements UndoManagerListener {
    public static final String ICON_BASE = "icons/redo.svg";

    private final transient UndoManager undoManager;

    public DesignRedoAction() {
        super(Localization.getString("platform.designer.redo"), ICON_BASE);
        setDefaultShortcut("SHORTCUT+R");
        this.undoManager = controller.getUndoManager();
        undoManager.addListener(this);
        enabledProperty().set(undoManager.canRedo());
    }

    @Override
    protected void performAction() {
        undoManager.redo();
    }

    @Override
    public void onChanged() {
        setEnabledLater(undoManager.canRedo());
    }
}
