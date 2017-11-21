/*
    Copyright 2013-2017 Will Winder

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

import com.willwinder.universalgcodesender.GrblUtils.Capabilities;
import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.*;

import com.willwinder.universalgcodesender.model.*;
import com.willwinder.universalgcodesender.model.UnitUtils;
import java.util.ArrayList;
import org.junit.Test;

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
        assertEquals(2, result.size());
        assertEquals(expResult, result.get(0));
        assertEquals(expResult2, result.get(1));

        // Check the z-raise command is sent first
        version = 0.8;
        letter = 'c';
        expResult = GrblUtils.GCODE_RETURN_TO_ZERO_LOCATION_V8;
        expResult2 = GrblUtils.GCODE_RETURN_TO_ZERO_LOCATION_Z0_V8;
        result = GrblUtils.getReturnToHomeCommands(version, letter, -10);
        assertEquals(3, result.size());
        assertEquals(expResult2, result.get(0));
        assertEquals(expResult, result.get(1));
        assertEquals(expResult2, result.get(2));
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
        Capabilities expResult = new Capabilities();
        Capabilities result;

        version = 0.8;
        letter = 'c';
        result = GrblUtils.getGrblStatusCapabilities(version, letter);
        assertEquals(true, result.REAL_TIME);
        assertEquals(false, result.OVERRIDES);
        
        version = 0.8;
        letter = 'a';
        result = GrblUtils.getGrblStatusCapabilities(version, letter);
        assertEquals(false, result.REAL_TIME);
        assertEquals(false, result.OVERRIDES);
        
        version = 0.9;
        letter = null;
        result = GrblUtils.getGrblStatusCapabilities(version, letter);
        assertEquals(true, result.REAL_TIME);
        assertEquals(false, result.OVERRIDES);

        version = 1.1;
        letter = null;
        result = GrblUtils.getGrblStatusCapabilities(version, letter);
        assertEquals(true, result.REAL_TIME);
        assertEquals(true, result.OVERRIDES);
        assertEquals(true, result.V1_FORMAT);
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
        version.REAL_TIME = true;
        expResult = "Idle";
        result = GrblUtils.getStateFromStatusString(status, version);
        assertEquals(expResult, result);
    }

    /**
     * Test of getMachinePositionFromStatusString method, of class GrblUtils.
     */
    @Test
    public void testGetMachinePositionFromStatusString() {
        System.out.println("getMachinePositionFromStatusString");
        String status = "<Idle,MPos:5.529,0.560,7.000,WPos:1.529,-5.440,-0.000>";
        Capabilities version = new Capabilities();
        version.REAL_TIME = true;
        Position expResult = new Position(5.529, 0.560, 7.000, UnitUtils.Units.UNKNOWN);
        Position result = GrblUtils.getMachinePositionFromStatusString(status, version, UnitUtils.Units.UNKNOWN);
        assertEquals(expResult, result);
    }

    /**
     * Test of getWorkPositionFromStatusString method, of class GrblUtils.
     */
    @Test
    public void testGetWorkPositionFromStatusString() {
        System.out.println("getWorkPositionFromStatusString");
        String status = "<Idle,MPos:5.529,0.560,7.000,WPos:1.529,-5.440,-0.000>";
        Capabilities version = new Capabilities();
        version.REAL_TIME = true;
        Position expResult = new Position(1.529, -5.440, -0.000, UnitUtils.Units.UNKNOWN);
        Position result = GrblUtils.getWorkPositionFromStatusString(status, version, UnitUtils.Units.UNKNOWN);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testGetResetCoordCommand() {
        System.out.println("getResetCoordCommand");

        double version = 0.8;
        Character letter = 'c';
        String result;
        
        result = GrblUtils.getResetCoordToZeroCommand('X', version, letter);
        assertEquals("G92 X0", result);
        result = GrblUtils.getResetCoordToZeroCommand('Y', version, letter);
        assertEquals("G92 Y0", result);
        result = GrblUtils.getResetCoordToZeroCommand('Z', version, letter);
        assertEquals("G92 Z0", result);
        
        version = 0.9;
        
        result = GrblUtils.getResetCoordToZeroCommand('X', version, letter);
        assertEquals("G10 P0 L20 X0", result);
        result = GrblUtils.getResetCoordToZeroCommand('Y', version, letter);
        assertEquals("G10 P0 L20 Y0", result);
        result = GrblUtils.getResetCoordToZeroCommand('Z', version, letter);
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

        capabilities.V1_FORMAT = false;
        assertTrue(GrblUtils.isGrblFeedbackMessage("[feedback]", capabilities));

        capabilities.V1_FORMAT = true;
        assertFalse(GrblUtils.isGrblFeedbackMessage("[feedback]", capabilities));
        assertTrue(GrblUtils.isGrblFeedbackMessage("[GC:feedback]", capabilities));
    }

    @Test
    public void parseFeedbackString() {
        Capabilities capabilities = new Capabilities();

        capabilities.V1_FORMAT = false;
        assertThat(GrblUtils.parseFeedbackMessage("[feedback]", capabilities)).isEqualTo("feedback");

        capabilities.V1_FORMAT = true;
        assertThat(GrblUtils.parseFeedbackMessage("[GC:feedback]", capabilities)).isEqualTo("feedback");
    }

}
