/*
    Copyright 2013-2020 Will Winder

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

import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.junit.Test;

import static com.willwinder.universalgcodesender.model.Axis.*;
import static com.willwinder.universalgcodesender.model.UnitUtils.Units.INCH;
import static com.willwinder.universalgcodesender.model.UnitUtils.Units.MM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

/**
 * @author wwinder
 */
public class GrblUtilsTest {
    /**
     * Test of isGrblVersionString method, of class GrblUtils.
     */
    @Test
    public void testIsGrblVersionString() {
        System.out.println("isGrblVersionString");
        String response;
        Boolean expResult;
        Boolean result;

        response = "Grbl 0.8c";
        expResult = true;
        result = GrblUtils.isGrblVersionString(response);
        assertEquals(expResult, result);

        response = "blah 0.8c";
        expResult = false;
        result = GrblUtils.isGrblVersionString(response);
        assertEquals(expResult, result);
    }

    /**
     * Test of getVersionDouble method, of class GrblUtils.
     */
    @Test
    public void testGetVersionDouble() {
        System.out.println("getVersionDouble");
        String response;
        double expResult;
        double result;

        response = "Grbl 0.8c";
        expResult = 0.8;
        result = GrblUtils.getVersionDouble(response);
        assertEquals(expResult, result, 0.0);


        response = "CarbideMotion 0.9g";
        expResult = 0.9;
        result = GrblUtils.getVersionDouble(response);
        assertEquals(expResult, result, 0.0);


    }

