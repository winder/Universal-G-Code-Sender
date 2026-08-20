/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.ugs.designer.io.gcode;

import com.willwinder.ugs.designer.entities.cuttable.CutType;
import com.willwinder.ugs.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.model.PenMode;
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.ugs.designer.model.Size;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

public class SimpleGcodeRouterPlotterTest {

    @Test
    public void toGcode_shouldLowerAndLiftThePenWithTheZAxis() throws IOException {
        Settings settings = new Settings();
        settings.setPenMode(PenMode.Z_AXIS);
        settings.setSafeHeight(4);
        settings.setPenDownDepth(0.4);
        settings.setPlungeSpeed(250);

        String gcode = routeRectangle(settings);

        assertTrue(gcode, gcode.lines().anyMatch(line -> line.equals("G0 Z4")));
        assertTrue(gcode, gcode.lines().anyMatch(line -> line.equals("G1 F250 Z-0.4")));
    }

    @Test
    public void toGcode_shouldLowerAndLiftThePenWithTheSpindleSpeed() throws IOException {
        Settings settings = new Settings();
        settings.setPenMode(PenMode.SPINDLE_SPEED);
        settings.setPenDownSpindleSpeed(800);
        settings.setPenUpSpindleSpeed(100);

        String gcode = routeRectangle(settings);

        assertTrue(gcode, gcode.lines().anyMatch(line -> line.equals("M3 S800")));
        assertTrue(gcode, gcode.lines().anyMatch(line -> line.equals("M3 S100")));
        assertFalse(gcode, gcode.lines().anyMatch(line -> line.contains("Z")));
    }

    @Test
    public void toGcode_shouldLowerAndLiftThePenWithCustomCommands() throws IOException {
        Settings settings = new Settings();
        settings.setPenMode(PenMode.CUSTOM_COMMAND);
        settings.setPenDownCommand("M280 P0 S30");
        settings.setPenUpCommand("M280 P0 S90");

        String gcode = routeRectangle(settings);

        assertTrue(gcode, gcode.lines().anyMatch(line -> line.equals("M280 P0 S30")));
        assertTrue(gcode, gcode.lines().anyMatch(line -> line.equals("M280 P0 S90")));
    }

    @Test
    public void toGcode_shouldLiftThePenAfterTheLastLine() throws IOException {
        Settings settings = new Settings();
        settings.setPenMode(PenMode.CUSTOM_COMMAND);
        settings.setPenUpCommand("PENUP");

        String gcode = routeRectangle(settings);

        List<String> motionLines = gcode.lines()
                .filter(line -> line.equals("PENUP") || line.startsWith("G1 ") || line.startsWith("G0 "))
                .toList();
        assertEquals("PENUP", motionLines.get(motionLines.size() - 1));
    }

    @Test
    public void toGcode_shouldFillAShapeWithLinesAtTheConfiguredSpacing() throws IOException {
        Settings settings = new Settings();
        settings.setPenMode(PenMode.CUSTOM_COMMAND);
        settings.setPenDownCommand("PENDOWN");
        settings.setPenUpCommand("PENUP");
        settings.setPenWidth(0);

        String gcode = routeRectangle(settings, CutType.PLOTTER_FILL, 0, 2);

        // A ten millimeter tall shape hatched every two millimeters is six lines
        assertEquals(6, gcode.lines().filter(line -> line.startsWith("G1 ")).count());
        assertEquals(6, gcode.lines().filter(line -> line.equals("PENDOWN")).count());
        assertEquals(7, gcode.lines().filter(line -> line.equals("PENUP")).count());
        assertFalse(gcode, gcode.lines().anyMatch(line -> line.contains("Z")));
    }

    @Test
    public void toGcode_shouldHoldTheFillInsideTheShapeByHalfThePenWidth() throws IOException {
        Settings settings = new Settings();
        settings.setPenMode(PenMode.CUSTOM_COMMAND);
        settings.setPenWidth(2);

        String gcode = routeRectangle(settings, CutType.PLOTTER_FILL, 0, 2);

        // A two millimeter pen leaves a shape running from 1 to 9, which is one line less
        assertEquals(5, gcode.lines().filter(line -> line.startsWith("G1 ")).count());
        assertTrue(gcode, gcode.lines().anyMatch(line -> line.contains("Y1")));
        assertFalse(gcode, gcode.lines().anyMatch(line -> line.contains("Y10")));
    }

    private static String routeRectangle(Settings settings) throws IOException {
        return routeRectangle(settings, CutType.PLOTTER_ON_PATH, 0, Cuttable.DEFAULT_LINE_SPACING);
    }

    private static String routeRectangle(Settings settings, CutType cutType, double angle, double lineSpacing) throws IOException {
        Rectangle rectangle = new Rectangle(0, 0);
        rectangle.setSize(new Size(10, 10));
        rectangle.setCutType(cutType);
        rectangle.setToolPathAngle(angle);
        rectangle.setLineSpacing(lineSpacing);
        rectangle.setFeedRate(2000);

        StringWriter writer = new StringWriter();
        new SimpleGcodeRouter(settings).toGcode(List.of((Cuttable) rectangle), writer);
        return writer.toString();
    }
}
