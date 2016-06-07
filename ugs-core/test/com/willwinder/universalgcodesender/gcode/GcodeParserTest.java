/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender.gcode;

import com.willwinder.universalgcodesender.types.PointSegment;
import java.util.Collection;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;

/**
 *
 * @author wwinder
 */
public class GcodeParserTest {
    
    public GcodeParserTest() {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    private void testCommand(List<PointSegment> segments, int numResults, double speed,
            double x, double y, double z,
            boolean fastTraversal, boolean zMovement, boolean arc, boolean clockwise,
            boolean isMetric,
            int num) {
        assertEquals(numResults, segments.size());

        for (PointSegment ps : segments) {
            assertEquals(ps.getSpeed(), speed, 0);
            assertEquals(x, ps.point().x, 0);
            assertEquals(y, ps.point().y, 0);
            assertEquals(z, ps.point().z, 0);
            assertEquals(fastTraversal, ps.isFastTraverse());
            assertEquals(zMovement, ps.isZMovement());
            assertEquals(arc, ps.isArc());
            if (arc) {
                assertEquals(clockwise, ps.isClockwise());
            }
            assertEquals(num, ps.getLineNumber());
            assertEquals(isMetric, ps.isMetric());
        }
    }

    /**
     * Test of addCommand method, of class GcodeParser.
     */
    @Test
    public void testAddCommand_String() throws Exception {
        System.out.println("addCommand");
        List<PointSegment> results;
        GcodeParser instance = new GcodeParser();

        // X movement with speed
        results = instance.addCommand("G20 G0X1F150");
        testCommand(results, 1, 150, 1., 0., 0., true, false, false, false, false, 0);

        results = instance.addCommand("G1Y1F250");
        testCommand(results, 1, 250, 1., 1., 0., false, false, false, false, false, 1);

        // Use same speed from before
        results = instance.addCommand("G1Z1");
        testCommand(results, 1, 250, 1., 1., 1., false, true, false, false, false, 2);

        // Use same G command from before
        results = instance.addCommand("X2Y2Z2");
        testCommand(results, 1, 250, 2., 2., 2., false, false, false, false, false, 3);

        results = instance.addCommand("X-0.5Y0Z0");
        testCommand(results, 1, 250, -0.5, 0., 0., false, false, false, false, false, 4);

        // Clockwise arc!
        results = instance.addCommand("G2 X0. Y0.5 I0.5 J0. F2.5");
        testCommand(results, 1, 2.5, 0., 0.5, 0., false, false, true, true, false, 5);

        results = instance.addCommand("X0.5 Y0. I0. J-0.5");
        testCommand(results, 1, 2.5, 0.5, 0., 0., false, false, true, true, false, 6);

        results = instance.addCommand("X0. Y-0.5 I-0.5 J0.");
        testCommand(results, 1, 2.5, 0., -0.5, 0., false, false, true, true, false, 7);
   
        results = instance.addCommand("X-0.5 Y0. I0. J0.5");
        testCommand(results, 1, 2.5, -0.5, 0., 0., false, false, true, true, false, 8);
   
        // Move up a bit.
        results = instance.addCommand("G0 Z2");
        testCommand(results, 1, 2.5, -0.5, 0., 2., true, true, false, false, false, 9);

        // Counter-clockwise arc!
        results = instance.addCommand("G3 X-0.5 Y0. I0. J0.5");
        testCommand(results, 1, 2.5, -0.5, 0., 2., false, false, true, false, false, 10);

        results = instance.addCommand("X0. Y-0.5 I-0.5 J0.");
        testCommand(results, 1, 2.5, 0., -0.5, 2., false, false, true, false, false, 11);

        results = instance.addCommand("X0.5 Y0. I0. J-0.5");
        testCommand(results, 1, 2.5, 0.5, 0., 2., false, false, true, false, false, 12);

        results = instance.addCommand("X0. Y0.5 I0.5 J0. F2.5");
        testCommand(results, 1, 2.5, 0., 0.5, 2., false, false, true, false, false, 13);
    }

    /**
     * Test of addCommand method, of class GcodeParser.
     */
    @Test
    public void testAddCommand_String_int() throws Exception {
        System.out.println("addCommand");
        GcodeParser instance = new GcodeParser();

        // More or less the same thing as the above test, so just make sure the
        // line number is applied.
        List<PointSegment> results = instance.addCommand("G20 G0X1F150", 123);
        testCommand(results, 1, 150, 1., 0., 0., true, false, false, false, false, 123);
    }

    /**
     * Test of getCurrentPoint method, of class GcodeParser.
     */
    @Test
    public void testGetCurrentState() throws Exception {
        System.out.println("getCurrentPoint");
        GcodeParser instance = new GcodeParser();

        instance.addCommand("G17 G21 G90 G94 G54 M0 M5 M9");
        GcodeState state = instance.getCurrentState();
        assertEquals(true, state.isMetric);
        assertEquals(true, state.inAbsoluteMode);
    }
}
