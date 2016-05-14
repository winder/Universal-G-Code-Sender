/*
    Copywrite 2015-2016 Will Winder

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

import com.willwinder.universalgcodesender.gcode.GcodeCommandCreator;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import com.willwinder.universalgcodesender.utils.GcodeStreamTest;
import com.willwinder.universalgcodesender.utils.GcodeStreamWriter;
import java.io.File;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.easymock.Capture;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.newCapture;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import org.easymock.IMockBuilder;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Ignore;

/**
 *
 * @author wwinder
 */
public class AbstractControllerTest {
    
    public AbstractControllerTest() {
    }

    final static AbstractCommunicator mockCommunicator = EasyMock.createMock(AbstractCommunicator.class);
    final static ControllerListener mockListener = EasyMock.createMock(ControllerListener.class);
    final static GcodeCommandCreator gcodeCreator = new GcodeCommandCreator();

    static AbstractController instance;

    static File tempDir = null;

    //@BeforeClass
    public static void init() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, IOException {
        instance = EasyMock
                .createMockBuilder(AbstractController.class)
                    .addMockedMethods(
                        "closeCommBeforeEvent",
                        "closeCommAfterEvent",
                        "openCommAfterEvent",
                        "cancelSendBeforeEvent",
                        "cancelSendAfterEvent",
                        "pauseStreamingEvent",
                        "resumeStreamingEvent",
                        "isReadyToStreamCommandsEvent",
                        "isReadyToSendCommandsEvent",
                        "rawResponseHandler",
                        "statusUpdatesEnabledValueChanged",
                        "statusUpdatesRateValueChanged",
                        "isCommOpen")
                    .withConstructor(AbstractCommunicator.class)
                    .withArgs(mockCommunicator)
                    .createMock();

        // Initialize private variable.
        Field f = AbstractController.class.getDeclaredField("commandCreator");
        f.setAccessible(true);
        f.set(instance, gcodeCreator);
        
        instance.addListener(mockListener);
    }

    @BeforeClass
    static public void setup() throws IOException {
        tempDir = GcodeStreamTest.createTempDirectory();

    }

    @AfterClass
    static public void teardown() throws IOException {
        FileUtils.forceDelete(tempDir);
    }

    @Before
    public void setUp() throws Exception {
        // AbstractCommunicator calls a function on mockCommunicator that I
        // don't want to deal with.
        EasyMock.reset(mockCommunicator, mockListener);
        init();
        EasyMock.reset(mockCommunicator, mockListener);
    }


    ///////////////
    // UTILITIES //
    ///////////////
    public void openInstanceExpectUtility(String port, int portRate) throws Exception {
        instance.openCommAfterEvent();
        EasyMock.expect(EasyMock.expectLastCall()).anyTimes();
        mockListener.messageForConsole(anyObject(), EasyMock.anyString());
        EasyMock.expect(EasyMock.expectLastCall()).anyTimes();
        EasyMock.expect(mockCommunicator.openCommPort(port, portRate)).andReturn(true).once();
        EasyMock.expect(instance.isCommOpen()).andReturn(false).once();
        EasyMock.expect(instance.isCommOpen()).andReturn(true).anyTimes();
    }
    private void streamInstanceExpectUtility() throws Exception {
        EasyMock.expect(mockCommunicator.areActiveCommands()).andReturn(false).anyTimes();
        instance.isReadyToStreamCommandsEvent();
        EasyMock.expect(EasyMock.expectLastCall()).once();
        mockCommunicator.streamCommands();
        EasyMock.expect(EasyMock.expectLastCall()).once();
    }
    private void startStreamExpectation(String port, int rate, String command) throws Exception {
        openInstanceExpectUtility(port, rate);
        streamInstanceExpectUtility();
        
        // Making sure the commands get queued.
        mockCommunicator.queueStringForComm(command + "\n");
        EasyMock.expect(EasyMock.expectLastCall()).times(2);
    }
    private void startStream(String port, int rate, String command) throws Exception {
        // Open port, send some commands, make sure they are streamed.
        instance.openCommPort(port, rate);
        instance.queueCommand(instance.createCommand(command));
        instance.queueCommand(instance.createCommand(command));
        instance.beginStreaming();
    }

