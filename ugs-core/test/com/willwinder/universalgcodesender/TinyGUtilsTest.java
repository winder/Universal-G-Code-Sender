/*
    Copyright 2018-2020 Will Winder

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

import com.google.gson.JsonObject;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.model.*;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the TinyGUtils class
 *
 * @author Joacim Breiler
 */
public class TinyGUtilsTest {

    @Test
    public void generateSetWorkPositionCommandShouldGenerateGcode() {
        ControllerStatus controllerStatus = new ControllerStatus(ControllerState.UNKNOWN, new Position(10, 10, 10, UnitUtils.Units.MM), new Position(10, 10, 10, UnitUtils.Units.MM));
        GcodeState gcodeState = new GcodeState();

        gcodeState.offset = Code.G54;
        String command = TinyGUtils.generateSetWorkPositionCommand(controllerStatus, gcodeState, PartialPosition.from(Axis.X, 5.0, UnitUtils.Units.MM));
        assertEquals("G10 L2 P1 X5", command);

        gcodeState.offset = Code.G55;
        command = TinyGUtils.generateSetWorkPositionCommand(controllerStatus, gcodeState, PartialPosition.from(Axis.Y, 15.0, UnitUtils.Units.MM));
        assertEquals("G10 L2 P2 Y-5", command);

        gcodeState.offset = Code.G56;
        command = TinyGUtils.generateSetWorkPositionCommand(controllerStatus, gcodeState, PartialPosition.from(Axis.Z, 0.0, UnitUtils.Units.MM));
        assertEquals("G10 L2 P3 Z10", command);

        gcodeState.offset = Code.G57;
        command = TinyGUtils.generateSetWorkPositionCommand(controllerStatus, gcodeState, PartialPosition.from(Axis.Z, 0.0, UnitUtils.Units.MM));
        assertEquals("G10 L2 P4 Z10", command);

        gcodeState.offset = Code.G58;
        command = TinyGUtils.generateSetWorkPositionCommand(controllerStatus, gcodeState, PartialPosition.from(Axis.Z, 0.0, UnitUtils.Units.MM));
        assertEquals("G10 L2 P5 Z10", command);

        gcodeState.offset = Code.G59;
        command = TinyGUtils.generateSetWorkPositionCommand(controllerStatus, gcodeState, PartialPosition.from(Axis.Z, 0.0, UnitUtils.Units.MM));
        assertEquals("G10 L2 P6 Z10", command);

        gcodeState.offset = Code.G59;
        command = TinyGUtils.generateSetWorkPositionCommand(controllerStatus, gcodeState, new PartialPosition(10.0, 20.0, UnitUtils.Units.MM));
        assertEquals("G10 L2 P6 X-0Y-10", command); // the negative Zero gets formatted as "-0" - is this a problem?

        gcodeState.offset = Code.G59;
        command = TinyGUtils.generateSetWorkPositionCommand(controllerStatus, gcodeState, new PartialPosition(10.0, 20.0, 30.0, UnitUtils.Units.MM));
        assertEquals("G10 L2 P6 X-0Y-10Z-20", command);
    }

    @Test
    public void generateResetCoordinatesToZeroCommandShouldGenerateGcode() {
        ControllerStatus controllerStatus = new ControllerStatus(ControllerState.UNKNOWN, new Position(10, 20, 30, UnitUtils.Units.MM), new Position(10, 10, 10, UnitUtils.Units.MM));
        GcodeState gcodeState = new GcodeState();

        gcodeState.offset = Code.G54;
        String command = TinyGUtils.generateResetCoordinatesToZeroCommand(controllerStatus, gcodeState);
        assertEquals("G10 L2 P1 X10 Y20 Z30", command);

        gcodeState.offset = Code.G55;
        command = TinyGUtils.generateResetCoordinatesToZeroCommand(controllerStatus, gcodeState);
        assertEquals("G10 L2 P2 X10 Y20 Z30", command);
    }

    @Test
    public void convertStatusReportShouldHandleOffset() {
        // When
        JsonObject response = TinyGUtils.jsonToObject("{sr:{coor:2}}");
        List<String> result = TinyGUtils.convertStatusReportToGcode(response);

        // Then
        assertTrue(result.contains(Code.G55.name()));
    }

    @Test
    public void convertStatusReportShouldHandleUnit() {
        // When switch to inch
        JsonObject response = TinyGUtils.jsonToObject("{sr:{unit:1}}");
        List<String> result = TinyGUtils.convertStatusReportToGcode(response);

        // Then
        assertTrue(result.contains(Code.G21.name()));


        // When switch to mm
        response = TinyGUtils.jsonToObject("{sr:{unit:0}}");
        result = TinyGUtils.convertStatusReportToGcode(response);

        // Then
        assertTrue(result.contains(Code.G20.name()));
    }


    @Test
    public void convertStatusReportShouldHandlePlane() {
        // When switch XY
        JsonObject response = TinyGUtils.jsonToObject("{sr:{plan:0}}");
        List<String> result = TinyGUtils.convertStatusReportToGcode(response);

        // Then
        assertTrue(result.contains(Code.G17.name()));


        // When switch to ZX
        response = TinyGUtils.jsonToObject("{sr:{plan:1}}");
        result = TinyGUtils.convertStatusReportToGcode(response);

        // Then
        assertTrue(result.contains(Code.G18.name()));


        // When switch to YZ
        response = TinyGUtils.jsonToObject("{sr:{plan:2}}");
        result = TinyGUtils.convertStatusReportToGcode(response);

        // Then
        assertTrue(result.contains(Code.G19.name()));
    }

