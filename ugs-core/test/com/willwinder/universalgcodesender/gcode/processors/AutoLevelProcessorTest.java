/*
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

import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.utils.AutoLevelArcMode;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AutoLevelProcessorTest {
    private static final double ARC_TOLERANCE = 0.01;
    private static final double MAX_SEGMENT = 2.5;

    @Test
    public void matchesArcSplitAndMeshChain() throws Exception {
        List<Position> points = Arrays.asList(
                new Position(0, 0, 0, Units.MM),
                new Position(0, 20, 0.4, Units.MM),
                new Position(40, 0, 0.4, Units.MM),
                new Position(40, 20, 0.8, Units.MM));
        List<String> program = Arrays.asList(
                "G21",
                "G90",
                "G17",
                "G0 X0 Y0 Z5",
                "G1 Z0 F100",
                "G1 X10 Y0",
                "X10 Y5",
                "G1 X40 Y5 F80",
                "G2 X42 Y7 I2 J0",
                "G1 X42 Y10",
                "G91",
                "G1 X1 Y1 Z-0.5",
                "G90",
                "G0 X0 Y0 Z5",
                "(comment)",
                "M5",
                "G20",
                "G1 X1 Y0.5 Z-0.1",
                "G21");

        assertEquals(level(legacy(points), program), level(new AutoLevelProcessor(ARC_TOLERANCE, MAX_SEGMENT, 0, points), program));
    }

    @Test
    public void keepArcsShiftsStartAndEndWithoutExpanding() throws Exception {
        AutoLevelProcessor processor = keep(flatSurface());
        GcodeState state = stateAt(0, 0, 0);

        assertEquals(List.of("G1Z1", "G2 X10 Y0 I5 J0Z1"), processor.processCommand("G2 X10 Y0 I5 J0", state));

        state.currentPoint = new Position(10, 0, 0, Units.MM);
        state.currentMotionMode = Code.G2;
        assertEquals(List.of("G2 X0 Y0 I-5 J0Z1"), processor.processCommand("X0 Y0 I-5 J0", state));
    }

    @Test
    public void previousLeveledMoveAlreadySetsArcStart() throws Exception {
        AutoLevelProcessor processor = keep(flatSurface());
        GcodeState state = stateAt(0, 0, 1);

        assertEquals(List.of("G1 Z1"), processor.processCommand("G1 Z0", state));

        state.currentPoint = new Position(0, 0, 0, Units.MM);
        assertEquals(List.of("G2 X10 Y0 I5 J0Z1"), processor.processCommand("G2 X10 Y0 I5 J0", state));
    }

    @Test
    public void resetRequiresStartShiftAgain() throws Exception {
        AutoLevelProcessor processor = keep(flatSurface());
        GcodeState state = stateAt(0, 0, 0);
        processor.processCommand("G1 Z0", state);

        state.currentPoint = new Position(0, 0, 0, Units.MM);
        processor.reset();

        assertEquals(List.of("G1Z1", "G2 X10 Y0 I5 J0Z1"), processor.processCommand("G2 X10 Y0 I5 J0", state));
    }

    @Test
    public void keepArcsPreservesRadiusAndProgrammedEndDepth() throws Exception {
        AutoLevelProcessor processor = keep(flatSurface());

        assertEquals(List.of("G1Z1", "G2 X10 Y0 Z0 I5 J0"), processor.processCommand("G2 X10 Y0 Z-1 I5 J0", stateAt(0, 0, 0)));
        processor.reset();
        assertEquals(List.of("G1Z1", "G2 X10 Y0 R5Z1"), processor.processCommand("G2 X10 Y0 R5", stateAt(0, 0, 0)));
    }

    @Test
    public void slopedSurfaceUsesDifferentStartAndEndZ() throws Exception {
        List<Position> slope = Arrays.asList(
                new Position(0, 0, 0, Units.MM),
                new Position(0, 20, 0, Units.MM),
                new Position(40, 0, 2, Units.MM),
                new Position(40, 20, 2, Units.MM));
        AutoLevelProcessor processor = keep(slope);

        assertEquals(List.of("G1Z0.5", "G2 X20 Y5 I5 J0Z1"),
                processor.processCommand("G2 X20 Y5 I5 J0", stateAt(10, 5, 0)));
    }

    @Test
    public void relativeArcShiftsStartThenOnlyTheZChange() throws Exception {
        AutoLevelProcessor processor = keep(flatSurface());
        GcodeState state = stateAt(0, 0, 0);
        state.inAbsoluteMode = false;

        assertEquals(List.of("G1Z1", "G2 X10 Y0 I5 J0Z0"), processor.processCommand("G2 X10 Y0 I5 J0", state));
    }

    @Test
    public void keepArcsInAProgramDoesNotExpandTheArc() throws Exception {
        List<String> program = Arrays.asList(
                "G21",
                "G90",
                "G17",
                "G0 X0 Y0 Z5",
                "G1 Z0 F100",
                "G2 X10 Y0 I5 J0 F80");
        List<String> out = level(keep(flatSurface()), program);

        long arcs = out.stream().filter(AutoLevelProcessorTest::isKeptArc).count();
        assertEquals(1, arcs);
        String arc = out.stream().filter(AutoLevelProcessorTest::isKeptArc).findFirst().orElseThrow();
        assertTrue(arc.contains("I5"));
        assertTrue(arc.contains("J0"));
        assertTrue(arc.contains("Z1"));
        assertFalse(arc.contains("G1"));
        assertTrue(out.stream().anyMatch("F80"::equals));
    }

    private static boolean isKeptArc(String line) {
        return line.contains("G2 ") || line.contains("G2X") || line.contains("G3 ") || line.contains("G3X");
    }

    private static List<Position> flatSurface() {
        return Arrays.asList(
                new Position(0, 0, 1, Units.MM),
                new Position(0, 20, 1, Units.MM),
                new Position(40, 0, 1, Units.MM),
                new Position(40, 20, 1, Units.MM));
    }

    private static AutoLevelProcessor keep(List<Position> points) {
        return new AutoLevelProcessor(ARC_TOLERANCE, MAX_SEGMENT, 0, points, AutoLevelArcMode.KEEP_ARCS);
    }

    private static GcodeState stateAt(double x, double y, double z) {
        GcodeState state = new GcodeState();
        state.currentPoint = new Position(x, y, z, Units.MM);
        state.inAbsoluteMode = true;
        return state;
    }

    private static CommandProcessorList legacy(List<Position> points) {
        CommandProcessorList list = new CommandProcessorList();
        list.add(new ArcExpander(true, ARC_TOLERANCE, GcodePreprocessorUtils.getDecimalFormatter(), true));
        list.add(new LineSplitter(MAX_SEGMENT));
        list.add(new MeshLeveler(0, points));
        return list;
    }

    private static List<String> level(CommandProcessor processor, List<String> program) throws GcodeParserException {
        GcodeParser parser = new GcodeParser();
        parser.addCommandProcessor(processor);
        List<String> out = new ArrayList<>();
        for (String line : program) {
            List<String> processed = parser.preprocessCommand(line, parser.getCurrentState());
            if (processed == null) {
                fail("preprocessor dropped " + line);
            }
            out.addAll(processed);
            parser.addCommand(line);
        }
        return out;
    }
}
