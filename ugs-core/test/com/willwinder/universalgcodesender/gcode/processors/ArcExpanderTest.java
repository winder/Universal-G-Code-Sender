/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.gcode.util.Plane;
import static com.willwinder.universalgcodesender.gcode.util.Plane.*;
import com.willwinder.universalgcodesender.i18n.Localization;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.vecmath.Point3d;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wwinder
 */
public class ArcExpanderTest {
    
    public ArcExpanderTest() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    public void arcExpandadState() throws Exception {
        System.out.println("arcExpandBadState");

        GcodeState state = new GcodeState();
        ArcExpander instance = new ArcExpander(true, 1, 1);
        boolean threwException = false;
        try {
            List<String> result = instance.processCommand("G02 X5 Y0 R12", state);
        } catch (GcodeParserException e) {
            threwException = true;
            assertEquals(Localization.getString("parser.processor.arc.start-error"), e.getMessage());
        }
        assertTrue(threwException);
    }

    @Test
    public void arcExpandBadCommand() throws Exception {
        System.out.println("arcExpandBadCommand");
        GcodeState state = new GcodeState();
        state.currentPoint = new Point3d(0,0,0);
        state.plane = XY;
        ArcExpander instance = new ArcExpander(true, 1, 1);
        boolean threwException = false;
        try {
            List<String> result = instance.processCommand("G17 G02 X5 Y0 R12", state);
        } catch (GcodeParserException e) {
            threwException = true;
            assertEquals(Localization.getString("parser.processor.arc.multiple-commands"), e.getMessage());
        }
        assertTrue(threwException);
    }
    
    @Test
    public void arcExpandIgnoreNonArc() throws Exception {
        System.out.println("arcExpandIgnoreNonArc");
        GcodeState state = new GcodeState();
        state.currentPoint = new Point3d(0,0,0);
        state.plane = XY;
        ArcExpander instance = new ArcExpander(true, 1, 1);
        boolean threwException = false;
        String command = "G17 G0 X12";
        List<String> result = instance.processCommand(command, state);
        assertEquals(1, result.size());
        assertEquals(command, result.get(0));
    }

    @Test
    public void expandArcG17() throws Exception {
        System.out.println("expandArcG17");
        GcodeState state = new GcodeState();
        state.currentPoint = new Point3d(-1,0,0);
        state.plane = XY;

        /////////////////////////////////////////////////////////
        // Using a unit circle, so I can verify a^2 + b^2 = 1. //
        /////////////////////////////////////////////////////////
        for (double segmentLength = 0.1; segmentLength < 1; segmentLength+=0.1) {
            ArcExpander instance = new ArcExpander(true, segmentLength, 4);

            // Half circle clockwise, X-1 -> X1, Y0 -> Y1 -> Y0
            String command = "G2 Y0 X1 R1";
            List<String> result = instance.processCommand(command, state);
            assertEquals((int)Math.ceil(Math.PI / segmentLength), result.size());
            verifyLines(new Point3d(0,0,0), result, 1., new Point3d(-1, 0, 0), new Point3d(1,1,0), state.plane);

            // Half circle counter-clockwise, X-1 -> X1, Y0 -> Y-1 -> Y0
            command = "G3 Y0 X1 R1";
            result = instance.processCommand(command, state);
            assertEquals((int)Math.ceil(Math.PI / segmentLength), result.size());
            verifyLines(new Point3d(0,0,0), result, 1., new Point3d(-1, -1, 0), new Point3d(1,0,0), state.plane);
        }
    }