    /**
     * Test of getCommandCreator method, of class AbstractController.
     */
    @Test
    public void testGetCommandCreator() {
        System.out.println("getCommandCreator");
        GcodeCommandCreator result = instance.getCommandCreator();
        assertEquals(gcodeCreator, result);
    }

    /**
     * Test of openCommPort method, of class AbstractController.
     */
    @Test
    @Ignore
    public void testOpenCommPort() throws Exception {
        System.out.println("openCommPort");
        String port = "";
        int portRate = 0;

        instance.openCommAfterEvent();
        EasyMock.expect(EasyMock.expectLastCall()).once();
        mockListener.messageForConsole(anyObject(), anyString());
        EasyMock.expect(EasyMock.expectLastCall()).once();
        EasyMock.expect(mockCommunicator.openCommPort(port, portRate)).andReturn(true).once();
        EasyMock.replay(instance, mockCommunicator, mockListener);

        Boolean expResult = true;
        Boolean result = instance.openCommPort(port, portRate);
        assertEquals(expResult, result);

        boolean threw = false;
        try {
            instance.openCommPort(port, portRate);
        } catch (Exception e) {
            threw = true;
        }
        assertEquals("Cannot open a comm port twice.", true, threw);
        
        EasyMock.verify(instance, mockCommunicator, mockListener);
    }

    /**
     * Test of closeCommPort method, of class AbstractController.
     */
    @Test
    @Ignore
    public void testCloseCommPort() throws Exception {
        System.out.println("closeCommPort");

        String port = "/some/port";
        int baud = 1234;

        // Events
        instance.openCommAfterEvent();
        EasyMock.expect(EasyMock.expectLastCall()).once();
        instance.closeCommBeforeEvent();
        EasyMock.expect(EasyMock.expectLastCall()).once();
        instance.closeCommAfterEvent();
        EasyMock.expect(EasyMock.expectLastCall()).once();

        // Message for open and close.
        mockListener.messageForConsole(anyObject(), anyString());
        EasyMock.expect(EasyMock.expectLastCall()).times(2);
        EasyMock.expect(mockCommunicator.openCommPort(port, baud)).andReturn(true).once();
        mockCommunicator.closeCommPort();
        EasyMock.expect(EasyMock.expectLastCall()).once();
        EasyMock.replay(instance, mockCommunicator, mockListener);;

        // Close a closed port.
        Boolean result = instance.closeCommPort();
        assertEquals(true, result);

        // Open port to close it.
        result = instance.openCommPort(port, baud);
        assertEquals(result, result);

        // Close an open port.
        result = instance.closeCommPort();
        assertEquals(true, result);
    }

    /**
     * Test of isCommOpen method, of class AbstractController.
     */
    @Test
    @Ignore
    public void testIsCommOpen() throws Exception {
        System.out.println("isCommOpen");

        EasyMock.expect(mockCommunicator.openCommPort("port", 1234)).andReturn(true);
        mockCommunicator.closeCommPort();
        EasyMock.expect(EasyMock.expectLastCall());
        EasyMock.replay(mockCommunicator);

        assertEquals(false, instance.isCommOpen());

        instance.openCommPort("port", 1234);

        assertEquals(true, instance.isCommOpen());

        instance.closeCommPort();

        assertEquals(false, instance.isCommOpen());
    }

