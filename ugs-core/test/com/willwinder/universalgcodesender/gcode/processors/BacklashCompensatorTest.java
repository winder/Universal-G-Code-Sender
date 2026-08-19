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
package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.List;

/**
 * @author Joacim Breiler
 */
public class BacklashCompensatorTest {
    public static final double DEFAULT_ARC_SEGMENT_LENGTH_MM = 0.1;

    @Test
    public void processCommand_shouldNotChangeCommandsWithoutBacklash() throws GcodeParserException {
        BacklashCompensator compensator = new BacklashCompensator(new Position(0, 0, 0, Units.MM), DEFAULT_ARC_SEGMENT_LENGTH_MM);

        List<String> result = compensator.processCommand("G1 X10 Y10", createState(0, 0, 0));

        Assertions.assertThat(result).containsExactly("G1 X10 Y10");
    }

    @Test
    public void processCommand_shouldTakeUpBacklashOnFirstMovement() throws GcodeParserException {
        BacklashCompensator compensator = new BacklashCompensator(new Position(0.1, 0.2, 0, Units.MM), DEFAULT_ARC_SEGMENT_LENGTH_MM);

        List<String> result = compensator.processCommand("G1X10Y10", createState(0, 0, 0));

        Assertions.assertThat(result).containsExactly("G1X0.1Y0.2", "G1X10.1Y10.2");
    }

    @Test
    public void processCommand_shouldOnlyWindUpAnAxisOnce() throws GcodeParserException {
        BacklashCompensator compensator = new BacklashCompensator(new Position(0.1, 0.1, 0.1, Units.MM), DEFAULT_ARC_SEGMENT_LENGTH_MM);
        compensator.processCommand("G0X10", createState(0, 0, 0));

        List<String> result = compensator.processCommand("G0X20", createState(10, 0, 0));

        Assertions.assertThat(result).containsExactly("G0X20.1");
    }

    @Test
    public void processCommand_shouldWindUpStationaryAxesOnRapidMovements() throws GcodeParserException {
        BacklashCompensator compensator = new BacklashCompensator(new Position(0.1, 0.1, 0.1, Units.MM), DEFAULT_ARC_SEGMENT_LENGTH_MM);

        List<String> result = compensator.processCommand("G0X10", createState(0, 0, 0));

        Assertions.assertThat(result).containsExactly("G0X0.1", "G0X10.1", "G0Y0.1Z0.1");
    }

    @Test
    public void processCommand_shouldNotTakeUpBacklashOfAnAxisThatHasBeenWoundUp() throws GcodeParserException {
        BacklashCompensator compensator = new BacklashCompensator(new Position(0.1, 0.1, 0.1, Units.MM), DEFAULT_ARC_SEGMENT_LENGTH_MM);
        compensator.processCommand("G0X10", createState(0, 0, 0));

        List<String> result = compensator.processCommand("G1Y10", createState(10, 0, 0));

        Assertions.assertThat(result).containsExactly("G1Y10.1");
    }

    @Test
    public void processCommand_shouldNotWindUpAxesWithUnknownPosition() throws GcodeParserException {
        BacklashCompensator compensator = new BacklashCompensator(new Position(0.1, 0.1, 0.1, Units.MM), DEFAULT_ARC_SEGMENT_LENGTH_MM);
        GcodeState state = createState(0, 0, 0);
        state.currentPoint = new Position(0, 0, Double.NaN, Units.MM);

        List<String> result = compensator.processCommand("G0X10", state);

        Assertions.assertThat(result).containsExactly("G0X0.1", "G0X10.1", "G0Y0.1");
    }

    @Test
    public void processCommand_shouldNotWindUpOnCuttingMovements() throws GcodeParserException {
        BacklashCompensator compensator = new BacklashCompensator(new Position(0.1, 0.1, 0.1, Units.MM), DEFAULT_ARC_SEGMENT_LENGTH_MM);

        List<String> result = compensator.processCommand("G1X10", createState(0, 0, 0));

        Assertions.assertThat(result).containsExactly("G1X0.1", "G1X10.1");
    }

    @Test
    public void processCommand_shouldTakeUpBacklashWhenChangingDirection() throws GcodeParserException {
        BacklashCompensator compensator = new BacklashCompensator(new Position(0.1, 0.2, 0, Units.MM), DEFAULT_ARC_SEGMENT_LENGTH_MM);
        compensator.processCommand("G1X10Y10", createState(0, 0, 0));

        List<String> result = compensator.processCommand("G1X5Y5", createState(10, 10, 0));

        Assertions.assertThat(result).containsExactly("G1X10Y10", "G1X5Y5");
    }

    @Test
    public void processCommand_shouldNotTakeUpBacklashWhenKeepingDirection() throws GcodeParserException {
        BacklashCompensator compensator = new BacklashCompensator(new Position(0.1, 0, 0, Units.MM), DEFAULT_ARC_SEGMENT_LENGTH_MM);
        compensator.processCommand("G1X10", createState(0, 0, 0));

        List<String> result = compensator.processCommand("G1X20", createState(10, 0, 0));

        Assertions.assertThat(result).containsExactly("G1X20.1");
    }

    @Test
    public void processCommand_shouldNotAccumulateBacklashOnMultipleDirectionChanges() throws GcodeParserException {
        BacklashCompensator compensator = new BacklashCompensator(new Position(0.1, 0, 0, Units.MM), DEFAULT_ARC_SEGMENT_LENGTH_MM);
        compensator.processCommand("G1X10", createState(0, 0, 0));
        compensator.processCommand("G1X5", createState(10, 0, 0));
        compensator.processCommand("G1X8", createState(5, 0, 0));

        List<String> result = compensator.processCommand("G1X2", createState(8, 0, 0));

        Assertions.assertThat(result).containsExactly("G1X8", "G1X2");
    }

