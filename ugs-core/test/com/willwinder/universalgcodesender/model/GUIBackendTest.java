/*
    Copywrite 2016 Will Winder

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
package com.willwinder.universalgcodesender.model;

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.model.UGSEvent.ControlState;
import com.willwinder.universalgcodesender.pendantui.SystemStateBean;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.Settings;
import java.io.File;
import java.lang.reflect.Field;
import javax.vecmath.Point3d;
import static org.easymock.EasyMock.createMock;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author wwinder
 */
@Ignore
public class GUIBackendTest {
    
    public GUIBackendTest() {
    }
    
    private static IController mockController = createMock(IController.class);
    private GUIBackend instance;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        instance = new GUIBackend();

        // Initialize private variable.
        Field f = GUIBackend.class.getDeclaredField("controller");
        f.setAccessible(true);
        f.set(instance, mockController);
    }

    ////////////////////////////////////////
    // Test helpers
    ////////////////////////////////////////

    /**
     * Test of preprocessAndExportToFile method, of class GUIBackend.
     */
    @Test
    public void testPreprocessAndExportToFile() throws Exception {
        System.out.println("preprocessAndExportToFile");
        File f = null;
        GUIBackend instance = new GUIBackend();
        instance.preprocessAndExportToFile(f);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of connect method, of class GUIBackend.
     */
    @Test
    public void testConnect() throws Exception {
        System.out.println("connect");
        System.out.println("-- cannot test connect because of static call --");
    }

    /**
     * Test of isConnected method, of class GUIBackend.
     */
    @Test
    public void testIsConnected() {
        System.out.println("isConnected");
        GUIBackend instance = new GUIBackend();
        boolean expResult = false;
        boolean result = instance.isConnected();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of disconnect method, of class GUIBackend.
     */
    @Test
    public void testDisconnect() throws Exception {
        System.out.println("disconnect");
        GUIBackend instance = new GUIBackend();
        instance.disconnect();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of applySettings method, of class GUIBackend.
     */
    @Test
    public void testApplySettings() throws Exception {
        System.out.println("applySettings");
        Settings settings = null;
        GUIBackend instance = new GUIBackend();
        instance.applySettings(settings);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of updateSystemState method, of class GUIBackend.
     */
    @Test
    public void testUpdateSystemState() {
        System.out.println("updateSystemState");
        SystemStateBean systemStateBean = null;
        GUIBackend instance = new GUIBackend();
        instance.updateSystemState(systemStateBean);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sendGcodeCommand method, of class GUIBackend.
     */
    @Test
    public void testSendGcodeCommand() throws Exception {
        System.out.println("sendGcodeCommand");
        String commandText = "";
        GUIBackend instance = new GUIBackend();
        instance.sendGcodeCommand(commandText);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of adjustManualLocation method, of class GUIBackend.
     */
    @Test
    public void testAdjustManualLocation() throws Exception {
        System.out.println("adjustManualLocation");
        int dirX = 0;
        int dirY = 0;
        int dirZ = 0;
        double stepSize = 0.0;
        Utils.Units units = null;
        GUIBackend instance = new GUIBackend();
        instance.adjustManualLocation(dirX, dirY, dirZ, stepSize, units);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSettings method, of class GUIBackend.
     */
    @Test
    public void testGetSettings() {
        System.out.println("getSettings");
        GUIBackend instance = new GUIBackend();
        Settings expResult = null;
        Settings result = instance.getSettings();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getControlState method, of class GUIBackend.
     */
    @Test
    public void testGetControlState() {
        System.out.println("getControlState");
        GUIBackend instance = new GUIBackend();
        ControlState expResult = null;
        ControlState result = instance.getControlState();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getController method, of class GUIBackend.
     */
    @Test
    public void testGetController() {
        System.out.println("getController");
        GUIBackend instance = new GUIBackend();
        IController expResult = null;
        IController result = instance.getController();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setTempDir method, of class GUIBackend.
     */
    @Test
    public void testSetTempDir() throws Exception {
        System.out.println("setTempDir");
        File file = null;
        GUIBackend instance = new GUIBackend();
        instance.setTempDir(file);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setGcodeFile method, of class GUIBackend.
     */
    @Test
    public void testSetGcodeFile() throws Exception {
        System.out.println("setGcodeFile");
        File file = null;
        GUIBackend instance = new GUIBackend();
        instance.setGcodeFile(file);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getGcodeFile method, of class GUIBackend.
     */
    @Test
    public void testGetGcodeFile() {
        System.out.println("getGcodeFile");
        GUIBackend instance = new GUIBackend();
        File expResult = null;
        File result = instance.getGcodeFile();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of send method, of class GUIBackend.
     */
    @Test
    public void testSend() throws Exception {
        System.out.println("send");
        GUIBackend instance = new GUIBackend();
        instance.send();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getNumRows method, of class GUIBackend.
     */
    @Test
    public void testGetNumRows() {
        System.out.println("getNumRows");
        GUIBackend instance = new GUIBackend();
        long expResult = 0L;
        long result = instance.getNumRows();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getNumSentRows method, of class GUIBackend.
     */
    @Test
    public void testGetNumSentRows() {
        System.out.println("getNumSentRows");
        GUIBackend instance = new GUIBackend();
        long expResult = 0L;
        long result = instance.getNumSentRows();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getNumRemainingRows method, of class GUIBackend.
     */
    @Test
    public void testGetNumRemainingRows() {
        System.out.println("getNumRemainingRows");
        GUIBackend instance = new GUIBackend();
        long expResult = 0L;
        long result = instance.getNumRemainingRows();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSendDuration method, of class GUIBackend.
     */
    @Test
    public void testGetSendDuration() {
        System.out.println("getSendDuration");
        GUIBackend instance = new GUIBackend();
        long expResult = 0L;
        long result = instance.getSendDuration();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSendRemainingDuration method, of class GUIBackend.
     */
    @Test
    public void testGetSendRemainingDuration() {
        System.out.println("getSendRemainingDuration");
        GUIBackend instance = new GUIBackend();
        long expResult = 0L;
        long result = instance.getSendRemainingDuration();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of pauseResume method, of class GUIBackend.
     */
    @Test
    public void testPauseResume() throws Exception {
        System.out.println("pauseResume");
        GUIBackend instance = new GUIBackend();
        instance.pauseResume();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPauseResumeText method, of class GUIBackend.
     */
    @Test
    public void testGetPauseResumeText() {
        System.out.println("getPauseResumeText");
        GUIBackend instance = new GUIBackend();
        String expResult = "";
        String result = instance.getPauseResumeText();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isSending method, of class GUIBackend.
     */
    @Test
    public void testIsSending() {
        System.out.println("isSending");
        GUIBackend instance = new GUIBackend();
        boolean expResult = false;
        boolean result = instance.isSending();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isPaused method, of class GUIBackend.
     */
    @Test
    public void testIsPaused() {
        System.out.println("isPaused");
        GUIBackend instance = new GUIBackend();
        boolean expResult = false;
        boolean result = instance.isPaused();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of canPause method, of class GUIBackend.
     */
    @Test
    public void testCanPause() {
        System.out.println("canPause");
        GUIBackend instance = new GUIBackend();
        boolean expResult = false;
        boolean result = instance.canPause();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of canCancel method, of class GUIBackend.
     */
    @Test
    public void testCanCancel() {
        System.out.println("canCancel");
        GUIBackend instance = new GUIBackend();
        boolean expResult = false;
        boolean result = instance.canCancel();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of canSend method, of class GUIBackend.
     */
    @Test
    public void testCanSend() {
        System.out.println("canSend");
        GUIBackend instance = new GUIBackend();
        boolean expResult = false;
        boolean result = instance.canSend();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of cancel method, of class GUIBackend.
     */
    @Test
    public void testCancel() throws Exception {
        System.out.println("cancel");
        GUIBackend instance = new GUIBackend();
        instance.cancel();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of returnToZero method, of class GUIBackend.
     */
    @Test
    public void testReturnToZero() throws Exception {
        System.out.println("returnToZero");
        GUIBackend instance = new GUIBackend();
        instance.returnToZero();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of resetCoordinatesToZero method, of class GUIBackend.
     */
    @Test
    public void testResetCoordinatesToZero() throws Exception {
        System.out.println("resetCoordinatesToZero");
        GUIBackend instance = new GUIBackend();
        instance.resetCoordinatesToZero();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of resetCoordinateToZero method, of class GUIBackend.
     */
    @Test
    public void testResetCoordinateToZero() throws Exception {
        System.out.println("resetCoordinateToZero");
        char coordinate = ' ';
        GUIBackend instance = new GUIBackend();
        instance.resetCoordinateToZero(coordinate);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of killAlarmLock method, of class GUIBackend.
     */
    @Test
    public void testKillAlarmLock() throws Exception {
        System.out.println("killAlarmLock");
        GUIBackend instance = new GUIBackend();
        instance.killAlarmLock();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of performHomingCycle method, of class GUIBackend.
     */
    @Test
    public void testPerformHomingCycle() throws Exception {
        System.out.println("performHomingCycle");
        GUIBackend instance = new GUIBackend();
        instance.performHomingCycle();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toggleCheckMode method, of class GUIBackend.
     */
    @Test
    public void testToggleCheckMode() throws Exception {
        System.out.println("toggleCheckMode");
        GUIBackend instance = new GUIBackend();
        instance.toggleCheckMode();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of issueSoftReset method, of class GUIBackend.
     */
    @Test
    public void testIssueSoftReset() throws Exception {
        System.out.println("issueSoftReset");
        GUIBackend instance = new GUIBackend();
        instance.issueSoftReset();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of requestParserState method, of class GUIBackend.
     */
    @Test
    public void testRequestParserState() throws Exception {
        System.out.println("requestParserState");
        GUIBackend instance = new GUIBackend();
        instance.requestParserState();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of fileStreamComplete method, of class GUIBackend.
     */
    @Test
    public void testFileStreamComplete() {
        System.out.println("fileStreamComplete");
        String filename = "";
        boolean success = false;
        GUIBackend instance = new GUIBackend();
        instance.fileStreamComplete(filename, success);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of commandQueued method, of class GUIBackend.
     */
    @Test
    public void testCommandQueued() {
        System.out.println("commandQueued");
        System.out.println("-NO-OP-");
    }

    /**
     * Test of commandSent method, of class GUIBackend.
     */
    @Test
    public void testCommandSent() {
        System.out.println("commandSent");
        System.out.println("-NO-OP-");
    }

    /**
     * Test of commandComplete method, of class GUIBackend.
     */
    @Test
    public void testCommandComplete() {
        System.out.println("commandComplete");
        GcodeCommand command = null;
        GUIBackend instance = new GUIBackend();
        instance.commandComplete(command);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of commandComment method, of class GUIBackend.
     */
    @Test
    public void testCommandComment() {
        System.out.println("commandComment");
        String comment = "";
        GUIBackend instance = new GUIBackend();
        instance.commandComment(comment);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of messageForConsole method, of class GUIBackend.
     */
    @Test
    public void testMessageForConsole() {
        System.out.println("messageForConsole");
        String msg = "";
        Boolean verbose = null;
        GUIBackend instance = new GUIBackend();
        //instance.messageForConsole(msg, verbose);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of statusStringListener method, of class GUIBackend.
     */
    @Test
    public void testStatusStringListener() {
        System.out.println("statusStringListener");
        String state = "";
        Position machineCoord = null;
        Position workCoord = null;
        GUIBackend instance = new GUIBackend();
        instance.statusStringListener(state, machineCoord, workCoord);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of postProcessData method, of class GUIBackend.
     */
    @Test
    public void testPostProcessData() {
        System.out.println("postProcessData");
        int numRows = 0;
        GUIBackend instance = new GUIBackend();
        instance.postProcessData(numRows);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of applySettingsToController method, of class GUIBackend.
     */
    @Test
    public void testApplySettingsToController() throws Exception {
        System.out.println("applySettingsToController");
        Settings settings = null;
        IController controller = null;
        GUIBackend instance = new GUIBackend();
        instance.applySettingsToController(settings, controller);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
