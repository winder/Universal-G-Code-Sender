/*
    Copywrite 2013 Will Winder

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
import javax.vecmath.Point3d;
import static org.junit.Assert.*;

import com.willwinder.universalgcodesender.model.*;
import com.willwinder.universalgcodesender.model.Utils;
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
        String expResult = "c";
        String result = GrblUtils.getVersionLetter(response);
        assertEquals(expResult, result);
    }

    /**
     * Test of isRealTimeCapable method, of class GrblUtils.
     */
    @Test
    public void testIsRealTimeCapable() {
        System.out.println("isRealTimeCapable");
        double version;
        Boolean expResult;
        Boolean result;

        version = 0.8;
        expResult = true;
        result = GrblUtils.isRealTimeCapable(version);
        assertEquals(expResult, result);

        version = 0.7;
        expResult = false;
        result = GrblUtils.isRealTimeCapable(version);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testGetHomingCommand() {
        System.out.println("getHomingCommand");
        double version;
        String letter;
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
        letter = "c";
        expResult = GrblUtils.GCODE_PERFORM_HOMING_CYCLE_V8C;
        result = GrblUtils.getHomingCommand(version, letter);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testGetReturnToHomeCommand() {
        System.out.println("getReturnToHomeCommand");
        double version;
        String letter;
        String result;
        String expResult;
        
        version = 0.7;
        letter = null;
        expResult = "";
        result = GrblUtils.getReturnToHomeCommand(version, letter);
        assertEquals(expResult, result);
        
        version = 0.8;
        letter = null;
        expResult = GrblUtils.GCODE_RETURN_TO_ZERO_LOCATION_V8;
        result = GrblUtils.getReturnToHomeCommand(version, letter);
        assertEquals(expResult, result);

        version = 0.8;
        letter = "c";
        expResult = GrblUtils.GCODE_RETURN_TO_ZERO_LOCATION_V8C;
        result = GrblUtils.getReturnToHomeCommand(version, letter);
        assertEquals(expResult, result);
    }
        
    @Test
    public void testGetKillAlarmLockCommand() {
        System.out.println("getKillAlarmLockCommand");
        double version;
        String letter;
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
        letter = "c";
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
        String letter;
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
        letter = "c";
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
        String letter;
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
        letter = "c";
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
        String letter;
        Capabilities expResult = new Capabilities();
        Capabilities result;

        version = 0.8;
        letter = "c";
        result = GrblUtils.getGrblStatusCapabilities(version, letter);
        assertEquals(true, result.REAL_TIME);
        assertEquals(false, result.OVERRIDES);
        
        version = 0.8;
        letter = "a";
        result = GrblUtils.getGrblStatusCapabilities(version, letter);
        assertEquals(false, result.REAL_TIME);
        assertEquals(false, result.OVERRIDES);
        
        version = 0.9;
        letter = null;
        result = GrblUtils.getGrblStatusCapabilities(version, letter);
        assertEquals(true, result.REAL_TIME);
        assertEquals(false, result.OVERRIDES);

        version = 1.0;
        letter = null;
        result = GrblUtils.getGrblStatusCapabilities(version, letter);
        assertEquals(true, result.REAL_TIME);
        assertEquals(true, result.OVERRIDES);
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
        Position expResult = new Position(5.529, 0.560, 7.000, Utils.Units.UNKNOWN);
        Position result = GrblUtils.getMachinePositionFromStatusString(status, version, Utils.Units.UNKNOWN);
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
        Position expResult = new Position(1.529, -5.440, -0.000, Utils.Units.UNKNOWN);
        Position result = GrblUtils.getWorkPositionFromStatusString(status, version, Utils.Units.UNKNOWN);
        assertEquals(expResult, result);
    }
    
    @Test
    public void testGetResetCoordCommand() {
        System.out.println("getResetCoordCommand");

        double version = 0.8;
        String letter = "c";
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
}
