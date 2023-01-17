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
package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;

/**
 * @author Joacim Breiler
 */
@ActionID(
        id = "com.willwinder.ugs.nbp.designer.actions.SelectNextAction",
        category = LocalizingService.CATEGORY_DESIGNER)
@ActionRegistration(
        displayName = "Select next",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = "Shortcuts",
                name = "SD-N")
})
public class SelectNextAction extends AbstractSelectAction {

    public SelectNextAction() {
        putValue("menuText", "Select next");
        putValue(NAME, "Select next");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<Entity> entities = getVisibleEntities();
        if (entities.isEmpty()) {
            return;
        }

        int currentIndex = getCurrentEntitiesIndex(entities);
        if (currentIndex < entities.size() - 1) {
            currentIndex++;
        }

        Controller controller = ControllerFactory.getController();
        controller.getSelectionManager().setSelection(Collections.singletonList(entities.get(currentIndex)));
    }
}
