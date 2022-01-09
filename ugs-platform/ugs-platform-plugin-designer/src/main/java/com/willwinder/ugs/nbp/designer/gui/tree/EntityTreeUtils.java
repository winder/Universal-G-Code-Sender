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
package com.willwinder.ugs.nbp.designer.gui.tree;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.logic.Controller;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joacim Breiler
 */
public class EntityTreeUtils {
    public static List<TreePath> getSelectedPaths(Controller controller, Entity entity, List<Entity> currentTreePath) {
        ArrayList<TreePath> result = new ArrayList<>();
        if (controller.getSelectionManager().isSelected(entity)) {
            List<Entity> entities = new ArrayList<>(currentTreePath);
            entities.add(entity);
            result.add(new TreePath(entities.toArray(new Entity[0])));
        }

        if (entity instanceof EntityGroup) {
            List<Entity> newCurrentTreePath = new ArrayList<>(currentTreePath);
            newCurrentTreePath.add(entity);
            result.addAll(((EntityGroup) entity).getChildren()
                    .stream()
                    .flatMap(e -> getSelectedPaths(controller, e, newCurrentTreePath).stream())
                    .collect(Collectors.toList()));

        }

        return result;
    }
}
