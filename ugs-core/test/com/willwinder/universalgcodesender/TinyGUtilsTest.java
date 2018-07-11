/*
    Copyright 2018 Will Winder

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
import com.willwinder.universalgcodesender.gcode.util.Plane;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for the TinyGUtils class
 *
 * @author Joacim Breiler
 */
public class TinyGUtilsTest {

    @Test
    public void generateSetWorkPositionCommandShouldGenerateGcode() {
        ControllerStatus controllerStatus = new ControllerStatus("", ControllerState.UNKNOWN, new Position(10, 10, 10, UnitUtils.Units.MM), new Position(10, 10, 10, UnitUtils.Units.MM));
        GcodeState gcodeState = new GcodeState();

        gcodeState.offset = Code.G54;
        String command = TinyGUtils.generateSetWorkPositionCommand(controllerStatus, gcodeState, Axis.X, 5);
        assertEquals("G10 L2 P1 X5", command);

        gcodeState.offset = Code.G55;
        command = TinyGUtils.generateSetWorkPositionCommand(controllerStatus, gcodeState, Axis.Y, 15);
        assertEquals("G10 L2 P2 Y-5", command);

        gcodeState.offset = Code.G56;
        command = TinyGUtils.generateSetWorkPositionCommand(controllerStatus, gcodeState, Axis.Z, 0);
        assertEquals("G10 L2 P3 Z10", command);

        gcodeState.offset = Code.G57;
        command = TinyGUtils.generateSetWorkPositionCommand(controllerStatus, gcodeState, Axis.Z, 0);
        assertEquals("G10 L2 P4 Z10", command);

        gcodeState.offset = Code.G58;
        command = TinyGUtils.generateSetWorkPositionCommand(controllerStatus, gcodeState, Axis.Z, 0);
        assertEquals("G10 L2 P5 Z10", command);

        gcodeState.offset = Code.G59;
        command = TinyGUtils.generateSetWorkPositionCommand(controllerStatus, gcodeState, Axis.Z, 0);
        assertEquals("G10 L2 P6 Z10", command);
    }

    @Test
    public void generateResetCoordinatesToZeroCommandShouldGenerateGcode() {
        ControllerStatus controllerStatus = new ControllerStatus("", ControllerState.UNKNOWN, new Position(10, 10, 10, UnitUtils.Units.MM), new Position(10, 10, 10, UnitUtils.Units.MM));
        GcodeState gcodeState = new GcodeState();

        gcodeState.offset = Code.G54;
        String command = TinyGUtils.generateResetCoordinatesToZeroCommand(controllerStatus, gcodeState);
        assertEquals("G10 L2 P1 X10 Y10 Z10", command);

        gcodeState.offset = Code.G55;
        command = TinyGUtils.generateResetCoordinatesToZeroCommand(controllerStatus, gcodeState);
        assertEquals("G10 L2 P2 X10 Y10 Z10", command);
    }

    @Test
    public void updateGcodeStateShouldUpdateOffset() {
        // Given
        GcodeState gcodeState = new GcodeState();
        TinyGController controller = mock(TinyGController.class);
        when(controller.getCurrentGcodeState()).thenReturn(gcodeState);

        // When
        JsonObject response = TinyGUtils.jsonToObject("{sr:{coor:2}}");
        TinyGUtils.updateGcodeState(controller, response);

        // Then
        assertEquals(Code.G55, gcodeState.offset);
    }

    @Test
    public void updateGcodeStateShouldUpdateUnit() {
        // Given
        GcodeState gcodeState = new GcodeState();
        TinyGController controller = mock(TinyGController.class);
        when(controller.getCurrentGcodeState()).thenReturn(gcodeState);

        // When switch to inch
        JsonObject response = TinyGUtils.jsonToObject("{sr:{unit:1}}");
        TinyGUtils.updateGcodeState(controller, response);

        // Then
        assertEquals(Code.G21, gcodeState.units);
        verify(controller).setUnitsCode("G21");


        // When switch to mm
        response = TinyGUtils.jsonToObject("{sr:{unit:0}}");
        TinyGUtils.updateGcodeState(controller, response);

        // Then
        assertEquals(Code.G20, gcodeState.units);
        verify(controller).setUnitsCode("G20");
    }


    @Test
    public void updateGcodeStateShouldUpdatePlane() {
        // Given
        GcodeState gcodeState = new GcodeState();
        TinyGController controller = mock(TinyGController.class);
        when(controller.getCurrentGcodeState()).thenReturn(gcodeState);

        // When switch XY
        JsonObject response = TinyGUtils.jsonToObject("{sr:{plan:0}}");
        TinyGUtils.updateGcodeState(controller, response);

        // Then
        assertEquals(Plane.XY, gcodeState.plane);


        // When switch to ZX
        response = TinyGUtils.jsonToObject("{sr:{plan:1}}");
        TinyGUtils.updateGcodeState(controller, response);

        // Then
        assertEquals(Plane.ZX, gcodeState.plane);


        // When switch to YZ
        response = TinyGUtils.jsonToObject("{sr:{plan:2}}");
        TinyGUtils.updateGcodeState(controller, response);

        // Then
        assertEquals(Plane.YZ, gcodeState.plane);
    }

    @Test
    public void updateGcodeStateShouldUpdateFeedMode() {
        // Given
        GcodeState gcodeState = new GcodeState();
        TinyGController controller = mock(TinyGController.class);
        when(controller.getCurrentGcodeState()).thenReturn(gcodeState);

        // When switch to units per minute mode
        JsonObject response = TinyGUtils.jsonToObject("{sr:{frmo:0}}");
        TinyGUtils.updateGcodeState(controller, response);

        // Then
        assertEquals(Code.G93, gcodeState.feedMode);


        // When switch to inverse time mode
        response = TinyGUtils.jsonToObject("{sr:{frmo:1}}");
        TinyGUtils.updateGcodeState(controller, response);

        // Then
        assertEquals(Code.G94, gcodeState.feedMode);
    }


    @Test
    public void updateGcodeStateShouldUpdateDistanceMode() {
        // Given
        GcodeState gcodeState = new GcodeState();
        TinyGController controller = mock(TinyGController.class);
        when(controller.getCurrentGcodeState()).thenReturn(gcodeState);

        // When switch to units per minute mode
        JsonObject response = TinyGUtils.jsonToObject("{sr:{dist:0}}");
        TinyGUtils.updateGcodeState(controller, response);

        // Then
        assertEquals(Code.G90, gcodeState.distanceMode);


        // When switch to inverse time mode
        response = TinyGUtils.jsonToObject("{sr:{dist:1}}");
        TinyGUtils.updateGcodeState(controller, response);

        // Then
        assertEquals(Code.G91, gcodeState.distanceMode);
    }

    @Test
    public void updateGcodeStateShouldUpdateArcDistanceMode() {
        // Given
        GcodeState gcodeState = new GcodeState();
        TinyGController controller = mock(TinyGController.class);
        when(controller.getCurrentGcodeState()).thenReturn(gcodeState);

        // When switch to units per minute mode
        JsonObject response = TinyGUtils.jsonToObject("{sr:{admo:0}}");
        TinyGUtils.updateGcodeState(controller, response);

        // Then
        assertEquals(Code.G90_1, gcodeState.arcDistanceMode);


        // When switch to inverse time mode
        response = TinyGUtils.jsonToObject("{sr:{admo:1}}");
        TinyGUtils.updateGcodeState(controller, response);

        // Then
        assertEquals(Code.G91_1, gcodeState.arcDistanceMode);
    }
}
