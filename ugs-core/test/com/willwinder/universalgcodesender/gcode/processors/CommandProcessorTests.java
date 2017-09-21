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
