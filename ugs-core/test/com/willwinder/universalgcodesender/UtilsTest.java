/*
    Copyright 2013 Will Winder

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
package com.willwinder.universalgcodesender;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author wwinder
 */
public class UtilsTest {
    /**
     * Test of timeSince method, of class Utils.
     */
    @Test
    public void testTimeSince() {
        System.out.println("timeSince");
        // This one isn't testable
    }

    /**
     * Test of millisSince method, of class Utils.
     */
    @Test
    public void testMillisSince() {
        System.out.println("millisSince");
        // This one isn't testable
    }

    /**
     * Test of formattedMillis method, of class Utils.
     */
    @Test
    public void testFormattedMillis() {
        System.out.println("formattedMillis");
        long millis;
        String expResult;
        String result;
        
        millis = 59L * 1000;
        expResult = "00:00:59";
        result = Utils.formattedMillis(millis);
        assertEquals(expResult, result);
        
        millis = 33L * 60 * 1000 + 59L * 1000;
        expResult = "00:33:59";
        result = Utils.formattedMillis(millis);
        assertEquals(expResult, result);
        
        millis = 12 * 60 * 60 * 1000 + 33L * 60 * 1000 + 59L * 1000;
        expResult = "12:33:59";
        result = Utils.formattedMillis(millis);
        assertEquals(expResult, result);
    }

    /**
     * Test of processFile method, of class Utils.
     */
    @Test
    public void testProcessFile() throws Exception {
        System.out.println("processFile");
        // I don't want to figure out how to mock a File.
        /*
        File file = null;
        int expResult = 0;
        int result = Utils.processFile(file);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
        */
    }

    @Test
    public void roundToNearestStepValueWithOnePercentIncrements() {
        assertEquals("Should round to lowest value", 0.01f, Utils.roundToNearestStepValue(0.001f, 0.01f, 2.00f, 0.01f), 0.0001f);
        assertEquals(0.01f, Utils.roundToNearestStepValue(0.014f, 0.01f, 2.00f, 0.01f), 0.0001f);
        assertEquals(0.02f, Utils.roundToNearestStepValue(0.015f, 0.01f, 2.00f, 0.01f), 0.0001f);
        assertEquals(1.00f, Utils.roundToNearestStepValue(1.001f, 0.01f, 2.00f, 0.01f), 0.0001f);
        assertEquals(2.00f, Utils.roundToNearestStepValue(2.001f, 0.01f, 2.00f, 0.01f), 0.0001f);
        assertEquals(2.00f, Utils.roundToNearestStepValue(1.995f, 0.01f, 2.00f, 0.01f), 0.0001f);
    }

    @Test
    public void roundToNearestStepValueWithOnePercentIncrementsAsIntegerValues() {
        assertEquals("Should round to lowest value", 1, Utils.roundToNearestStepValue(0.1, 1, 200, 1), 0.01);
        assertEquals(1d, Utils.roundToNearestStepValue(1.4, 1, 200, 1), 0.01);
        assertEquals(2d, Utils.roundToNearestStepValue(1.5, 1, 200, 1), 0.01);
        assertEquals(100d, Utils.roundToNearestStepValue(100.1, 1, 200, 1), 0.01);
        assertEquals(200d, Utils.roundToNearestStepValue(200.1, 1, 200, 1), 0.01);
        assertEquals(200d, Utils.roundToNearestStepValue(199.5, 1, 200, 1), 0.01);
    }

    @Test
    public void roundToNearestStepValueWith25PercentIncrements() {
        assertEquals(0.25f, Utils.roundToNearestStepValue(0.001f, 0.25f, 1.00f, 0.25f), 0.0001f);
        assertEquals(0.25f, Utils.roundToNearestStepValue(0.24f, 0.25f, 1.00f, 0.25f), 0.0001f);
        assertEquals(0.25f, Utils.roundToNearestStepValue(0.30f, 0.25f, 1.00f, 0.25f), 0.0001f);
        assertEquals(0.50f, Utils.roundToNearestStepValue(0.375f, 0.25f, 1.00f, 0.25f), 0.0001f);
        assertEquals(0.75f, Utils.roundToNearestStepValue(0.76f, 0.25f, 1.00f, 0.25f), 0.0001f);
        assertEquals(1f, Utils.roundToNearestStepValue(2f, 0.25f, 1.00f, 0.25f), 0.0001f);
    }
}
