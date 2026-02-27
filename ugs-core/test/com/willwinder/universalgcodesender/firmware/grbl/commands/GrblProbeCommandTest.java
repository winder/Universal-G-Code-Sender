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
package com.willwinder.universalgcodesender.firmware.grbl.commands;

import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.UnitValue;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class GrblProbeCommandTest {
    @Test
    public void getCommandStringShouldContainPointAndFeedRate() {
        PartialPosition distance = PartialPosition
                .builder(UnitUtils.Units.MM)
                .setX(10d)
                .setY(20d)
                .setZ(30d)
                .build();

        GrblProbeCommand command = new GrblProbeCommand(distance, new UnitValue(Unit.MM_PER_MINUTE, 1000));
        assertEquals("G21 G91 G38.2 X10 Y20 Z30 F1000", command.getCommandString());
    }

    @Test
    public void getCommandStringShouldContainOneAxisAndFeedRate() {
        PartialPosition distance = PartialPosition
                .builder(UnitUtils.Units.MM)
                .setZ(-10.111)
                .build();

        GrblProbeCommand command = new GrblProbeCommand(distance, new UnitValue(Unit.INCHES_PER_MINUTE, 39.3701));
        assertEquals("G21 G91 G38.2 Z-10.111 F1000", command.getCommandString());
    }
}