    @Test
    public void convertStatusReportShouldHandleFeedMode() {
        // When switch to units per minute mode
        JsonObject response = TinyGUtils.jsonToObject("{sr:{frmo:0}}");
        List<String> result = TinyGUtils.convertStatusReportToGcode(response);

        // Then
        assertTrue(result.contains(Code.G93.name()));


        // When switch to inverse time mode
        response = TinyGUtils.jsonToObject("{sr:{frmo:1}}");
        result = TinyGUtils.convertStatusReportToGcode(response);

        // Then
        assertTrue(result.contains(Code.G94.name()));
    }

    @Test
    public void convertStatusReportShouldHandleDistanceMode() {
        // When switch to units per minute mode
        JsonObject response = TinyGUtils.jsonToObject("{sr:{dist:0}}");
        List<String> result = TinyGUtils.convertStatusReportToGcode(response);

        // Then
        assertTrue(result.contains(Code.G90.name()));


        // When switch to inverse time mode
        response = TinyGUtils.jsonToObject("{sr:{dist:1}}");
        result = TinyGUtils.convertStatusReportToGcode(response);

        // Then
        assertTrue(result.contains(Code.G91.name()));
    }

    @Test
    public void convertStatusReportShouldHandleArcDistanceMode() {
        // When switch to units per minute mode
        JsonObject response = TinyGUtils.jsonToObject("{sr:{admo:0}}");
        List<String> result = TinyGUtils.convertStatusReportToGcode(response);

        // Then
        assertTrue(result.contains(Code.G90_1.toString()));


        // When switch to inverse time mode
        response = TinyGUtils.jsonToObject("{sr:{admo:1}}");
        result = TinyGUtils.convertStatusReportToGcode(response);

        // Then
        assertTrue(result.contains(Code.G91_1.toString()));
    }

    @Test
    public void createOverrideCommandForFeedOverride() {
        ControllerStatus.OverridePercents overridePercents = new ControllerStatus.OverridePercents(100, 150, 175);
        Optional<GcodeCommand> overrideCommand = TinyGUtils.createOverrideCommand(overridePercents, Overrides.CMD_FEED_OVR_COARSE_PLUS);
        assertEquals("{mfo:1.1}", overrideCommand.get().getCommandString());

        overrideCommand = TinyGUtils.createOverrideCommand(overridePercents, Overrides.CMD_FEED_OVR_COARSE_MINUS);
        assertEquals("{mfo:0.9}", overrideCommand.get().getCommandString());

        overrideCommand = TinyGUtils.createOverrideCommand(overridePercents, Overrides.CMD_FEED_OVR_FINE_MINUS);
        assertEquals("{mfo:0.95}", overrideCommand.get().getCommandString());

        overrideCommand = TinyGUtils.createOverrideCommand(overridePercents, Overrides.CMD_FEED_OVR_FINE_PLUS);
        assertEquals("{mfo:1.05}", overrideCommand.get().getCommandString());
    }

    @Test
    public void createOverrideCommandForSpindleOverride() {
        ControllerStatus.OverridePercents overridePercents = new ControllerStatus.OverridePercents(150, 175, 100);
        Optional<GcodeCommand> overrideCommand = TinyGUtils.createOverrideCommand(overridePercents, Overrides.CMD_SPINDLE_OVR_COARSE_PLUS);
        assertEquals("{sso:1.1}", overrideCommand.get().getCommandString());

        overrideCommand = TinyGUtils.createOverrideCommand(overridePercents, Overrides.CMD_SPINDLE_OVR_COARSE_MINUS);
        assertEquals("{sso:0.9}", overrideCommand.get().getCommandString());

        overrideCommand = TinyGUtils.createOverrideCommand(overridePercents, Overrides.CMD_SPINDLE_OVR_FINE_MINUS);
        assertEquals("{sso:0.95}", overrideCommand.get().getCommandString());

        overrideCommand = TinyGUtils.createOverrideCommand(overridePercents, Overrides.CMD_SPINDLE_OVR_FINE_PLUS);
        assertEquals("{sso:1.05}", overrideCommand.get().getCommandString());
    }

    @Test
    public void updateControllerStatusShouldHandleFeedOverrides() {
        ControllerStatus lastControllerStatus = new ControllerStatus(ControllerState.IDLE, new Position(0, 0, 0, UnitUtils.Units.MM), new Position(0, 0, 0, UnitUtils.Units.MM));

        JsonObject response = TinyGUtils.jsonToObject("{sr:{mfo:1.4}}");
        ControllerStatus controllerStatus = TinyGUtils.updateControllerStatus(lastControllerStatus, response);
        assertEquals(140, controllerStatus.getOverrides().feed);

        response = TinyGUtils.jsonToObject("{sr:{mfo:0.8}}");
        controllerStatus = TinyGUtils.updateControllerStatus(lastControllerStatus, response);
        assertEquals(80, controllerStatus.getOverrides().feed);
    }

    @Test
    public void updateControllerStatusShouldHandleSpindleverrides() {
        ControllerStatus lastControllerStatus = new ControllerStatus(ControllerState.IDLE, new Position(0, 0, 0, UnitUtils.Units.MM), new Position(0, 0, 0, UnitUtils.Units.MM));

        JsonObject response = TinyGUtils.jsonToObject("{sr:{sso:1.4}}");
        ControllerStatus controllerStatus = TinyGUtils.updateControllerStatus(lastControllerStatus, response);
        assertEquals(140, controllerStatus.getOverrides().spindle);

        response = TinyGUtils.jsonToObject("{sr:{sso:0.8}}");
        controllerStatus = TinyGUtils.updateControllerStatus(lastControllerStatus, response);
        assertEquals(80, controllerStatus.getOverrides().spindle);
    }
}
