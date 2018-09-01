/*
    Copyright 2017 Will Winder

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
package com.willwinder.universalgcodesender.gcode.processors;

import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 *
 * @author wwinder
 */
public class SpindleOnDwellerTest {
    
    public SpindleOnDwellerTest() {
    }

    @Test
    public void testReplaces() throws Exception {
        SpindleOnDweller dweller = new SpindleOnDweller(2.5);
        String command;

        command = "M3";
        Assertions.assertThat(dweller.processCommand(command, null)).containsExactly(command,"G4P2.50");

        command = "m3";
        Assertions.assertThat(dweller.processCommand(command, null)).containsExactly(command,"G4P2.50");

        command = "M3 S1000";
        Assertions.assertThat(dweller.processCommand(command, null)).containsExactly(command,"G4P2.50");

        command = "m3 S1000";
        Assertions.assertThat(dweller.processCommand(command, null)).containsExactly(command,"G4P2.50");

        command = "(this is ignored) M3 S1000";
        Assertions.assertThat(dweller.processCommand(command, null)).containsExactly(command,"G4P2.50");

        command = "M4";
        Assertions.assertThat(dweller.processCommand(command, null)).containsExactly(command,"G4P2.50");

        command = "m4";
        Assertions.assertThat(dweller.processCommand(command, null)).containsExactly(command,"G4P2.50");

        command = "M4 S1000";
        Assertions.assertThat(dweller.processCommand(command, null)).containsExactly(command,"G4P2.50");

        command = "m4 S1000";
        Assertions.assertThat(dweller.processCommand(command, null)).containsExactly(command,"G4P2.50");

        command = "(this is ignored) M4 S1000";
        Assertions.assertThat(dweller.processCommand(command, null)).containsExactly(command,"G4P2.50");
    }
    
    @Test
    public void testNoOp() throws Exception {
        SpindleOnDweller dweller = new SpindleOnDweller(2.5);
        String command;
        command = "anything else";
        Assertions.assertThat(dweller.processCommand(command, null)).containsExactly(command);

        command = "M30";
        Assertions.assertThat(dweller.processCommand(command, null)).containsExactly(command);

        command = "G0 X0 Y0 (definitely not ready to start the spindle with an M3 yet)";
        Assertions.assertThat(dweller.processCommand(command, null)).containsExactly(command);
    }
}
