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
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.mockobjects.MockGrblCommunicator;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import java.io.File;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Owen
 */
public class GrblControllerTest {
    MockGrblCommunicator mgc;
    
    public GrblControllerTest() {
    }
    
    @Before
    public void setUp() {
        this.mgc = new MockGrblCommunicator();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of setSpeedOverride method, of class GrblController.
     */
    @Test
    public void testSetSpeedOverride() {
        System.out.println("setSpeedOverride");
        double override = 0.0;
        GrblController instance = new GrblController(mgc);
        instance.setSpeedOverride(override);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of openCommPort method, of class GrblController.
     */
    @Test
    public void testOpenCommPort() throws Exception {
        System.out.println("openCommPort");
        String port = "";
        int portRate = 0;
        GrblController instance = new GrblController(mgc);
        Boolean expResult = null;
        Boolean result = instance.openCommPort(port, portRate);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of closeCommPort method, of class GrblController.
     */
    @Test
    public void testCloseCommPort() {
        System.out.println("closeCommPort");
        GrblController instance = new GrblController(mgc);
        Boolean expResult = null;
        Boolean result = instance.closeCommPort();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of issueSoftReset method, of class GrblController.
     */
    @Test
    public void testIssueSoftReset() {
        System.out.println("issueSoftReset");
        GrblController instance = new GrblController(mgc);
        instance.issueSoftReset();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isStreamingFile method, of class GrblController.
     */
    @Test
    public void testIsStreamingFile() {
        System.out.println("isStreamingFile");
        GrblController instance = new GrblController(mgc);
        Boolean expResult = null;
        Boolean result = instance.isStreamingFile();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getSendDuration method, of class GrblController.
     */
    @Test
    public void testGetSendDuration() {
        System.out.println("getSendDuration");
        GrblController instance = new GrblController(mgc);
        long expResult = 0L;
        long result = instance.getSendDuration();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of rowsInSend method, of class GrblController.
     */
    @Test
    public void testRowsInSend() {
        System.out.println("rowsInSend");
        GrblController instance = new GrblController(mgc);
        int expResult = 0;
        int result = instance.rowsInSend();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of rowsSent method, of class GrblController.
     */
    @Test
    public void testRowsSent() {
        System.out.println("rowsSent");
        GrblController instance = new GrblController(mgc);
        int expResult = 0;
        int result = instance.rowsSent();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of rowsRemaining method, of class GrblController.
     */
    @Test
    public void testRowsRemaining() {
        System.out.println("rowsRemaining");
        GrblController instance = new GrblController(mgc);
        int expResult = 0;
        int result = instance.rowsRemaining();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of queueStringForComm method, of class GrblController.
     */
    @Test
    public void testQueueStringForComm() throws Exception {
        System.out.println("queueStringForComm");
        String str = "";
        GrblController instance = new GrblController(mgc);
        instance.queueStringForComm(str);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isReadyToStreamFile method, of class GrblController.
     */
    @Test
    public void testIsReadyToStreamFile() throws Exception {
        System.out.println("isReadyToStreamFile");
        GrblController instance = new GrblController(mgc);
        instance.isReadyToStreamFile();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of appendGcodeCommand method, of class GrblController.
     */
    @Test
    public void testAppendGcodeCommand() {
        System.out.println("appendGcodeCommand");
        String commandString = "";
        GrblController instance = new GrblController(mgc);
        instance.appendGcodeCommand(commandString);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of appendGcodeFile method, of class GrblController.
     */
    @Test
    public void testAppendGcodeFile() throws Exception {
        System.out.println("appendGcodeFile");
        File file = null;
        GrblController instance = new GrblController(mgc);
        instance.appendGcodeFile(file);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of beginStreaming method, of class GrblController.
     */
    @Test
    public void testBeginStreaming() throws Exception {
        System.out.println("beginStreaming");
        GrblController instance = new GrblController(mgc);
        instance.beginStreaming();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of pauseStreaming method, of class GrblController.
     */
    @Test
    public void testPauseStreaming() throws Exception {
        System.out.println("pauseStreaming");
        GrblController instance = new GrblController(mgc);
        instance.pauseStreaming();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of resumeStreaming method, of class GrblController.
     */
    @Test
    public void testResumeStreaming() throws Exception {
        System.out.println("resumeStreaming");
        GrblController instance = new GrblController(mgc);
        instance.resumeStreaming();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of cancelSend method, of class GrblController.
     */
    @Test
    public void testCancelSend() {
        System.out.println("cancelSend");
        GrblController instance = new GrblController(mgc);
        instance.cancelSend();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of commandQueued method, of class GrblController.
     */
    @Test
    public void testCommandQueued() {
        System.out.println("commandQueued");
        GcodeCommand command = null;
        GrblController instance = new GrblController(mgc);
        instance.commandQueued(command);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of fileStreamComplete method, of class GrblController.
     */
    @Test
    public void testFileStreamComplete() {
        System.out.println("fileStreamComplete");
        String filename = "";
        boolean success = false;
        GrblController instance = new GrblController(mgc);
        instance.fileStreamComplete(filename, success);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of preprocessCommand method, of class GrblController.
     */
    @Test
    public void testPreprocessCommand() {
        System.out.println("preprocessCommand");
        String command = "";
        GrblController instance = new GrblController(mgc);
        String expResult = "";
        String result = instance.preprocessCommand(command);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of capabilitiesListener method, of class GrblController.
     */
    @Test
    public void testCapabilitiesListener() {
        System.out.println("capabilitiesListener");
        Capabilities capability = null;
        GrblController instance = new GrblController(mgc);
        instance.capabilitiesListener(capability);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of handlePositionString method, of class GrblController.
     */
    @Test
    public void testHandlePositionString() {
        System.out.println("handlePositionString");
        String string = "";
        GrblController instance = new GrblController(mgc);
        instance.handlePositionString(string);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of commandSent method, of class GrblController.
     */
    @Test
    public void testCommandSent() {
        System.out.println("commandSent");
        GcodeCommand command = null;
        GrblController instance = new GrblController(mgc);
        instance.commandSent(command);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of commandComplete method, of class GrblController.
     */
    @Test
    public void testCommandComplete() {
        System.out.println("commandComplete");
        GcodeCommand command = null;
        GrblController instance = new GrblController(mgc);
        instance.commandComplete(command);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of messageForConsole method, of class GrblController.
     */
    @Test
    public void testMessageForConsole() {
        System.out.println("messageForConsole");
        String msg = "";
        GrblController instance = new GrblController(mgc);
        instance.messageForConsole(msg);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of verboseMessageForConsole method, of class GrblController.
     */
    @Test
    public void testVerboseMessageForConsole() {
        System.out.println("verboseMessageForConsole");
        String msg = "";
        GrblController instance = new GrblController(mgc);
        instance.verboseMessageForConsole(msg);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of rawResponseListener method, of class GrblController.
     */
    @Test
    public void testRawResponseListener() {
        System.out.println("rawResponseListener");
        String response = "";
        GrblController instance = new GrblController(mgc);
        instance.rawResponseListener(response);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of addListener method, of class GrblController.
     */
    @Test
    public void testAddListener() {
        System.out.println("addListener");
        ControllerListener cl = null;
        GrblController instance = new GrblController(mgc);
        instance.addListener(cl);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}
