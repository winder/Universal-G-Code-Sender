/*
    Copyright 2013-2022 Will Winder

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
import com.willwinder.universalgcodesender.firmware.grbl.GrblVersion;
import com.willwinder.universalgcodesender.gcode.DefaultCommandCreator;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.willwinder.universalgcodesender.GrblUtils.*;
import static com.willwinder.universalgcodesender.model.CommunicatorState.*;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * @author wwinder
 */
public class GrblControllerTest {
    public static final String VERSION_GRBL_1_1F = "1.1f";
    public static final String VERSION_GRBL_0_9 = "0.9";
    public static final String VERSION_GRBL_0_8 = "0.8";
    public static final String VERSION_GRBL_0_8C = "0.8c";
    public static final String VERSION_GRBL_0_7 = "0.7";

    public static final String VERSION_GRBL_0_1 = "0.1";
    private MockGrblCommunicator mgc;
    private static File tempDir;
    private final Settings settings = new Settings();

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
    public void rawResponseHandlerOnWelcomeStringShouldInitializeTheController() throws Exception {
        GrblControllerInitializer controllerInitializer = mock(GrblControllerInitializer.class);
        when(controllerInitializer.initialize()).thenReturn(true);
        GrblController instance = new GrblController(mgc, controllerInitializer);

        instance.rawResponseHandler("Grbl 0.5b");
        Thread.sleep(50);
        verify(controllerInitializer, times(1)).initialize();
    }

    @Test
    public void testOpenCommPort() throws Exception {
        GrblController instance = initializeAndConnectController(VERSION_GRBL_1_1F);

        String exception = "";
        try {
            instance.openCommPort(getSettings().getConnectionDriver(), "blah", 1234);
            fail("Expected exception");
        } catch (Exception e) {
            exception = e.getMessage();
        }
        assertEquals("Comm port is already open.", exception);
    }

