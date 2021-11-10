/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * @author Joacim Breiler
 */
public class UndoAction extends AbstractAction implements UndoManagerListener {
    public static final String SMALL_ICON_PATH = "img/undo.svg";
    public static final String LARGE_ICON_PATH = "img/undo24.svg";
    private final UndoManager undoManager;

    public UndoAction() {
        undoManager = CentralLookup.getDefault().lookup(UndoManager.class);
        undoManager.addListener(this);
        setEnabled(undoManager.canUndo());

        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
        putValue("menuText", "Undo");
        putValue(NAME, "Undo");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        undoManager.undo();
    }

    @Override
    public void onChanged() {
        setEnabled(undoManager.canUndo());
    }
}