    /**
     * Test of setStatusUpdatesEnabled method, of class AbstractController.
     */
    @Test
    @Ignore
    public void testStatusUpdates() {
        System.out.println("testStatusUpdates");
        boolean enabled = false;
        instance.setStatusUpdatesEnabled(enabled);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isStreamingFile method, of class AbstractController.
     */
    @Test
    public void testIsStreamingFile() throws Exception {
        System.out.println("isStreamingFile");

        assertEquals(false, instance.isStreamingFile());
        testQueueCommands();
        assertEquals(true, instance.isStreamingFile());
    }

    /**
     * Test of getSendDuration method, of class AbstractController.
     */
    @Test
    public void testGetSendDuration() throws Exception {
        System.out.println("getSendDuration");

        String command = "command";
        String port = "/some/port";
        int rate = 1234;

        startStreamExpectation(port, rate, command);

        EasyMock.replay(instance, mockCommunicator);

        // Time starts at zero when nothing has been sent.
        assertEquals(0L, instance.getSendDuration());

        startStream(port, rate, command);
        long start = System.currentTimeMillis();

        Thread.sleep(1000);

        long time = instance.getSendDuration();
        long checkpoint = System.currentTimeMillis();

        // Began streaming at least 1 second ago.
        assertTrue( time > (start-checkpoint));

        Thread.sleep(1000);

        instance.commandSent(new GcodeCommand(command));
        instance.commandSent(new GcodeCommand(command));
        instance.commandComplete(command);
        instance.commandComplete(command);

        time = instance.getSendDuration();
        checkpoint = System.currentTimeMillis();

        // Completed commands after at least "checkpoint" milliseconds.
        assertTrue( time > (start-checkpoint));

        Thread.sleep(1000);

        // Make sure the time stopped after the last command was completed.
        long newtime = instance.getSendDuration();
        assertEquals( time, newtime );

        EasyMock.verify(mockCommunicator, instance);
    }

    /**
     * Test of rowsInSend method, of class AbstractController.
     */
    @Test
    public void testRowStatFailure() throws Exception {
        System.out.println("rowsStatExceptions");

        testQueueRawStreamForComm();

        Assert.assertThat(instance.rowsSent(), CoreMatchers.equalTo(-1));
        Assert.assertThat(instance.rowsRemaining(), CoreMatchers.equalTo(-1));
        Assert.assertThat(instance.rowsInSend(), CoreMatchers.equalTo(-1));
    }

    @Test
    public void testRowStats() throws Exception {
        testQueueStreamForComm();

        Assert.assertThat(instance.rowsSent(), CoreMatchers.equalTo(0));
        Assert.assertThat(instance.rowsRemaining(), CoreMatchers.equalTo(2));
        Assert.assertThat(instance.rowsInSend(), CoreMatchers.equalTo(2));
    }

    /**
     * Test of sendCommandImmediately method, of class AbstractController.
     */
    @Test
    @Ignore
    public void testSendCommandImmediately() throws Exception {
        System.out.println("sendCommandImmediately");
        String str = "";

        boolean threwException = false;
        try {
            instance.sendCommandImmediately(instance.createCommand(str));
        } catch (Exception e) {
            Assert.assertThat(e.getMessage(), CoreMatchers.startsWith("Cannot send command(s)"));
            threwException = true;
        }
        Assert.assertTrue(threwException);

        String command = "command";
        String port = "/some/port";
        int rate = 1234;

        openInstanceExpectUtility(port, rate);
        mockCommunicator.queueStringForComm(str + "\n");
        expect(expectLastCall()).times(1);
        mockCommunicator.streamCommands();
        expect(expectLastCall()).times(1);
        EasyMock.replay(instance, mockCommunicator);

        instance.openCommPort(port, rate);
        instance.sendCommandImmediately(instance.createCommand(str));

        EasyMock.verify(mockCommunicator, instance);
    }

    /**
     * Test of isReadyToStreamFile method, of class AbstractController.
     */
    @Test
    @Ignore
    public void testIsReadyToStreamFile() throws Exception {
        System.out.println("isReadyToStreamFile");

        instance.isReadyToStreamCommandsEvent();
        expect(expectLastCall()).times(3);

        Boolean commPortNotOpen = false;
        try {
            Boolean result = instance.isReadyToStreamFile();
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("comm port is not open"));
            commPortNotOpen = true;
        }
        assertTrue(commPortNotOpen);

        String command = "command";
        String port = "/some/port";
        int rate = 1234;

        startStreamExpectation(port, rate, command);
        replay(instance, mockCommunicator);

        instance.openCommPort(port, rate);

        assertEquals(true, instance.isReadyToStreamFile());

        instance.queueCommand(instance.createCommand(command));
        instance.queueCommand(instance.createCommand(command));
        instance.beginStreaming();

        Boolean alreadyStreaming = false;
        try {
            instance.isReadyToStreamFile();
        } catch (Exception e) {
            assertEquals("Already streaming.", e.getMessage());
            alreadyStreaming = true;
        }
        assertTrue(alreadyStreaming);
    }

