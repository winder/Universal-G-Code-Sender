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
package com.willwinder.ugs.nbp.designer.io.gcode.writer;

import com.willwinder.ugs.nbp.designer.io.gcode.path.Segment;
import com.willwinder.ugs.nbp.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.nbp.designer.model.Settings;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

public class GrblGcodeWriterTest {

    @Test
    public void beginShouldWriteHeader() throws IOException {
        StringWriter result = new StringWriter();
        GrblGcodeWriter writer = new GrblGcodeWriter(new Settings(), result);

        writer.begin();
        String[] lines = result.toString().split("\n");
        assertTrue(lines[0].startsWith("; This file was generated"));
        assertTrue(lines[2].startsWith("G21"));
        assertTrue(lines[3].startsWith("G90"));
        assertTrue(lines[4].startsWith("G17"));
        assertTrue(lines[5].startsWith("G94"));
        assertTrue(lines[7].startsWith("; Tool"));
        assertTrue(lines[8].startsWith("; Depth per pass"));
        assertTrue(lines[9].startsWith("; Plunge speed"));
        assertTrue(lines[10].startsWith("; Safe height"));
        assertTrue(lines[11].startsWith("; Tool step over"));
        assertTrue(lines[12].startsWith("; Spindle start command"));
    }

    @Test
    public void endShouldStopSpindle() throws IOException {
        StringWriter result = new StringWriter();
        GrblGcodeWriter writer = new GrblGcodeWriter(new Settings(), result);

        writer.end();
        String[] lines = result.toString().split("\n");
        assertEquals("; Turning off spindle", lines[1]);
        assertEquals("M5", lines[2]);
    }

    @Test
    public void moveCommandWithSpindleAndFeedSpeed() throws IOException {
        StringWriter result = new StringWriter();
        GrblGcodeWriter writer = new GrblGcodeWriter(new Settings(), result);

        writer.writeSegment(new Segment(SegmentType.MOVE,
                PartialPosition.builder(UnitUtils.Units.MM).build(),
                null,
                10_000, 1_000));

        String[] lines = result.toString().split("\n");
        assertEquals(2, lines.length);
        assertEquals("M3 S10000", lines[0]);
        assertEquals("G0", lines[1]);
    }

    @Test
    public void moveCommandWithMultipleSpindleAndFeedSpeedShouldNotCreateDuplicateSpindleStart() throws IOException {
        StringWriter result = new StringWriter();
        GrblGcodeWriter writer = new GrblGcodeWriter(new Settings(), result);

        writer.writeSegment(new Segment(SegmentType.MOVE,
                PartialPosition.builder(UnitUtils.Units.MM).build(),
                null,
                10_000, 1_000));

        writer.writeSegment(new Segment(SegmentType.MOVE,
                PartialPosition.builder(UnitUtils.Units.MM).build(),
                null,
                10_000, 1_000));

        String[] lines = result.toString().split("\n");
        assertEquals(3, lines.length);
        assertEquals("M3 S10000", lines[0]);
        assertEquals("G0", lines[1]);
        assertEquals("G0", lines[2]);
    }

    @Test
    public void lineCommandWithFeedAndSpeed() throws IOException {
        StringWriter result = new StringWriter();
        GrblGcodeWriter writer = new GrblGcodeWriter(new Settings(), result);

        writer.writeSegment(new Segment(SegmentType.MOVE,
                PartialPosition.builder(UnitUtils.Units.MM).setX(0d).setY(0d).build(),
                null,
                10_000, 1_000));

        writer.writeSegment(new Segment(SegmentType.LINE,
                PartialPosition.builder(UnitUtils.Units.MM).setX(10d).setY(10d).build(),
                null,
                10_000, 1_000));

        String[] lines = result.toString().split("\n");
        assertEquals(3, lines.length);
        assertEquals("M3 S10000", lines[0]);
        assertEquals("G0 X0Y0", lines[1]);
        assertEquals("G1 F1000 X10Y10", lines[2]);
    }

    @Test
    public void lineCommandWithMultipleFeedAndSpeed() throws IOException {
        StringWriter result = new StringWriter();
        GrblGcodeWriter writer = new GrblGcodeWriter(new Settings(), result);

        writer.writeSegment(new Segment(SegmentType.MOVE,
                PartialPosition.builder(UnitUtils.Units.MM).setX(0d).setY(0d).build(),
                null,
                10_000, 1_000));

        writer.writeSegment(new Segment(SegmentType.LINE,
                PartialPosition.builder(UnitUtils.Units.MM).setX(10d).setY(10d).build(),
                null,
                10_000, 1_000));

        writer.writeSegment(new Segment(SegmentType.LINE,
                PartialPosition.builder(UnitUtils.Units.MM).setX(15d).setY(15d).build(),
                null,
                10_000, 1_000));

        writer.writeSegment(new Segment(SegmentType.LINE,
                PartialPosition.builder(UnitUtils.Units.MM).setX(20d).setY(20d).build(),
                null,
                11_000, 1_200));

        String[] lines = result.toString().split("\n");
        assertEquals(5, lines.length);
        assertEquals("M3 S10000", lines[0]);
        assertEquals("G0 X0Y0", lines[1]);
        assertEquals("G1 F1000 X10Y10", lines[2]);
        assertEquals("G1 X15Y15", lines[3]);
        assertEquals("G1 F1200 S11000 X20Y20", lines[4]);
    }

}