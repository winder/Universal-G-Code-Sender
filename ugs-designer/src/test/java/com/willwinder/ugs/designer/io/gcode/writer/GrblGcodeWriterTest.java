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
package com.willwinder.ugs.designer.io.gcode.writer;

import com.willwinder.ugs.designer.io.gcode.path.Segment;
import com.willwinder.ugs.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.designer.model.CoolantMode;
import com.willwinder.ugs.designer.model.PenMode;
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.awt.geom.Point2D;
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
        assertTrue(lines[7].startsWith("; Depth per pass"));
        assertTrue(lines[8].startsWith("; Plunge speed"));
        assertTrue(lines[9].startsWith("; Safe height"));
        assertTrue(lines[10].startsWith("; Tool step over"));
        assertTrue(lines[11].startsWith("; Spindle start command"));
        assertTrue(lines[12].startsWith("; Max spindle speed"));
        assertTrue(lines[13].startsWith("; Tool"));
    }

    @Test
    public void beginShouldWriteToolChangeForNumberedTool() throws IOException {
        StringWriter result = new StringWriter();
        Settings settings = new Settings();
        settings.setUseToolChanges(true);
        settings.setToolNumber(2);
        GrblGcodeWriter writer = new GrblGcodeWriter(settings, result);

        writer.begin();

        assertTrue(result.toString().contains("M6 T2 ; Tool: "));
    }

    @Test
    public void beginShouldNotWriteToolChangeWhenToolHasNoNumber() throws IOException {
        StringWriter result = new StringWriter();
        Settings settings = new Settings();
        settings.setUseToolChanges(true);
        GrblGcodeWriter writer = new GrblGcodeWriter(settings, result);

        writer.begin();

        assertFalse(result.toString().contains("M6"));
    }

    @Test
    public void beginShouldNotWriteToolChangeWhenTheSettingIsOff() throws IOException {
        StringWriter result = new StringWriter();
        Settings settings = new Settings();
        settings.setToolNumber(2);
        GrblGcodeWriter writer = new GrblGcodeWriter(settings, result);

        writer.begin();

        assertFalse(result.toString().contains("M6"));
    }

    @Test
    public void beginShouldNotWriteToolChangeByDefault() throws IOException {
        StringWriter result = new StringWriter();
        GrblGcodeWriter writer = new GrblGcodeWriter(new Settings(), result);

        writer.begin();

        assertFalse(result.toString().contains("M6"));
    }

    @Test
    public void beginShouldStartFloodCoolant() throws IOException {
        StringWriter result = new StringWriter();
        Settings settings = new Settings();
        settings.setCoolantMode(CoolantMode.FLOOD);
        GrblGcodeWriter writer = new GrblGcodeWriter(settings, result);

        writer.begin();

        assertTrue(result.toString().contains("M8 ; Coolant: "));
    }

    @Test
    public void beginShouldStartMistCoolant() throws IOException {
        StringWriter result = new StringWriter();
        Settings settings = new Settings();
        settings.setCoolantMode(CoolantMode.MIST);
        GrblGcodeWriter writer = new GrblGcodeWriter(settings, result);

        writer.begin();

        assertTrue(result.toString().contains("M7 ; Coolant: "));
    }

    @Test
    public void beginShouldStartCoolantAfterTheToolChange() throws IOException {
        StringWriter result = new StringWriter();
        Settings settings = new Settings();
        settings.setCoolantMode(CoolantMode.FLOOD);
        settings.setUseToolChanges(true);
        settings.setToolNumber(2);
        GrblGcodeWriter writer = new GrblGcodeWriter(settings, result);

        writer.begin();

        String written = result.toString();
        assertTrue("The coolant must be opened once the tool has been selected",
                written.indexOf("M6 T2") < written.indexOf("M8"));
    }

    @Test
    public void endShouldStopCoolantAfterTheSpindle() throws IOException {
        StringWriter result = new StringWriter();
        Settings settings = new Settings();
        settings.setCoolantMode(CoolantMode.FLOOD);
        GrblGcodeWriter writer = new GrblGcodeWriter(settings, result);

        writer.end();

        String written = result.toString();
        assertTrue(written.contains("M9"));
        assertTrue("The coolant must be closed after the spindle has stopped",
                written.indexOf("M5") < written.indexOf("M9"));
    }

    @Test
    public void shouldNotWriteAnyCoolantCodesByDefault() throws IOException {
        StringWriter result = new StringWriter();
        GrblGcodeWriter writer = new GrblGcodeWriter(new Settings(), result);

        writer.begin();
        writer.end();

        String written = result.toString();
        assertFalse(written.contains("M7"));
        assertFalse(written.contains("M8"));
        assertFalse(written.contains("M9"));
    }

    @Test
    public void beginShouldCommentTheToolDiameterWhenNoToolChangeIsWritten() throws IOException {
        StringWriter result = new StringWriter();
        GrblGcodeWriter writer = new GrblGcodeWriter(new Settings(), result);

        writer.begin();

        assertTrue(result.toString().contains("; Tool: "));
    }

    @Test
    public void beginShouldCommentTheToolAlongsideTheToolChange() throws IOException {
        StringWriter result = new StringWriter();
        Settings settings = new Settings();
        settings.setUseToolChanges(true);
        settings.setToolNumber(2);
        GrblGcodeWriter writer = new GrblGcodeWriter(settings, result);

        writer.begin();

        assertTrue(result.toString().contains("M6 T2 ; Tool:"));
    }

    @Test
    public void beginShouldDescribeTheToolByShapeWhenNotBoundToTheLibrary() throws IOException {
        StringWriter result = new StringWriter();
        Settings settings = new Settings();
        settings.setToolDiameter(6);
        settings.setToolShape(EndmillShape.BALL);
        GrblGcodeWriter writer = new GrblGcodeWriter(settings, result);

        writer.begin();

        assertTrue(result.toString().contains("; Tool: 6mm Ball"));
    }

    @Test
    public void beginShouldDescribeTheToolByItsLibraryName() throws IOException {
        StringWriter result = new StringWriter();
        Settings settings = new Settings();
        settings.setToolDiameter(6.35);
        settings.setCurrentToolSnapshot(namedTool("1/4\" Upcut"));
        GrblGcodeWriter writer = new GrblGcodeWriter(settings, result);

        writer.begin();

        assertTrue(result.toString().contains("; Tool: 1/4\" Upcut"));
    }

    @Test
    public void beginShouldIncludeTheAngleForAnUnnamedVBit() throws IOException {
        StringWriter result = new StringWriter();
        Settings settings = new Settings();
        settings.setToolDiameter(6);
        settings.setToolShape(EndmillShape.V_BIT);
        settings.setVBitAngle(60);
        GrblGcodeWriter writer = new GrblGcodeWriter(settings, result);

        writer.begin();

        assertTrue(result.toString().contains("; Tool: 6mm V-bit 60°"));
    }

    private static ToolDefinition namedTool(String name) {
        ToolDefinition tool = new ToolDefinition();
        tool.setName(name);
        return tool;
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
        assertEquals(1, lines.length);
        assertEquals("M3 S10000", lines[0]);
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
        assertEquals(1, lines.length);
        assertEquals("M3 S10000", lines[0]);
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
        assertEquals(6, lines.length);
        assertEquals("M3 S10000", lines[0]);
        assertEquals("G0 X0Y0", lines[1]);
        assertEquals("G1 F1000 X10Y10", lines[2]);
        assertEquals("G1 X15Y15", lines[3]);
        assertEquals("M3 S11000", lines[4]);
        assertEquals("G1 F1200 X20Y20", lines[5]);
    }

    @Test
    public void seamShouldWriteLabelAndFeedWithoutMotion() throws IOException {
        StringWriter result = new StringWriter();
        GrblGcodeWriter writer = new GrblGcodeWriter(new Settings(), result);

        writer.writeSegment(new Segment(
                SegmentType.SEAM,
                null,
                "my seam",
                null,
                1234
        ));

        String[] lines = result.toString().split("\n");
        assertEquals(2, lines.length);
        assertEquals(";my seam", lines[0]);
        assertEquals("F1234 ", lines[1]);
    }

    @Test
    public void seamWithoutFeedShouldOnlyWriteLabel() throws IOException {
        StringWriter result = new StringWriter();
        GrblGcodeWriter writer = new GrblGcodeWriter(new Settings(), result);

        writer.writeSegment(new Segment(
                SegmentType.SEAM,
                null,
                "only label"
        ));

        String[] lines = result.toString().split("\n");
        assertEquals(1, lines.length);
        assertEquals(";only label", lines[0]);
    }

    @Test
    public void pointCommandShouldUsePlungeSpeedAndWriteCoordinates() throws IOException {
        StringWriter result = new StringWriter();
        Settings settings = new Settings();
        settings.setPlungeSpeed(777);
        GrblGcodeWriter writer = new GrblGcodeWriter(settings, result);

        writer.writeSegment(new Segment(
                SegmentType.POINT,
                PartialPosition.builder(UnitUtils.Units.MM).setX(1d).setY(2d).setZ(3d).build()
        ));

        String[] lines = result.toString().split("\n");
        assertEquals(1, lines.length);
        assertEquals("G1 F777 X1Y2Z3", lines[0]);
    }

    @Test
    public void moveShouldWriteOnlyChangedCoordinates() throws IOException {
        StringWriter result = new StringWriter();
        GrblGcodeWriter writer = new GrblGcodeWriter(new Settings(), result);

        writer.writeSegment(new Segment(
                SegmentType.MOVE,
                PartialPosition.builder(UnitUtils.Units.MM).setX(10d).setY(20d).setZ(30d).build()
        ));

        writer.writeSegment(new Segment(
                SegmentType.MOVE,
                PartialPosition.builder(UnitUtils.Units.MM).setX(10d).setY(25d).setZ(30d).build()
        ));

        String[] lines = result.toString().split("\n");
        assertEquals(2, lines.length);
        assertEquals("G0 X10Y20Z30", lines[0]);
        assertEquals("G0 Y25", lines[1]);
    }

    @Test
    public void lineShouldReuseFeedRateForRepeatedSegments() throws IOException {
        StringWriter result = new StringWriter();
        GrblGcodeWriter writer = new GrblGcodeWriter(new Settings(), result);

        writer.writeSegment(new Segment(
                SegmentType.LINE,
                PartialPosition.builder(UnitUtils.Units.MM).setX(0d).setY(0d).build(),
                null,
                null,
                1200
        ));

        writer.writeSegment(new Segment(
                SegmentType.LINE,
                PartialPosition.builder(UnitUtils.Units.MM).setX(1d).setY(1d).build(),
                null,
                null,
                1200
        ));

        String[] lines = result.toString().split("\n");
        assertEquals(2, lines.length);
        assertEquals("G1 F1200 X0Y0", lines[0]);
        assertEquals("G1 X1Y1", lines[1]);
    }

    @Test
    public void writeSegment_shouldWriteArcWithIncrementalOffsetsToCenter() throws IOException {
        StringWriter result = new StringWriter();
        GrblGcodeWriter writer = new GrblGcodeWriter(new Settings(), result);
        writer.writeSegment(new Segment(SegmentType.MOVE, position(10d, 0d)));

        writer.writeSegment(Segment.arc(SegmentType.CWARC, position(0d, 10d), new Point2D.Double(0, 0), 1_000));

        String[] lines = result.toString().split("\n");
        assertEquals(2, lines.length);
        assertEquals("G0 X10Y0", lines[0]);
        assertEquals("G2 F1000 X0Y10I-10J0", lines[1]);
    }

    @Test
    public void writeSegment_shouldWriteArcOffsetsRelativeToEndOfPreviousArc() throws IOException {
        StringWriter result = new StringWriter();
        GrblGcodeWriter writer = new GrblGcodeWriter(new Settings(), result);
        writer.writeSegment(new Segment(SegmentType.MOVE, position(10d, 0d)));

        writer.writeSegment(Segment.arc(SegmentType.CWARC, position(0d, 10d), new Point2D.Double(0, 0), 900));
        writer.writeSegment(Segment.arc(SegmentType.CCWARC, position(-10d, 0d), new Point2D.Double(0, 0), 900));

        String[] lines = result.toString().split("\n");
        assertEquals(3, lines.length);
        assertEquals("G2 F900 X0Y10I-10J0", lines[1]);
        assertEquals("G3 X-10Y0I0J-10", lines[2]);
    }

    @Test
    public void writeSegment_shouldWriteArcOffsetsUsingCoordinatesFromEarlierSegments() throws IOException {
        StringWriter result = new StringWriter();
        GrblGcodeWriter writer = new GrblGcodeWriter(new Settings(), result);
        writer.writeSegment(new Segment(SegmentType.MOVE, position(10d, 0d)));
        writer.writeSegment(new Segment(SegmentType.MOVE, PartialPosition.from(Axis.Z, -1d, UnitUtils.Units.MM)));

        writer.writeSegment(Segment.arc(SegmentType.CWARC, position(0d, 10d), new Point2D.Double(0, 0), 1_000));

        String[] lines = result.toString().split("\n");
        assertEquals(3, lines.length);
        assertEquals("G0 Z-1", lines[1]);
        assertEquals("G2 F1000 X0Y10I-10J0", lines[2]);
    }

    @Test
    public void writeSegment_shouldWriteCoordinatesForArcEndingWhereItStarted() throws IOException {
        // Grbl rejects an arc that carries no axis words with "error:26", so the coordinates have
        // to be written even though they have not changed
        StringWriter result = new StringWriter();
        GrblGcodeWriter writer = new GrblGcodeWriter(new Settings(), result);
        writer.writeSegment(new Segment(SegmentType.MOVE, position(10d, 0d)));

        writer.writeSegment(Segment.arc(SegmentType.CWARC, position(10d, 0d), new Point2D.Double(0, 0), 1_000));

        String[] lines = result.toString().split("\n");
        assertEquals(2, lines.length);
        assertEquals("G2 F1000 X10Y0I-10J0", lines[1]);
    }

    @Test
    public void writeSegment_shouldWriteUnchangedCoordinateForArc() throws IOException {
        StringWriter result = new StringWriter();
        GrblGcodeWriter writer = new GrblGcodeWriter(new Settings(), result);
        writer.writeSegment(new Segment(SegmentType.MOVE, position(10d, 0d)));

        writer.writeSegment(Segment.arc(SegmentType.CCWARC, position(10d, 5d), new Point2D.Double(10, 2.5), 1_000));

        String[] lines = result.toString().split("\n");
        assertEquals("G3 F1000 X10Y5I0J2.5", lines[1]);
    }

    @Test
    public void writeSegment_shouldNotWriteUnchangedCoordinateForLine() throws IOException {
        StringWriter result = new StringWriter();
        GrblGcodeWriter writer = new GrblGcodeWriter(new Settings(), result);
        writer.writeSegment(new Segment(SegmentType.MOVE, position(10d, 0d)));

        writer.writeSegment(new Segment(SegmentType.LINE, position(10d, 5d), null, null, 1_000));

        String[] lines = result.toString().split("\n");
        assertEquals("G1 F1000 Y5", lines[1]);
    }

    @Test
    public void writeSegment_shouldFailForArcWithoutKnownStartPosition() {
        StringWriter result = new StringWriter();
        GrblGcodeWriter writer = new GrblGcodeWriter(new Settings(), result);
        Segment arc = Segment.arc(SegmentType.CWARC, position(0d, 10d), new Point2D.Double(0, 0), 1_000);

        assertThrows(IllegalStateException.class, () -> writer.writeSegment(arc));
    }

    private static PartialPosition position(Double x, Double y) {
        return PartialPosition.builder(UnitUtils.Units.MM).setX(x).setY(y).build();
    }

    @Test
    public void spindleShouldNotRestartForSameSpeedUntilChanged() throws IOException {
        StringWriter result = new StringWriter();
        GrblGcodeWriter writer = new GrblGcodeWriter(new Settings(), result);

        writer.writeSegment(new Segment(
                SegmentType.MOVE,
                PartialPosition.builder(UnitUtils.Units.MM).setX(0d).build(),
                null,
                10000,
                null
        ));

        writer.writeSegment(new Segment(
                SegmentType.LINE,
                PartialPosition.builder(UnitUtils.Units.MM).setX(1d).build(),
                null,
                10000,
                500
        ));

        writer.writeSegment(new Segment(
                SegmentType.LINE,
                PartialPosition.builder(UnitUtils.Units.MM).setX(2d).build(),
                null,
                11000,
                600
        ));

        String[] lines = result.toString().split("\n");
        assertEquals(5, lines.length);
        assertEquals("M3 S10000", lines[0]);
        assertEquals("G0 X0", lines[1]);
        assertEquals("G1 F500 X1", lines[2]);
        assertEquals("M3 S11000", lines[3]);
        assertEquals("G1 F600 X2", lines[4]);
    }

    @Test
    public void separateLinesShouldApplyFeedSpeed() throws IOException {
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

        writer.writeSegment(new Segment(SegmentType.SEAM, PartialPosition.builder(UnitUtils.Units.MM).setX(10d).setY(10d).setZ(10d).build()));

        writer.writeSegment(new Segment(SegmentType.MOVE,
                PartialPosition.builder(UnitUtils.Units.MM).setX(15d).setY(15d).build(),
                null,
                10_000, 1_000));

        writer.writeSegment(new Segment(SegmentType.LINE,
                PartialPosition.builder(UnitUtils.Units.MM).setX(20d).setY(20d).build(),
                null,
                11_000, 1_200));

        String[] lines = result.toString().split("\n");
        assertEquals(6, lines.length);
        assertEquals("M3 S10000", lines[0]);
        assertEquals("G0 X0Y0", lines[1]);
        assertEquals("G1 F1000 X10Y10", lines[2]);
        assertEquals("G0 X15Y15", lines[3]);
        assertEquals("M3 S11000", lines[4]);
        assertEquals("G1 F1200 X20Y20", lines[5]);
    }

    @Test
    public void penSegmentsShouldMoveTheZAxisWhenThePenIsCarriedByIt() throws IOException {
        StringWriter result = new StringWriter();
        Settings settings = new Settings();
        settings.setPenMode(PenMode.Z_AXIS);
        settings.setSafeHeight(4);
        settings.setPenDownDepth(0.3);
        settings.setPlungeSpeed(300);
        GrblGcodeWriter writer = new GrblGcodeWriter(settings, result);

        writer.writeSegment(new Segment(SegmentType.PEN_UP, null));
        writer.writeSegment(new Segment(SegmentType.MOVE, position(1d, 2d)));
        writer.writeSegment(new Segment(SegmentType.PEN_DOWN, null));

        String[] lines = result.toString().split("\n");
        assertEquals(3, lines.length);
        assertEquals("G0 Z4", lines[0]);
        assertEquals("G0 X1Y2", lines[1]);
        assertEquals("G1 F300 Z-0.3", lines[2]);
    }

    @Test
    public void penSegmentsShouldSetSpindleSpeedWhenThePenIsDrivenFromTheSpindleOutput() throws IOException {
        StringWriter result = new StringWriter();
        Settings settings = new Settings();
        settings.setPenMode(PenMode.SPINDLE_SPEED);
        settings.setPenDownSpindleSpeed(600);
        settings.setPenUpSpindleSpeed(50);
        GrblGcodeWriter writer = new GrblGcodeWriter(settings, result);

        writer.writeSegment(new Segment(SegmentType.PEN_UP, null));
        writer.writeSegment(new Segment(SegmentType.PEN_DOWN, null));
        writer.writeSegment(new Segment(SegmentType.PEN_UP, null));

        String[] lines = result.toString().split("\n");
        assertEquals(3, lines.length);
        assertEquals("M3 S50", lines[0]);
        assertEquals("M3 S600", lines[1]);
        assertEquals("M3 S50", lines[2]);
    }

    @Test
    public void penSegmentsShouldNotRepeatASpindleSpeedThatIsAlreadySet() throws IOException {
        StringWriter result = new StringWriter();
        Settings settings = new Settings();
        settings.setPenMode(PenMode.SPINDLE_SPEED);
        settings.setPenUpSpindleSpeed(50);
        GrblGcodeWriter writer = new GrblGcodeWriter(settings, result);

        writer.writeSegment(new Segment(SegmentType.PEN_UP, null));
        writer.writeSegment(new Segment(SegmentType.PEN_UP, null));

        assertEquals("M3 S50\n", result.toString());
    }

    @Test
    public void penSegmentsShouldWriteTheConfiguredCommands() throws IOException {
        StringWriter result = new StringWriter();
        Settings settings = new Settings();
        settings.setPenMode(PenMode.CUSTOM_COMMAND);
        settings.setPenDownCommand("M280 P0 S30");
        settings.setPenUpCommand("M280 P0 S90");
        GrblGcodeWriter writer = new GrblGcodeWriter(settings, result);

        writer.writeSegment(new Segment(SegmentType.PEN_UP, null));
        writer.writeSegment(new Segment(SegmentType.PEN_DOWN, null));

        String[] lines = result.toString().split("\n");
        assertEquals(2, lines.length);
        assertEquals("M280 P0 S90", lines[0]);
        assertEquals("M280 P0 S30", lines[1]);
    }

    @Test
    public void penSegmentsShouldWriteNothingForAnEmptyCommand() throws IOException {
        StringWriter result = new StringWriter();
        Settings settings = new Settings();
        settings.setPenMode(PenMode.CUSTOM_COMMAND);
        settings.setPenDownCommand("");
        settings.setPenUpCommand("");
        GrblGcodeWriter writer = new GrblGcodeWriter(settings, result);

        writer.writeSegment(new Segment(SegmentType.PEN_UP, null));
        writer.writeSegment(new Segment(SegmentType.PEN_DOWN, null));

        assertEquals("", result.toString());
    }
}
