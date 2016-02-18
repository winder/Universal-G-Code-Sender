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

import com.willwinder.universalgcodesender.AbstractController.UnexpectedCommand;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.mockobjects.MockGrblCommunicator;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import java.io.IOException;
import org.junit.After;
import org.junit.Ignore; 
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author wwinder
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
        instance.rawResponseHandler(expResult);
        result = instance.getGrblVersion();
        assertEquals(expResult, result);
        
        expResult = "Grbl 0.57";
        instance.rawResponseHandler(expResult);
        result = instance.getGrblVersion();
        assertEquals(expResult, result);
        
        expResult = "Grbl 0.8";
        instance.rawResponseHandler(expResult);
        result = instance.getGrblVersion();
        assertEquals(expResult, result);
        
        expResult = "Grbl 0.8c";
        instance.rawResponseHandler(expResult);
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
        
        Boolean result = false;
        try {
            // Test closing while already closed.
            result = instance.closeCommPort();
            assertEquals(true, result);
            assertEquals(false, instance.isCommOpen());

            // Test closed after opening thenc losing.
            instance.openCommPort("blah", 1234);
            result = instance.closeCommPort();
        } catch (Exception e) {
            fail("Unexpected exception from GrblController: " + e.getMessage());
        }
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
        
        instance.rawResponseHandler("Grbl 0.7");
        assertEquals(1, mgc.numStreamCommandsCalls);

        hitIt = false;
        try {
            instance.performHomingCycle();
        } catch (Exception e) {
            hitIt = true;
            assert(e.getMessage().startsWith("No supported homing method for this version."));
        }
        assertEquals(true, hitIt);
        
        instance.rawResponseHandler("Grbl 0.8");
        assertEquals(2, mgc.numStreamCommandsCalls);

        instance.performHomingCycle();
        assertEquals(3, mgc.numStreamCommandsCalls);
        expResult = GrblUtils.GCODE_PERFORM_HOMING_CYCLE_V8 + "\n";
        assertEquals(expResult, mgc.queuedString);
        
        instance.rawResponseHandler("Grbl 0.8c");
        assertEquals(5, mgc.numStreamCommandsCalls);

        instance.performHomingCycle();
        assertEquals(6, mgc.numStreamCommandsCalls);
        expResult = GrblUtils.GCODE_PERFORM_HOMING_CYCLE_V8C + "\n";
        assertEquals(expResult, mgc.queuedString);
        
        instance.rawResponseHandler("Grbl 0.9");
        assertEquals(8, mgc.numStreamCommandsCalls);

        instance.performHomingCycle();
        assertEquals(9, mgc.numStreamCommandsCalls);
        expResult = GrblUtils.GCODE_PERFORM_HOMING_CYCLE_V8C + "\n";
        assertEquals(expResult, mgc.queuedString);
    }
    /**
     * Test of issueSoftReset method, of class GrblController.
     */
    @Test
    public void testIssueSoftReset() throws IOException, Exception {
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

        // Automatic soft reset
        assertEquals(24, mgc.sentByte);
        assertEquals(0, mgc.numSoftResetCalls);

        // Enable real time mode by sending correct GRBL version:
        instance.rawResponseHandler("Grbl 0.8c");
        instance.issueSoftReset();
        // Sent reset command to communicator and issued reset.
        assertEquals(GrblUtils.GRBL_RESET_COMMAND, mgc.sentByte);
        assertEquals(1, mgc.numSoftResetCalls);
        
        // GRBL version that might not have the command but I send it to anyway:
        mgc.resetInputsAndFunctionCalls();
        instance.openCommPort("blah", 1234);
        assertEquals(GrblUtils.GRBL_RESET_COMMAND, mgc.sentByte);
        mgc.sentByte = 0;
        instance.rawResponseHandler("Grbl 0.8a");
        instance.issueSoftReset();
        // Sent reset command to communicator and issued reset.
        assertEquals(GrblUtils.GRBL_RESET_COMMAND, mgc.sentByte);
        assertEquals(1, mgc.numSoftResetCalls);
        
        // GRBL version that should not be sent the command:
        mgc.resetInputsAndFunctionCalls();
        instance.openCommPort("blah", 1234);
        assertEquals(GrblUtils.GRBL_RESET_COMMAND, mgc.sentByte);
        mgc.sentByte = 0;
        instance.rawResponseHandler("Grbl 0.7");
        instance.issueSoftReset();
        // Sent reset command to communicator and issued reset.
        assertEquals(0x0, mgc.sentByte);
        assertEquals(0, mgc.numSoftResetCalls);
    }

    /**
     * Test of isStreamingFile method, of class GrblController.
     */
    @Test
    public void testIsStreamingFile() throws Exception {
        System.out.println("isStreamingFile");
        GrblController instance = new GrblController(mgc);
        instance.openCommPort("blah", 1234);
        instance.rawResponseHandler("Grbl 0.8c");

        //$G and $$ get queued on startup
        assertEquals(2, mgc.numQueueStringForCommCalls);
        assertEquals(2, mgc.numStreamCommandsCalls);

        // By default nothing is streaming.
        Boolean expResult = false;
        Boolean result = instance.isStreamingFile();
        assertEquals(expResult, result);
        
        // Test begining stream with no data to stream.
        expResult = false;
        boolean threwException = false;
        try {
//            instance.openCommPort("blah", 123);
            instance.beginStreaming();
        } catch (Exception ex) {
            assertEquals("There are no commands queued for streaming.", ex.getMessage());
            threwException = true;
        }
        assertTrue(threwException);
        result = instance.isStreamingFile();
        assertEquals(expResult, result);

        GcodeCommand cmd = instance.createCommand("G0X1");
        instance.queueCommand(cmd);
        try {
            instance.beginStreaming();
        } catch (Exception ex) {
            fail("Unexpected exception from GrblController: " +ex.getMessage());
        }
        result = instance.isStreamingFile();
        expResult = true;
        assertEquals(expResult, result);
        assertEquals(3, mgc.numQueueStringForCommCalls);
        assertEquals(3, mgc.numStreamCommandsCalls);
    
        // Wrap up streaming and make sure isStreaming switches back.
        GcodeCommand command = new GcodeCommand("G0X1"); // Whitespace removed.
        command.setSent(true);
        command.setResponse("ok");
        try {
            instance.commandSent(command);
            instance.commandComplete(command.getCommandString());
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
    public void testGetSendDuration() throws Exception {
        System.out.println("getSendDuration");
        GrblController instance = new GrblController(mgc);
        instance.openCommPort("blah", 1234);
        instance.rawResponseHandler("Grbl 0.8c");

        // Test 1.
        // Result when not sending and nothing has been sent.
        long expResult = 0L;
        long result = instance.getSendDuration();
        assertEquals(expResult, result);

        // Test 2.
        // Result when stream has begun but not completed.
        instance.queueCommand(instance.createCommand("G0X1"));
        try {
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
        GcodeCommand command = new GcodeCommand("G0X1"); // Whitespace removed.
        command.setSent(true);
        command.setResponse("ok");
        try {
            instance.commandSent(command);
            instance.commandComplete(command.getCommandString());
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

    private void assertCounts(GrblController instance, int total, int sent, int remaining) {
        assertEquals(total, instance.rowsInSend());
        assertEquals(sent, instance.rowsSent());
        assertEquals(remaining, instance.rowsRemaining());
    }
    /**
     * Test of rowsInSend method, of class GrblController.
     */
    @Test
    public void testRowsAsteriskMethods() throws Exception {
        System.out.println("testRowsAsteriskMethods");
        GrblController instance = new GrblController(mgc);
        instance.openCommPort("blah", 1234);
        instance.rawResponseHandler("Grbl 0.8c");

        int expResult;
        int result;

        // Test 1.
        // When not sending, no commands queues, everything should be zero.
        assertCounts(instance, 0, 0, 0);
        
        // Add 30 commands.
        for (int i=0; i < 30; i++) {
            instance.queueCommand(instance.createCommand("G0X" + i));
        }
        
        try {
//            instance.openCommPort("blah", 123);
            instance.beginStreaming();
            mgc.areActiveCommands = true;
        } catch (Exception ex) {
            fail("Unexpected exception from GrblController: " +ex.getMessage());
        }
        
        // Test 2.
        // 30 Commands queued, zero sent, 30 completed.
        assertCounts(instance, 30, 0, 30);
        
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
        assertCounts(instance, 30, 15, 30);
        
        // Test 4.
        // Complete 15 of them.
        try {
            for (int i=0; i < 15; i++) {
                GcodeCommand command = new GcodeCommand("G0X1"); // Whitespace removed.
                command.setSent(true);
                command.setResponse("ok");
                instance.commandComplete(command.getCommandString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Unexpected exception from command complete: " + ex.getMessage());
        }
        assertCounts(instance, 30, 15, 15);
        
        // Test 5.
        // Finish sending/completing the remaining 15 commands.
        try {
            for (int i=0; i < 15; i++) {
                GcodeCommand command = new GcodeCommand("G0 X1");
                command.setSent(true);
                command.setResponse("ok");
                instance.commandSent(command);
                instance.commandComplete(command.getCommandString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            fail("Unexpected exception from command complete: " + ex.getMessage());
        }
        mgc.areActiveCommands = false;
        assertCounts(instance, 30, 30, 0);
    }

    /**
     * Test of numQueueStringForCommCalls method, of class GrblController.
     */
    @Test
    public void testSendCommandImmediately() throws Exception {
        System.out.println("queueStringForComm");
        String str = "G0 X0 ";
        GrblController instance = new GrblController(mgc);
        instance.openCommPort("blah", 123);
        instance.rawResponseHandler("Grbl 0.8c");
        //$G and $$ get queued on startup
        assertEquals(2, mgc.numQueueStringForCommCalls);
        assertEquals(2, mgc.numStreamCommandsCalls);
        GcodeCommand command = instance.createCommand(str);
        instance.sendCommandImmediately(command);
        assertEquals(3, mgc.numQueueStringForCommCalls);
        assertEquals(3, mgc.numStreamCommandsCalls);
        assertEquals(str  + "\n", mgc.queuedString);
    }

    /**
     * Test of isReadyToStreamFile method, of class GrblController.
     */
    @Test
    public void testIsReadyToStreamFile() throws Exception {
        System.out.println("isReadyToStreamFile");
        GrblController instance = new GrblController(mgc);

        boolean asserted;
        
        // Test 1. Grbl has not yet responded.
        try {
            asserted = false;
            instance.openCommPort("blah", 1234);
            instance.isReadyToStreamFile();
        } catch (Exception e) {
            asserted = true;
            assertEquals("Grbl has not finished booting.", e.getMessage());
        }
        assertTrue(asserted);
        
        // Test 2. No streaming if comm isn't open.
        instance.closeCommPort();
//Since the rawResponseHandler call can't execute without the port open, this section doesn't work any longer
//        instance.rawResponseHandler("Grbl 0.8c");
//        try {
//            asserted = false;
//            instance.isReadyToStreamFile();
//        } catch (Exception e) {
//            asserted = true;
//            assertEquals("Cannot begin streaming, comm port is not open.", e.getMessage());
//        }
//        assertTrue(asserted);
        
        // Test 3. Grbl ready, ready for send.
        instance.openCommPort("blah", 1234);
        instance.rawResponseHandler("Grbl 0.8c");
        Boolean result = instance.isReadyToStreamFile();
        assertEquals(true, result);
        
        // Test 4. Can't send during active command.
        instance.queueCommand(instance.createCommand("G0X0"));
        try {
            mgc.areActiveCommands = true;
            asserted = false;
            instance.isReadyToStreamFile();
        } catch (Exception e) {
            asserted = true;
            assertEquals("Cannot stream while there are active commands: ", e.getMessage());
        }
        assertTrue(asserted);
    }

    /**
     * Test of preprocessAndAppendGcodeCommand method, of class GrblController.
     */
    @Test
    public void testAppendGcodeCommand() {
        System.out.println("preprocessAndAppendGcodeCommand");
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
        instance.rawResponseHandler("Grbl 0.8c");
        //$G and $$ get queued on startup
        assertEquals(2, mgc.numQueueStringForCommCalls);

        // Test 1. No commands to stream.
        boolean caughtException = false;
        try {
            instance.beginStreaming();
        } catch (Exception e) {
            caughtException = true;
            assertEquals("There are no commands queued for streaming.", e.getMessage());
        }
        assertTrue(caughtException);
        
        // Test 2. Command already streaming.
        instance.queueCommand(instance.createCommand("G0X1"));
        Boolean check = false;
        caughtException = false;
        try {
            // Trigger the error.
            instance.beginStreaming();
        } catch (Exception ex) {
            caughtException = true;
            assertEquals("Cannot stream while there are active commands (controller).", ex.getMessage());
        }
        assertFalse(caughtException);
        assertEquals(3, mgc.numQueueStringForCommCalls);

        // Wrap up test 2.
        GcodeCommand command = new GcodeCommand("G0X1"); // Whitespace removed.
        command.setSent(true);
        command.setResponse("ok");
        instance.commandSent(command);
        instance.commandComplete(command.getCommandString());
        
        // Test 3. Stream some commands and make sure they get sent.
        for (int i=0; i < 30; i++) {
            instance.queueCommand(instance.createCommand("G0X" + i));
        }
        
        try {
            // Trigger the error.
            instance.beginStreaming();
        } catch (Exception ex) {
            caughtException = true;
            assertEquals("Cannot stream while there are active commands (controller).", ex.getMessage());
        }
        assertFalse(caughtException);
        
        assertEquals(30, instance.rowsRemaining());
        assertEquals(33, mgc.numQueueStringForCommCalls);
        // Wrap up test 3.
        for (int i=0; i < 30; i++) {
            command.setCommand("G0X" + i);
            instance.commandSent(command);
            instance.commandComplete(command.getCommandString());
        }
    }

    /**
     * Test of pauseStreaming method, of class GrblController.
     */
    @Test
    public void testPauseStreaming() throws Exception {
        System.out.println("pauseStreaming");
        GrblController instance = new GrblController(mgc);
        instance.openCommPort("blah", 1234);
        assertEquals(GrblUtils.GRBL_RESET_COMMAND, mgc.sentByte);
        mgc.sentByte = 0;

        instance.pauseStreaming();
        assertEquals(1, mgc.numPauseSendCalls);
        
        instance.rawResponseHandler("Grbl 0.7");
        instance.pauseStreaming();
        assertEquals(2, mgc.numPauseSendCalls);
        assertEquals(0x0, mgc.sentByte);
        
        instance.rawResponseHandler("Grbl 0.8c");
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
        instance.openCommPort("blah", 1234);
        assertEquals(GrblUtils.GRBL_RESET_COMMAND, mgc.sentByte);

        instance.resumeStreaming();
        assertEquals(1, mgc.numResumeSendCalls);
        
        instance.rawResponseHandler("Grbl 0.7");
        instance.resumeStreaming();
        assertEquals(2, mgc.numResumeSendCalls);
        assertEquals(GrblUtils.GRBL_RESET_COMMAND, mgc.sentByte);

        instance.rawResponseHandler("Grbl 0.8c");
        instance.resumeStreaming();
        assertEquals(3, mgc.numResumeSendCalls);
        assertEquals(GrblUtils.GRBL_RESUME_COMMAND, mgc.sentByte);

    }

    private static void wrapUp(GrblController gc, int numCommands) {
        // wrap up
        try {
            GcodeCommand command = new GcodeCommand("blah");
            command.setSent(true);
            command.setResponse("ok");
            for (int i=0; i < numCommands; i++) {
                gc.commandComplete(command.getCommandString());
            }
        } catch (Exception ex) {
            fail("Unexpected exception testing cancelSend: " + ex.getMessage());
        }
    }
    
    /**
     * Test of numCancelSendCalls method, of class GrblController.
     */
    @Test
    public void testCancelSend() throws Exception {
        System.out.println("cancelSend");
        GrblController instance = new GrblController(mgc);
        instance.openCommPort("blah", 1234);

        // Test 1.1 Cancel when nothing is running (Grbl 0.7).
        instance.rawResponseHandler("Grbl 0.7");
        instance.cancelSend();
        assertEquals(1, mgc.numCancelSendCalls);
        
        // Test 1.2 Cancel when nothing is running (Grbl 0.8c).
        //          Check for soft reset??
        instance.rawResponseHandler("Grbl 0.8c");
        instance.cancelSend();
        assertEquals(2, mgc.numCancelSendCalls);

        // Test 2.1 
        // Add 30 commands, start send, cancel before any sending. (Grbl 0.7)
        instance.rawResponseHandler("Grbl 0.7");
        for (int i=0; i < 30; i++) {
            instance.queueCommand(instance.createCommand("G0X" + i));
        }
        try {
            instance.beginStreaming();
        } catch (Exception ex) {
            fail("Unexpected exception from GrblController: " +ex.getMessage());
        }
        instance.cancelSend();
        assertEquals(30, instance.rowsInSend());
        // Note: It is hoped that one day cancel will proactively clear out the
        //       in progress commands. But that day is not today.
        assertEquals(30, instance.rowsRemaining());
        
        // Test 2.2
        // Add 30 commands, start send, cancel before any sending. (Grbl 0.8c)
        //setUp();
        //instance = new GrblController(mgc);
        instance.rawResponseHandler("Grbl 0.8c");
        for (int i=0; i < 30; i++) {
            instance.queueCommand(instance.createCommand("G0X" + i));
        }
        try {
            instance.beginStreaming();
        } catch (Exception ex) {
            fail("Unexpected exception from GrblController: " +ex.getMessage());
        }
        instance.cancelSend();
        assertEquals(30, instance.rowsInSend());
        assertEquals(30, instance.rowsRemaining());
        
        // Test 3.1
        // Add 30 commands, start send, cancel after sending 15. (Grbl 0.7)
        instance.rawResponseHandler("Grbl 0.7");
        for (int i=0; i < 30; i++) {
            instance.queueCommand(instance.createCommand("G0X0"));
        }
        try {
            instance.beginStreaming();
            for (int i=0; i < 15; i++) {
                GcodeCommand command = new GcodeCommand("G0X0");
                command.setSent(true);
                command.setResponse("ok");
                instance.commandSent(command);
            }
        } catch (Exception ex) {
            fail("Unexpected exception from command sent: " + ex.getMessage());
        }
        instance.cancelSend();
        assertEquals(30, instance.rowsInSend());
        assertEquals(30, instance.rowsRemaining());
        // wrap up
        wrapUp(instance, 15);
        
        // Test 3.2
        // Add 30 commands, start send, cancel after sending 15. (Grbl 0.8c)
        instance.rawResponseHandler("Grbl 0.8c");
        for (int i=0; i < 30; i++) {
            instance.queueCommand(instance.createCommand("G0X" + i));
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
        assertEquals(30, instance.rowsRemaining());
    }
    
    @Ignore("disabled until implementation is finished")
    public void testCancelSendResetBuffer() throws Exception {
        System.out.println("cancelSendResetBuffer");
        GrblController instance = new GrblController(mgc);
        
        // Test 1.1 Cancel when nothing is running (Grbl 0.7).
        instance.rawResponseHandler("Grbl 0.7");
        instance.cancelSend();
        assertEquals(1, mgc.numCancelSendCalls);
        
        // Test 1.2 Cancel when nothing is running (Grbl 0.8c).
        //          Check for soft reset??
        instance.rawResponseHandler("Grbl 0.8c");
        instance.cancelSend();
        assertEquals(2, mgc.numCancelSendCalls);

        // Test 2.1 
        // Add 30 commands, start send, cancel before any sending. (Grbl 0.7)
        instance.rawResponseHandler("Grbl 0.7");
        for (int i=0; i < 30; i++) {
            instance.queueCommand(instance.createCommand("G0X" + i));
        }
        try {
            instance.openCommPort("blah", 123);
            instance.beginStreaming();
        } catch (Exception ex) {
            fail("Unexpected exception from GrblController: " +ex.getMessage());
        }
        instance.cancelSend();
        assertEquals(30, instance.rowsInSend());
        // Note: It is hoped that one day cancel will proactively clear out the
        //       in progress commands. But that day is not today.
        assertEquals(30, instance.rowsRemaining());

        // Test 2.2
        // Add 30 commands, start send, cancel before any sending. (Grbl 0.8c)
        instance.rawResponseHandler("Grbl 0.8c");
        for (int i=0; i < 30; i++) {
            instance.queueCommand(instance.createCommand("G0X" + i));
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
        instance.rawResponseHandler("Grbl 0.7");
        for (int i=0; i < 30; i++) {
            instance.queueCommand(instance.createCommand("G0X" + i));
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
        wrapUp(instance, 15);

        
        // Test 3.2
        // Add 30 commands, start send, cancel after sending 15. (Grbl 0.8c)
        instance.rawResponseHandler("Grbl 0.8c");
        for (int i=0; i < 30; i++) {
            instance.queueCommand(instance.createCommand("G0X" + i));
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
            instance.commandComplete(command.getCommandString());
        } catch (UnexpectedCommand ex) {
            hitException = true;
        }
        assertEquals(true, hitException);
        
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
        try {
            instance.openCommPort("foo", 2400);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        instance.rawResponseHandler("Grbl 0.8c");
        
        // TODO: Test that ok/error trigger listener events.
        
        // TODO: Test that version strings update versions.
        
        // TODO: Test that status strings trigger both listener events
        //          (verbose console and position event)
    }
    /**
     * Test of rawResponseListener method, of class GrblController.
     */
    @Ignore("This has problems on the CI server.")
    public void testPolling() {
        System.out.println("testPolling (via rawResponseListener)");
        String response = "";
        GrblController instance = new GrblController(mgc);
        
        // Test 1. Check that polling works. (Grbl 0.8c)
        instance.rawResponseHandler("Grbl 0.8c");
        try {
            
            // Enough time for a few polling callsthe next poll to be sent.
            Thread.sleep(600);
        } catch (InterruptedException ex) {
            fail("Unexpected exception while testing rawResponseListener: " +ex.getMessage());
        }
        assertTrue(1 <= mgc.numSendByteImmediatelyCalls);
        //assertEquals(1, mgc.numSendByteImmediatelyCalls);
        assertEquals(GrblUtils.GRBL_STATUS_COMMAND, mgc.sentByte);
        
        // Test 2. Check that another poll is sent shortly after receiving a
        //         status message.
        instance.rawResponseHandler("<blah blah blah>");
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