    /**
     * Test of getVersionLetter method, of class GrblUtils.
     */
    @Test
    public void testGetVersionLetter() {
        System.out.println("getVersionLetter");
        String response = "Grbl 0.8c";
        Character expResult = 'c';
        Character result = GrblUtils.getVersionLetter(response);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetHomingCommand() {
        System.out.println("getHomingCommand");
        double version;
        Character letter;
        String result;
        String expResult;

        version = 0.7;
        letter = null;
        expResult = "";
        result = GrblUtils.getHomingCommand(version, letter);
        assertEquals(expResult, result);

        version = 0.8;
        letter = null;
        expResult = GrblUtils.GCODE_PERFORM_HOMING_CYCLE_V8;
        result = GrblUtils.getHomingCommand(version, letter);
        assertEquals(expResult, result);

        version = 0.8;
        letter = 'c';
        expResult = GrblUtils.GCODE_PERFORM_HOMING_CYCLE_V8C;
        result = GrblUtils.getHomingCommand(version, letter);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetKillAlarmLockCommand() {
        System.out.println("getKillAlarmLockCommand");
        double version;
        Character letter;
        String result;
        String expResult;

        version = 0.7;
        letter = null;
        expResult = "";
        result = GrblUtils.getKillAlarmLockCommand(version, letter);
        assertEquals(expResult, result);

        version = 0.8;
        letter = null;
        expResult = "";
        result = GrblUtils.getKillAlarmLockCommand(version, letter);
        assertEquals(expResult, result);

        version = 0.8;
        letter = 'c';
        expResult = GrblUtils.GRBL_KILL_ALARM_LOCK_COMMAND;
        result = GrblUtils.getKillAlarmLockCommand(version, letter);
        assertEquals(expResult, result);

        version = 0.9;
        letter = null;
        expResult = GrblUtils.GRBL_KILL_ALARM_LOCK_COMMAND;
        result = GrblUtils.getKillAlarmLockCommand(version, letter);
        assertEquals(expResult, result);
    }

    @Test
    public void testToggleCheckModeCommand() {
        System.out.println("getToggleCheckModeCommand");
        double version;
        Character letter;
        String result;
        String expResult;

        version = 0.7;
        letter = null;
        expResult = "";
        result = GrblUtils.getToggleCheckModeCommand(version, letter);
        assertEquals(expResult, result);

        version = 0.8;
        letter = null;
        expResult = "";
        result = GrblUtils.getToggleCheckModeCommand(version, letter);
        assertEquals(expResult, result);

        version = 0.8;
        letter = 'c';
        expResult = GrblUtils.GRBL_TOGGLE_CHECK_MODE_COMMAND;
        result = GrblUtils.getToggleCheckModeCommand(version, letter);
        assertEquals(expResult, result);

        version = 0.9;
        letter = null;
        expResult = GrblUtils.GRBL_TOGGLE_CHECK_MODE_COMMAND;
        result = GrblUtils.getToggleCheckModeCommand(version, letter);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetViewParserStateCommand() {
        System.out.println("getViewParserStateCommand");
        double version;
        Character letter;
        String result;
        String expResult;

        version = 0.7;
        letter = null;
        expResult = "";
        result = GrblUtils.getViewParserStateCommand(version, letter);
        assertEquals(expResult, result);

        version = 0.8;
        letter = null;
        expResult = "";
        result = GrblUtils.getViewParserStateCommand(version, letter);
        assertEquals(expResult, result);

        version = 0.8;
        letter = 'c';
        expResult = GrblUtils.GRBL_VIEW_PARSER_STATE_COMMAND;
        result = GrblUtils.getViewParserStateCommand(version, letter);
        assertEquals(expResult, result);

        version = 0.9;
        letter = null;
        expResult = GrblUtils.GRBL_VIEW_PARSER_STATE_COMMAND;
        result = GrblUtils.getViewParserStateCommand(version, letter);
        assertEquals(expResult, result);
    }

    /**
     * Test of getGrblStatusCapabilities method, of class GrblUtils.
     */
    @Test
    public void testGetGrblStatusCapabilities() {
        System.out.println("getGrblStatusCapabilities");
        double version;
        Character letter;
        Capabilities result;

        version = 0.8;
        letter = 'c';
        result = GrblUtils.getGrblStatusCapabilities(version, letter);
        assertTrue(result.hasCapability(GrblCapabilitiesConstants.REAL_TIME));
        assertFalse(result.hasCapability(GrblCapabilitiesConstants.V1_FORMAT));
        assertFalse(result.hasCapability(GrblCapabilitiesConstants.HARDWARE_JOGGING));
        assertFalse(result.hasOverrides());
        assertFalse(result.hasContinuousJogging());

        version = 0.8;
        letter = 'a';
        result = GrblUtils.getGrblStatusCapabilities(version, letter);
        assertFalse(result.hasCapability(GrblCapabilitiesConstants.REAL_TIME));
        assertFalse(result.hasCapability(GrblCapabilitiesConstants.V1_FORMAT));
        assertFalse(result.hasCapability(GrblCapabilitiesConstants.HARDWARE_JOGGING));
        assertFalse(result.hasOverrides());
        assertFalse(result.hasContinuousJogging());

        version = 0.9;
        letter = null;
        result = GrblUtils.getGrblStatusCapabilities(version, letter);
        assertTrue(result.hasCapability(GrblCapabilitiesConstants.REAL_TIME));
        assertFalse(result.hasCapability(GrblCapabilitiesConstants.V1_FORMAT));
        assertFalse(result.hasCapability(GrblCapabilitiesConstants.HARDWARE_JOGGING));
        assertFalse(result.hasOverrides());
        assertFalse(result.hasContinuousJogging());

        version = 1.1;
        letter = null;
        result = GrblUtils.getGrblStatusCapabilities(version, letter);
        assertTrue(result.hasCapability(GrblCapabilitiesConstants.REAL_TIME));
        assertTrue(result.hasCapability(GrblCapabilitiesConstants.V1_FORMAT));
        assertTrue(result.hasCapability(GrblCapabilitiesConstants.HARDWARE_JOGGING));
        assertTrue(result.hasOverrides());
        assertTrue(result.hasContinuousJogging());
    }

    /**
     * Test of isGrblStatusString method, of class GrblUtils.
     */
    @Test
    public void testIsGrblStatusString() {
        System.out.println("isGrblStatusString");
        String response;
        Boolean expResult;
        Boolean result;

        response = "<position string is in angle brackets...>";
        expResult = true;
        result = GrblUtils.isGrblStatusString(response);
        assertEquals(expResult, result);

        response = "blah";
        expResult = false;
        result = GrblUtils.isGrblStatusString(response);
        assertEquals(expResult, result);
    }

    /**
     * Test of getStateFromStatusString method, of class GrblUtils.
     */
    @Test
    public void testGetStateFromStatusString() {
        System.out.println("getStateFromStatusString");
        String status;
        Capabilities version = new Capabilities();
        String expResult;
        String result;

        status = "<Idle,MPos:5.529,0.560,7.000,WPos:1.529,-5.440,-0.000>";
        version.addCapability(GrblCapabilitiesConstants.REAL_TIME);
        expResult = "Idle";
        result = GrblUtils.getStateFromStatusString(status, version);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetWorkPositionFromStatusStringWithEvenNumbers() {
        String status = "<Run,MPos:50.400,43.200,0.000,WPos:50000,42.600,0.000>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.REAL_TIME);
        Position position = GrblUtils.getWorkPositionFromStatusString(status, version, MM);

        Position expResult = new Position(50000, 42.6, 0, MM);
        assertEquals(expResult, position);
    }

    /**
     * Test of getMachinePositionFromStatusString method, of class GrblUtils.
     */
    @Test
    public void testGetMachinePositionFromStatusString() {
        System.out.println("getMachinePositionFromStatusString");
        String status = "<Idle,MPos:5.529,0.560,7.000,WPos:1.529,-5.440,-0.000>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.REAL_TIME);
        Position expResult = new Position(5.529, 0.560, 7.000, UnitUtils.Units.MM);
        Position result = GrblUtils.getMachinePositionFromStatusString(status, version, UnitUtils.Units.MM);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetMachinePositionFromStatusStringWithEvenNumbers() {
        String status = "<Run,MPos:5,43.200,1,WPos:50000,42.600,0.000>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.REAL_TIME);
        Position position = GrblUtils.getMachinePositionFromStatusString(status, version, MM);

        Position expResult = new Position(5, 43.2, 1, MM);
        assertEquals(expResult, position);
    }

    /**
     * Test of getWorkPositionFromStatusString method, of class GrblUtils.
     */
    @Test
    public void testGetWorkPositionFromStatusString() {
        System.out.println("getWorkPositionFromStatusString");
        String status = "<Idle,MPos:5.529,0.560,7.000,WPos:1.529,-5.440,-0.000>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.REAL_TIME);
        Position expResult = new Position(1.529, -5.440, -0.000, UnitUtils.Units.MM);
        Position result = GrblUtils.getWorkPositionFromStatusString(status, version, UnitUtils.Units.MM);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetResetCoordCommand() {
        System.out.println("getResetCoordCommand");

        double version = 0.8;
        Character letter = 'c';
        String result;

        result = GrblUtils.getResetCoordToZeroCommand(X, MM, version, letter);
        assertEquals("G92 X0", result);
        result = GrblUtils.getResetCoordToZeroCommand(Y, MM, version, letter);
        assertEquals("G92 Y0", result);
        result = GrblUtils.getResetCoordToZeroCommand(Z, MM, version, letter);
        assertEquals("G92 Z0", result);

        version = 0.9;

        result = GrblUtils.getResetCoordToZeroCommand(X, INCH, version, letter);
        assertEquals("G10 P0 L20 X0", result);
        result = GrblUtils.getResetCoordToZeroCommand(Y, INCH, version, letter);
        assertEquals("G10 P0 L20 Y0", result);
        result = GrblUtils.getResetCoordToZeroCommand(Z, INCH, version, letter);
        assertEquals("G10 P0 L20 Z0", result);
    }

    @Test
    public void okErrorAlarmTests() {
        assertTrue(GrblUtils.isOkResponse("ok"));
        assertFalse(GrblUtils.isOkResponse("not ok"));

        assertTrue(GrblUtils.isErrorResponse("error: some error"));
        assertFalse(GrblUtils.isErrorResponse("ok"));

        assertTrue(GrblUtils.isAlarmResponse("ALARM:1"));
        assertFalse(GrblUtils.isAlarmResponse("ok"));

        assertTrue(GrblUtils.isOkErrorAlarmResponse("ok"));
        assertTrue(GrblUtils.isOkErrorAlarmResponse("error: some error"));
        assertTrue(GrblUtils.isOkErrorAlarmResponse("ALARM:1"));
        assertFalse(GrblUtils.isOkErrorAlarmResponse("not ok"));
    }

    @Test
    public void isFeedbackString() {
        Capabilities capabilities = new Capabilities();

        capabilities.removeCapability(GrblCapabilitiesConstants.V1_FORMAT);
        assertTrue(GrblUtils.isGrblFeedbackMessage("[feedback]", capabilities));

        capabilities.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        assertFalse(GrblUtils.isGrblFeedbackMessage("[feedback]", capabilities));
        assertTrue(GrblUtils.isGrblFeedbackMessage("[GC:feedback]", capabilities));
    }

    @Test
    public void parseFeedbackString() {
        Capabilities capabilities = new Capabilities();

        capabilities.removeCapability(GrblCapabilitiesConstants.V1_FORMAT);
        assertThat(GrblUtils.parseFeedbackMessage("[feedback]", capabilities)).isEqualTo("feedback");

        capabilities.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        assertThat(GrblUtils.parseFeedbackMessage("[GC:feedback]", capabilities)).isEqualTo("feedback");
    }

    @Test
    public void getStatusFromStringVersion1WithCompleteStatusString() {
        String status = "<Idle|MPos:1.1,2.2,3.3|WPos:4.4,5.5,6.6|WCO:7.7,8.8,9.9|Ov:1,2,3|F:12345.6|FS:12345.7,65432.1|Pn:XYZPDHRS|A:SFMC>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        UnitUtils.Units unit = MM;

        ControllerStatus controllerStatus = GrblUtils.getStatusFromStatusString(null, status, version, unit);

        assertEquals(ControllerState.IDLE, controllerStatus.getState());

        assertEquals(new Position(1.1, 2.2, 3.3, MM), controllerStatus.getMachineCoord());
        assertEquals(new Position(4.4, 5.5, 6.6, MM), controllerStatus.getWorkCoord());
        assertEquals(new Position(7.7, 8.8, 9.9, MM), controllerStatus.getWorkCoordinateOffset());

        assertEquals(1, controllerStatus.getOverrides().feed);
        assertEquals(2, controllerStatus.getOverrides().rapid);
        assertEquals(3, controllerStatus.getOverrides().spindle);

        assertEquals(Double.valueOf(12345.7), controllerStatus.getFeedSpeed());
        assertEquals(Double.valueOf(65432.1), controllerStatus.getSpindleSpeed());

        assertTrue(controllerStatus.getEnabledPins().CycleStart);
        assertTrue(controllerStatus.getEnabledPins().Door);
        assertTrue(controllerStatus.getEnabledPins().Hold);
        assertTrue(controllerStatus.getEnabledPins().SoftReset);
        assertTrue(controllerStatus.getEnabledPins().Probe);
        assertTrue(controllerStatus.getEnabledPins().X);
        assertTrue(controllerStatus.getEnabledPins().Y);
        assertTrue(controllerStatus.getEnabledPins().Z);

        assertTrue(controllerStatus.getAccessoryStates().Flood);
        assertTrue(controllerStatus.getAccessoryStates().Mist);
        assertTrue(controllerStatus.getAccessoryStates().SpindleCCW);
        assertTrue(controllerStatus.getAccessoryStates().SpindleCW);
    }

    @Test
    public void getStatusFromStringVersion1WithoutWorkCoordinateOffsetStatusString() {
        String status = "<Idle|MPos:1.1,2.2,3.3|WPos:4.4,5.5,6.6|Ov:1,2,3|F:12345.6|FS:12345.7,65432.1|Pn:XYZPDHRS|A:SFMC>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        UnitUtils.Units unit = MM;

        ControllerStatus controllerStatus = GrblUtils.getStatusFromStatusString(null, status, version, unit);

        assertEquals(new Position(1.1, 2.2, 3.3, MM), controllerStatus.getMachineCoord());
        assertEquals(new Position(4.4, 5.5, 6.6, MM), controllerStatus.getWorkCoord());
        assertEquals(new Position(0, 0, 0, MM), controllerStatus.getWorkCoordinateOffset());
    }

    @Test
    public void getStatusFromStringVersion1WithoutWorkCoordinateStatusString() {
        String status = "<Idle|MPos:1.0,2.0,3.0|WCO:7.0,8.0,9.0|Ov:1,2,3|F:12345.6|FS:12345.7,65432.1|Pn:XYZPDHRS|A:SFMC>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        UnitUtils.Units unit = MM;

        ControllerStatus controllerStatus = GrblUtils.getStatusFromStatusString(null, status, version, unit);

        assertEquals(new Position(1, 2, 3, MM), controllerStatus.getMachineCoord());
        assertEquals(new Position(-6, -6, -6, MM), controllerStatus.getWorkCoord());
        assertEquals(new Position(7, 8, 9, MM), controllerStatus.getWorkCoordinateOffset());
    }

    @Test
    public void getStatusFromStringVersion1WithoutMachineCoordinateStatusString() {
        String status = "<Idle|WPos:4.0,5.0,6.0|WCO:7.0,8.0,9.0|Ov:1,2,3|F:12345.6|FS:12345.7,65432.1|Pn:XYZPDHRS|A:SFMC>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        UnitUtils.Units unit = MM;

        ControllerStatus controllerStatus = GrblUtils.getStatusFromStatusString(null, status, version, unit);

        assertEquals(new Position(11, 13, 15, MM), controllerStatus.getMachineCoord());
        assertEquals(new Position(4, 5, 6, MM), controllerStatus.getWorkCoord());
        assertEquals(new Position(7, 8, 9, MM), controllerStatus.getWorkCoordinateOffset());
    }

    @Test
    public void getStatusFromStringVersion1WhereFeedOverridesFeedSpindleStatusString() {
        String status = "<Idle|WPos:4.0,5.0,6.0|WCO:7.0,8.0,9.0|Ov:1,2,3|FS:12345.7,65432.1|F:12345.6|Pn:XYZPDHRS|A:SFMC>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        UnitUtils.Units unit = MM;

        ControllerStatus controllerStatus = GrblUtils.getStatusFromStatusString(null, status, version, unit);

        assertEquals(Double.valueOf(12345.6), controllerStatus.getFeedSpeed());
        assertEquals(Double.valueOf(65432.1), controllerStatus.getSpindleSpeed());
    }

    @Test
    public void getStatusFromStringVersion1WhereFeedRateIsGivenAsTwoValuesStatusString() {
        String status = "<Idle|WPos:4.0,5.0,6.0|WCO:7.0,8.0,9.0|Ov:1,2,3|F:12345.6,1000.0>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        UnitUtils.Units unit = MM;

        ControllerStatus controllerStatus = GrblUtils.getStatusFromStatusString(null, status, version, unit);

        assertEquals(Double.valueOf(0), controllerStatus.getFeedSpeed());
    }

    @Test
    public void getStatusFromStringVersion1WhereFeedRateIsGivenAsThreeValuesStatusString() {
        String status = "<Idle|WPos:4.0,5.0,6.0|WCO:7.0,8.0,9.0|Ov:1,2,3|F:12345.6,1000.0,2000.0>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        UnitUtils.Units unit = MM;

        ControllerStatus controllerStatus = GrblUtils.getStatusFromStatusString(null, status, version, unit);

        assertEquals(Double.valueOf(12345.6), controllerStatus.getFeedSpeed());
    }

    @Test
    public void getStatusFromStringVersion1WithoutPinsStatusString() {
        String status = "<Idle|WPos:4.0,5.0,6.0|WCO:7.0,8.0,9.0|Ov:1,2,3|FS:12345.7,65432.1|F:12345.6|A:SFMC>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        UnitUtils.Units unit = MM;

        ControllerStatus controllerStatus = GrblUtils.getStatusFromStatusString(null, status, version, unit);

        assertFalse(controllerStatus.getEnabledPins().CycleStart);
        assertFalse(controllerStatus.getEnabledPins().Door);
        assertFalse(controllerStatus.getEnabledPins().Hold);
        assertFalse(controllerStatus.getEnabledPins().SoftReset);
        assertFalse(controllerStatus.getEnabledPins().Probe);
        assertFalse(controllerStatus.getEnabledPins().X);
        assertFalse(controllerStatus.getEnabledPins().Y);
        assertFalse(controllerStatus.getEnabledPins().Z);
    }

    @Test
    public void getStatusFromStringVersion1WithoutAccessoryStatusString() {
        String status = "<Idle|WPos:4.0,5.0,6.0|WCO:7.0,8.0,9.0|Ov:1,2,3|FS:12345.7,65432.1|F:12345.6>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        UnitUtils.Units unit = MM;

        ControllerStatus controllerStatus = GrblUtils.getStatusFromStatusString(null, status, version, unit);

        assertFalse(controllerStatus.getAccessoryStates().Flood);
        assertFalse(controllerStatus.getAccessoryStates().Mist);
        assertFalse(controllerStatus.getAccessoryStates().SpindleCCW);
        assertFalse(controllerStatus.getAccessoryStates().SpindleCW);
    }

    @Test
    public void get6AxesCoordinates() {
        String status = "<Idle|MPos:1.1,2.2,3.3,4.4,5.5,6.6|WPos:7.7,8.8,9.9,10.10,11.11,12.12|WCO:13.13,14.14,15.15,16.16,17.17,18.18|Ov:1,2,3|F:12345.6|FS:12345.7,65432.1|Pn:XYZABCPDHRS|A:SFMC>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        UnitUtils.Units unit = MM;

        ControllerStatus controllerStatus = GrblUtils.getStatusFromStatusString(null, status, version, unit);

        assertEquals(ControllerState.IDLE, controllerStatus.getState());

        assertEquals(new Position(1.1, 2.2, 3.3, 4.4, 5.5, 6.6, MM), controllerStatus.getMachineCoord());
        assertEquals(new Position(7.7, 8.8, 9.9, 10.10, 11.11, 12.12, MM), controllerStatus.getWorkCoord());
        assertEquals(new Position(13.13, 14.14, 15.15, 16.16, 17.17, 18.18, MM), controllerStatus.getWorkCoordinateOffset());

        assertTrue(controllerStatus.getEnabledPins().A);
        assertTrue(controllerStatus.getEnabledPins().B);
        assertTrue(controllerStatus.getEnabledPins().C);
    }

    @Test
    public void get5AxesCoordinates() {
        String status = "<Idle|MPos:1.1,2.2,3.3,4.4,5.5|WPos:7.7,8.8,9.9,10.10,11.11|WCO:13.13,14.14,15.15,16.16,17.17|Ov:1,2,3|F:12345.6|FS:12345.7,65432.1|Pn:XYZABPDHRS|A:SFMC>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        UnitUtils.Units unit = MM;

        ControllerStatus controllerStatus = GrblUtils.getStatusFromStatusString(null, status, version, unit);

        assertEquals(ControllerState.IDLE, controllerStatus.getState());

        assertEquals(new Position(1.1, 2.2, 3.3, 4.4, 5.5, 0.0, MM), controllerStatus.getMachineCoord());
        assertEquals(new Position(7.7, 8.8, 9.9, 10.10, 11.11, 0.0, MM), controllerStatus.getWorkCoord());
        assertEquals(new Position(13.13, 14.14, 15.15, 16.16, 17.17, 0.0, MM), controllerStatus.getWorkCoordinateOffset());
        assertTrue(controllerStatus.getEnabledPins().A);
        assertTrue(controllerStatus.getEnabledPins().B);
        assertFalse(controllerStatus.getEnabledPins().C);
    }

    @Test
    public void get4AxesCoordinates() {
        String status = "<Idle|MPos:1.1,2.2,3.3,4.4|WPos:7.7,8.8,9.9,10.10|WCO:13.13,14.14,15.15,16.16|Ov:1,2,3|F:12345.6|FS:12345.7,65432.1|Pn:XYZAPDHRS|A:SFMC>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        UnitUtils.Units unit = MM;

        ControllerStatus controllerStatus = GrblUtils.getStatusFromStatusString(null, status, version, unit);

        assertEquals(ControllerState.IDLE, controllerStatus.getState());

        assertEquals(new Position(1.1, 2.2, 3.3, 4.4, 0.0, 0.0, MM), controllerStatus.getMachineCoord());
        assertEquals(new Position(7.7, 8.8, 9.9, 10.10, 0.0, 0.0, MM), controllerStatus.getWorkCoord());
        assertEquals(new Position(13.13, 14.14, 15.15, 16.16, 0.0, 0.0, MM), controllerStatus.getWorkCoordinateOffset());

        assertTrue(controllerStatus.getEnabledPins().A);
        assertFalse(controllerStatus.getEnabledPins().B);
        assertFalse(controllerStatus.getEnabledPins().C);
    }

    @Test
    public void parseProbePosition() {
        String ThreeAxisFail = "[PRB:0.000,0.000,0.000:0]";
        assertEquals(null, GrblUtils.parseProbePosition(ThreeAxisFail, MM));
        String FourAxisFail = "[PRB:0.000,0.000,0.000,0.000:0]";
        assertEquals(null, GrblUtils.parseProbePosition(FourAxisFail, MM));
        String FiveAxisFail = "[PRB:0.000,0.000,0.000,0.000,0.000:0]";
        assertEquals(null, GrblUtils.parseProbePosition(FiveAxisFail, MM));
        String SixAxisFail = "[PRB:0.000,0.000,0.000,0.000,0.000,0.000:0]";
        assertEquals(null, GrblUtils.parseProbePosition(SixAxisFail, MM));

        String ThreeAxis = "[PRB:1.1,2.2,3.3:1]";
        assertEquals(new Position(1.1, 2.2, 3.3, MM), GrblUtils.parseProbePosition(ThreeAxis, MM));
        String FourAxis = "[PRB:1.1,2.2,3.3,4.4:1]";
        assertEquals(new Position(1.1, 2.2, 3.3, 4.4, 0.0, 0.0, MM), GrblUtils.parseProbePosition(FourAxis, MM));
        String FiveAxis = "[PRB:1.1,2.2,3.3,4.4,5.5:1]";
        assertEquals(new Position(1.1, 2.2, 3.3, 4.4, 5.5, 0.0, MM), GrblUtils.parseProbePosition(FiveAxis, MM));
        String SixAxis = "[PRB:1.1,2.2,3.3,4.4,5.5,6.6:1]";
        assertEquals(new Position(1.1, 2.2, 3.3, 4.4, 5.5, 6.6, MM), GrblUtils.parseProbePosition(SixAxis, MM));
    }
}
