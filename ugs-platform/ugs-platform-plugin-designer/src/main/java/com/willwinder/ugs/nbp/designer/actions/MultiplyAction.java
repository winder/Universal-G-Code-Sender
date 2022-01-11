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

import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.gui.MultiplyDialog;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * An action for multiplying entities into a grid.
 * This will also add the operation to the undo stack.
 *
 * @author Joacim Breiler
 */
public class MultiplyAction extends AbstractAction implements SelectionListener {
    private static final String SMALL_ICON_PATH = "img/multiply.svg";
    private static final String LARGE_ICON_PATH = "img/multiply24.svg";
    private final Controller controller;

    public MultiplyAction(Controller controller) {
        putValue("menuText", "Multiply");
        putValue(NAME, "Multiply");
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));

        this.controller = controller;
        SelectionManager selectionManager = controller.getSelectionManager();
        selectionManager.addSelectionListener(this);
        setEnabled(!selectionManager.isEmpty());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ThreadHelper.invokeLater(() -> {
            MultiplyDialog multiplyDialog = new MultiplyDialog(controller);
            multiplyDialog.setVisible(true);
        });
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        SelectionManager selectionManager = controller.getSelectionManager();
        setEnabled(!selectionManager.getSelection().isEmpty());
    }
}