    @Test
    public void testCloseCommPort() {
        GrblControllerInitializer controllerInitializer = mock(GrblControllerInitializer.class);
        GrblController instance = new GrblController(mgc, controllerInitializer);

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
            assertEquals(true, instance.isCommOpen());
            result = instance.closeCommPort();
        } catch (Exception e) {
            fail("Unexpected exception from GrblController: " + e.getMessage());
        }
        assertEquals(true, result);
        assertEquals(false, instance.isCommOpen());
    }

    @Test
    public void performHomingCycleOnUnsupportedGrblVersionShouldThrowError() throws Exception {
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_7);
        try {
            instance.performHomingCycle();
            fail("Expected exception");
        } catch (Exception e) {
            assertTrue(e.getMessage().startsWith("No supported homing method for "));
        }
    }

    @Test
    public void performHomingCycleOnAncientGrbl() throws Exception {
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_8);
        instance.performHomingCycle();
        assertEquals(1, mgc.numStreamCommandsCalls);
        assertEquals(GrblUtils.GCODE_PERFORM_HOMING_CYCLE_V8, mgc.queuedString);
    }
    @Test
    public void performHomingCycleOnGrbl0_8() throws Exception {
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_8C);
        instance.performHomingCycle();
        assertEquals(1, mgc.numStreamCommandsCalls);
        assertEquals(GrblUtils.GCODE_PERFORM_HOMING_CYCLE_V8C, mgc.queuedString);
    }

    @Test
    public void performHomingCycleOnGrbl0_9() throws Exception {
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_9);
        instance.performHomingCycle();
        assertEquals(1, mgc.numStreamCommandsCalls);
        assertEquals(GrblUtils.GCODE_PERFORM_HOMING_CYCLE_V8C, mgc.queuedString);
    }

    @Test
    public void testPerformHomingCycleShouldChangeControllerState() throws Exception {
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_9);
        assertEquals(ControllerState.CONNECTING, instance.getControllerStatus().getState());

        instance.performHomingCycle();
        assertEquals(ControllerState.HOME, instance.getControllerStatus().getState());
    }

    @Test
    public void issueSoftResetShouldNotSendIfNotConnected() throws Exception {
        GrblController instance = new GrblController(mgc);
        instance.issueSoftReset();

        // Noop if called while comm is closed.
        // Did not send reset command to communicator or issue reset.
        assertEquals(0, mgc.sentBytes.size());
        assertEquals(0, mgc.numCancelSendCalls);
    }

    @Test
    public void issueSoftResetOnGrbl0_8c() throws Exception {
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_8C);
        assertEquals(0, mgc.sentBytes.size());
        assertEquals(0, mgc.numCancelSendCalls);

        // Sent reset command to communicator and issued reset.
        instance.issueSoftReset();
        assertEquals(1, mgc.numCancelSendCalls);
        assertEquals(1, mgc.sentBytes.size());
        assertEquals(Byte.valueOf(GRBL_RESET_COMMAND), mgc.sentBytes.get(mgc.sentBytes.size() - 1));
    }

    @Test
    public void issueSoftResetOnOlderGrblVersionsShouldNotSendAnything() throws Exception {
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_7);
        assertEquals(0, mgc.sentBytes.size());
        assertEquals(0, mgc.numCancelSendCalls);

        // This version doesn't support soft reset.
        instance.issueSoftReset();
        assertEquals(0, mgc.sentBytes.size());
        assertEquals(0, mgc.numCancelSendCalls);
    }

    /**
     * Test of getSendDuration method, of class GrblController.
     */
    @Test
    public void testGetSendDuration() throws Exception {
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_8C);
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
            fail("Unexpected exception from GrblController: " + ex.getMessage());
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
        assert (expResult <= result);
        assert (result <= (expResult + 500));

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
        assert (expResult <= result);
        assert (result <= (expResult + 500));

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
        assert (expResult <= result);
        assert (result <= (expResult + 500));
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
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_8C);

        // Test 1.
        // When not sending, no commands queues, everything should be zero.
        assertCounts(instance, 0, 0, 0);

        // Add 30 commands.
        List<GcodeCommand> commands = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            commands.add(new GcodeCommand("G0X" + i));
        }
        instance.queueStream(new SimpleGcodeStreamReader(commands));


        try {
//            instance.openCommPort("blah", 123);
            instance.beginStreaming();
            mgc.areActiveCommands = true;
        } catch (Exception ex) {
            fail("Unexpected exception from GrblController: " + ex.getMessage());
        }

        // Test 2.
        // 30 Commands queued, zero sent, 30 completed.
        assertCounts(instance, 30, 0, 30);

        // Test 3.
        // Sent 15 of them, none completed.
        try {
            for (int i = 0; i < 15; i++) {
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
            for (int i = 0; i < 15; i++) {
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
            for (int i = 0; i < 15; i++) {
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
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_8C);
        String str = "G0 X0";
        GcodeCommand command = instance.createCommand(str);
        instance.sendCommandImmediately(command);
        assertEquals(1, mgc.numQueueStringForCommCalls);
        assertEquals(1, mgc.numStreamCommandsCalls);
        assertEquals(str, mgc.queuedString);
    }

    @Test
    public void isReadyToStreamFileShouldThrowErrorIfNotConnected() {
        GrblController instance = new GrblController(mgc);

        try {
            instance.isReadyToStreamFile();
            fail();
        } catch (Exception e) {
            assertEquals("Grbl has not finished booting.", e.getMessage());
        }
    }

    @Test
    public void isReadyToStreamFileShouldThrowErrorWhen() throws Exception {
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_8C);
        assertTrue(instance.isReadyToStreamFile());

        // Test 4. Can't send during active command.
        instance.queueStream(new SimpleGcodeStreamReader(instance.createCommand("G0X0")));
        try {
            mgc.areActiveCommands = true;
            instance.isReadyToStreamFile();
            fail();
        } catch (Exception e) {
            assertEquals("Cannot stream while there are active commands: ", e.getMessage());
        }
    }


    @Test
    public void beginStreamingShouldThrowErrorIfNoCommandsToStream() throws Exception {
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_8C);
        instance.rawResponseHandler("<Idle,WPos:1,2,3,MPos:1,2,3>");
        assertEquals(0, mgc.numQueueStringForCommCalls);

        // Test 1. No commands to stream.
        try {
            instance.beginStreaming();
            fail("Expected exception");
        } catch (Exception e) {
            assertEquals("There are no commands queued for streaming.", e.getMessage());
        }
    }

    @Test
    public void beginStreamingShouldStartStream() throws Exception {
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_8C);
        instance.rawResponseHandler("<Idle,WPos:1,2,3,MPos:1,2,3>");
        assertEquals(0, mgc.numQueueStringForCommCalls);

        GcodeCommand command = instance.createCommand("G0X1");
        instance.queueStream(new SimpleGcodeStreamReader(command));

        instance.beginStreaming();

        assertEquals(0, mgc.numQueueStringForCommCalls);
        assertEquals(1, mgc.numStreamCommandsCalls);
    }

    @Test
    public void beginStreamingShouldThrowErrorIfStreamAlreadyStarted() throws Exception {
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_8C);
        instance.rawResponseHandler("<Idle,WPos:1,2,3,MPos:1,2,3>");
        assertEquals(0, mgc.numQueueStringForCommCalls);

        GcodeCommand command = instance.createCommand("G0X1");
        instance.queueStream(new SimpleGcodeStreamReader(command));
        instance.beginStreaming();

        // Trigger error
        Exception exception = assertThrows(Exception.class, instance::beginStreaming);
        assertEquals("Already streaming.", exception.getMessage());
        assertEquals(0, mgc.numQueueStringForCommCalls);
        assertEquals(1, mgc.numStreamCommandsCalls);
    }

    @Test
    public void pauseStreamingOnGrbl0_8cShouldSendRealTimeCommand() throws Exception {
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_8C);
        instance.pauseStreaming();

        assertEquals(1,  mgc.sentBytes.size());
        assertEquals(Byte.valueOf(GRBL_PAUSE_COMMAND), mgc.sentBytes.get(0));
        assertEquals(1, mgc.numPauseSendCalls);
    }
    @Test
    public void pauseStreamingOnGrbl0_7ShouldNotSendRealTimeCommand() throws Exception {
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_7);
        instance.pauseStreaming();
        assertEquals(1, mgc.numPauseSendCalls);
        assertEquals(0, mgc.sentBytes.size());
    }

    /**
     * Test of resumeStreaming method, of class GrblController.
     */
    @Test
    public void resumeStreamingOnAncientGrblShouldNotSendRealTimeCommand() throws Exception {
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_1);
        instance.resumeStreaming();
        assertEquals(1, mgc.numResumeSendCalls);
        assertEquals(0, mgc.sentBytes.size());
    }
    @Test
    public void resumeStreamingOnGrbl0_7ShouldNotSendRealTimeCommand() throws Exception {
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_7);
        instance.resumeStreaming();
        assertEquals(1, mgc.numResumeSendCalls);
        assertEquals(0, mgc.sentBytes.size());
    }

    @Test
    public void resumeStreamingOnGrbl0_8cShouldSendRealTimeCommand() throws Exception {
        GrblController instance  = initializeAndConnectController(VERSION_GRBL_0_8C);
        instance.resumeStreaming();
        assertEquals(1, mgc.numResumeSendCalls);
        assertEquals(Byte.valueOf(GRBL_RESUME_COMMAND), mgc.sentBytes.get(mgc.sentBytes.size() - 1));
    }

    private static void wrapUp(GrblController gc) throws UnexpectedCommand {
        // wrap up
        GcodeCommand command = new GcodeCommand("blah");
        command.setSent(true);
        command.setResponse("ok");
        for (int i = 0; i < 15; i++) {
            gc.commandComplete(command.getCommandString());
        }
    }

    @Test
    public void cancelSendShouldNotReturnToIdleDuringCancel() throws Exception {
        // 0. Test GRBL not returning to idle during cancel.
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_8C);
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
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_7);
        instance.cancelSend();
        assertEquals(1, mgc.numCancelSendCalls);
        assertEquals(0, mgc.numPauseSendCalls);
    }

    @Test
    public void cancelWhenNothingIsRunningGrblOn0_8c() throws Exception {
        // Test 1.2 Cancel when nothing is running (Grbl 0.8c).
        //          Check for soft reset.
        this.mgc = new MockGrblCommunicator();
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_8C);
        instance.cancelSend();
        instance.rawResponseHandler("<Hold,MPos:1.0,2.0,3.0>");
        instance.rawResponseHandler("<Hold,MPos:1.0,2.0,3.0>");
        assertEquals(1, mgc.numCancelSendCalls);
        assertEquals(1, mgc.numPauseSendCalls);
        instance.resumeStreaming();
    }

    @Test
    public void cancelSendResumeAfterCancelBeforeAnySendingOnGrbl0_7() throws Exception {
        // Test 2.1
        // Add 30 commands, start send, cancel before any sending. (Grbl 0.7)
        this.mgc = new MockGrblCommunicator();
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_7);
        List<GcodeCommand> commands = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            commands.add(instance.createCommand("G0X" + i));
        }
        instance.queueStream(new SimpleGcodeStreamReader(commands));
        try {
            instance.beginStreaming();
        } catch (Exception ex) {
            fail("Unexpected exception from GrblController: " + ex.getMessage());
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
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_8C);
        instance.setStatusUpdatesEnabled(true);
        List<GcodeCommand> commands = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            commands.add(instance.createCommand("G0X" + i));
        }
        instance.queueStream(new SimpleGcodeStreamReader(commands));

        try {
            instance.beginStreaming();
        } catch (Exception ex) {
            fail("Unexpected exception from GrblController: " + ex.getMessage());
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
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_7);
        List<GcodeCommand> commands = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            commands.add(instance.createCommand("G0X0"));
        }
        instance.queueStream(new SimpleGcodeStreamReader(commands));

        instance.beginStreaming();
        for (int i = 0; i < 15; i++) {
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
        wrapUp(instance);
    }

    @Test
    public void cancelSendHalfwayThroughJobOnGrbl0_8c() throws Exception {
        // Test 3.2
        // Add 30 commands, start send, cancel after sending 15. (Grbl 0.8c)
        this.mgc = new MockGrblCommunicator();
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_8C);
        instance.setStatusUpdatesEnabled(true);
        List<GcodeCommand> commands = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            commands.add(instance.createCommand("G0X" + i));
        }
        instance.queueStream(new SimpleGcodeStreamReader(commands));

        try {
            instance.beginStreaming();
            for (int i = 0; i < 15; i++) {
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
        assertEquals(Byte.valueOf(GRBL_RESET_COMMAND), mgc.sentBytes.get(mgc.sentBytes.size() - 1));
        instance.resumeStreaming();
    }

    @Test
    public void cancelSendOnDoorStateShouldCancelCommandAndIssueReset() throws Exception {
        this.mgc = new MockGrblCommunicator();
        GrblController instance = initializeAndConnectController(VERSION_GRBL_1_1F);
        instance.setStatusUpdatesEnabled(true);
        instance.rawResponseHandler("<Door|MPos:0.000,0.000,0.000|FS:0,0|Pn:XYZ>");
        mgc.sentBytes.clear();

        // Set internal state to cancelling
        instance.cancelSend();

        assertEquals(1, mgc.numCancelSendCalls);
        assertEquals(0, mgc.numPauseSendCalls);
        assertEquals(0, mgc.sentBytes.size());

        // First round we will store the last position
        instance.rawResponseHandler("<Door|MPos:0.000,0.000,0.000|FS:0,0|Pn:XYZ>");

        assertEquals(1, mgc.numCancelSendCalls);
        assertEquals(0, mgc.numPauseSendCalls);
        assertEquals(0, mgc.sentBytes.size());

        // Now we will do the actual cancel
        instance.rawResponseHandler("<Door|MPos:0.000,0.000,0.000|FS:0,0|Pn:XYZ>");

        assertEquals(2, mgc.numCancelSendCalls);
        assertEquals(0, mgc.numPauseSendCalls);
        assertEquals(1, mgc.sentBytes.size());
        assertEquals(Byte.valueOf(GRBL_RESET_COMMAND), mgc.sentBytes.get(0));
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
        GrblController instance;

        // Test 1.1 cancel throws an exception (Grbl 0.7).
        this.mgc = new MockGrblCommunicator();
        instance = initializeAndConnectController(VERSION_GRBL_0_8);

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
        instance = initializeAndConnectController(VERSION_GRBL_0_8C);
        instance.pauseStreaming();
        instance.cancelSend();
        instance.rawResponseHandler("<Hold,MPos:1.0,2.0,3.0>");
        instance.rawResponseHandler("<Hold,MPos:1.0,2.0,3.0>");
        assertEquals(1, mgc.numCancelSendCalls);
        instance.resumeStreaming();

        // Test 2.1
        // Add 30 commands, start send, cancel before any sending. (Grbl 0.7)
        this.mgc = new MockGrblCommunicator();
        instance = initializeAndConnectController(VERSION_GRBL_0_8);
        List<GcodeCommand> commands = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            commands.add(instance.createCommand("G0X" + i));
        }
        instance.queueStream(new SimpleGcodeStreamReader(commands));

        try {
            instance.beginStreaming();
        } catch (Exception ex) {
            fail("Unexpected exception from GrblController: " + ex.getMessage());
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
        instance = initializeAndConnectController(VERSION_GRBL_0_8C);
        commands = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            commands.add(instance.createCommand("G0X" + i));
        }
        instance.queueStream(new SimpleGcodeStreamReader(commands));

        try {
            instance.beginStreaming();
        } catch (Exception ex) {
            fail("Unexpected exception from GrblController: " + ex.getMessage());
        }
        instance.pauseStreaming();
        instance.cancelSend();
        instance.rawResponseHandler("<Hold,MPos:1.0,2.0,3.0>");
        instance.rawResponseHandler("<Hold,MPos:1.0,2.0,3.0>");
        assertEquals(30, instance.rowsInSend());
        assertEquals(30, instance.rowsRemaining());
        instance.resumeStreaming();

        // Test 3.1 - N/A, exception thrown.

        // Test 3.2
        // Add 30 commands, start send, cancel after sending 15. (Grbl 0.8c)
        this.mgc = new MockGrblCommunicator();
        instance = initializeAndConnectController(VERSION_GRBL_0_8C);
        commands = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            commands.add(instance.createCommand("G0X" + i));
        }
        instance.queueStream(new SimpleGcodeStreamReader(commands));

        try {
            instance.beginStreaming();
            for (int i = 0; i < 15; i++) {
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
        assertEquals(30, instance.rowsInSend());
        // Left this failing because it should be possible to make it work
        // this way someday.
        assertEquals(30, instance.rowsRemaining());
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

    @Test
    public void jogMachineWithLegacyVersion() throws Exception {
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_8C);
        instance.setDistanceModeCode("G90");
        instance.setUnitsCode("G21");

        // Abstract controller should be used when grbl jog mode is disabled.
        instance.jogMachine(new PartialPosition(-10., null, 10., UnitUtils.Units.INCH), 11);
        assertEquals("G20G91G1X-10Z10F11", mgc.queuedStrings.get(0));
        assertEquals("G90 G21", mgc.queuedStrings.get(1));

        instance.jogMachine(new PartialPosition(null, 10., null, UnitUtils.Units.MM), 11);
        assertEquals("G21G91G1Y10F11", mgc.queuedStrings.get(2));
        assertEquals("G90 G21", mgc.queuedStrings.get(3));
    }

    @Test
    public void jogMachineWithNewJogCommandsVersion() throws Exception {
        GrblController instance = initializeAndConnectController(VERSION_GRBL_1_1F);
        instance.setDistanceModeCode("G90");
        instance.setUnitsCode("G21");

        instance.jogMachine(new PartialPosition(-10., null, 10., UnitUtils.Units.INCH), 11);
        assertEquals("$J=G20G91X-10Z10F11", mgc.queuedStrings.get(0));

        instance.jogMachine(new PartialPosition(null, 10., null, UnitUtils.Units.MM), 11);
        assertEquals("$J=G21G91Y10F11", mgc.queuedStrings.get(1));
    }

    /**
     * Test of jogMachineTo method
     */
    @Test
    public void testJogMachineTo() throws Exception {
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_8C);
        instance.setDistanceModeCode("G90");
        instance.setUnitsCode("G21");

        // Abstract controller should be used when grbl jog mode is disabled.
        instance.jogMachineTo(new PartialPosition(1.0, 2.0, 3.0, UnitUtils.Units.MM), 200);
        assertEquals("G21G90G1X1Y2Z3F200", mgc.queuedStrings.get(0));
        assertEquals("G90 G21", mgc.queuedStrings.get(1));

        instance.jogMachineTo(new PartialPosition(1.0, 2.0, UnitUtils.Units.MM), 200);
        assertEquals("G21G90G1X1Y2F200", mgc.queuedStrings.get(2));
        assertEquals("G90 G21", mgc.queuedStrings.get(3));

        instance.jogMachineTo(new PartialPosition(1.2345678, 2.0, UnitUtils.Units.MM), 200);
        assertEquals("G21G90G1X1.235Y2F200", mgc.queuedStrings.get(4));
        assertEquals("G90 G21", mgc.queuedStrings.get(5));

        instance.jogMachineTo(new PartialPosition(1.0, 2.0, UnitUtils.Units.INCH), 200);
        assertEquals("G20G90G1X1Y2F200", mgc.queuedStrings.get(6));
        assertEquals("G90 G21", mgc.queuedStrings.get(7));
    }

    @Test
    public void testReturnToHomeWhenZIsPositive() throws Exception {
        // Set up
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_8C);
        instance.rawResponseHandler("<Idle,MPos:1.000,1.000,1.000,WPos:0.0,0.0,0.0>");

        // Test the function for going home
        instance.returnToHome(0);

        assertEquals(2, mgc.queuedStrings.size());
        assertEquals("Go to XY-zero", "G90 G0 X0 Y0", mgc.queuedStrings.get(0));
        assertEquals("Go to Z-zero", "G90 G0 Z0", mgc.queuedStrings.get(1));
    }

    @Test
    public void testReturnToHomeWhenWorkPositionZIsNegative() throws Exception {
        // Set up
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_8C);
        instance.rawResponseHandler("<Idle,MPos:1.000,1.000,1.000,WPos:0.0,0.0,-1.0>");

        // Test the function for going home
        instance.returnToHome(0);

        assertEquals(3, mgc.queuedStrings.size());
        assertEquals("The machine is in the material, go to zero with the Z axis first", "G90 G0 Z0", mgc.queuedStrings.get(0));
        assertEquals("Go to XY-zero", "G90 G0 X0 Y0", mgc.queuedStrings.get(1));
        assertEquals("Go to Z-zero", "G90 G0 Z0", mgc.queuedStrings.get(2));
    }

    @Test
    public void testReturnToHomeWhenWorkPositionZIsNegativeInMmAndWithSafetyHeightEnabled() throws Exception {
        // Set up
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_8C);
        instance.getCurrentGcodeState().units = Code.G21;
        instance.getCurrentGcodeState().distanceMode = Code.G90;

        instance.rawResponseHandler("<Idle,MPos:1.000,1.000,1.000,WPos:0.0,0.0,-1.0>");

        // Test the function for going home
        instance.returnToHome(10);

        assertEquals(3, mgc.queuedStrings.size());
        assertEquals("The machine is in the material, go to safety height in mm with the Z axis first", "G21G90 G0Z10", mgc.queuedStrings.get(0));
        assertEquals("Go to XY-zero", "G90 G0 X0 Y0", mgc.queuedStrings.get(1));
        assertEquals("Go to Z-zero", "G90 G0 Z0", mgc.queuedStrings.get(2));
    }

    @Test
    public void testReturnToHomeWhenWorkPositionZIsNegativeInInchAndWithSafetyHeightEnabled() throws Exception {
        // Set up
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_8C);
        instance.getCurrentGcodeState().units = Code.G20;
        instance.getCurrentGcodeState().distanceMode = Code.G90;

        instance.rawResponseHandler("<Idle,MPos:1.000,1.000,1.000,WPos:0.0,0.0,-1.0>");

        // Test the function for going home
        instance.returnToHome(10);

        assertEquals(3, mgc.queuedStrings.size());
        assertEquals("The machine is in the material, go to safety height in inches with the Z axis first", "G20G90 G0Z0.394", mgc.queuedStrings.get(0));
        assertEquals("Go to XY-zero", "G90 G0 X0 Y0", mgc.queuedStrings.get(1));
        assertEquals("Go to Z-zero", "G90 G0 Z0", mgc.queuedStrings.get(2));
    }


    @Test
    public void testReturnToHomeWhenWorkPositionZIsOverSafetyHeightEnabled() throws Exception {
        // Set up
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_8C);
        instance.rawResponseHandler("<Idle,MPos:1.000,1.000,1.000,WPos:0.0,0.0,11.0>");

        // Test the function for going home
        instance.returnToHome(10);

        assertEquals(2, mgc.queuedStrings.size());
        assertEquals("Go to XY-zero", "G90 G0 X0 Y0", mgc.queuedStrings.get(0));
        assertEquals("Go to Z-zero", "G90 G0 Z0", mgc.queuedStrings.get(1));
    }

    @Test
    public void rawResponseHandlerWithKnownErrorShouldWriteMessageToConsole() throws Exception {
        // Given
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_9);
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
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_9);
        instance.setDistanceModeCode("G90");
        instance.setUnitsCode("G21");
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
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_9);
        instance.setDistanceModeCode("G90");
        instance.setUnitsCode("G21");

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
        GrblController instance = initializeAndConnectController(VERSION_GRBL_1_1F);
        instance.setDistanceModeCode("G90");
        instance.setUnitsCode("G21");

        instance.rawResponseHandler("<Hold|MPos:0.000,0.000,0.000|FS:0,0|Pn:XYZ>");
        assertFalse("We should start with a non idle state", instance.isIdle());

        // When
        instance.rawResponseHandler("<Check|MPos:0.000,0.000,0.000|FS:0,0|Pn:XYZ>");

        // Then
        assertTrue(instance.isIdle());
        assertTrue(instance.isIdleEvent());
        assertEquals(COMM_CHECK, instance.getCommunicatorState());
        assertEquals(ControllerState.CHECK, instance.getControllerStatus().getState());
    }

    @Test
    public void controllerShouldGetAxisCapabilitiesOnStatusStringWithMachinePosition() throws Exception {
        // Given
        GrblController instance = initializeAndConnectController(VERSION_GRBL_1_1F);

        assertFalse("We should not start with A axis capabilities", instance.getCapabilities().hasCapability(CapabilitiesConstants.A_AXIS));
        assertFalse("We should not start with B axis capabilities", instance.getCapabilities().hasCapability(CapabilitiesConstants.B_AXIS));
        assertFalse("We should not start with C axis capabilities", instance.getCapabilities().hasCapability(CapabilitiesConstants.C_AXIS));

        // When
        instance.rawResponseHandler("<Idle|MPos:1.000,2.000,3.000,4.000,5.000,6.0000|FS:0,0|Pn:XYZ>");

        // Then
        assertTrue("We should now have A axis capability", instance.getCapabilities().hasCapability(CapabilitiesConstants.A_AXIS));
        assertTrue("We should now have B axis capability", instance.getCapabilities().hasCapability(CapabilitiesConstants.B_AXIS));
        assertTrue("We should now have C axis capability", instance.getCapabilities().hasCapability(CapabilitiesConstants.C_AXIS));
        assertEquals(4, instance.getControllerStatus().getMachineCoord().a, 0.1);
        assertEquals(5, instance.getControllerStatus().getMachineCoord().b, 0.1);
        assertEquals(6, instance.getControllerStatus().getMachineCoord().c, 0.1);
    }

    @Test
    public void controllerShouldGetAxisCapabilitiesOnStatusStringWithWorkPosition() throws Exception {
        // Given
        GrblController instance = initializeAndConnectController(VERSION_GRBL_1_1F);

        assertFalse("We should not start with A axis capabilities", instance.getCapabilities().hasCapability(CapabilitiesConstants.A_AXIS));
        assertFalse("We should not start with B axis capabilities", instance.getCapabilities().hasCapability(CapabilitiesConstants.B_AXIS));
        assertFalse("We should not start with C axis capabilities", instance.getCapabilities().hasCapability(CapabilitiesConstants.C_AXIS));

        // When
        instance.rawResponseHandler("<Idle|WPos:1.000,2.000,3.000,4.000,5.000,6.0000|FS:0,0|Pn:XYZ>");

        // Then
        assertTrue("We should now have A axis capability", instance.getCapabilities().hasCapability(CapabilitiesConstants.A_AXIS));
        assertTrue("We should now have B axis capability", instance.getCapabilities().hasCapability(CapabilitiesConstants.B_AXIS));
        assertTrue("We should now have C axis capability", instance.getCapabilities().hasCapability(CapabilitiesConstants.C_AXIS));
        assertEquals(4, instance.getControllerStatus().getWorkCoord().a, 0.1);
        assertEquals(5, instance.getControllerStatus().getWorkCoord().b, 0.1);
        assertEquals(6, instance.getControllerStatus().getWorkCoord().c, 0.1);
    }

    @Test
    public void rawResponseHandlerOnVersionStringShouldResetStatus() throws Exception {
        // Given
        GrblController instance = initializeAndConnectController(VERSION_GRBL_1_1F);
        instance.rawResponseHandler("<Run|MPos:0.000,0.000,0.000|FS:0,0|Pn:XYZ>");
        assertEquals("We should be in sending mode", COMM_SENDING, instance.getCommunicatorState());
        assertEquals(ControllerState.RUN, instance.getControllerStatus().getState());

        // When
        instance.rawResponseHandler("Grbl " + VERSION_GRBL_1_1F);

        // Then
        assertEquals(COMM_IDLE, instance.getCommunicatorState());
        assertEquals(ControllerState.CONNECTING, instance.getControllerStatus().getState());
    }

    /**
     * When exiting check mode the controller does a soft reset and sends a new version string. The
     * default behavior is to reset the controller status. But we need it to determine if single
     * step mode is supposed to be activated.
     */
    @Test
    public void versionStringShouldNotResetStatusWhenInCheckMode() throws Exception {
        // Given
        GrblController instance = initializeAndConnectController(VERSION_GRBL_1_1F);
        instance.rawResponseHandler("<Check|MPos:0.000,0.000,0.000|FS:0,0|Pn:XYZ>");
        assertEquals("We should be in check mode", COMM_CHECK, instance.getCommunicatorState());

        // When
        instance.rawResponseHandler(VERSION_GRBL_1_1F);

        // Then
        assertEquals(COMM_CHECK, instance.getCommunicatorState());
    }

    @Test
    public void controllerShouldHaveStateRunningWhenStreamingAndInCheckMode() throws Exception {
        // Given
        GrblController instance = initializeAndConnectController(VERSION_GRBL_1_1F);
        instance.setDistanceModeCode("G90");
        instance.setUnitsCode("G21");

        instance.rawResponseHandler("<Check|MPos:0.000,0.000,0.000|FS:0,0|Pn:XYZ>");
        assertTrue(instance.isIdle());

        // Create a gcode file stream
        File gcodeFile = new File(tempDir, "controllerShouldHaveStateRunningWhenStreamingAndInCheckMode");
        GcodeStreamWriter gcodeStreamWriter = new GcodeStreamWriter(gcodeFile);
        gcodeStreamWriter.addLine("G0", "G0", null, 0);
        gcodeStreamWriter.addLine("G0", "G0", null, 0);
        gcodeStreamWriter.close();

        // When
        instance.queueStream(new GcodeStreamReader(gcodeFile, new DefaultCommandCreator()));
        instance.beginStreaming();

        // Then
        assertFalse(instance.isIdle());
        assertFalse(instance.isPaused());
        assertEquals(COMM_SENDING, instance.getCommunicatorState());
    }

    @Test
    public void controllerShouldBeIdleWhenInCheckModeWithOldStatusFormat() throws Exception {
        // Given
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_8C);
        instance.setDistanceModeCode("G90");
        instance.setUnitsCode("G21");

        instance.rawResponseHandler("<Hold,MPos:0.000,0.000,0.000|FS:0,0|Pn:XYZ>");
        assertFalse("We should start with a non idle state", instance.isIdle());

        // When
        instance.rawResponseHandler("<Check,MPos:0.000,0.000,0.000|FS:0,0|Pn:XYZ>");

        // Then
        assertTrue(instance.isIdle());
        assertTrue(instance.isIdleEvent());
        assertEquals(COMM_CHECK, instance.getCommunicatorState());
        assertEquals(ControllerState.CHECK, instance.getControllerStatus().getState());
    }

    @Test
    public void errorInCheckModeNotSending() throws Exception {
        // Given
        GrblController gc = initializeAndConnectController(VERSION_GRBL_1_1F);
        gc.rawResponseHandler("<Check|MPos:0.000,0.000,0.000|FS:0,0|Pn:XYZ>");

        // When
        gc.communicatorPausedOnError();
        gc.rawResponseHandler("error:1");

        // Then
        assertEquals(COMM_CHECK, gc.getCommunicatorState());
        assertEquals(ControllerState.CHECK, gc.getControllerStatus().getState());
        assertFalse(gc.isPaused());
        assertEquals(1, mgc.numResumeSendCalls);
    }

    @Test
    public void errorInCheckModeSending() throws Exception {
        // Given
        GrblController gc = initializeAndConnectController(VERSION_GRBL_1_1F);
        gc.rawResponseHandler("<Check|MPos:0.000,0.000,0.000|FS:0,0|Pn:XYZ>");

        // When
        gc.queueStream(new SimpleGcodeStreamReader("G0 X10"));
        gc.beginStreaming();
        gc.communicatorPausedOnError();
        gc.rawResponseHandler("error:1");

        // Then
        assertEquals(COMM_SENDING, gc.getCommunicatorState());
        assertEquals(ControllerState.CHECK, gc.getControllerStatus().getState());
        assertFalse(gc.isPaused());
        assertEquals(Byte.valueOf(GRBL_PAUSE_COMMAND), mgc.sentBytes.get(0));
    }

    @Test
    public void capabilityIsolation() throws Exception {
        boolean hasRealTime;
        GrblController gc = initializeAndConnectController(VERSION_GRBL_1_1F);
        hasRealTime = gc.getCapabilities().hasCapability(GrblCapabilitiesConstants.REAL_TIME);
        assertTrue(hasRealTime);

        mgc = new MockGrblCommunicator();
        gc = initializeAndConnectController(VERSION_GRBL_0_7);
        hasRealTime = gc.getCapabilities().hasCapability(GrblCapabilitiesConstants.REAL_TIME);
        assertFalse(hasRealTime);
    }

    @Test
    public void isStreamingShouldReturnTrueWhenStartedStreaming() throws Exception {
        GrblController gc = initializeAndConnectController(VERSION_GRBL_1_1F);
        assertEquals(false, gc.isStreaming());
        gc.queueStream(new SimpleGcodeStreamReader("G0 X10"));
        gc.beginStreaming();
        assertEquals(true, gc.isStreaming());
    }

    @Test
    public void beginStreamingShouldQueueTheCommandsInTheStream() throws Exception {
        mgc = spy(mgc);
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_8);

        String command = "command";
        Collection<String> commands = Arrays.asList(command, command);

        File f = new File(tempDir, "beginStreamingShouldQueueTheCommandsInTheStream");
        try {
            try (GcodeStreamWriter gsw = new GcodeStreamWriter(f)) {
                commands.forEach(c -> gsw.addLine("blah", command, null, -1));
            }

            reset(mgc);
            try (IGcodeStreamReader gsr = new GcodeStreamReader(f, new DefaultCommandCreator())) {
                // Open port, send some commands, make sure they are streamed.
                instance.queueStream(gsr);
                instance.beginStreaming();
            }

            verify(mgc, times(2)).isConnected();
            verify(mgc, times(1)).areActiveCommands();
            verify(mgc, times(1)).queueStreamForComm(any());
            verify(mgc, times(1)).streamCommands();

            assertEquals(0, instance.rowsSent());
            assertEquals(2, instance.rowsRemaining());
            assertEquals(2, instance.rowsInSend());
            verifyNoMoreInteractions(mgc);
        } finally {
            FileUtils.forceDelete(f);
        }
    }

    @Test
    public void commandSentShouldNotifyListeners() throws Exception {
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_8);

        GcodeCommand command = new GcodeCommand("command");
        ControllerListener controllerListener = mock(ControllerListener.class);
        instance.addListener(controllerListener);
        instance.commandSent(command);

        verify(controllerListener, times(1)).commandSent(any());
        assertTrue(command.isSent());
        assertTrue(instance.getActiveCommand().isPresent());
    }

    @Test(expected = UnexpectedCommand.class)
    public void commandCompleteWithNoSentCommandsShouldThrowError() throws Exception {
        GrblController instance = initializeAndConnectController(VERSION_GRBL_0_8);

        ControllerListener controllerListener = mock(ControllerListener.class);
        instance.addListener(controllerListener);
        instance.commandComplete("done");
    }

    /**
     * Helper function for initializing a grbl controller for testing
     *
     * @param grblVersionString - the GRBL version we are testing with.
     * @return an instance of a GRBL controller
     * @throws Exception on any error while simulating a connection
     */
    private GrblController initializeAndConnectController(String grblVersionString) throws Exception {
        GrblControllerInitializer initializer = mock(GrblControllerInitializer.class);
        when(initializer.isInitialized()).thenReturn(true);

        GrblVersion version = new GrblVersion("[VER:" + grblVersionString + "]");
        when(initializer.getVersion()).thenReturn(version);

        GrblController instance = new GrblController(mgc, initializer);
        instance.openCommPort(getSettings().getConnectionDriver(), "/dev/port", 1234);
        Thread.sleep(50);

        instance.rawResponseHandler(grblVersionString);
        return instance;
    }
}