    @Test
    public void processCommand_shouldOnlyTakeUpBacklashForAxesChangingDirection() throws GcodeParserException {
        BacklashCompensator compensator = new BacklashCompensator(new Position(0.1, 0.1, 0.1, Units.MM), DEFAULT_ARC_SEGMENT_LENGTH_MM);
        compensator.processCommand("G1X10Y10Z10", createState(0, 0, 0));

        List<String> result = compensator.processCommand("G1X5Y20Z10", createState(10, 10, 10));

        Assertions.assertThat(result).containsExactly("G1X10", "G1X5Y20.1Z10.1");
    }

    @Test
    public void processCommand_shouldTakeUpBacklashWithoutMovingTheOtherAxesInACorner() throws GcodeParserException {
        BacklashCompensator compensator = new BacklashCompensator(new Position(0, 0.1, 0, Units.MM), DEFAULT_ARC_SEGMENT_LENGTH_MM);
        compensator.processCommand("G1X10Y10", createState(0, 0, 0));

        List<String> result = compensator.processCommand("G1X20Y0", createState(10, 10, 0));

        Assertions.assertThat(result).containsExactly("G1Y10", "G1X20Y0");
    }

    @Test
    public void processCommand_shouldNotCompensateNonMovementCommands() throws GcodeParserException {
        BacklashCompensator compensator = new BacklashCompensator(new Position(0.1, 0, 0, Units.MM), DEFAULT_ARC_SEGMENT_LENGTH_MM);
        compensator.processCommand("G1X10", createState(0, 0, 0));

        List<String> result = compensator.processCommand("G92X0", createState(10, 0, 0));

        Assertions.assertThat(result).containsExactly("G92X0");
    }

    @Test
    public void processCommand_shouldNotCompensateMachineCoordinateMovements() throws GcodeParserException {
        BacklashCompensator compensator = new BacklashCompensator(new Position(0.1, 0, 0, Units.MM), DEFAULT_ARC_SEGMENT_LENGTH_MM);
        compensator.processCommand("G1X10", createState(0, 0, 0));

        List<String> result = compensator.processCommand("G53G0X0", createState(10, 0, 0));

        Assertions.assertThat(result).containsExactly("G53G0X0");
    }

    @Test
    public void processCommand_shouldKeepOtherWordsInCommand() throws GcodeParserException {
        BacklashCompensator compensator = new BacklashCompensator(new Position(0.1, 0, 0, Units.MM), DEFAULT_ARC_SEGMENT_LENGTH_MM);
        compensator.processCommand("G1X10F100", createState(0, 0, 0));

        List<String> result = compensator.processCommand("G1 X5 F100 M8 (a comment)", createState(10, 0, 0));

        Assertions.assertThat(result).containsExactly("G1X10F100", "G1X5F100M8(a comment)");
    }

    @Test
    public void processCommand_shouldCompensateInInches() throws GcodeParserException {
        BacklashCompensator compensator = new BacklashCompensator(new Position(2.54, 0, 0, Units.MM), DEFAULT_ARC_SEGMENT_LENGTH_MM);

        List<String> result = compensator.processCommand("G1X1", createInchState(0));

        Assertions.assertThat(result).containsExactly("G1X0.1", "G1X1.1");
    }

    @Test
    public void processCommand_shouldCompensateRelativeMovements() throws GcodeParserException {
        BacklashCompensator compensator = new BacklashCompensator(new Position(0.1, 0, 0, Units.MM), DEFAULT_ARC_SEGMENT_LENGTH_MM);

        List<String> firstResult = compensator.processCommand("G1X10", createRelativeState(0));
        List<String> result = compensator.processCommand("G1X-5", createRelativeState(10));

        Assertions.assertThat(firstResult).containsExactly("G1X0.1", "G1X10");
        Assertions.assertThat(result).containsExactly("G1X-0.1", "G1X-5");
    }

    @Test
    public void processCommand_shouldExpandArcsToCompensatedLines() throws GcodeParserException {
        BacklashCompensator compensator = new BacklashCompensator(new Position(0.1, 0.1, 0, Units.MM), 10);
        compensator.processCommand("G1X-10Y0", createState(0, 0, 0));

        List<String> result = compensator.processCommand("G2X10Y0I10J0", createState(-10, 0, 0));

        Assertions.assertThat(result).hasSizeGreaterThan(1);
        Assertions.assertThat(result).allMatch(command -> command.startsWith("G1"));
    }

    @Test
    public void reset_shouldForgetTheCompensatedDirections() throws GcodeParserException {
        BacklashCompensator compensator = new BacklashCompensator(new Position(0.1, 0, 0, Units.MM), DEFAULT_ARC_SEGMENT_LENGTH_MM);
        compensator.processCommand("G1X10", createState(0, 0, 0));

        compensator.reset();
        List<String> result = compensator.processCommand("G1X5", createState(10, 0, 0));

        Assertions.assertThat(result).containsExactly("G1X9.9", "G1X4.9");
    }

    private GcodeState createInchState(double x) {
        GcodeState state = createState(0, 0, 0);
        state.currentPoint = new Position(x, 0, 0, Units.INCH);
        state.units = Code.G20;
        state.isMetric = false;
        return state;
    }

    private GcodeState createRelativeState(double x) {
        GcodeState state = createState(x, 0, 0);
        state.inAbsoluteMode = false;
        return state;
    }

    private GcodeState createState(double x, double y, double z) {
        GcodeState state = new GcodeState();
        state.currentPoint = new Position(x, y, z, Units.MM);
        state.currentMotionMode = Code.G1;
        return state;
    }
}
