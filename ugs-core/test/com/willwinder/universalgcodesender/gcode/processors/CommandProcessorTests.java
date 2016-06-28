package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.gcode.GcodeState;
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
    
    public CommandProcessorTests() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of processCommand method, of class CommandLengthProcessor.
     */
    @Test
    public void testCommandSplitter() throws Exception {
        System.out.println("CommandSplitter");
        CommandSplitter instance = new CommandSplitter();
        List<String> result;
        
        result = instance.processCommand("G17 G20 G54 G90", null);
        assertEquals(4, result.size());
        assertEquals("G17", result.get(0));
        assertEquals("G20", result.get(1));
        assertEquals("G54", result.get(2));
        assertEquals("G90", result.get(3));

        result = instance.processCommand("M3 S1000", null);
        assertEquals(2, result.size());
        assertEquals("M3", result.get(0));
        assertEquals("S1000", result.get(1));

        result = instance.processCommand("X0 Y10 F300", null);
        assertEquals(1, result.size());
        assertEquals("X0 Y10 F300", result.get(0));

        result = instance.processCommand("T0 M6", null);
        assertEquals(2, result.size());
        assertEquals("T0", result.get(0));
        assertEquals("M6", result.get(1));
    }
    
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
