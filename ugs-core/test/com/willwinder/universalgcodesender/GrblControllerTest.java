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

import com.willwinder.universalgcodesender.AbstractController.UnexpectedCommand;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.mockobjects.MockGrblCommunicator;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.services.MessageService;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.*;
import org.apache.commons.io.FileUtils;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.willwinder.universalgcodesender.GrblUtils.GRBL_PAUSE_COMMAND;
import static com.willwinder.universalgcodesender.model.CommunicatorState.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author wwinder
 */
public class GrblControllerTest {
    private MockGrblCommunicator mgc;
    private static File tempDir;
    private Settings settings = new Settings();

    public GrblControllerTest() {
    }

    @BeforeClass
    static public void setup() throws IOException {
        tempDir = GcodeStreamTest.createTempDirectory();
    }

    @AfterClass
    static public void teardown() throws IOException {
        FileUtils.forceDeleteOnExit(tempDir);
    }

    @Before
    public void setUp() throws Exception {
        this.mgc = new MockGrblCommunicator();

        // Initialize private variable.
        Field f = GUIHelpers.class.getDeclaredField("unitTestMode");
        f.setAccessible(true);
        f.set(null, true);
        Localization.initialize("en_US");
    }

    @After
    public void tearDown() throws Exception {
        // Initialize private variable.
        Field f = GUIHelpers.class.getDeclaredField("unitTestMode");
        f.setAccessible(true);
        f.set(null, false);
    }

    private Settings getSettings() {
        return settings;
    }

