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
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joacim Breiler
 */
public abstract class AbstractSelectAction extends AbstractDesignAction {
    protected int getCurrentEntitiesIndex(List<Entity> entities) {
        Controller controller = ControllerFactory.getController();
        return controller.getSelectionManager()
                .getSelection()
                .stream()
                .findFirst()
                .map(entities::indexOf)
                .orElse(0);
    }

    protected List<Entity> getVisibleEntities() {
        return ControllerFactory.getController().getDrawing()
                .getEntities()
                .stream()
                .filter(Cuttable.class::isInstance)
                .map(Cuttable.class::cast)
                .filter(cuttable -> !cuttable.isHidden())
                .collect(Collectors.toList());
    }
}
