/*
    Copyright 2016-2020 Will Winder

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

import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wwinder
 */
public class CommandProcessorTests {
    /**
     * Processor: WhitespaceProcessor
     */
    @Test
    public void testWhitespaceProcessor() {
        System.out.println("WhitespaceProcessor");
        String command;
        String expResult;
        List<String> result;
        WhitespaceProcessor wp = new WhitespaceProcessor();
        
        // Normal case.
        command = "G1 X1.234 Y0.9994 Z123";
        expResult = "G1X1.234Y0.9994Z123";
        result = wp.processCommand(command, null);
        assertEquals(1, result.size());
        assertEquals(expResult, result.get(0));
        
        // Odd case (newlines, spaces, hard tabs).
        command = "\nG1 \n    X1.234		Y0.9994   Z123     \n  ";
        expResult = "G1X1.234Y0.9994Z123";
        result = wp.processCommand(command, null);
        assertEquals(1, result.size());
        assertEquals(expResult, result.get(0));
    }

    /**
     * Processor: CommentProcessor
     */
    @Test
    public void testCommentProcessor() {
        System.out.println("removeComment");
        String command;
        String expResult;
        List<String> result;
        CommentProcessor cp = new CommentProcessor();

        command   = "some command ;comment";
        expResult = "some command";
        result = cp.processCommand(command, null);
        assertEquals(expResult, result.get(0));

        command   = "some (comment here) command ;comment";
        expResult = "some  command";
        result = cp.processCommand(command, null);
        assertEquals(expResult, result.get(0));

        command   = "some command %";
        expResult = "some command ";
        result = cp.processCommand(command, null);
        assertEquals(expResult, result.get(0));
    }
}