    @Test
    public void testGetGrblVersion() throws Exception {
        System.out.println("getGrblVersion");
        GrblController instance = new GrblController(mgc);
        String result;
        String expResult;

        expResult = "<Not connected>";
        result = instance.getGrblVersion();
        assertEquals(expResult, result);

        instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);
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
            result = instance.openCommPort(getSettings().getConnectionDriver(), port, portRate);
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
            instance.openCommPort(getSettings().getConnectionDriver(), port, portRate);
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
            instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);
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
        instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);

        boolean hitIt = false;
        try {
            instance.performHomingCycle();
        } catch (Exception e) {
            hitIt = true;
            assert(e.getMessage().startsWith("No supported homing method for "));
        }
        assertEquals(true, hitIt);

        instance.rawResponseHandler("Grbl 0.7");
        assertEquals(2, mgc.numStreamCommandsCalls);

        hitIt = false;
        try {
            instance.performHomingCycle();
        } catch (Exception e) {
            hitIt = true;
            assert(e.getMessage().startsWith("No supported homing method for this version."));
        }
        assertEquals(true, hitIt);

        instance.rawResponseHandler("Grbl 0.8");
        assertEquals(4, mgc.numStreamCommandsCalls);

        instance.performHomingCycle();
        assertEquals(5, mgc.numStreamCommandsCalls);
        expResult = GrblUtils.GCODE_PERFORM_HOMING_CYCLE_V8;
        assertEquals(expResult, mgc.queuedString);

        instance.rawResponseHandler("Grbl 0.8c");
        assertEquals(7, mgc.numStreamCommandsCalls);

        instance.performHomingCycle();
        assertEquals(8, mgc.numStreamCommandsCalls);
        expResult = GrblUtils.GCODE_PERFORM_HOMING_CYCLE_V8C;
        assertEquals(expResult, mgc.queuedString);

        instance.rawResponseHandler("Grbl 0.9");
        assertEquals(10, mgc.numStreamCommandsCalls);

        instance.performHomingCycle();
        assertEquals(11, mgc.numStreamCommandsCalls);
        expResult = GrblUtils.GCODE_PERFORM_HOMING_CYCLE_V8C;
        assertEquals(expResult, mgc.queuedString);
    }

    @Test
    public void testPerformHomingCycleShouldChangeControllerState() throws Exception {
        GrblController instance = new GrblController(mgc);
        instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);
        instance.rawResponseHandler("Grbl 0.9");
        assertEquals(ControllerState.UNKNOWN, instance.getControllerStatus().getState());

        instance.performHomingCycle();
        assertEquals(ControllerState.HOME, instance.getControllerStatus().getState());
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
        assertEquals(0, mgc.sentBytes.size());
        assertEquals(0, mgc.numCancelSendCalls);

        try {
            instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);
        } catch (Exception e) {
            fail("Unexpected exception from GrblController: " + e.getMessage());
        }

        // Automatic soft reset
        assertEquals(new Byte(GrblUtils.GRBL_RESET_COMMAND), mgc.sentBytes.get(mgc.sentBytes.size()-1));
        assertEquals(0, mgc.numCancelSendCalls);

        // Enable real time mode by sending correct GRBL version:
        instance.rawResponseHandler("Grbl 0.8c");
        instance.issueSoftReset();
        // Sent reset command to communicator and issued reset.
        assertEquals(new Byte(GrblUtils.GRBL_RESET_COMMAND), mgc.sentBytes.get(mgc.sentBytes.size()-1));
        assertEquals(1, mgc.numCancelSendCalls);

        // GRBL version that might not have the command but I send it to anyway:
        mgc.resetInputsAndFunctionCalls();
        instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);
        assertEquals(new Byte(GrblUtils.GRBL_RESET_COMMAND), mgc.sentBytes.get(mgc.sentBytes.size()-1));
        instance.rawResponseHandler("Grbl 0.8a");
        instance.issueSoftReset();
        // This version doesn't support soft reset.
        assertEquals(0, mgc.numCancelSendCalls);

        // GRBL version that should not be sent the command:
        mgc.resetInputsAndFunctionCalls();
        instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);
        assertEquals(new Byte(GrblUtils.GRBL_RESET_COMMAND), mgc.sentBytes.get(mgc.sentBytes.size()-1));
        mgc.sentBytes.clear();
        instance.rawResponseHandler("Grbl 0.7");
        instance.issueSoftReset();
        // Sent reset command to communicator and issued reset.
        assertEquals(0, mgc.sentBytes.size());
        assertEquals(0, mgc.numCancelSendCalls);
    }

    /**
     * Test of getSendDuration method, of class GrblController.
     */
    @Test
    public void testGetSendDuration() throws Exception {
        System.out.println("getSendDuration");
        GrblController instance = new GrblController(mgc);
        instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);
        instance.rawResponseHandler("Grbl 0.8c");
        instance.rawResponseHandler("<Idle,WPos:1,2,3,MPos:1,2,3>");

        // Test 1.
        // Result when not sending and nothing has been sent.
        long expResult = 0L;
        long result = instance.getSendDuration();
        assertEquals(expResult, result);

        // Test 2.
        // Result when stream has begun but not completed.
        instance.queueStream(new SimpleGcodeStreamReader("G0X1"));
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
        instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);
        instance.rawResponseHandler("Grbl 0.8c");

        // Test 1.
        // When not sending, no commands queues, everything should be zero.
        assertCounts(instance, 0, 0, 0);

        // Add 30 commands.
        List<GcodeCommand> commands = new ArrayList<>();
        for (int i=0; i < 30; i++) {
            commands.add(new GcodeCommand("G0X" + i));
        }
        instance.queueStream(new SimpleGcodeStreamReader(commands));


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
        instance.openCommPort(getSettings().getConnectionDriver(), "blah", 123);
        instance.rawResponseHandler("Grbl 0.8c");
        //$G and $$ get queued on startup
        assertEquals(2, mgc.numQueueStringForCommCalls);
        assertEquals(2, mgc.numStreamCommandsCalls);
        GcodeCommand command = instance.createCommand(str);
        instance.sendCommandImmediately(command);
        assertEquals(3, mgc.numQueueStringForCommCalls);
        assertEquals(3, mgc.numStreamCommandsCalls);
        assertEquals(str, mgc.queuedString);
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
            instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);
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
        instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);
        instance.rawResponseHandler("Grbl 0.8c");
        Boolean result = instance.isReadyToStreamFile();
        assertEquals(true, result);

        // Test 4. Can't send during active command.
        instance.queueStream(new SimpleGcodeStreamReader(instance.createCommand("G0X0")));
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
     * Test of beginStreaming method, of class GrblController.
     */
    @Test
    public void testBeginStreaming() throws Exception {
        System.out.println("beginStreaming");
        GrblController instance = new GrblController(mgc);

        instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);
        instance.rawResponseHandler("Grbl 0.8c");
        instance.rawResponseHandler("<Idle,WPos:1,2,3,MPos:1,2,3>");

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
        GcodeCommand command = instance.createCommand("G0X1");
        instance.queueStream(new SimpleGcodeStreamReader(command));
        caughtException = false;
        try {
            // Trigger the error.
            instance.beginStreaming();
        } catch (Exception ex) {
            caughtException = true;
            assertEquals("Cannot stream while there are active commands (controller).", ex.getMessage());
        }
        assertFalse(caughtException);
        assertEquals(2, mgc.numQueueStringForCommCalls);
        assertEquals(3, mgc.numStreamCommandsCalls);

        // Wrap up test 2.
        command.setSent(true);
        command.setResponse("ok");
        instance.commandSent(command);
        instance.commandComplete(command.getCommandString());

        // Test 3. Stream some commands and make sure they get sent.
        List<GcodeCommand> commands = new ArrayList<>();
        for (int i=0; i < 30; i++) {
            commands.add(instance.createCommand("G0X" + i));
        }
        instance.queueStream(new SimpleGcodeStreamReader(commands));


        try {
            // Trigger the error.
            instance.beginStreaming();
        } catch (Exception ex) {
            caughtException = true;
            assertEquals("Cannot stream while there are active commands (controller).", ex.getMessage());
        }
        assertFalse(caughtException);

        assertEquals(30, instance.rowsRemaining());
        assertEquals(2, mgc.numQueueStringForCommCalls);
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
        instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);
        assertEquals(new Byte(GrblUtils.GRBL_RESET_COMMAND), mgc.sentBytes.get(mgc.sentBytes.size()-1));

        instance.pauseStreaming();
        assertEquals(1, mgc.numPauseSendCalls);
        mgc.sentBytes.clear();

        instance.rawResponseHandler("Grbl 0.7");
        instance.pauseStreaming();
        assertEquals(2, mgc.numPauseSendCalls);
        assertEquals(0, mgc.sentBytes.size());

        instance.rawResponseHandler("Grbl 0.8c");
        instance.pauseStreaming();
        assertEquals(3, mgc.numPauseSendCalls);
        assertEquals(new Byte(GrblUtils.GRBL_PAUSE_COMMAND), mgc.sentBytes.get(mgc.sentBytes.size()-1));
    }

    /**
     * Test of resumeStreaming method, of class GrblController.
     */
    @Test
    public void testResumeStreaming() throws Exception {
        System.out.println("resumeStreaming");
        GrblController instance = new GrblController(mgc);
        instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);
        assertEquals(new Byte(GrblUtils.GRBL_RESET_COMMAND), mgc.sentBytes.get(mgc.sentBytes.size()-1));

        instance.resumeStreaming();
        assertEquals(1, mgc.numResumeSendCalls);

        instance.rawResponseHandler("Grbl 0.7");
        instance.resumeStreaming();
        assertEquals(2, mgc.numResumeSendCalls);
        assertEquals(new Byte(GrblUtils.GRBL_RESET_COMMAND), mgc.sentBytes.get(mgc.sentBytes.size()-1));

        instance.rawResponseHandler("Grbl 0.8c");
        instance.resumeStreaming();
        assertEquals(3, mgc.numResumeSendCalls);
        assertEquals(new Byte(GrblUtils.GRBL_RESUME_COMMAND), mgc.sentBytes.get(mgc.sentBytes.size()-1));

    }

    private static void wrapUp(GrblController gc, int numCommands) throws UnexpectedCommand {
        // wrap up
        GcodeCommand command = new GcodeCommand("blah");
        command.setSent(true);
        command.setResponse("ok");
        for (int i=0; i < numCommands; i++) {
            gc.commandComplete(command.getCommandString());
        }
    }

    @Test
    public void cancelSendShouldNotReturnToIdleDuringCancel() throws Exception {
        // 0. Test GRBL not returning to idle during cancel.
        this.mgc = new MockGrblCommunicator();
        GrblController instance = new GrblController(mgc);
        instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);
        instance.rawResponseHandler("Grbl 0.8c");
        instance.cancelSend();
        for (int i = 0; i < 50; i++) {
            instance.rawResponseHandler("<Running,MPos:1.0,2.0,3.0>");
        }
        assertEquals(1, mgc.numPauseSendCalls);
        assertEquals(1, mgc.numCancelSendCalls);
        instance.resumeStreaming();
    }

    @Test
    public void cancelWhenNothingIsRunningGrblOn0_7() throws Exception {
        // Test 1.1 Cancel when nothing is running (Grbl 0.7).
        this.mgc = new MockGrblCommunicator();
        GrblController instance = new GrblController(mgc);
        instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);
        instance.rawResponseHandler("Grbl 0.7");
        instance.cancelSend();
        assertEquals(1, mgc.numCancelSendCalls);
        assertEquals(0, mgc.numPauseSendCalls);
    }

    @Test
    public void cancelWhenNothingIsRunningGrblOn0_8c() throws Exception {
        // Test 1.2 Cancel when nothing is running (Grbl 0.8c).
        //          Check for soft reset.
        this.mgc = new MockGrblCommunicator();
        GrblController instance = new GrblController(mgc);
        instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);
        instance.rawResponseHandler("Grbl 0.8c");
        instance.cancelSend();
        instance.rawResponseHandler("<Hold,MPos:1.0,2.0,3.0>");
        instance.rawResponseHandler("<Hold,MPos:1.0,2.0,3.0>");
        assertEquals(2, mgc.numCancelSendCalls);
        assertEquals(1, mgc.numPauseSendCalls);
        instance.resumeStreaming();
    }

    @Test
    public void cancelSendResumeAfterCancelBeforeAnySendingOnGrbl0_7() throws Exception {
        // Test 2.1
        // Add 30 commands, start send, cancel before any sending. (Grbl 0.7)
        this.mgc = new MockGrblCommunicator();
        GrblController instance = new GrblController(mgc);
        instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);
        instance.rawResponseHandler("Grbl 0.7");
        List<GcodeCommand> commands = new ArrayList<>();
        for (int i=0; i < 30; i++) {
            commands.add(instance.createCommand("G0X" + i));
        }
        instance.queueStream(new SimpleGcodeStreamReader(commands));
        try {
            instance.beginStreaming();
        } catch (Exception ex) {
            fail("Unexpected exception from GrblController: " +ex.getMessage());
        }
        instance.cancelSend();
        assertEquals(1, mgc.numCancelSendCalls);
        assertEquals(0, mgc.numPauseSendCalls);
        assertEquals(30, instance.rowsInSend());
        assertEquals(30, instance.rowsRemaining());
    }

    @Test
    public void cancelSendResumeAfterCancelBeforeAnySendingOnGrbl0_8c() throws Exception {
        // Test 2.2
        // Add 30 commands, start send, cancel before any sending. (Grbl 0.8c)
        // 2nd cancel is from a soft reset.
        this.mgc = new MockGrblCommunicator();
        GrblController instance = new GrblController(mgc);
        instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);
        instance.rawResponseHandler("Grbl 0.8c");
        List<GcodeCommand> commands = new ArrayList<>();
        for (int i=0; i < 30; i++) {
            commands.add(instance.createCommand("G0X" + i));
        }
        instance.queueStream(new SimpleGcodeStreamReader(commands));

        try {
            instance.beginStreaming();
        } catch (Exception ex) {
            fail("Unexpected exception from GrblController: " +ex.getMessage());
        }
        instance.cancelSend();
        instance.rawResponseHandler("<Hold,MPos:1.0,2.0,3.0>");
        instance.rawResponseHandler("<Hold,MPos:1.0,2.0,3.0>");
        assertEquals(0, instance.rowsInSend());
        assertEquals(0, instance.rowsRemaining());
        assertEquals(2, mgc.numCancelSendCalls);
        assertEquals(1, mgc.numPauseSendCalls);
        instance.resumeStreaming();
    }

    @Test
    public void cancelSendHalfwayThroughJobOnGrbl0_7() throws Exception {
        // Test 3.1
        // Add 30 commands, start send, cancel after sending 15. (Grbl 0.7)
        this.mgc = new MockGrblCommunicator();
        GrblController instance = new GrblController(mgc);
        instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);
        instance.rawResponseHandler("Grbl 0.7");
        List<GcodeCommand> commands = new ArrayList<>();
        for (int i=0; i < 30; i++) {
            commands.add(instance.createCommand("G0X0"));
        }
        instance.queueStream(new SimpleGcodeStreamReader(commands));

        instance.beginStreaming();
        for (int i=0; i < 15; i++) {
            GcodeCommand command = new GcodeCommand("G0X0");
            command.setSent(true);
            command.setResponse("ok");
            instance.commandSent(command);
        }

        instance.cancelSend();
        assertEquals(1, mgc.numCancelSendCalls);
        assertEquals(0, mgc.numPauseSendCalls); // not supported in 0.7
        assertEquals(30, instance.rowsInSend());
        assertEquals(30, instance.rowsRemaining());
        // wrap up
        wrapUp(instance, 15);
    }

    @Test
    public void cancelSendHalfwayThroughJobOnGrbl0_8c() throws Exception {
        // Test 3.2
        // Add 30 commands, start send, cancel after sending 15. (Grbl 0.8c)
        this.mgc = new MockGrblCommunicator();
        GrblController instance = new GrblController(mgc);
        instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);
        instance.rawResponseHandler("Grbl 0.8c");
        List<GcodeCommand> commands = new ArrayList<>();
        for (int i=0; i < 30; i++) {
            commands.add(instance.createCommand("G0X" + i));
        }
        instance.queueStream(new SimpleGcodeStreamReader(commands));

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
        instance.rawResponseHandler("<Hold,MPos:1.0,2.0,3.0>");
        instance.rawResponseHandler("<Hold,MPos:1.0,2.0,3.0>");
        assertEquals(15, instance.rowsSent());
        assertEquals(0, instance.rowsInSend());
        assertEquals(0, instance.rowsRemaining());
        assertEquals(2, mgc.numCancelSendCalls);
        assertEquals(1, mgc.numPauseSendCalls);
        assertEquals(new Byte(GrblUtils.GRBL_RESET_COMMAND), mgc.sentBytes.get(mgc.sentBytes.size()-1));
        instance.resumeStreaming();
    }

    private void sendStuff(GrblController instance) throws Exception {
        List<GcodeCommand> commands = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            commands.add(instance.createCommand("G0X" + i));
        }
        instance.queueStream(new SimpleGcodeStreamReader(commands));
        instance.beginStreaming();
    }

    @Test
    public void testPauseAndCancelSend() throws Exception {
        System.out.println("Pause + cancelSend");
        GrblController instance;

        // Test 1.1 cancel throws an exception (Grbl 0.7).
        this.mgc = new MockGrblCommunicator();
        instance = new GrblController(mgc);
        instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);
        instance.rawResponseHandler("Grbl 0.7");
        sendStuff(instance);
        boolean threwException = false;
        try {
            instance.pauseStreaming();
            instance.cancelSend();
        } catch (Exception ex) {
            threwException = true;
        }
        assertTrue(threwException);
        assertEquals(0, mgc.numCancelSendCalls);
        instance.resumeStreaming();

        // Test 1.2 Cancel when nothing is running (Grbl 0.8c).
        //          Check for soft reset.
        this.mgc = new MockGrblCommunicator();
        instance = new GrblController(mgc);
        instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);
        instance.rawResponseHandler("Grbl 0.8c");
        instance.pauseStreaming();
        instance.cancelSend();
        instance.rawResponseHandler("<Hold,MPos:1.0,2.0,3.0>");
        instance.rawResponseHandler("<Hold,MPos:1.0,2.0,3.0>");
        assertEquals(2, mgc.numCancelSendCalls);
        instance.resumeStreaming();

        // Test 2.1
        // Add 30 commands, start send, cancel before any sending. (Grbl 0.7)
        this.mgc = new MockGrblCommunicator();
        instance = new GrblController(mgc);
        instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);
        instance.rawResponseHandler("Grbl 0.7");
        List<GcodeCommand> commands = new ArrayList<>();
        for (int i=0; i < 30; i++) {
            commands.add(instance.createCommand("G0X" + i));
        }
        instance.queueStream(new SimpleGcodeStreamReader(commands));

        try {
            instance.beginStreaming();
        } catch (Exception ex) {
            fail("Unexpected exception from GrblController: " +ex.getMessage());
        }
        instance.pauseStreaming();

        boolean exceptionThrown = false;
        try {
            instance.cancelSend();
        } catch (Exception e) {
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        assertEquals(30, instance.rowsInSend());
        // Note: It is hoped that one day cancel will proactively clear out the
        //       in progress commands. But that day is not today.
        assertEquals(30, instance.rowsRemaining());
        instance.resumeStreaming();
        instance.cancelSend();

        // Test 2.2
        // Add 30 commands, start send, cancel before any sending. (Grbl 0.8c)
        this.mgc = new MockGrblCommunicator();
        instance = new GrblController(mgc);
        instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);
        instance.rawResponseHandler("Grbl 0.8c");
        commands = new ArrayList<>();
        for (int i=0; i < 30; i++) {
            commands.add(instance.createCommand("G0X" + i));
        }
        instance.queueStream(new SimpleGcodeStreamReader(commands));

        try {
            instance.beginStreaming();
        } catch (Exception ex) {
            fail("Unexpected exception from GrblController: " +ex.getMessage());
        }
        instance.pauseStreaming();
        instance.cancelSend();
        instance.rawResponseHandler("<Hold,MPos:1.0,2.0,3.0>");
        instance.rawResponseHandler("<Hold,MPos:1.0,2.0,3.0>");
        assertEquals(0, instance.rowsInSend());
        assertEquals(0, instance.rowsRemaining());
        instance.resumeStreaming();

        // Test 3.1 - N/A, exception thrown.

        // Test 3.2
        // Add 30 commands, start send, cancel after sending 15. (Grbl 0.8c)
        this.mgc = new MockGrblCommunicator();
        instance = new GrblController(mgc);
        instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);
        instance.rawResponseHandler("Grbl 0.8c");
        commands = new ArrayList<>();
        for (int i=0; i < 30; i++) {
            commands.add(instance.createCommand("G0X" + i));
        }
        instance.queueStream(new SimpleGcodeStreamReader(commands));

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

        instance.pauseStreaming();
        instance.cancelSend();
        instance.rawResponseHandler("<Hold,MPos:1.0,2.0,3.0>");
        instance.rawResponseHandler("<Hold,MPos:1.0,2.0,3.0>");
        assertEquals(15, instance.rowsSent());
        assertEquals(0, instance.rowsInSend());
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
    public void testDispatchConsoleInfoMessage() {
        String msg = "test message";
        GrblController instance = new GrblController(mgc);

        MessageService messageService = mock(MessageService.class);
        instance.setMessageService(messageService);

        instance.dispatchConsoleMessage(MessageType.INFO, msg);

        verify(messageService, times(1)).dispatchMessage(MessageType.INFO, msg);
    }

    /**
     * Test of verboseMessageForConsole method, of class GrblController.
     */
    @Test
    public void testDispatchConsoleVerboseMessage() {
        String msg = "test message";
        GrblController instance = new GrblController(mgc);

        MessageService messageService = mock(MessageService.class);
        instance.setMessageService(messageService);

        instance.dispatchConsoleMessage(MessageType.VERBOSE, msg);

        verify(messageService, times(1)).dispatchMessage(MessageType.VERBOSE, msg);
    }

    /**
     * Test of rawResponseListener method, of class GrblController.
     */
    @Test
    public void testRawResponseListener() throws Exception {
        System.out.println("rawResponseListener");
        GrblController instance = new GrblController(mgc);
        instance.openCommPort(getSettings().getConnectionDriver(), "foo", 2400);
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
        assertEquals(new Byte(GrblUtils.GRBL_STATUS_COMMAND), mgc.sentBytes.get(mgc.sentBytes.size()-1));

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
        assertEquals(new Byte(GrblUtils.GRBL_STATUS_COMMAND), mgc.sentBytes.get(mgc.sentBytes.size()-1));

        // Test 3. Check that after a long enough delay, additional polls are
        //         sent even without responses.
        try {
            // Enough time for a few polling callsthe next poll to be sent.
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            fail("Unexpected exception while testing rawResponseListener: " +ex.getMessage());
        }
        assert(2 < mgc.numSendByteImmediatelyCalls);
        assertEquals(new Byte(GrblUtils.GRBL_STATUS_COMMAND), mgc.sentBytes.get(mgc.sentBytes.size()-1));
    }

    /**
     * Test of jogMachine method, of class AbstractController.
     */
    @Test
    public void testJogMachine() throws Exception {
        System.out.println("jogMachine");
        GrblController instance = new GrblController(mgc);

        instance.setDistanceModeCode("G90");
        instance.setUnitsCode("G21");
        instance.openCommPort(getSettings().getConnectionDriver(), "foo", 2400);

        // Abstract controller should be used when grbl jog mode is disabled.
        instance.rawResponseHandler("Grbl 0.8c");
        instance.jogMachine(new PartialPosition(-10., null, 10., UnitUtils.Units.INCH), 11);
        assertEquals(mgc.queuedStrings.get(2), "G20G91G1X-10Z10F11");
        assertEquals(mgc.queuedStrings.get(3), "G90 G21 ");

        instance.jogMachine(new PartialPosition(null, 10., null, UnitUtils.Units.MM), 11);
        assertEquals(mgc.queuedStrings.get(4), "G21G91G1Y10F11");
        assertEquals(mgc.queuedStrings.get(5), "G90 G21 ");

        instance.rawResponseHandler("Grbl 1.1a");
        instance.jogMachine(new PartialPosition(-10., null, 10., UnitUtils.Units.INCH), 11);
        assertEquals(mgc.queuedStrings.get(8), "$J=G20G91X-10Z10F11");

        instance.jogMachine(new PartialPosition(null, 10., null, UnitUtils.Units.MM), 11);
        assertEquals(mgc.queuedStrings.get(9), "$J=G21G91Y10F11");
    }

    /**
     * Test of jogMachineTo method
     */
    @Test
    public void testJogMachineTo() throws Exception {
        System.out.println("jogMachineTo");
        GrblController instance = new GrblController(mgc);

        instance.setDistanceModeCode("G90");
        instance.setUnitsCode("G21");
        instance.openCommPort(getSettings().getConnectionDriver(), "foo", 2400);

        // Abstract controller should be used when grbl jog mode is disabled.
        instance.rawResponseHandler("Grbl 0.8c");
        instance.jogMachineTo(new PartialPosition(1.0, 2.0, 3.0, UnitUtils.Units.MM), 200);
        assertEquals("G21G90G1X1Y2Z3F200", mgc.queuedStrings.get(2));
        assertEquals("G90 G21 ", mgc.queuedStrings.get(3));

        instance.jogMachineTo(new PartialPosition(1.0, 2.0, UnitUtils.Units.MM), 200);
        assertEquals("G21G90G1X1Y2F200", mgc.queuedStrings.get(4));
        assertEquals("G90 G21 ", mgc.queuedStrings.get(5));

        instance.jogMachineTo(new PartialPosition(1.2345678, 2.0, UnitUtils.Units.MM), 200);
        assertEquals("G21G90G1X1.235Y2F200", mgc.queuedStrings.get(6));
        assertEquals("G90 G21 ", mgc.queuedStrings.get(7));

        instance.jogMachineTo(new PartialPosition(1.0, 2.0, UnitUtils.Units.INCH), 200);
        assertEquals("G20G90G1X1Y2F200", mgc.queuedStrings.get(8));
        assertEquals("G90 G21 ", mgc.queuedStrings.get(9));
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

    @Test
    public void testReturnToHomeWhenZIsPositive() throws Exception {
        // Set up
        GrblController instance = new GrblController(mgc);
        instance.openCommPort(getSettings().getConnectionDriver(), "foo", 2400);

        instance.rawResponseHandler("Grbl 0.8c");
        instance.rawResponseHandler("<Idle,MPos:1.000,1.000,1.000,WPos:0.0,0.0,0.0>");

        // Test the function for going home
        instance.returnToHome(0);

        assertEquals(4, mgc.queuedStrings.size());
        assertEquals("View all grbl settings", "$$", mgc.queuedStrings.get(0));
        assertEquals("View gcode parser state", "$G", mgc.queuedStrings.get(1));
        assertEquals("Go to XY-zero", "G90 G0 X0 Y0", mgc.queuedStrings.get(2));
        assertEquals("Go to Z-zero", "G90 G0 Z0", mgc.queuedStrings.get(3));
    }

    @Test
    public void testReturnToHomeWhenWorkPositionZIsNegative() throws Exception {
        // Set up
        GrblController instance = new GrblController(mgc);
        instance.openCommPort(getSettings().getConnectionDriver(), "foo", 2400);

        instance.rawResponseHandler("Grbl 0.8c");
        instance.rawResponseHandler("<Idle,MPos:1.000,1.000,1.000,WPos:0.0,0.0,-1.0>");

        // Test the function for going home
        instance.returnToHome(0);

        assertEquals(5, mgc.queuedStrings.size());
        assertEquals("View all grbl settings", "$$", mgc.queuedStrings.get(0));
        assertEquals("View gcode parser state", "$G", mgc.queuedStrings.get(1));
        assertEquals("The machine is in the material, go to zero with the Z axis first", "G90 G0 Z0", mgc.queuedStrings.get(2));
        assertEquals("Go to XY-zero", "G90 G0 X0 Y0", mgc.queuedStrings.get(3));
        assertEquals("Go to Z-zero", "G90 G0 Z0", mgc.queuedStrings.get(4));
    }

    @Test
    public void testReturnToHomeWhenWorkPositionZIsNegativeInMmAndWithSafetyHeightEnabled() throws Exception {
        // Set up
        GrblController instance = new GrblController(mgc);
        instance.getCurrentGcodeState().units = Code.G21;
        instance.getCurrentGcodeState().distanceMode = Code.G90;

        instance.openCommPort(getSettings().getConnectionDriver(), "foo", 2400);

        instance.rawResponseHandler("Grbl 0.8c");
        instance.rawResponseHandler("<Idle,MPos:1.000,1.000,1.000,WPos:0.0,0.0,-1.0>");

        // Test the function for going home
        instance.returnToHome(10);

        assertEquals(5, mgc.queuedStrings.size());
        assertEquals("View all grbl settings", "$$", mgc.queuedStrings.get(0));
        assertEquals("View gcode parser state", "$G", mgc.queuedStrings.get(1));
        assertEquals("The machine is in the material, go to safety height in mm with the Z axis first", "G21G90 G0Z10", mgc.queuedStrings.get(2));
        assertEquals("Go to XY-zero", "G90 G0 X0 Y0", mgc.queuedStrings.get(3));
        assertEquals("Go to Z-zero", "G90 G0 Z0", mgc.queuedStrings.get(4));
    }

    @Test
    public void testReturnToHomeWhenWorkPositionZIsNegativeInInchAndWithSafetyHeightEnabled() throws Exception {
        // Set up
        GrblController instance = new GrblController(mgc);
        instance.getCurrentGcodeState().units = Code.G20;
        instance.getCurrentGcodeState().distanceMode = Code.G90;
        instance.openCommPort(getSettings().getConnectionDriver(), "foo", 2400);

        instance.rawResponseHandler("Grbl 0.8c");
        instance.rawResponseHandler("<Idle,MPos:1.000,1.000,1.000,WPos:0.0,0.0,-1.0>");

        // Test the function for going home
        instance.returnToHome(10);

        assertEquals(5, mgc.queuedStrings.size());
        assertEquals("View all grbl settings", "$$", mgc.queuedStrings.get(0));
        assertEquals("View gcode parser state", "$G", mgc.queuedStrings.get(1));
        assertEquals("The machine is in the material, go to safety height in inches with the Z axis first", "G20G90 G0Z0.394", mgc.queuedStrings.get(2));
        assertEquals("Go to XY-zero", "G90 G0 X0 Y0", mgc.queuedStrings.get(3));
        assertEquals("Go to Z-zero", "G90 G0 Z0", mgc.queuedStrings.get(4));
    }


    @Test
    public void testReturnToHomeWhenWorkPositionZIsOverSafetyHeightEnabled() throws Exception {
        // Set up
        GrblController instance = new GrblController(mgc);
        instance.openCommPort(getSettings().getConnectionDriver(), "foo", 2400);

        instance.rawResponseHandler("Grbl 0.8c");
        instance.rawResponseHandler("<Idle,MPos:1.000,1.000,1.000,WPos:0.0,0.0,11.0>");

        // Test the function for going home
        instance.returnToHome(10);

        assertEquals(4, mgc.queuedStrings.size());
        assertEquals("View all grbl settings", "$$", mgc.queuedStrings.get(0));
        assertEquals("View gcode parser state", "$G", mgc.queuedStrings.get(1));
        assertEquals("Go to XY-zero", "G90 G0 X0 Y0", mgc.queuedStrings.get(2));
        assertEquals("Go to Z-zero", "G90 G0 Z0", mgc.queuedStrings.get(3));
    }

    @Test
    public void rawResponseHandlerWithKnownErrorShouldWriteMessageToConsole() throws Exception {
        // Given
        GrblController instance = new GrblController(mgc);
        instance.openCommPort(getSettings().getConnectionDriver(), "foo", 2400);
        instance.commandSent(new GcodeCommand("G0"));

        MessageService messageService = mock(MessageService.class);
        instance.setMessageService(messageService);

        // When
        instance.rawResponseHandler("error:1");

        //Then
        String genericErrorMessage = "Error while processing response <error:1>\n";
        verify(messageService, times(0)).dispatchMessage(MessageType.ERROR, genericErrorMessage);

        String errorMessage = "An error was detected while sending 'G0': (error:1) G-code words consist of a letter and a value. Letter was not found. Streaming has been paused.\n";
        verify(messageService).dispatchMessage(MessageType.ERROR, errorMessage);

        verify(messageService, times(1)).dispatchMessage(any(), anyString());

        assertFalse(instance.getActiveCommand().isPresent());
    }

    @Test
    public void rawResponseHandlerWithUnknownErrorShouldWriteGenericMessageToConsole() throws Exception {
        // Given
        GrblController instance = new GrblController(mgc);
        instance.setDistanceModeCode("G90");
        instance.setUnitsCode("G21");
        instance.openCommPort(getSettings().getConnectionDriver(), "foo", 2400);
        instance.commandSent(new GcodeCommand("G21"));

        MessageService messageService = mock(MessageService.class);
        instance.setMessageService(messageService);

        // When
        instance.rawResponseHandler("error:18");

        // Then
        String genericErrorMessage = "An error was detected while sending 'G21': (error:18) An unknown error has occurred. Streaming has been paused.\n";
        verify(messageService, times(1)).dispatchMessage(MessageType.ERROR, genericErrorMessage);
        verify(messageService, times(1)).dispatchMessage(any(), anyString());

        assertFalse(instance.getActiveCommand().isPresent());
    }

    @Test
    public void rawResponseHandlerOnErrorWithNoSentCommandsShouldSendMessageToConsole() throws Exception {
        // Given
        GrblController instance = new GrblController(mgc);
        instance.setDistanceModeCode("G90");
        instance.setUnitsCode("G21");
        instance.openCommPort(getSettings().getConnectionDriver(), "foo", 2400);

        MessageService messageService = mock(MessageService.class);
        instance.setMessageService(messageService);

        // When
        instance.rawResponseHandler("error:1");

        // Then
        String genericErrorMessage = "An unexpected error was detected: (error:1) G-code words consist of a letter and a value. Letter was not found.\n";
        verify(messageService, times(1)).dispatchMessage(MessageType.INFO, genericErrorMessage);
        verify(messageService, times(1)).dispatchMessage(any(), anyString());

        assertFalse(instance.getActiveCommand().isPresent());
    }

    @Test
    public void controllerShouldBeIdleWhenInCheckMode() throws Exception {
        // Given
        GrblController instance = new GrblController(mgc);
        instance.setDistanceModeCode("G90");
        instance.setUnitsCode("G21");

        instance.openCommPort(getSettings().getConnectionDriver(), "foo", 2400);
        instance.rawResponseHandler("Grbl 1.1f"); // We will assume that we are using version Grbl 1.0 with streaming support
        instance.rawResponseHandler("<Hold|MPos:0.000,0.000,0.000|FS:0,0|Pn:XYZ>");
        assertFalse("We should start with a non idle state", instance.isIdle());

        // When
        instance.rawResponseHandler("<Check|MPos:0.000,0.000,0.000|FS:0,0|Pn:XYZ>");

        // Then
        assertTrue(instance.isIdle());
        assertTrue(instance.isIdleEvent());
        assertEquals(COMM_CHECK, instance.getControlState());
        assertEquals(ControllerState.CHECK, instance.getControllerStatus().getState());
    }

    @Test
    public void versionStringShouldResetStatus() throws Exception {
        // Given
        GrblController instance = new GrblController(mgc);
        instance.openCommPort(getSettings().getConnectionDriver(), "foo", 2400);
        instance.rawResponseHandler("Grbl 1.1f");
        instance.rawResponseHandler("<Run|MPos:0.000,0.000,0.000|FS:0,0|Pn:XYZ>");
        assertEquals("We should be in sending mode", COMM_SENDING, instance.getControlState());
        assertEquals(ControllerState.RUN, instance.getControllerStatus().getState());

        // When
        instance.rawResponseHandler("Grbl 1.1f");

        // Then
        assertEquals(COMM_IDLE, instance.getControlState());
        assertEquals(ControllerState.UNKNOWN, instance.getControllerStatus().getState());
    }

    /**
     * When exiting check mode the controller does a soft reset and sends a new version string. The
     * default behavior is to reset the controller status. But we need it to determine if single
     * step mode is supposed to be activated.
     */
    @Test
    public void versionStringShouldNotResetStatusWhenInCheckMode() throws Exception {
        // Given
        GrblController instance = new GrblController(mgc);
        instance.openCommPort(getSettings().getConnectionDriver(), "foo", 2400);
        instance.rawResponseHandler("Grbl 1.1f");
        instance.rawResponseHandler("<Check|MPos:0.000,0.000,0.000|FS:0,0|Pn:XYZ>");
        assertEquals("We should be in check mode", COMM_CHECK, instance.getControlState());

        // When
        instance.rawResponseHandler("Grbl 1.1f");

        // Then
        assertEquals(COMM_CHECK, instance.getControlState());
    }

    @Test
    public void controllerShouldHaveStateRunningWhenStreamingAndInCheckMode() throws Exception {
        // Given
        GrblController instance = new GrblController(mgc);
        instance.setDistanceModeCode("G90");
        instance.setUnitsCode("G21");

        instance.openCommPort(getSettings().getConnectionDriver(), "foo", 2400);
        instance.rawResponseHandler("Grbl 1.1f"); // We will assume that we are using version Grbl 1.0 with streaming support
        instance.rawResponseHandler("<Check|MPos:0.000,0.000,0.000|FS:0,0|Pn:XYZ>");
        assertTrue(instance.isIdle());

        // Create a gcode file stream
        File gcodeFile = new File(tempDir, "gcodeFile");
        GcodeStreamWriter gcodeStreamWriter = new GcodeStreamWriter(gcodeFile);
        gcodeStreamWriter.addLine("G0", "G0", null, 0);
        gcodeStreamWriter.addLine("G0", "G0", null, 0);
        gcodeStreamWriter.close();

        // When
        instance.queueStream(new GcodeStreamReader(gcodeFile));
        instance.beginStreaming();

        // Then
        assertFalse(instance.isIdle());
        assertFalse(instance.isPaused());
        assertEquals(COMM_SENDING, instance.getControlState());
    }

    @Test
    public void controllerShouldBeIdleWhenInCheckModeWithOldStatusFormat() throws Exception {
        // Given
        GrblController instance = new GrblController(mgc);
        instance.setDistanceModeCode("G90");
        instance.setUnitsCode("G21");

        instance.openCommPort(getSettings().getConnectionDriver(), "foo", 2400);
        instance.rawResponseHandler("Grbl 0.8c"); // We will assume that we are using version Grbl without streaming support
        instance.rawResponseHandler("<Hold,MPos:0.000,0.000,0.000|FS:0,0|Pn:XYZ>");
        assertFalse("We should start with a non idle state", instance.isIdle());

        // When
        instance.rawResponseHandler("<Check,MPos:0.000,0.000,0.000|FS:0,0|Pn:XYZ>");

        // Then
        assertTrue(instance.isIdle());
        assertTrue(instance.isIdleEvent());
        assertEquals(COMM_CHECK, instance.getControlState());
        assertEquals(ControllerState.CHECK, instance.getControllerStatus().getState());
    }

    @Test
    public void errorInCheckModeNotSending() {
        // Given
        AbstractCommunicator communicator = mock(AbstractCommunicator.class);
        when(communicator.isConnected()).thenReturn(true);
        GrblController gc = new GrblController(communicator);
        gc.rawResponseHandler("Grbl 1.1f"); // We will assume that we are using version Grbl 1.0 with streaming support
        gc.rawResponseHandler("<Check|MPos:0.000,0.000,0.000|FS:0,0|Pn:XYZ>");

        // When
        gc.communicatorPausedOnError();
        gc.rawResponseHandler("error:1");

        // Then
        assertEquals(COMM_CHECK, gc.getControlState());
        assertEquals(ControllerState.CHECK, gc.getControllerStatus().getState());
        assertFalse(gc.isPaused());
        verify(communicator, times(1)).resumeSend();
    }

    @Test
    public void errorInCheckModeSending() throws Exception {
        // Given
        AbstractCommunicator communicator = mock(AbstractCommunicator.class);
        when(communicator.isConnected()).thenReturn(true);
        ControllerListener controllerListener = mock(ControllerListener.class);
        GrblController gc = new GrblController(communicator);
        gc.addListener(controllerListener);
        gc.rawResponseHandler("Grbl 1.1f"); // We will assume that we are using version Grbl 1.0 with streaming support
        gc.rawResponseHandler("<Check|MPos:0.000,0.000,0.000|FS:0,0|Pn:XYZ>");
        doReturn(true).when(communicator).isConnected();

        // When
        gc.queueStream(new SimpleGcodeStreamReader("G0 X10"));
        gc.beginStreaming();
        gc.communicatorPausedOnError();
        gc.rawResponseHandler("error:1");

        // Then
        assertEquals(COMM_SENDING, gc.getControlState());
        assertEquals(ControllerState.CHECK, gc.getControllerStatus().getState());
        assertFalse(gc.isPaused());
        verify(communicator, times(1)).sendByteImmediately(GRBL_PAUSE_COMMAND);
        verify(controllerListener, times(1)).controlStateChange(COMM_SENDING_PAUSED);
    }

    @Test
    public void capabilityIsolation() throws Exception {
        boolean hasRealTime;
        GrblController gc = new GrblController(mgc);
        gc.openCommPort(getSettings().getConnectionDriver(), "foo", 2400);

        // This was being tested unintentionally in some of the other tests.
        // Now that it has been fixed, go ahead and test it directly.
        gc.rawResponseHandler("Grbl 1.1f");
        hasRealTime = gc.getCapabilities().hasCapability(GrblCapabilitiesConstants.REAL_TIME);
        assertTrue(hasRealTime);
        gc.rawResponseHandler("Grbl 0.7");
        hasRealTime = gc.getCapabilities().hasCapability(GrblCapabilitiesConstants.REAL_TIME);
        assertFalse(hasRealTime);
    }
}
