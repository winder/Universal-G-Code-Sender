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

import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.mockobjects.MockGrblCommunicator;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import java.io.IOException;
import java.util.NoSuchElementException;
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
    public void testSetGetSpeedOverride() {
        System.out.println("setSpeedOverride/getSpeedOverride");
        double override = 1.0;
        GrblController instance = new GrblController(mgc);
        instance.setSpeedOverride(override);
        assertEquals(override, instance.getSpeedOverride(), 0.0);
        
        override = 1234.567;
        instance.setSpeedOverride(override);
        assertEquals(override, instance.getSpeedOverride(), 0.0);
    }
    
    @Test
    public void testGetGrblVersion() throws Exception {
        System.out.println("getGrblVersion");
        GrblController instance = new GrblController(mgc);
        String result;
        String expResult;
        String versionString;
        
        expResult = "<not connected>";
        result = instance.getGrblVersion();
        assertEquals(expResult, result);
        
        instance.openCommPort("blah", 1234);
        expResult = "Grbl 0.5b";
        instance.rawResponseListener(expResult);
        result = instance.getGrblVersion();
        assertEquals(expResult, result);
        
        expResult = "Grbl 0.57";
        instance.rawResponseListener(expResult);
        result = instance.getGrblVersion();
        assertEquals(expResult, result);
        
        expResult = "Grbl 0.8";
        instance.rawResponseListener(expResult);
        result = instance.getGrblVersion();
        assertEquals(expResult, result);
        
        expResult = "Grbl 0.8c";
        instance.rawResponseListener(expResult);
        result = instance.getGrblVersion();
        assertEquals(expResult, result);
    }

    /**
     * Test of numOpenCommPortCalls method, of class GrblController.
     */
    @Test
    public void testOpenCommPort() {
        System.out.println("openCommPort/isCommOpen");
        String port = "serialPort";
        int portRate = 12345;
        GrblController instance = new GrblController(mgc);
        Boolean expResult = true;
        Boolean result = false;
        try {
            result = instance.openCommPort(port, portRate);
        } catch (Exception e) {
            fail("Unexpected exception from GrblController: " + e.getMessage());
        }
        assertEquals(expResult, result);
        assertEquals(expResult, instance.isCommOpen());
        assertEquals(port, mgc.portName);
        assertEquals(portRate, mgc.portRate);
        
        String exception = "";
        // Check exception trying to open the comm port twice.
        try {
            instance.openCommPort(port, portRate);
        } catch (Exception e) {
            exception = e.getMessage();
        }
        assertEquals("Comm port is already open.", exception);
    }

    /**
     * Test of numCloseCommPortCalls method, of class GrblController.
     */
    @Test
    public void testCloseCommPort() {
        System.out.println("closeCommPort/isCommOpen");
        GrblController instance = new GrblController(mgc);
        
        // Make sure comm is closed
        assertEquals(false, instance.isCommOpen());
        
        // Test closing while already closed.
        Boolean result = instance.closeCommPort();
        assertEquals(true, result);
        assertEquals(false, instance.isCommOpen());
        
        // Test closed after opening thenc losing.
        try {
            instance.openCommPort("blah", 1234);
        } catch (Exception e) {
            fail("Unexpected exception from GrblController: " + e.getMessage());
        }
        result = instance.closeCommPort();
        assertEquals(true, result);
        assertEquals(false, instance.isCommOpen());
    }

    @Test
    public void testPerformHomingCycle() throws Exception {
        System.out.println("performHomingCycle");
        String expResult;
        GrblController instance = new GrblController(mgc);
        instance.openCommPort("blah", 1234);
        
        boolean hitIt = false;
        try {
            instance.performHomingCycle();
        } catch (Exception e) {
            hitIt = true;
            assert(e.getMessage().startsWith("No supported homing method for "));
        }
        assertEquals(true, hitIt);
        
        instance.rawResponseListener("Grbl 0.7");
        hitIt = false;
        try {
            instance.performHomingCycle();
        } catch (Exception e) {
            hitIt = true;
            assert(e.getMessage().startsWith("No supported homing method for Grbl 0.7"));
        }
        assertEquals(true, hitIt);
        
        instance.rawResponseListener("Grbl 0.8");
        instance.performHomingCycle();
        assertEquals(1, mgc.numStreamCommandsCalls);
        expResult = GrblUtils.GCODE_PERFORM_HOMING_CYCLE_V8 + "\n";
        assertEquals(expResult, mgc.queuedString);
        
        instance.rawResponseListener("Grbl 0.8c");
        instance.performHomingCycle();
        assertEquals(2, mgc.numStreamCommandsCalls);
        expResult = GrblUtils.GCODE_PERFORM_HOMING_CYCLE_V8C + "\n";
        assertEquals(expResult, mgc.queuedString);
        
        instance.rawResponseListener("Grbl 0.9");
        instance.performHomingCycle();
        assertEquals(3, mgc.numStreamCommandsCalls);
        expResult = GrblUtils.GCODE_PERFORM_HOMING_CYCLE_V8C + "\n";
        assertEquals(expResult, mgc.queuedString);
    }
    /**
     * Test of issueSoftReset method, of class GrblController.
     */
    @Test
    public void testIssueSoftReset() throws IOException {
        System.out.println("issueSoftReset");
        GrblController instance = new GrblController(mgc);
        
        // Noop if called while comm is closed.
        instance.issueSoftReset();
        // Did not send reset command to communicator or issue reset.
        assertEquals(0x0, mgc.sentByte);
        assertEquals(0, mgc.numSoftResetCalls);
        
        try {
            instance.openCommPort("blah", 1234);
        } catch (Exception e) {
            fail("Unexpected exception from GrblController: " + e.getMessage());
        }
        
        // Noop if called while controller doesn't have real_time mode enabled.
        instance.issueSoftReset();
        // Did not send reset command to communicator or issue reset.
        assertEquals(0x0, mgc.sentByte);
        assertEquals(0, mgc.numSoftResetCalls);

        // Enable real time mode by sending correct GRBL version:
        instance.rawResponseListener("Grbl 0.8c");
        instance.issueSoftReset();
        // Sent reset command to communicator and issued reset.
        assertEquals(GrblUtils.GRBL_RESET_COMMAND, mgc.sentByte);
        assertEquals(1, mgc.numSoftResetCalls);
        
        // GRBL version that might not have the command but I send it to anyway:
        mgc.resetInputsAndFunctionCalls();
        instance.rawResponseListener("Grbl 0.8a");
        instance.issueSoftReset();
        // Sent reset command to communicator and issued reset.
        assertEquals(GrblUtils.GRBL_RESET_COMMAND, mgc.sentByte);
        assertEquals(1, mgc.numSoftResetCalls);
        
        // GRBL version that should not be sent the command:
        mgc.resetInputsAndFunctionCalls();
        instance.rawResponseListener("Grbl 0.7");
        instance.issueSoftReset();
        // Sent reset command to communicator and issued reset.
        assertEquals(0x0, mgc.sentByte);
        assertEquals(0, mgc.numSoftResetCalls);
    }

    /**
     * Test of isStreamingFile method, of class GrblController.
     */
    @Test
    public void testIsStreamingFile() {
        System.out.println("isStreamingFile");
        GrblController instance = new GrblController(mgc);
        instance.rawResponseListener("Grbl 0.8c");
        
        // By default nothing is streaming.
        Boolean expResult = false;
        Boolean result = instance.isStreamingFile();
        assertEquals(expResult, result);
        
        // Test begining stream with no data to stream.
        expResult = false;
        try {
            instance.openCommPort("blah", 123);
            instance.beginStreaming();
        } catch (Exception ex) {
            assertEquals("There are no commands queued for streaming.", ex.getMessage());
        }
        result = instance.isStreamingFile();
        assertEquals(expResult, result);
        
        instance.appendGcodeCommand("G0 X1");
        try {
            instance.beginStreaming();
        } catch (Exception ex) {
            fail("Unexpected exception from GrblController: " +ex.getMessage());
        }
        result = instance.isStreamingFile();
        expResult = true;
        assertEquals(expResult, result);
        assertEquals(1, mgc.numQueueStringForCommCalls);
        assertEquals(1, mgc.numStreamCommandsCalls);
        
        // Wrap up streaming and make sure isStreaming switches back.
        GcodeCommand command = new GcodeCommand("G0 X1");
        command.setSent(true);
        command.setResponse("ok");
        try {
            instance.commandSent(command);
            instance.commandComplete(command);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Unexpected exception from command complete: " + ex.getMessage());
        }
        result = instance.isStreamingFile();
        expResult = false;
        assertEquals(expResult, result);
    }

    /**
     * Test of getSendDuration method, of class GrblController.
     */
    @Test
    public void testGetSendDuration() {
        System.out.println("getSendDuration");
        GrblController instance = new GrblController(mgc);
        instance.rawResponseListener("Grbl 0.8c");

        // Test 1.
        // Result when not sending and nothing has been sent.
        long expResult = 0L;
        long result = instance.getSendDuration();
        assertEquals(expResult, result);

        // Test 2.
        // Result when stream has begin but not completed.
        instance.appendGcodeCommand("G0 X1");
        try {
            instance.openCommPort("blah", 123);
            instance.beginStreaming();
        } catch (Exception ex) {
            fail("Unexpected exception from GrblController: " +ex.getMessage());
        }
        try {
            // Sleep for 2 seconds
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            fail("Unexpected exception from Thread.sleep: " + ex.getMessage());
        }
        // Send duration should be around 2 seconds.
        expResult = 2000L;
        result = instance.getSendDuration();
        // Assert that result is within 0.5 seconds of expected value.
        assert(expResult <= result);
        assert(result <= (expResult + 500));

        try {
            // Sleep for 2 seconds
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            fail("Unexpected exception from Thread.sleep: " + ex.getMessage());
        }

        // Test 3.
        // Wrap up the send and check the duration.
        GcodeCommand command = new GcodeCommand("G0 X1");
        command.setSent(true);
        command.setResponse("ok");
        try {
            instance.commandSent(command);
            instance.commandComplete(command);
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Unexpected exception from command complete: " + ex.getMessage());
        }

        expResult = 4000L;
        result = instance.getSendDuration();
        // Assert that result is within 0.5 seconds of expected value.
        assert(expResult <= result);
        assert(result <= (expResult + 500));

        // Test 4.
        // Make sure the duration is no longer increasing.
        try {
            // Sleep for 2 seconds
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            fail("Unexpected exception from Thread.sleep: " + ex.getMessage());
        }
        result = instance.getSendDuration();
        // Assert that result is within 0.5 seconds of expected value.
        assert(expResult <= result);
        assert(result <= (expResult + 500));
    }

    /**
     * Test of rowsInSend method, of class GrblController.
     */
    @Test
    public void testRowsAsteriskMethods() {
        System.out.println("rowsInSend / rowsSent / rowsRemaining");
        GrblController instance = new GrblController(mgc);
        instance.rawResponseListener("Grbl 0.8c");

        int expResult;
        int result;

        // Test 1.
        // When not sending, no commands queues, everything should be zero.
        expResult = 0;
        result = instance.rowsInSend();
        assertEquals(expResult, result);
        expResult = 0;
        result = instance.rowsSent();
        assertEquals(expResult, result);
        expResult = 0;
        result = instance.rowsRemaining();
        assertEquals(expResult, result);
        
        // Add 30 commands.
        for (int i=0; i < 30; i++) {
            instance.appendGcodeCommand("G0 X" + i);
        }
        
        try {
            instance.openCommPort("blah", 123);
            instance.beginStreaming();
        } catch (Exception ex) {
            fail("Unexpected exception from GrblController: " +ex.getMessage());
        }
        
        // Test 2.
        // 30 Commands queued, zero sent, zero completed.
        expResult = 30;
        result = instance.rowsInSend();
        assertEquals(expResult, result);
        expResult = 0;
        result = instance.rowsSent();
        assertEquals(expResult, result);
        expResult = 30;
        result = instance.rowsRemaining();
        assertEquals(expResult, result);
    
        
        // Test 3.
        // Sent 15 of them, none completed.
        try {
            for (int i=0; i < 15; i++) {
                GcodeCommand command = new GcodeCommand("G0 X1");
                command.setSent(true);
                command.setResponse("ok");
                instance.commandSent(command);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Unexpected exception from command sent: " + ex.getMessage());
        }
        expResult = 30;
        result = instance.rowsInSend();
        assertEquals(expResult, result);
        expResult = 15;
        result = instance.rowsSent();
        assertEquals(expResult, result);
        expResult = 30;
        result = instance.rowsRemaining();
        assertEquals(expResult, result);
        
        // Test 4.
        // Complete 15 of them.
        try {
            for (int i=0; i < 15; i++) {
                GcodeCommand command = new GcodeCommand("G0 X1");
                command.setSent(true);
                command.setResponse("ok");
                instance.commandComplete(command);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Unexpected exception from command complete: " + ex.getMessage());
        }
        expResult = 30;
        result = instance.rowsInSend();
        assertEquals(expResult, result);
        expResult = 15;
        result = instance.rowsSent();
        assertEquals(expResult, result);
        expResult = 15;
        result = instance.rowsRemaining();
        assertEquals(expResult, result);
        
        // Test 5.
        // Finish sending/completing the remaining 15 commands.
        try {
            for (int i=0; i < 15; i++) {
                GcodeCommand command = new GcodeCommand("G0 X1");
                command.setSent(true);
                command.setResponse("ok");
                instance.commandSent(command);
                instance.commandComplete(command);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Unexpected exception from command complete: " + ex.getMessage());
        }
        expResult = 30;
        result = instance.rowsInSend();
        assertEquals(expResult, result);
        expResult = 30;
        result = instance.rowsSent();
        assertEquals(expResult, result);
        expResult = 0;
        result = instance.rowsRemaining();
        assertEquals(expResult, result);
    }

    /**
     * Test of numQueueStringForCommCalls method, of class GrblController.
     */
    @Test
    public void testQueueStringForComm() throws Exception {
        System.out.println("queueStringForComm");
        String str = "G0 X0 ";
        GrblController instance = new GrblController(mgc);
        instance.queueStringForComm(str);
        assertEquals(1, mgc.numQueueStringForCommCalls);
        assertEquals(1, mgc.numStreamCommandsCalls);
        assertEquals(str  + "\n", mgc.queuedString);
    }

    /**
     * Test of isReadyToStreamFile method, of class GrblController.
     */
    @Test
    public void testIsReadyToStreamFile() throws Exception {
        System.out.println("isReadyToStreamFile");
        GrblController instance = new GrblController(mgc);
        
        // Test 1. No streaming if comm isn't open.
        try {
            instance.isReadyToStreamFile();
        } catch (Exception e) {
            assertEquals("Cannot begin streaming, comm port is not open.", e.getMessage());
        }

        // Test 2. Grbl has not yet responded.
        try {
            instance.openCommPort("blah", 1234);
            instance.isReadyToStreamFile();
        } catch (Exception e) {
            assertEquals("Grbl has not finished booting.", e.getMessage());
        }
        
        // Test 3. Grbl ready, ready for send.
        instance.rawResponseListener("Grbl 0.8c");
        Boolean result = instance.isReadyToStreamFile();
        assertEquals(true, result);
        
        // Test 4. Can't send during active command.
        instance.queueStringForComm("blah");
        try {
            instance.isReadyToStreamFile();
        } catch (Exception e) {
            assertEquals("Cannot stream while there are active commands (controller).", e.getMessage());
        }
    }

    /**
     * Test of appendGcodeCommand method, of class GrblController.
     */
    @Test
    public void testAppendGcodeCommand() {
        System.out.println("appendGcodeCommand");
        // This is fully tested by other tests.
    }

    /**
     * Test of appendGcodeFile method, of class GrblController.
     */
    @Test
    public void testAppendGcodeFile() throws Exception {
        System.out.println("appendGcodeFile");
        //File file = null;
        //GrblController instance = new GrblController(mgc);
        //instance.appendGcodeFile(file);
        
        // Not testing file inputs now.
    }

    /**
     * Test of beginStreaming method, of class GrblController.
     */
    @Test
    public void testBeginStreaming() throws Exception {
        System.out.println("beginStreaming");
        GrblController instance = new GrblController(mgc);
        
        instance.openCommPort("blah", 1234);
        instance.rawResponseListener("Grbl 0.8c");
        
        // Test 1. No commands to stream.
        try {
            instance.beginStreaming();
        } catch (Exception e) {
            assertEquals("There are no commands queued for streaming.", e.getMessage());
        }
        
        // Test 2. Command already streaming.
        instance.appendGcodeCommand("G0 X1");
        Boolean check = false;
        try {
            // Start the stream.
            instance.beginStreaming();
            // Make sure we got this far.
            check = true;
            // Trigger the error.
            instance.beginStreaming();
        } catch (Exception ex) {
            assertEquals("Cannot stream while there are active commands (controller).", ex.getMessage());
        }
        assertEquals(1, mgc.numQueueStringForCommCalls);
        assertEquals(true, check);
        // Wrap up test 2.
        GcodeCommand command = new GcodeCommand("G0 X1");
        command.setSent(true);
        command.setResponse("ok");
        instance.commandSent(command);
        instance.commandComplete(command);
        
        // Test 3. Stream some commands and make sure they get sent.
        for (int i=0; i < 30; i++) {
            instance.appendGcodeCommand("G0 X" + i);
        }
        try {
            instance.beginStreaming();
        } catch (Exception ex) {
            fail("Unexpected exception in GrblController: " + ex.getMessage());
        }
        assertEquals(30, instance.rowsRemaining());
        assertEquals(31, mgc.numQueueStringForCommCalls);
        // Wrap up test 3.
        for (int i=0; i < 30; i++) {
            command.setCommand("G0 X" + i);
            instance.commandSent(command);
            instance.commandComplete(command);
        }

        // Test 4. Commands being sent are properly preprocessed.
        instance.setSpeedOverride(1000.0);
        instance.appendGcodeCommand("G0 X1 Y1 Z1 F100");
        try {
            // Start the stream.
            instance.beginStreaming();
        } catch (Exception ex) {
            System.out.println("Remaining rows: " + instance.rowsRemaining());
            ex.printStackTrace();
            fail("Unexpected exception in GrblController: " + ex.getMessage());
        }
        assertEquals(32, mgc.numQueueStringForCommCalls);
        assertEquals("G0 X1 Y1 Z1 F1000.0", mgc.queuedString.trim());
        // Wrap up test 4.
        command.setCommand("G0 X1 Y1 Z1 F1000.0");
        instance.commandSent(command);
        instance.commandComplete(command);

    }

    /**
     * Test of pauseStreaming method, of class GrblController.
     */
    @Test
    public void testPauseStreaming() throws Exception {
        System.out.println("pauseStreaming");
        GrblController instance = new GrblController(mgc);
        
        instance.pauseStreaming();
        assertEquals(1, mgc.numPauseSendCalls);
        
        instance.rawResponseListener("Grbl 0.7");
        instance.pauseStreaming();
        assertEquals(2, mgc.numPauseSendCalls);
        assertEquals(0x0, mgc.sentByte);
        
        instance.rawResponseListener("Grbl 0.8c");
        instance.pauseStreaming();
        assertEquals(3, mgc.numPauseSendCalls);
        assertEquals(GrblUtils.GRBL_PAUSE_COMMAND, mgc.sentByte);
    }

    /**
     * Test of resumeStreaming method, of class GrblController.
     */
    @Test
    public void testResumeStreaming() throws Exception {
        System.out.println("resumeStreaming");
        GrblController instance = new GrblController(mgc);

        instance.resumeStreaming();
        assertEquals(1, mgc.numResumeSendCalls);
        
        instance.rawResponseListener("Grbl 0.7");
        instance.resumeStreaming();
        assertEquals(2, mgc.numResumeSendCalls);
        assertEquals(0x0, mgc.sentByte);
        
        instance.rawResponseListener("Grbl 0.8c");
        instance.resumeStreaming();
        assertEquals(3, mgc.numResumeSendCalls);
        assertEquals(GrblUtils.GRBL_RESUME_COMMAND, mgc.sentByte);

    }

    /**
     * Test of numCancelSendCalls method, of class GrblController.
     */
    @Test
    public void testCancelSend() {
        System.out.println("cancelSend");
        GrblController instance = new GrblController(mgc);
        
        
        // Test 1.1 Cancel when nothing is running (Grbl 0.7).
        instance.rawResponseListener("Grbl 0.7");
        instance.cancelSend();
        assertEquals(1, mgc.numCancelSendCalls);
        
        // Test 1.2 Cancel when nothing is running (Grbl 0.8c).
        //          Check for soft reset??
        instance.rawResponseListener("Grbl 0.8c");
        instance.cancelSend();
        assertEquals(2, mgc.numCancelSendCalls);

        // Test 2.1 
        // Add 30 commands, start send, cancel before any sending. (Grbl 0.7)
        instance.rawResponseListener("Grbl 0.7");
        for (int i=0; i < 30; i++) {
            instance.appendGcodeCommand("G0 X" + i);
        }
        try {
            instance.openCommPort("blah", 123);
            instance.beginStreaming();
        } catch (Exception ex) {
            fail("Unexpected exception from GrblController: " +ex.getMessage());
        }
        instance.cancelSend();
        assertEquals(30, instance.rowsInSend());
        assertEquals(0, instance.rowsRemaining());

        // Test 2.2
        // Add 30 commands, start send, cancel before any sending. (Grbl 0.8c)
        instance.rawResponseListener("Grbl 0.8c");
        for (int i=0; i < 30; i++) {
            instance.appendGcodeCommand("G0 X" + i);
        }
        try {
            instance.beginStreaming();
        } catch (Exception ex) {
            fail("Unexpected exception from GrblController: " +ex.getMessage());
        }
        instance.cancelSend();
        assertEquals(30, instance.rowsInSend());
        assertEquals(0, instance.rowsRemaining());
        
        // Test 3.1
        // Add 30 commands, start send, cancel after sending 15. (Grbl 0.7)
        instance.rawResponseListener("Grbl 0.7");
        for (int i=0; i < 30; i++) {
            instance.appendGcodeCommand("G0 X" + i);
        }
        try {
            instance.beginStreaming();
            for (int i=0; i < 15; i++) {
                GcodeCommand command = new GcodeCommand("G0 X1");
                command.setSent(true);
                command.setResponse("ok");
                instance.commandSent(command);
            }
        } catch (Exception ex) {
            fail("Unexpected exception from command sent: " + ex.getMessage());
        }
        instance.cancelSend();
        assertEquals(30, instance.rowsInSend());
        assertEquals(15, instance.rowsRemaining());
        // wrap up
        try {
            GcodeCommand command = new GcodeCommand("blah");
            command.setSent(true);
            command.setResponse("ok");
            for (int i=0; i < 15; i++) {
                instance.commandComplete(command);
            }
        } catch (Exception ex) {
            fail("Unexpected exception testing cancelSend: " + ex.getMessage());
        }
        
        // Test 3.2
        // Add 30 commands, start send, cancel after sending 15. (Grbl 0.8c)
        instance.rawResponseListener("Grbl 0.8c");
        for (int i=0; i < 30; i++) {
            instance.appendGcodeCommand("G0 X" + i);
        }
        try {
            instance.beginStreaming();
            for (int i=0; i < 15; i++) {
                GcodeCommand command = new GcodeCommand("G0 X1");
                command.setSent(true);
                command.setResponse("ok");
                instance.commandSent(command);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Unexpected exception from command sent: " + ex.getMessage());
        }
        
        instance.cancelSend();
        assertEquals(30, instance.rowsInSend());
        // Left this failing because it should be possible to make it work
        // this way someday.
        assertEquals(0, instance.rowsRemaining());
    }

    /**
     * Test of commandSent method, of class GrblController.
     */
    @Test
    public void testCommandSent() {
        System.out.println("commandSent");
        GcodeCommand command = null;
        GrblController instance = new GrblController(mgc);
        
        // Test 1. Sending command when none are queued.
        boolean hitException = false;
        try {
            instance.commandSent(command);
        } catch (NoSuchElementException e) {
            hitException = true;
        }
        assertEquals(true, hitException);
        
        // Test 2.
        // The good case is utilized extensively in other tests.
        
        // TODO: Test that commandSent triggers a listener event.
    }

    /**
     * Test of commandComplete method, of class GrblController.
     */
    @Test
    public void testCommandComplete() {
        System.out.println("commandComplete");
        GcodeCommand command = null;
        GrblController instance = new GrblController(mgc);
        
        // Test 1. Complete a command that was marked as sent but never declared
        //         within commandSent(command).
        command = new GcodeCommand("blah");
        command.setSent(true);
        boolean hitException = false;
        try {
            instance.commandComplete(command);
        } catch (Exception ex) {
            hitException = true;
            System.out.println("Exception: " + ex.getMessage());
            assert(ex.getMessage().startsWith(
                    "Attempting to completing a command that doesn't exist: <"));
        }
        // Note: Completing a command that is not in the awaiting response queue
        //       is now synonymous with skipping a command.
        assertEquals(false, hitException);
        
        // TODO: Test that command complete triggers a listener event.

        // TODO: Test that command complete triggers fileStreamComplete.
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

        // TODO: Test that this triggers a listener event.
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
        
        // TODO: Test that this triggers a listener event.
    }

    /**
     * Test of rawResponseListener method, of class GrblController.
     */
    @Test
    public void testRawResponseListener() {
        System.out.println("rawResponseListener");
        String response = "";
        GrblController instance = new GrblController(mgc);
        instance.rawResponseListener("Grbl 0.8c");
        
        // TODO: Test that ok/error trigger listener events.
        
        // TODO: Test that version strings update versions.
        
        // TODO: Test that status strings trigger both listener events
        //          (verbose console and position event)
    }
    /**
     * Test of rawResponseListener method, of class GrblController.
     */
    @Test
    public void testPolling() {
        System.out.println("testPolling (via rawResponseListener)");
        String response = "";
        GrblController instance = new GrblController(mgc);
        
        // Test 1. Check that polling works. (Grbl 0.8c)
        instance.rawResponseListener("Grbl 0.8c");
        try {
            // Enough time for a few polling callsthe next poll to be sent.
            Thread.sleep(300);
        } catch (InterruptedException ex) {
            fail("Unexpected exception while testing rawResponseListener: " +ex.getMessage());
        }
        assertEquals(1, mgc.numSendByteImmediatelyCalls);
        assertEquals(GrblUtils.GRBL_STATUS_COMMAND, mgc.sentByte);
        
        // Test 2. Check that another poll is sent shortly after receiving a
        //         status message.
        instance.rawResponseListener("<blah blah blah>");
        try {
            // Enough time for a few polling callsthe next poll to be sent.
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            fail("Unexpected exception while testing rawResponseListener: " +ex.getMessage());
        }
        assertEquals(2, mgc.numSendByteImmediatelyCalls);
        assertEquals(GrblUtils.GRBL_STATUS_COMMAND, mgc.sentByte);
        
        // Test 3. Check that after a long enough delay, additional polls are
        //         sent even without responses.
        try {
            // Enough time for a few polling callsthe next poll to be sent.
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            fail("Unexpected exception while testing rawResponseListener: " +ex.getMessage());
        }
        assert(2 < mgc.numSendByteImmediatelyCalls);
        assertEquals(GrblUtils.GRBL_STATUS_COMMAND, mgc.sentByte);
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
        
        // TODO: Test that (multiple?) listener events work.
    }
}
