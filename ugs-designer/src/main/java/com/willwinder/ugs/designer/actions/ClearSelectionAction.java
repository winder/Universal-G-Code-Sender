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
package com.willwinder.ugs.designer.actions;

import com.willwinder.ugs.designer.entities.entities.selection.SelectionEvent;
import com.willwinder.ugs.designer.entities.entities.selection.SelectionListener;
import com.willwinder.ugs.designer.entities.entities.selection.SelectionManager;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.universalgcodesender.utils.SvgIconLoader;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Joacim Breiler
 */
public class ClearSelectionAction extends AbstractAction implements SelectionListener {

    public static final String SMALL_ICON_PATH = "img/clear-selection.svg";
    public static final String LARGE_ICON_PATH = "img/clear-selection24.svg";

    private final transient SelectionManager selectionManager;

    public ClearSelectionAction() {
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, SvgIconLoader.loadImageIcon(SMALL_ICON_PATH, SvgIconLoader.SIZE_SMALL).orElse(null));
        putValue(LARGE_ICON_KEY, SvgIconLoader.loadImageIcon(SMALL_ICON_PATH, SvgIconLoader.SIZE_MEDIUM).orElse(null));
        putValue("menuText", "Clear selection");
        putValue(NAME, "Clear selection");

        selectionManager = ControllerFactory.getSelectionManager();
        selectionManager.addSelectionListener(this);
        setEnabled(!selectionManager.getSelection().isEmpty());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        selectionManager.clearSelection();
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        setEnabled(!selectionManager.getSelection().isEmpty());
    }
}
