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

import com.willwinder.ugs.nbp.designer.gui.DrawingEvent;
import com.willwinder.ugs.nbp.designer.gui.DrawingListener;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Joacim Breiler
 */
@ActionID(
        id = "com.willwinder.ugs.nbp.designer.actions.SelectAllAction",
        category = "Edit")
@ActionReferences({
        @ActionReference(
                path = "Shortcuts",
                name = "D-A")
})
public class SelectAllAction extends AbstractAction implements DrawingListener {

    public static final String SMALL_ICON_PATH = "img/select-all.svg";
    public static final String LARGE_ICON_PATH = "img/select-all24.svg";

    private final Controller controller;

    public SelectAllAction(Controller controller) {
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
        putValue("menuText", "Select all");
        putValue(NAME, "Select all");

        this.controller = controller;

        setEnabled(!this.controller.getDrawing().getEntities().isEmpty());
        this.controller.getDrawing().addListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SelectionManager selectionManager = controller.getSelectionManager();
        ThreadHelper.invokeLater(() -> {
            selectionManager.clearSelection();
            selectionManager.setSelection(controller.getDrawing().getEntities());
            controller.getDrawing().repaint();
        });
    }

    @Override
    public void onDrawingEvent(DrawingEvent event) {
        setEnabled(!controller.getDrawing().getEntities().isEmpty());
    }
}