    /**
     * Test of queueRawStream method, of class AbstractController.
     */
    @Test
    public void testQueueRawStreamForComm() throws Exception {
        System.out.println("queueStream");

        String command = "command";
        Collection<String> commands = Arrays.asList(command, command);
        String port = "/some/port";
        int rate = 1234;

        PipedReader in = new PipedReader();
        PipedWriter out = new PipedWriter(in);

        for(String i : commands) {
            out.append(i);
        }

        openInstanceExpectUtility(port, rate);
        streamInstanceExpectUtility();
        
        // TODO Fix this
        // Making sure the commands get queued.
        mockCommunicator.queueRawStreamForComm(in);
        EasyMock.expect(EasyMock.expectLastCall()).times(1);

        EasyMock.replay(instance, mockCommunicator);

        // Open port, send some commands, make sure they are streamed.
        instance.openCommPort(port, rate);
        instance.queueRawStream(in);
        instance.beginStreaming();

        EasyMock.verify(mockCommunicator, instance);
    }

    /**
     * Test of queueStream method, of class AbstractController.
     */
    @Test
    public void testQueueStreamForComm() throws Exception {
        System.out.println("queueStream");

        String command = "command";
        Collection<String> commands = Arrays.asList(command, command);
        String port = "/some/port";
        int rate = 1234;

        File f = new File(tempDir,"gcodeFile");
        try {
            try (GcodeStreamWriter gsw = new GcodeStreamWriter(f)) {
                for (String i : commands) {
                    gsw.addLine("blah", command, null, -1);
                }
            }

            try (GcodeStreamReader gsr = new GcodeStreamReader(f)) {
                openInstanceExpectUtility(port, rate);
                streamInstanceExpectUtility();

                // TODO Fix this
                // Making sure the commands get queued.
                mockCommunicator.queueStreamForComm(gsr);

                EasyMock.expect(EasyMock.expectLastCall()).times(1);

                EasyMock.replay(instance, mockCommunicator);

                // Open port, send some commands, make sure they are streamed.
                instance.openCommPort(port, rate);
                instance.queueStream(gsr);
                instance.beginStreaming();
            }

            EasyMock.verify(mockCommunicator, instance);
        } finally {
            FileUtils.forceDelete(f);
        }
    }

    /**
     * Test of queueCommand method, of class AbstractController.
     */
    @Test
    public void testQueueCommand() throws Exception {
        System.out.println("queueCommand");

        String command = "command";
        String port = "/some/port";
        int rate = 1234;

        openInstanceExpectUtility(port, rate);
        streamInstanceExpectUtility();
        
        // Making sure the commands get queued.
        mockCommunicator.queueStringForComm(command + "\n");
        EasyMock.expect(EasyMock.expectLastCall()).times(2);

        EasyMock.replay(instance, mockCommunicator);

        // Open port, send some commands, make sure they are streamed.
        instance.openCommPort(port, rate);
        instance.queueCommand(instance.createCommand(command));
        instance.queueCommand(instance.createCommand(command));
        instance.beginStreaming();

        EasyMock.verify(mockCommunicator, instance);
    }

