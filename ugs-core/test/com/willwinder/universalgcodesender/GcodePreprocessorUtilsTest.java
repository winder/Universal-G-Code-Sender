/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wwinder
 */
public class GcodePreprocessorUtilsTest {
    
    public GcodePreprocessorUtilsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of overrideSpeed method, of class CommUtils.
     */
    @Test
    public void testOverrideSpeed() {
        System.out.println("overrideSpeed");
        String command;
        double speed;
        String expResult;
        String result;

        
        command = "some command F100 blah blah blah";
        speed = 22.5;
        expResult = "some command F22.5 blah blah blah";
        result = GcodePreprocessorUtils.overrideSpeed(command, speed);
        assertEquals(expResult, result);
        
        command = "some command F100.0 blah blah blah";
        result = GcodePreprocessorUtils.overrideSpeed(command, speed);
        assertEquals(expResult, result);
    }

    /**
     * Test of removeComment method, of class GcodePreprocessorUtils.
     */
    @Test
    public void testRemoveComment() {
        System.out.println("removeComment");
        String command;
        String expResult;
        String result;

        command   = "some command ;comment";
        expResult = "some command";
        result = GcodePreprocessorUtils.removeComment(command);
        assertEquals(expResult, result);

        command   = "some (comment here) command ;comment";
        expResult = "some  command";
        result = GcodePreprocessorUtils.removeComment(command);
        assertEquals(expResult, result);
    }

    /**
     * Test of parseComment method, of class GrblUtils.
     */
    @Test
    public void testParseComment() {
        System.out.println("parseComment");
        String command;
        String expResult;
        String result;
        
        command   = "some command ;comment";
        expResult = "comment";
        result = GcodePreprocessorUtils.parseComment(command);
        assertEquals(expResult, result);
        
        command   = "some (comment here) command ;comment";
        expResult = "comment here";
        result = GcodePreprocessorUtils.parseComment(command);
        assertEquals(expResult, result);
    }

    /**
     * Test of truncateDecimals method, of class GcodePreprocessorUtils.
     */
    @Test
    public void testTruncateDecimals() {
        System.out.println("truncateDecimals");
        int length;
        String command;
        String result;
        String expResult;
        
        // Length tests.
        length = 0;
        command = "G1 X0.11111111111111111111";
        expResult = "G1 X0";
        result = GcodePreprocessorUtils.truncateDecimals(length, command);
        assertEquals(expResult, result);
        
        length = 8;
        expResult = "G1 X0.11111111";
        result = GcodePreprocessorUtils.truncateDecimals(length, command);
        assertEquals(expResult, result);
        
        length = 800;
        expResult = command;
        result = GcodePreprocessorUtils.truncateDecimals(length, command);
        assertEquals(expResult, result);
        
        // Rounding tests.
        length = 3;
        command = "G1 X1.5555555";
        expResult = "G1 X1.556";
        result = GcodePreprocessorUtils.truncateDecimals(length, command);
        assertEquals(expResult, result);
        
        length = 0;
        expResult = "G1 X2";
        result = GcodePreprocessorUtils.truncateDecimals(length, command);
        assertEquals(expResult, result);
        
        length = 5;
        command = "G1 X1.99999999";
        expResult = "G1 X2";
        result = GcodePreprocessorUtils.truncateDecimals(length, command);
        assertEquals(expResult, result);
        
        // Multiple hits.
        length = 3;
        command = "G1 X1.23456 Y9.87654 Z104.49443";
        expResult = "G1 X1.235 Y9.877 Z104.494";
        result = GcodePreprocessorUtils.truncateDecimals(length, command);
        assertEquals(expResult, result);
    }

    /**
     * Test of removeAllWhitespace method, of class GcodePreprocessorUtils.
     */
    @Test
    public void testRemoveAllWhitespace() {
        System.out.println("removeAllWhitespace");
        String command;
        String expResult;
        String result;
        
        // Normal case.
        command = "G1 X1.234 Y0.9994 Z123";
        expResult = "G1X1.234Y0.9994Z123";
        result = GcodePreprocessorUtils.removeAllWhitespace(command);
        assertEquals(expResult, result);
        
        // Odd case (newlines, spaces, hard tabs).
        command = "\nG1 \n    X1.234		Y0.9994   Z123     \n  ";
        expResult = "G1X1.234Y0.9994Z123";
        result = GcodePreprocessorUtils.removeAllWhitespace(command);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testParseCodes() {
        System.out.println("parseCodes");
        
        // Basic case, find one gcode.
        List<String> sl = new ArrayList<>();
        sl.add("G0");
        sl.add("X7");
        sl.add("Y5.235235");
        List<String> l = GcodePreprocessorUtils.parseCodes(sl, 'G');
        assertEquals(1, l.size());
        assertEquals("0", l.get(0));
        
        // Find two gcodes.
        sl.add("G20");
        l = GcodePreprocessorUtils.parseCodes(sl, 'G');
        assertEquals(2, l.size());
        assertEquals("0", l.get(0));
        assertEquals("20", l.get(1));
        
        // Find X, mismatched case.
        sl.add("G20");
        l = GcodePreprocessorUtils.parseCodes(sl, 'x');
        assertEquals(1, l.size());
        assertEquals("7", l.get(0));
    }
    
}
