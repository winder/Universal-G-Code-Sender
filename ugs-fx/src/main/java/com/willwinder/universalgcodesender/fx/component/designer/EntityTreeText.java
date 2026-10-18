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
package com.willwinder.universalgcodesender.fx.component.designer;

import com.willwinder.ugs.designer.Utils;
import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.EntityGroup;
import com.willwinder.ugs.designer.entities.cuttable.CutType;
import com.willwinder.ugs.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.designer.entities.cuttable.Path;
import com.willwinder.universalgcodesender.model.UnitUtils;

/**
 * The texts the {@link EntityTreeRow} derives from an entity, kept free of JavaFX so they can be
 * unit tested.
 */
final class EntityTreeText {

    private EntityTreeText() {
    }

    /**
     * The second line of a row. A group tells how many objects it holds, a cuttable with a cut
     * type gives its start and target depth in the preferred units followed by the feed rate and
     * spindle speed as they are entered in the settings panel, and anything else names its cut type.
     */
    static String detail(Entity entity, UnitUtils.Units units) {
        if (entity instanceof EntityGroup group) {
            int count = group.getChildren().size();
            return count == 1 ? "1 object" : count + " objects";
        }
        if (entity instanceof Cuttable cuttable && cuttable.getCutType() != CutType.NONE) {
            double scale = UnitUtils.scaleUnits(UnitUtils.Units.MM, units);
            return Utils.toString(cuttable.getStartDepth() * scale)
                    + " - " + Utils.toString(cuttable.getTargetDepth() * scale)
                    + " " + units.abbreviation
                    + ", " + cuttable.getFeedRate() + " mm/min"
                    + ", " + cuttable.getSpindleSpeed() + "%";
        }
        return CutType.NONE.getName();
    }

    static boolean isOpenPath(Entity entity) {
        return entity instanceof Path path && !path.isClosed();
    }
}