    /**
     * Test of queueCommands method, of class AbstractController.
     */
    @Test
    public void testQueueCommands() throws Exception {
        System.out.println("queueCommands");

        String command = "command";
        String port = "/some/port";
        int rate = 1234;

        openInstanceExpectUtility(port, rate);
        streamInstanceExpectUtility();
        
        // Making sure the commands get queued.
        mockCommunicator.queueStringForComm(command + "\n");
        EasyMock.expect(EasyMock.expectLastCall()).times(2);

        EasyMock.replay(instance, mockCommunicator);

        // Open port, send some commands, make sure they are streamed.
        instance.openCommPort(port, rate);
        instance.queueCommand(instance.createCommand(command));
        instance.queueCommand(instance.createCommand(command));
        instance.beginStreaming();

        EasyMock.verify(mockCommunicator, instance);
    }

    /**
     * Test of beginStreaming method, of class AbstractController.
     */
    @Test
    public void testBeginStreaming() throws Exception {
        System.out.println("beginStreaming");
        System.out.println("-Covered by testQueueCommands-");
    }

    /**
     * Test of pauseStreaming method, of class AbstractController.
     */
    @Test
    @Ignore
    public void testPauseStreaming() throws Exception {
        System.out.println("pauseStreaming");
        instance.pauseStreaming();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of resumeStreaming method, of class AbstractController.
     */
    @Test
    @Ignore
    public void testResumeStreaming() throws Exception {
        System.out.println("resumeStreaming");
        instance.resumeStreaming();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of cancelSend method, of class AbstractController.
     */
    @Test
    @Ignore
    public void testCancelSend() throws Exception {
        // This is covered pretty thoroughly in GrblControllerTest.
        System.out.println("cancelSend");
        instance.cancelSend();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of commandSent method, of class AbstractController.
     */
    @Test
    public void testCommandSent() throws Exception {
        System.out.println("commandSent");

        String port = "/some/port";
        int baud = 1234;
        String command = "command";

        // Setup instance with commands buffered on the communicator.
        startStreamExpectation(port, baud, command);
        EasyMock.replay(instance, mockCommunicator);
        startStream(port, baud, command);
        EasyMock.reset(instance, mockCommunicator, mockListener);

        // Make sure the events are triggered.
        Capture<GcodeCommand> gc1 = EasyMock.newCapture();
        Capture<GcodeCommand> gc2 = EasyMock.newCapture();
        mockListener.commandSent(EasyMock.capture(gc1));
        EasyMock.expect(EasyMock.expectLastCall());
        mockListener.commandSent(EasyMock.capture(gc2));
        EasyMock.expect(EasyMock.expectLastCall());

        EasyMock.replay(instance, mockCommunicator, mockListener);

        // Run test.
        //assertEquals(0, instance.rowsSent());
        instance.commandSent(new GcodeCommand(command));
        //assertEquals(1, instance.rowsSent());
        instance.commandSent(new GcodeCommand(command));
        //assertEquals(2, instance.rowsSent());
        assertTrue(gc1.getValue().isSent());
        assertTrue(gc2.getValue().isSent());

        EasyMock.verify(mockListener);
    }

    /**
     * Test of commandComplete method, of class AbstractController.
     */
    @Test
    public void testCommandComplete() throws Exception {
        System.out.println("commandComplete");

        // Setup test with commands sent by communicator waiting on response.
        testCommandSent();
        reset(instance, mockCommunicator, mockListener);

        // Make sure the events are triggered.
        Capture<GcodeCommand> gc1 = newCapture();
        Capture<GcodeCommand> gc2 = newCapture();
        mockListener.commandComplete(capture(gc1));
        expect(expectLastCall());
        mockListener.commandComplete(capture(gc2));
        expect(expectLastCall());
        expect(expectLastCall());
        mockListener.messageForConsole(anyObject(), anyString());
        expect(expectLastCall());
        mockListener.fileStreamComplete("queued commands", true);
        expect(expectLastCall());

        expect(mockCommunicator.areActiveCommands()).andReturn(true);
        expect(mockCommunicator.areActiveCommands()).andReturn(false);

        replay(instance, mockCommunicator, mockListener);

        GcodeCommand first = instance.getActiveCommand();
        instance.commandComplete("ok");
        GcodeCommand second = instance.getActiveCommand();
        instance.commandComplete("ok");

        assertEquals(true, gc1.getValue().isDone());
        assertEquals(true, gc2.getValue().isDone());
        assertEquals("ok", gc1.getValue().getResponse());
        assertEquals("ok", gc2.getValue().getResponse());
        assertEquals(first, gc1.getValue());
        assertEquals(second, gc2.getValue());

        EasyMock.verify(mockListener);
    }




    // Exception tossing unimplemented methods.
    /**
     * Test of rawResponseHandler method, of class AbstractController.
     */
    @Test
    public void testRawResponseHandler() {
        System.out.println("rawResponseHandler");
        System.out.println("-N/A Abstract Function-");
    }

    /**
     * Test of performHomingCycle method, of class AbstractController.
     */
    @Test
    public void testPerformHomingCycle() throws Exception {
        System.out.println("performHomingCycle");
        System.out.println("-N/A Implementation Specific Function-");
    }

    /**
     * Test of returnToHome method, of class AbstractController.
     */
    @Test
    public void testReturnToHome() throws Exception {
        System.out.println("returnToHome");
        System.out.println("-N/A Implementation Specific Function-");
    }

    /**
     * Test of resetCoordinatesToZero method, of class AbstractController.
     */
    @Test
    public void testResetCoordinatesToZero() throws Exception {
        System.out.println("resetCoordinatesToZero");
        System.out.println("-N/A Implementation Specific Function-");
    }

    /**
     * Test of resetCoordinateToZero method, of class AbstractController.
     */
    @Test
    public void testResetCoordinateToZero() throws Exception {
        System.out.println("resetCoordinateToZero");
        System.out.println("-N/A Implementation Specific Function-");
    }

    /**
     * Test of killAlarmLock method, of class AbstractController.
     */
    @Test
    public void testKillAlarmLock() throws Exception {
        System.out.println("killAlarmLock");
        System.out.println("-N/A Implementation Specific Function-");
    }

    /**
     * Test of toggleCheckMode method, of class AbstractController.
     */
    @Test
    public void testToggleCheckMode() throws Exception {
        System.out.println("toggleCheckMode");
        System.out.println("-N/A Implementation Specific Function-");
    }

    /**
     * Test of viewParserState method, of class AbstractController.
     */
    @Test
    public void testViewParserState() throws Exception {
        System.out.println("viewParserState");
        System.out.println("-N/A Implementation Specific Function-");
    }

    /**
     * Test of issueSoftReset method, of class AbstractController.
     */
    @Test
    public void testIssueSoftReset() throws Exception {
        System.out.println("issueSoftReset");
        System.out.println("-N/A Implementation Specific Function-");
    }

    /**
     * Test of softReset method, of class AbstractController.
     */
    @Test
    public void testSoftReset() throws Exception {
        System.out.println("softReset");
        System.out.println("-N/A Implementation Specific Function-");
    }

    /**
     * Test of statusUpdatesEnabledValueChanged method, of class AbstractController.
     */
    @Test
    public void testStatusUpdatesEnabledValueChanged() {
        System.out.println("statusUpdatesEnabledValueChanged");
        System.out.println("-N/A Abstract Function-");
    }

    /**
     * Test of statusUpdatesRateValueChanged method, of class AbstractController.
     */
    @Test
    public void testStatusUpdatesRateValueChanged() {
        System.out.println("statusUpdatesRateValueChanged");
        System.out.println("-N/A Abstract Function-");
    }
}
