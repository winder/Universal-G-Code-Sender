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

import com.willwinder.ugs.designer.entities.cuttable.CutType;
import com.willwinder.ugs.designer.entities.cuttable.Direction;
import com.willwinder.ugs.designer.entities.cuttable.PlungeType;
import com.willwinder.ugs.designer.entities.cuttable.ToolPathDirection;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class EntitySettingIconsTest {

    @Test
    public void cutTypeResource_shouldPointToAnExistingIconForEveryCutType() {
        for (CutType cutType : CutType.values()) {
            String resource = EntitySettingIcons.cutTypeResource(cutType);

            assertResourceExists(resource, cutType);
        }
    }

    @Test
    public void plungeTypeResource_shouldPointToAnExistingIconForEveryPlungeType() {
        for (PlungeType plungeType : PlungeType.values()) {
            String resource = EntitySettingIcons.plungeTypeResource(plungeType);

            assertResourceExists(resource, plungeType);
        }
    }

    @Test
    public void toolPathDirectionResource_shouldPointToAnExistingIconForEveryDirection() {
        for (ToolPathDirection direction : ToolPathDirection.values()) {
            String resource = EntitySettingIcons.toolPathDirectionResource(direction);

            assertResourceExists(resource, direction);
        }
    }

    @Test
    public void directionResource_shouldPointToAnExistingIconForEveryDirection() {
        for (Direction direction : Direction.values()) {
            String resource = EntitySettingIcons.directionResource(direction);

            assertResourceExists(resource, direction);
        }
    }

    private static void assertResourceExists(String resource, Enum<?> value) {
        Assertions.assertThat(EntitySettingIcons.class.getResource("/" + resource))
                .as("icon for %s", value)
                .isNotNull();
    }
}
