/*
    Copyright 2013-2018 Will Winder

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

import java.util.ArrayList;

import static com.willwinder.universalgcodesender.model.Axis.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

/**
 *
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
        assertThat(result).isEqualTo(expResult);

        response = "blah 0.8c";
        expResult = false;
        result = GrblUtils.isGrblVersionString(response);
        assertThat(result).isEqualTo(expResult);
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
        assertThat(result).isCloseTo(expResult, offset(0.0));
        
        
        response = "CarbideMotion 0.9g";
        expResult = 0.9;
        result = GrblUtils.getVersionDouble(response);
        assertThat(result).isCloseTo(expResult, offset(0.0));

        
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
        assertThat(result).isEqualTo(expResult);
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
        assertThat(result).isEqualTo(expResult);
        
        version = 0.8;
        letter = null;
        expResult = GrblUtils.GCODE_PERFORM_HOMING_CYCLE_V8;
        result = GrblUtils.getHomingCommand(version, letter);
        assertThat(result).isEqualTo(expResult);

        version = 0.8;
        letter = 'c';
        expResult = GrblUtils.GCODE_PERFORM_HOMING_CYCLE_V8C;
        result = GrblUtils.getHomingCommand(version, letter);
        assertThat(result).isEqualTo(expResult);
    }
    
    @Test
    public void testGetReturnToHomeCommand() {
        System.out.println("getReturnToHomeCommands");
        double version;
        Character letter;
        ArrayList<String> result;
        String expResult;
        
        version = 0.8;
        letter = null;
        expResult = GrblUtils.GCODE_RETURN_TO_ZERO_LOCATION_V8;
        String expResult2 = GrblUtils.GCODE_RETURN_TO_ZERO_LOCATION_Z0_V8;
        result = GrblUtils.getReturnToHomeCommands(version, letter, 0);
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0)).isEqualTo(expResult);
        assertThat(result.get(1)).isEqualTo(expResult2);

        // Check the z-raise command is sent first
        version = 0.8;
        letter = 'c';
        expResult = GrblUtils.GCODE_RETURN_TO_ZERO_LOCATION_V8;
        expResult2 = GrblUtils.GCODE_RETURN_TO_ZERO_LOCATION_Z0_V8;
        result = GrblUtils.getReturnToHomeCommands(version, letter, -10);
        assertThat(result.size()).isEqualTo(3);
        assertThat(result.get(0)).isEqualTo(expResult2);
        assertThat(result.get(1)).isEqualTo(expResult);
        assertThat(result.get(2)).isEqualTo(expResult2);
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
        assertThat(result).isEqualTo(expResult);
        
        version = 0.8;
        letter = null;
        expResult = "";
        result = GrblUtils.getKillAlarmLockCommand(version, letter);
        assertThat(result).isEqualTo(expResult);

        version = 0.8;
        letter = 'c';
        expResult = GrblUtils.GRBL_KILL_ALARM_LOCK_COMMAND;
        result = GrblUtils.getKillAlarmLockCommand(version, letter);
        assertThat(result).isEqualTo(expResult);

        version = 0.9;
        letter = null;
        expResult = GrblUtils.GRBL_KILL_ALARM_LOCK_COMMAND;
        result = GrblUtils.getKillAlarmLockCommand(version, letter);
        assertThat(result).isEqualTo(expResult);
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
        assertThat(result).isEqualTo(expResult);
        
        version = 0.8;
        letter = null;
        expResult = "";
        result = GrblUtils.getToggleCheckModeCommand(version, letter);
        assertThat(result).isEqualTo(expResult);

        version = 0.8;
        letter = 'c';
        expResult = GrblUtils.GRBL_TOGGLE_CHECK_MODE_COMMAND;
        result = GrblUtils.getToggleCheckModeCommand(version, letter);
        assertThat(result).isEqualTo(expResult);

        version = 0.9;
        letter = null;
        expResult = GrblUtils.GRBL_TOGGLE_CHECK_MODE_COMMAND;
        result = GrblUtils.getToggleCheckModeCommand(version, letter);
        assertThat(result).isEqualTo(expResult);
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
        assertThat(result).isEqualTo(expResult);
        
        version = 0.8;
        letter = null;
        expResult = "";
        result = GrblUtils.getViewParserStateCommand(version, letter);
        assertThat(result).isEqualTo(expResult);

        version = 0.8;
        letter = 'c';
        expResult = GrblUtils.GRBL_VIEW_PARSER_STATE_COMMAND;
        result = GrblUtils.getViewParserStateCommand(version, letter);
        assertThat(result).isEqualTo(expResult);

        version = 0.9;
        letter = null;
        expResult = GrblUtils.GRBL_VIEW_PARSER_STATE_COMMAND;
        result = GrblUtils.getViewParserStateCommand(version, letter);
        assertThat(result).isEqualTo(expResult);
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
        assertThat(result.hasCapability(GrblCapabilitiesConstants.REAL_TIME)).isTrue();
        assertThat(result.hasCapability(GrblCapabilitiesConstants.V1_FORMAT)).isFalse();
        assertThat(result.hasCapability(GrblCapabilitiesConstants.HARDWARE_JOGGING)).isFalse();
        assertThat(result.hasOverrides()).isFalse();
        assertThat(result.hasContinuousJogging()).isFalse();

        version = 0.8;
        letter = 'a';
        result = GrblUtils.getGrblStatusCapabilities(version, letter);
        assertThat(result.hasCapability(GrblCapabilitiesConstants.REAL_TIME)).isFalse();
        assertThat(result.hasCapability(GrblCapabilitiesConstants.V1_FORMAT)).isFalse();
        assertThat(result.hasCapability(GrblCapabilitiesConstants.HARDWARE_JOGGING)).isFalse();
        assertThat(result.hasOverrides()).isFalse();
        assertThat(result.hasContinuousJogging()).isFalse();

        version = 0.9;
        letter = null;
        result = GrblUtils.getGrblStatusCapabilities(version, letter);
        assertThat(result.hasCapability(GrblCapabilitiesConstants.REAL_TIME)).isTrue();
        assertThat(result.hasCapability(GrblCapabilitiesConstants.V1_FORMAT)).isFalse();
        assertThat(result.hasCapability(GrblCapabilitiesConstants.HARDWARE_JOGGING)).isFalse();
        assertThat(result.hasOverrides()).isFalse();
        assertThat(result.hasContinuousJogging()).isFalse();

        version = 1.1;
        letter = null;
        result = GrblUtils.getGrblStatusCapabilities(version, letter);
        assertThat(result.hasCapability(GrblCapabilitiesConstants.REAL_TIME)).isTrue();
        assertThat(result.hasCapability(GrblCapabilitiesConstants.V1_FORMAT)).isTrue();
        assertThat(result.hasCapability(GrblCapabilitiesConstants.HARDWARE_JOGGING)).isTrue();
        assertThat(result.hasOverrides()).isTrue();
        assertThat(result.hasContinuousJogging()).isTrue();
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
        assertThat(result).isEqualTo(expResult);
        
        response = "blah";
        expResult = false;
        result = GrblUtils.isGrblStatusString(response);
        assertThat(result).isEqualTo(expResult);
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
        assertThat(result).isEqualTo(expResult);
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
        Position expResult = new Position(5.529, 0.560, 7.000, UnitUtils.Units.UNKNOWN);
        Position result = GrblUtils.getMachinePositionFromStatusString(status, version, UnitUtils.Units.UNKNOWN);
        assertThat(result).isEqualTo(expResult);
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
        Position expResult = new Position(1.529, -5.440, -0.000, UnitUtils.Units.UNKNOWN);
        Position result = GrblUtils.getWorkPositionFromStatusString(status, version, UnitUtils.Units.UNKNOWN);
        assertThat(result).isEqualTo(expResult);
    }
    
    @Test
    public void testGetResetCoordCommand() {
        System.out.println("getResetCoordCommand");

        double version = 0.8;
        Character letter = 'c';
        String result;
        
        result = GrblUtils.getResetCoordToZeroCommand(X, version, letter);
        assertThat(result).isEqualTo("G92 X0");
        result = GrblUtils.getResetCoordToZeroCommand(Y, version, letter);
        assertThat(result).isEqualTo("G92 Y0");
        result = GrblUtils.getResetCoordToZeroCommand(Z, version, letter);
        assertThat(result).isEqualTo("G92 Z0");
        
        version = 0.9;
        
        result = GrblUtils.getResetCoordToZeroCommand(X, version, letter);
        assertThat(result).isEqualTo("G10 P0 L20 X0");
        result = GrblUtils.getResetCoordToZeroCommand(Y, version, letter);
        assertThat(result).isEqualTo("G10 P0 L20 Y0");
        result = GrblUtils.getResetCoordToZeroCommand(Z, version, letter);
        assertThat(result).isEqualTo("G10 P0 L20 Z0");
    }

    @Test
    public void okErrorAlarmTests() {
        assertThat(GrblUtils.isOkResponse("ok")).isTrue();
        assertThat(GrblUtils.isOkResponse("not ok")).isFalse();
        
        assertThat(GrblUtils.isErrorResponse("error: some error")).isTrue();
        assertThat(GrblUtils.isErrorResponse("ok")).isFalse();

        assertThat(GrblUtils.isAlarmResponse("ALARM:1")).isTrue();
        assertThat(GrblUtils.isAlarmResponse("ok")).isFalse();

        assertThat(GrblUtils.isOkErrorAlarmResponse("ok")).isTrue();
        assertThat(GrblUtils.isOkErrorAlarmResponse("error: some error")).isTrue();
        assertThat(GrblUtils.isOkErrorAlarmResponse("ALARM:1")).isTrue();
        assertThat(GrblUtils.isOkErrorAlarmResponse("not ok")).isFalse();
    }

    @Test
    public void isFeedbackString() {
        Capabilities capabilities = new Capabilities();

        capabilities.removeCapability(GrblCapabilitiesConstants.V1_FORMAT);
        assertThat(GrblUtils.isGrblFeedbackMessage("[feedback]", capabilities)).isTrue();

        capabilities.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        assertThat(GrblUtils.isGrblFeedbackMessage("[feedback]", capabilities)).isFalse();
        assertThat(GrblUtils.isGrblFeedbackMessage("[GC:feedback]", capabilities)).isTrue();
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
        UnitUtils.Units unit = UnitUtils.Units.MM;

        ControllerStatus controllerStatus = GrblUtils.getStatusFromStatusString(null, status, version, unit);

        assertThat(controllerStatus.getStateString()).isEqualTo("Idle");
        assertThat(controllerStatus.getState()).isEqualTo(ControllerState.IDLE);

        assertThat(controllerStatus.getMachineCoord()).isEqualTo(new Position(1.1, 2.2, 3.3, UnitUtils.Units.MM));
        assertThat(controllerStatus.getWorkCoord()).isEqualTo(new Position(4.4, 5.5, 6.6, UnitUtils.Units.MM));
        assertThat(controllerStatus.getWorkCoordinateOffset()).isEqualTo(new Position(7.7, 8.8, 9.9, UnitUtils.Units.MM));

        assertThat(controllerStatus.getOverrides().feed).isEqualTo(1);
        assertThat(controllerStatus.getOverrides().rapid).isEqualTo(2);
        assertThat(controllerStatus.getOverrides().spindle).isEqualTo(3);

        assertThat(controllerStatus.getFeedSpeed()).isEqualTo(Double.valueOf(12345.7));
        assertThat(controllerStatus.getSpindleSpeed()).isEqualTo(Double.valueOf(65432.1));

        assertThat(controllerStatus.getEnabledPins().CycleStart).isTrue();
        assertThat(controllerStatus.getEnabledPins().Door).isTrue();
        assertThat(controllerStatus.getEnabledPins().Hold).isTrue();
        assertThat(controllerStatus.getEnabledPins().SoftReset).isTrue();
        assertThat(controllerStatus.getEnabledPins().Probe).isTrue();
        assertThat(controllerStatus.getEnabledPins().X).isTrue();
        assertThat(controllerStatus.getEnabledPins().Y).isTrue();
        assertThat(controllerStatus.getEnabledPins().Z).isTrue();

        assertThat(controllerStatus.getAccessoryStates().Flood).isTrue();
        assertThat(controllerStatus.getAccessoryStates().Mist).isTrue();
        assertThat(controllerStatus.getAccessoryStates().SpindleCCW).isTrue();
        assertThat(controllerStatus.getAccessoryStates().SpindleCW).isTrue();
    }

    @Test
    public void getStatusFromStringVersion1WithoutWorkCoordinateOffsetStatusString() {
        String status = "<Idle|MPos:1.1,2.2,3.3|WPos:4.4,5.5,6.6|Ov:1,2,3|F:12345.6|FS:12345.7,65432.1|Pn:XYZPDHRS|A:SFMC>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        UnitUtils.Units unit = UnitUtils.Units.MM;

        ControllerStatus controllerStatus = GrblUtils.getStatusFromStatusString(null, status, version, unit);

        assertThat(controllerStatus.getMachineCoord()).isEqualTo(new Position(1.1, 2.2, 3.3, UnitUtils.Units.MM));
        assertThat(controllerStatus.getWorkCoord()).isEqualTo(new Position(4.4, 5.5, 6.6, UnitUtils.Units.MM));
        assertThat(controllerStatus.getWorkCoordinateOffset()).isEqualTo(new Position(0, 0, 0, UnitUtils.Units.MM));
    }

    @Test
    public void getStatusFromStringVersion1WithoutWorkCoordinateStatusString() {
        String status = "<Idle|MPos:1.0,2.0,3.0|WCO:7.0,8.0,9.0|Ov:1,2,3|F:12345.6|FS:12345.7,65432.1|Pn:XYZPDHRS|A:SFMC>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        UnitUtils.Units unit = UnitUtils.Units.MM;

        ControllerStatus controllerStatus = GrblUtils.getStatusFromStatusString(null, status, version, unit);

        assertThat(controllerStatus.getMachineCoord()).isEqualTo(new Position(1, 2, 3, UnitUtils.Units.MM));
        assertThat(controllerStatus.getWorkCoord()).isEqualTo(new Position(-6, -6, -6, UnitUtils.Units.MM));
        assertThat(controllerStatus.getWorkCoordinateOffset()).isEqualTo(new Position(7, 8, 9, UnitUtils.Units.MM));
    }

    @Test
    public void getStatusFromStringVersion1WithoutMachineCoordinateStatusString() {
        String status = "<Idle|WPos:4.0,5.0,6.0|WCO:7.0,8.0,9.0|Ov:1,2,3|F:12345.6|FS:12345.7,65432.1|Pn:XYZPDHRS|A:SFMC>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        UnitUtils.Units unit = UnitUtils.Units.MM;

        ControllerStatus controllerStatus = GrblUtils.getStatusFromStatusString(null, status, version, unit);

        assertThat(controllerStatus.getMachineCoord()).isEqualTo(new Position(11, 13, 15, UnitUtils.Units.MM));
        assertThat(controllerStatus.getWorkCoord()).isEqualTo(new Position(4, 5, 6, UnitUtils.Units.MM));
        assertThat(controllerStatus.getWorkCoordinateOffset()).isEqualTo(new Position(7, 8, 9, UnitUtils.Units.MM));
    }

    @Test
    public void getStatusFromStringVersion1WhereFeedOverridesFeedSpindleStatusString() {
        String status = "<Idle|WPos:4.0,5.0,6.0|WCO:7.0,8.0,9.0|Ov:1,2,3|FS:12345.7,65432.1|F:12345.6|Pn:XYZPDHRS|A:SFMC>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        UnitUtils.Units unit = UnitUtils.Units.MM;

        ControllerStatus controllerStatus = GrblUtils.getStatusFromStatusString(null, status, version, unit);

        assertThat(controllerStatus.getFeedSpeed()).isEqualTo(Double.valueOf(12345.6));
        assertThat(controllerStatus.getSpindleSpeed()).isEqualTo(Double.valueOf(65432.1));
    }

    @Test
    public void getStatusFromStringVersion1WithoutPinsStatusString() {
        String status = "<Idle|WPos:4.0,5.0,6.0|WCO:7.0,8.0,9.0|Ov:1,2,3|FS:12345.7,65432.1|F:12345.6|A:SFMC>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        UnitUtils.Units unit = UnitUtils.Units.MM;

        ControllerStatus controllerStatus = GrblUtils.getStatusFromStatusString(null, status, version, unit);

        assertThat(controllerStatus.getEnabledPins().CycleStart).isFalse();
        assertThat(controllerStatus.getEnabledPins().Door).isFalse();
        assertThat(controllerStatus.getEnabledPins().Hold).isFalse();
        assertThat(controllerStatus.getEnabledPins().SoftReset).isFalse();
        assertThat(controllerStatus.getEnabledPins().Probe).isFalse();
        assertThat(controllerStatus.getEnabledPins().X).isFalse();
        assertThat(controllerStatus.getEnabledPins().Y).isFalse();
        assertThat(controllerStatus.getEnabledPins().Z).isFalse();
    }

    @Test
    public void getStatusFromStringVersion1WithoutAccessoryStatusString() {
        String status = "<Idle|WPos:4.0,5.0,6.0|WCO:7.0,8.0,9.0|Ov:1,2,3|FS:12345.7,65432.1|F:12345.6>";
        Capabilities version = new Capabilities();
        version.addCapability(GrblCapabilitiesConstants.V1_FORMAT);
        UnitUtils.Units unit = UnitUtils.Units.MM;

        ControllerStatus controllerStatus = GrblUtils.getStatusFromStatusString(null, status, version, unit);

        assertThat(controllerStatus.getAccessoryStates().Flood).isFalse();
        assertThat(controllerStatus.getAccessoryStates().Mist).isFalse();
        assertThat(controllerStatus.getAccessoryStates().SpindleCCW).isFalse();
        assertThat(controllerStatus.getAccessoryStates().SpindleCW).isFalse();
    }
}
