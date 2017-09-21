/*
    Copyright 2016-2017 Will Winder

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
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.assertj.core.data.Offset;
import org.junit.Assert;

/**
 *
 * @author wwinder
 */
public class ArcExpanderTest {
    
    @Test
    public void arcExpandadState() throws Exception {
        System.out.println("arcExpandBadState");

        GcodeState state = new GcodeState();
        state.currentPoint = null;
        ArcExpander instance = new ArcExpander(true, 1);

        assertThatThrownBy(() -> instance.processCommand("G02 X5 Y0 R12", state))
                .isInstanceOf(GcodeParserException.class)
                .hasMessage(Localization.getString("parser.processor.arc.start-error"));
    }

    @Test
    public void modalsReturnedFirst() throws Exception {
        System.out.println("arcExpandWithModals");
        GcodeState state = new GcodeState();
        state.currentPoint = new Point3d(0,0,0);
        state.plane = XY;
        ArcExpander instance = new ArcExpander(true, 1);

        List<String> result = instance.processCommand("G17 G20 G02 X5 Y0 R12 S1300", state);
        assertThat(result.get(0)).isEqualTo("G17G20S1300");
        assertThat(result).size().isGreaterThan(2);
    }
    
    @Test
    public void arcExpandIgnoreNonArc() throws Exception {
        System.out.println("arcExpandIgnoreNonArc");
        GcodeState state = new GcodeState();
        state.currentPoint = new Point3d(0,0,0);
        state.plane = XY;
        ArcExpander instance = new ArcExpander(true, 1);
        boolean threwException = false;
        String command = "G17 G0 X12";
        List<String> result = instance.processCommand(command, state);
        assertThat(result.size()).isEqualTo(1);
        assertThat(result.get(0)).isEqualTo(command);
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
            ArcExpander instance = new ArcExpander(true, segmentLength);

            // Half circle clockwise, X-1 -> X1, Y0 -> Y1 -> Y0
            String command = "G2 Y0 X1 R1";
            List<String> result = instance.processCommand(command, state);
            assertThat(result.size()).isEqualTo((int)Math.ceil(Math.PI / segmentLength));
            verifyLines(new Point3d(0,0,0), result, 1., new Point3d(-1, 0, 0), new Point3d(1,1,0), state.plane);

            // Half circle counter-clockwise, X-1 -> X1, Y0 -> Y-1 -> Y0
            command = "G3 Y0 X1 R1";
            result = instance.processCommand(command, state);
            assertThat(result.size()).isEqualTo((int)Math.ceil(Math.PI / segmentLength));
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
            ArcExpander instance = new ArcExpander(true, segmentLength);

            // Half circle clockwise, Z-1 -> Z1, X0 -> X1 -> X0
            String command = "G2 Z1 X0 R1";
            List<String> result = instance.processCommand(command, state);
            assertThat(result.size()).isEqualTo((int)Math.ceil(Math.PI / segmentLength));
            verifyLines(new Point3d(0,0,0), result, 1., new Point3d(0, 0, -1), new Point3d(1,0,1), state.plane);

            // Half circle clockwise, Z-1 -> Z1, X0 -> X-1 -> X0
            command = "G3 Z1 X0 R1";
            result = instance.processCommand(command, state);
            assertThat(result.size()).isEqualTo((int)Math.ceil(Math.PI / segmentLength));
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
            ArcExpander instance = new ArcExpander(true, segmentLength);

            // Half circle clockwise, Y-1 -> Y1, X0 -> X1 -> X0
            String command = "G2 Y1 X0 R1";
            List<String> result = instance.processCommand(command, state);
            assertThat(result.size()).isEqualTo((int)Math.ceil(Math.PI / segmentLength));
            verifyLines(new Point3d(0,0,0), result, 1., new Point3d(0, -1., 0), new Point3d(0,1,1), state.plane);

            // Half circle clockwise, Y-1 -> Y1, X0 -> X-1 -> X0
            command = "G3 Y1 X0 R1";
            result = instance.processCommand(command, state);
            assertThat(result.size()).isEqualTo((int)Math.ceil(Math.PI / segmentLength));
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

            assertThat(x).as("X is in bounds").isBetween(min.x, max.x);
            assertThat(y).as("Y is in bounds").isBetween(min.y, max.y);
            assertThat(z).as("Z is in bounds").isBetween(min.z, max.z);

            double r;
            switch (p) {
                case XY: r = Math.sqrt(x*x + y*y); break;
                case YZ: r = Math.sqrt(z*z + y*y); break;
                case ZX: r = Math.sqrt(z*z + x*x); break;
                default: r = -1;
            }
            assertThat(radius).isCloseTo(r, Offset.offset(0.0001));
        } else {
            Assert.fail("This should have matched.");
        }
    }
}
