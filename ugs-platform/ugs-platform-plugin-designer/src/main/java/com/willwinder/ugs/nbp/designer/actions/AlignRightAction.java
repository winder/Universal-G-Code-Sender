/*
    Copyright 2024 Will Winder

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

import com.willwinder.ugs.nbp.designer.entities.Anchor;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * An action for aligning objects
 *
 * @author Joacim Breiler
 */
@ActionID(
        category = LocalizingService.CATEGORY_DESIGNER,
        id = "AlignRightAction")
@ActionRegistration(
        iconBase = AlignRightAction.SMALL_ICON_PATH,
        displayName = "Align right",
        lazy = false)
public class AlignRightAction extends AbstractDesignAction implements SelectionListener {
    public static final String SMALL_ICON_PATH = "img/alignright.svg";
    public static final String LARGE_ICON_PATH = "img/alignright24.svg";
    private final transient Controller controller;

    public AlignRightAction() {
        putValue("menuText", "Align right");
        putValue(NAME, "Align right");
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SHORT_DESCRIPTION, "Align the objects to the right of the first selected entity");
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));

        this.controller = ControllerFactory.getController();
        SelectionManager selectionManager = controller.getSelectionManager();
        selectionManager.addSelectionListener(this);
        onSelectionEvent(new SelectionEvent());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<Entity> selection = controller.getSelectionManager().getSelection();
        Entity entity = selection.get(0);
        Point2D destination = entity.getPosition(Anchor.RIGHT_CENTER);

        UndoActionList actionList = new UndoActionList();
        for (int i = 1; i < selection.size(); i++) {
            Point2D position = selection.get(i).getPosition(Anchor.RIGHT_CENTER);
            actionList.add(new MoveAction(List.of(selection.get(i)), new Point2D.Double(destination.getX() - position.getX(), 0)));
        }

        actionList.redo();
        controller.getUndoManager().addAction(actionList);
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        SelectionManager selectionManager = controller.getSelectionManager();
        setEnabled(selectionManager.getSelection().size() > 1);
    }
}