    @Test
    public void expandArcG18() throws Exception {
        System.out.println("expandArcG18");
        GcodeState state = new GcodeState();
        state.currentPoint = new Point3d(0,0,-1);
        state.plane = ZX;

        /////////////////////////////////////////////////////////
        // Using a unit circle, so I can verify a^2 + b^2 = 1. //
        /////////////////////////////////////////////////////////
        for (double segmentLength = 0.1; segmentLength < 1; segmentLength+=0.1) {
            ArcExpander instance = new ArcExpander(true, segmentLength, 4);

            // Half circle clockwise, Z-1 -> Z1, X0 -> X1 -> X0
            String command = "G2 Z1 X0 R1";
            List<String> result = instance.processCommand(command, state);
            assertEquals((int)Math.ceil(Math.PI / segmentLength), result.size());
            verifyLines(new Point3d(0,0,0), result, 1., new Point3d(0, 0, -1), new Point3d(1,0,1), state.plane);

            // Half circle clockwise, Z-1 -> Z1, X0 -> X-1 -> X0
            command = "G3 Z1 X0 R1";
            result = instance.processCommand(command, state);
            assertEquals((int)Math.ceil(Math.PI / segmentLength), result.size());
            verifyLines(new Point3d(0,0,0), result, 1., new Point3d(-1, 0, -1), new Point3d(0,0,1), state.plane);
        }
    }

    @Test
    public void expandArcG19() throws Exception {
        System.out.println("expandArcG19");
        GcodeState state = new GcodeState();
        state.currentPoint = new Point3d(0,-1,0);
        state.plane = YZ;

        /////////////////////////////////////////////////////////
        // Using a unit circle, so I can verify a^2 + b^2 = 1. //
        /////////////////////////////////////////////////////////
        for (double segmentLength = 0.1; segmentLength < 1; segmentLength+=0.1) {
            ArcExpander instance = new ArcExpander(true, segmentLength, 4);

            // Half circle clockwise, Y-1 -> Y1, X0 -> X1 -> X0
            String command = "G2 Y1 X0 R1";
            List<String> result = instance.processCommand(command, state);
            assertEquals((int)Math.ceil(Math.PI / segmentLength), result.size());
            verifyLines(new Point3d(0,0,0), result, 1., new Point3d(0, -1., 0), new Point3d(0,1,1), state.plane);

            // Half circle clockwise, Y-1 -> Y1, X0 -> X-1 -> X0
            command = "G3 Y1 X0 R1";
            result = instance.processCommand(command, state);
            assertEquals((int)Math.ceil(Math.PI / segmentLength), result.size());
            verifyLines(new Point3d(0,0,0), result, 1., new Point3d(0, -1, -1), new Point3d(0,1,0), state.plane);
        }
    }


    /**
     * Verify that the points around given center point have a known radius and
     * fall within known boundaries.
     */
    static void verifyLines(Point3d center, Collection<String> lines, double radius, Point3d min, Point3d max, Plane p) {
        for (String line : lines) {
            verifyLine(center, line, radius, min, max, p);
        }
    }

    static Pattern LINE_COORDS = Pattern.compile("G1"
                + "X([\\-\\+]?[0-9]+(?:\\.[0-9]+)?)"
                + "Y([\\-\\+]?[0-9]+(?:\\.[0-9]+)?)"
                + "Z([\\-\\+]?[0-9]+(?:\\.[0-9]+)?)");
                //+ "(F\\d+)?");
    static void verifyLine(Point3d center, String line, double radius, Point3d min, Point3d max, Plane p) {
        Matcher m = LINE_COORDS.matcher(line);
        if (m.find()) {
            double x = Double.parseDouble(m.group(1)) - center.x;
            double y = Double.parseDouble(m.group(2)) - center.y;
            double z = Double.parseDouble(m.group(3)) - center.z;

            assertTrue("X is in bounds", x <= max.x && x >= min.x);
            assertTrue("Y is in bounds", y <= max.y && y >= min.y);
            assertTrue("Z is in bounds", z <= max.z && z >= min.z);

            double r;
            switch (p) {
                case XY: r = Math.sqrt(x*x + y*y); break;
                case YZ: r = Math.sqrt(z*z + y*y); break;
                case ZX: r = Math.sqrt(z*z + x*x); break;
                default: r = -1;
            }
            assertEquals(radius, r, 0.0001);
        } else {
            fail("This should have matched.");
        }
    }
}
