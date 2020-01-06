/*
    Copyright 2020 Will Winder

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
package com.willwinder.ugs.nbp.core.actions;

import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Joacim Breiler
 */
public class OutlineActionTest {

    private OutlineAction outlineAction;

    @Before
    public void setUp() {
        outlineAction = new OutlineAction();
    }

    @Test
    public void generateOutlineCommandsOfSquare() throws IOException, GcodeParserException {
        URL resource = OutlineActionTest.class.getResource("/square.nc");
        List<GcodeCommand> gcodeCommands = outlineAction.generateOutlineCommands(new File(resource.getPath()));
        assertEquals(gcodeCommands.size(), 4);
        assertEquals("G21G0X0Y0", gcodeCommands.get(0).getCommandString());
        assertEquals("G21G0X1000Y0", gcodeCommands.get(1).getCommandString());
        assertEquals("G21G0X1000Y1000", gcodeCommands.get(2).getCommandString());
        assertEquals("G21G0X0Y1000", gcodeCommands.get(3).getCommandString());
    }

    @Test
    public void generatingOutlineCommandsOfMixedUnits() throws IOException, GcodeParserException {
        URL resource = OutlineActionTest.class.getResource("/mixing_units.nc");
        List<GcodeCommand> gcodeCommands = outlineAction.generateOutlineCommands(new File(resource.getPath()));
        assertEquals(gcodeCommands.size(), 4);
        assertEquals("G21G0X0Y0", gcodeCommands.get(0).getCommandString());
        assertEquals("G21G0X25.4Y0", gcodeCommands.get(1).getCommandString());
        assertEquals("G21G0X25.4Y25.4", gcodeCommands.get(2).getCommandString());
        assertEquals("G21G0X0Y25.4", gcodeCommands.get(3).getCommandString());
    }
}
