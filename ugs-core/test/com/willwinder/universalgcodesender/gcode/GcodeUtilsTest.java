/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender.gcode;

import com.willwinder.universalgcodesender.model.UnitUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wwinder
 */
public class GcodeUtilsTest {
    
    /**
     * Test of generateXYZ method, of class GcodeUtils.
     */
    @Test
    public void testGenerateXYZ() {
        System.out.println("generateXYZ");
        String result;

        result = GcodeUtils.generateXYZ("G0", UnitUtils.Units.INCH, "10", "11", 1, 1, 1);
        assertEquals("G20G0X10Y10Z10F11", result);

        result = GcodeUtils.generateXYZ("G0", UnitUtils.Units.MM, "10", "11", 1, 1, 1);
        assertEquals("G21G0X10Y10Z10F11", result);

        result = GcodeUtils.generateXYZ("G91G0", UnitUtils.Units.MM, "10", "11", 1, 0, 0);
        assertEquals("G21G91G0X10F11", result);

        result = GcodeUtils.generateXYZ("G91G0", UnitUtils.Units.MM, "10", "11", 0, -1, -1);
        assertEquals("G21G91G0Y-10Z-10F11", result);
    }
    
}
