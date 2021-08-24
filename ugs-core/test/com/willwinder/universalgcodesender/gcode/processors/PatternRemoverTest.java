/*
 * Copyright (C) 2021 Will Winder, AndyCXL
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.model.Position;
import static com.willwinder.universalgcodesender.model.UnitUtils.Units.MM;
import java.util.Arrays;
import java.util.List;
import static org.assertj.core.api.Assertions.*;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

/**
 *
 * @author AndyCXL
 */
public class PatternRemoverTest {
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    
    private static void removerHarness(
            String command, String regex, Position start, List<String> expected) throws Exception {
        
        PatternRemover instance = new PatternRemover(regex);

        GcodeState state = new GcodeState();
        state.currentPoint = start;
        state.inAbsoluteMode = true;

        List<String> result = instance.processCommand(command, state);
        assertEquals(expected, result);
    }

    /**
     * Commands without regex match should not be modified.
     */
    @Test
    public void testIgnoreNoRegexLines() throws Exception {
        System.out.println("ignoreNoRegexLines");

        PatternRemover instance = new PatternRemover("^[mM]6\\s*[tT]([0-9]+)$");

        String command;

        GcodeState state = new GcodeState();
        state.currentPoint = new Position(0, 0, 0, MM);
        state.inAbsoluteMode = true;

        command = "G90NOR";
        List<String> result = instance.processCommand(command, state);
        System.out.println(">>"+command+" to \""+result.get(0)+"\"");
        assertThat(result).containsExactly(command);

        command = "G1X1NOR";
        result = instance.processCommand(command, state);
        System.out.println(">>"+command+" to \""+result.get(0)+"\"");
        assertThat(result).containsExactly(command);
    }    
    
    /**
     * Commands without sed match should not be modified.
     */
    @Test
    public void testIgnoreNoSedLines() throws Exception {
        System.out.println("ignoreNoSedLines");

        PatternRemover instance = new PatternRemover("s/^[mM]6\\s*[tT]([0-9]+)$/M123");

        String command;

        GcodeState state = new GcodeState();
        state.currentPoint = new Position(0, 0, 0, MM);
        state.inAbsoluteMode = true;

        command = "G90NOS";
        List<String> result = instance.processCommand(command, state);
        System.out.println(">>"+command+" to \""+result.get(0)+"\"");
        assertThat(result).containsExactly(command);

        command = "G1X1NOS";
        result = instance.processCommand(command, state);
        System.out.println(">>"+command+" to \""+result.get(0)+"\"");
        assertThat(result).containsExactly(command);
    }    

    /**
     * Commands with regex match should be modified.
     */
    @Test
    public void testMatchRegexpLines() throws Exception {
        System.out.println("matchRegexLines");

        PatternRemover instance = new PatternRemover("^[mM]6\\s*[tT]([0-9]+)$");

        String command;

        GcodeState state = new GcodeState();
        state.currentPoint = new Position(0, 0, 0, MM);
        state.inAbsoluteMode = true;

        command = "M6 T12";
        List<String> result = instance.processCommand(command, state);
        System.out.println(">>"+command+" to \""+result.get(0)+"\"");
        assertThat(result).containsExactly("");

        command = "M6T12";
        result = instance.processCommand(command, state);
        System.out.println(">>"+command+" to \""+result.get(0)+"\"");
        assertThat(result).containsExactly("");
    }    
    
    /**
     * Commands with sed match should be modified.
     */
    @Test
    public void testMatchSedLines() throws Exception {
        System.out.println("matchSedLines");

        PatternRemover instance = new PatternRemover("s/M6T[0-9]+/M123SED");

        String command;

        GcodeState state = new GcodeState();
        state.currentPoint = new Position(0, 0, 0, MM);
        state.inAbsoluteMode = true;
        
        command = "M6T12";
        List<String> result = instance.processCommand(command, state);
        System.out.println(">>"+command+" to \""+result.get(0)+"\"");
        assertThat(result).containsExactly("M123SED");
    }

    /**
     * Commands with sed match should be modified
     */
    @Test
    public void testMatchSedStartLines() throws Exception {
        System.out.println("matchSedStartLines");

        PatternRemover instance = new PatternRemover("s/^[mM]6\\s*[tT]([0-9]+)");

        String command;

        GcodeState state = new GcodeState();
        state.currentPoint = new Position(0, 0, 0, MM);
        state.inAbsoluteMode = true;

        command = "M6T12S1000";
        List<String> result = instance.processCommand(command, state);
        System.out.println(">>"+command+" to \""+result.get(0)+"\"");
        assertThat(result).containsExactly("S1000");
    }
    
    /**
     * Commands with sed match should be modified.
     */
    @Test
    public void testMatchSedMacroLines() throws Exception {
        System.out.println("matchSedMacroLines");

        // Vanilla install contains 1 macro named "Macro #1" defined as "G91 X0 Y0;"
        PatternRemover instance = new PatternRemover("s/M6\\s*T([0-9]+)/%Macro #1%");

        String command;

        GcodeState state = new GcodeState();
        state.currentPoint = new Position(0, 0, 0, MM);
        state.inAbsoluteMode = true;

        command = "M6 T113";
        List<String> result = instance.processCommand(command, state);
        System.out.println(">>"+command+" to \""+result.get(0)+"\"");
        assertThat(result).containsExactly("G91 X0 Y0;");
    }
}