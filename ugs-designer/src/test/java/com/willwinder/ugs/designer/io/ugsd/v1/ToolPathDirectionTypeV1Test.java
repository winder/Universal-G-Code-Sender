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
package com.willwinder.ugs.designer.io.ugsd.v1;

import com.willwinder.ugs.designer.entities.cuttable.ToolPathDirection;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ToolPathDirectionTypeV1Test {

    @Test
    public void toDirection_shouldReadLegacyHorizontalAsLeftToRight() {
        ToolPathDirection direction = ToolPathDirectionTypeV1.toDirection(ToolPathDirectionTypeV1.HORIZONTAL);

        assertEquals(ToolPathDirection.LEFT_RIGHT, direction);
    }

    @Test
    public void toDirection_shouldReadLegacyVerticalAsTopToBottom() {
        ToolPathDirection direction = ToolPathDirectionTypeV1.toDirection(ToolPathDirectionTypeV1.VERTICAL);

        assertEquals(ToolPathDirection.TOP_BOTTOM, direction);
    }

    @Test
    public void toDirection_shouldDefaultToLeftToRightWhenMissing() {
        ToolPathDirection direction = ToolPathDirectionTypeV1.toDirection(null);

        assertEquals(ToolPathDirection.LEFT_RIGHT, direction);
    }

    @Test
    public void fromDirection_shouldRoundTripEveryDirection() {
        for (ToolPathDirection direction : ToolPathDirection.values()) {
            ToolPathDirectionTypeV1 type = ToolPathDirectionTypeV1.fromDirection(direction);

            assertEquals(direction, ToolPathDirectionTypeV1.toDirection(type));
        }
    }
}
