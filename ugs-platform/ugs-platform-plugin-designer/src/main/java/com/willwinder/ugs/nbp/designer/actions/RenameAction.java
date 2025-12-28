/*
    Copyright 2025 Damian Nikodem

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
import com.willwinder.ugs.nbp.designer.gui.tree.EntitiesTree;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

import java.awt.event.ActionEvent;

@ActionID(
        id = "com.willwinder.ugs.nbp.designer.actions.RenameAction",
        category = "Edit")
@ActionRegistration(
        iconBase = RenameAction.SMALL_ICON_PATH,
        displayName = "Rename",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = "Shortcuts",
                name = "F2")
})
public class RenameAction extends AbstractDesignAction implements SelectionListener {
    public static final String SMALL_ICON_PATH = "img/text.svg";
    private static final String LARGE_ICON_PATH = "img/text.svg";
    
    private final EntitiesTree tree;
    
    public RenameAction() {
        this(null);
    }
    public RenameAction(EntitiesTree tree) {   
        super();
        this.tree = tree;
        putValue("menuText", "Rename");
        putValue(NAME, "Rename");
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));

        registerSelectionListener();
    }

    private void registerSelectionListener() {
        SelectionManager selectionManager = ControllerFactory.getController().getSelectionManager();
        selectionManager.addSelectionListener(this);
        setEnabled((tree != null) && (selectionManager.getChildren().size() == 1 ));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
          tree.renameSelectedTreeNode();
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        SelectionManager selectionManager = ControllerFactory.getSelectionManager();
        setEnabled(!selectionManager.getSelection().isEmpty());
        
    }

   
}
