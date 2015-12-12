/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.gcode.GcodeCommandCreator;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.SerialCommunicatorListener;
import com.willwinder.universalgcodesender.model.Utils;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import java.io.File;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import javax.vecmath.Point3d;
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
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

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

    IMockBuilder<AbstractController> builder;

    //@BeforeClass
    public static void init() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
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
                        "isReadyToSendCommandsEvent",
                        "rawResponseHandler",
                        "statusUpdatesEnabledValueChanged",
                        "statusUpdatesRateValueChanged")
                    .withConstructor(AbstractCommunicator.class)
                    .withArgs(mockCommunicator)
                    .createMock();

        // Initialize private variable.
        Field f = AbstractController.class.getDeclaredField("commandCreator");
        f.setAccessible(true);
        f.set(instance, gcodeCreator);
        
        instance.addListener(mockListener);
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
        mockListener.messageForConsole(EasyMock.anyString(), EasyMock.<Boolean>anyObject());
        EasyMock.expect(EasyMock.expectLastCall()).anyTimes();
        EasyMock.expect(mockCommunicator.openCommPort(port, portRate)).andReturn(true).once();
    }
    private void streamInstanceExpectUtility() throws Exception {
        EasyMock.expect(mockCommunicator.areActiveCommands()).andReturn(false).anyTimes();
        instance.isReadyToSendCommandsEvent();
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
        instance.queueCommand(command);
        instance.queueCommand(command);
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
    public void testOpenCommPort() throws Exception {
        System.out.println("openCommPort");
        String port = "";
        int portRate = 0;

        instance.openCommAfterEvent();
        EasyMock.expect(EasyMock.expectLastCall()).once();
        mockListener.messageForConsole(EasyMock.anyString(), EasyMock.<Boolean>anyObject());
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
        mockListener.messageForConsole(EasyMock.anyString(), EasyMock.<Boolean>anyObject());
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

        instance.commandSent(command);
        instance.commandSent(command);
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
     * Test of rowsInQueue method, of class AbstractController.
     */
    @Test
    public void testRowsInQueue() {
        System.out.println("rowsInQueue");
        int expResult = 0;
        int result = instance.rowsInQueue();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of rowsInSend method, of class AbstractController.
     */
    @Test
    public void testRowsInSend() {
        System.out.println("rowsInSend");
        int expResult = 0;
        int result = instance.rowsInSend();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of rowsSent method, of class AbstractController.
     */
    @Test
    public void testRowsSent() {
        System.out.println("rowsSent");
        int expResult = 0;
        int result = instance.rowsSent();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of rowsRemaining method, of class AbstractController.
     */
    @Test
    public void testRowsRemaining() {
        System.out.println("rowsRemaining");
        int expResult = 0;
        int result = instance.rowsRemaining();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of sendCommandImmediately method, of class AbstractController.
     */
    @Test
    public void testSendCommandImmediately() throws Exception {
        System.out.println("sendCommandImmediately");
        String str = "";
        instance.sendCommandImmediately(str);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isReadyToStreamFile method, of class AbstractController.
     */
    @Test
    public void testIsReadyToStreamFile() throws Exception {
        System.out.println("isReadyToStreamFile");

        instance.isReadyToSendCommandsEvent();
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

        instance.queueCommand(command);
        instance.queueCommand(command);
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
     * Test of queueStream method, of class AbstractController.
     */
    @Test
    public void testQueueStream() throws Exception {
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
        
        // Making sure the commands get queued.
        mockCommunicator.queueStreamForComm(in);
        EasyMock.expect(EasyMock.expectLastCall()).times(1);

        EasyMock.replay(instance, mockCommunicator);

        // Open port, send some commands, make sure they are streamed.
        instance.openCommPort(port, rate);
        instance.queueStream(in);
        instance.beginStreaming();

        EasyMock.verify(mockCommunicator, instance);
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
        instance.queueCommand(command);
        instance.queueCommand(command);
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
        Collection<String> commands = Arrays.asList(command, command);
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
        instance.queueCommands(commands);
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
    public void testCancelSend() {
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
        assertEquals(0, instance.rowsSent());
        instance.commandSent(command);
        assertEquals(1, instance.rowsSent());
        instance.commandSent(command);
        assertEquals(2, instance.rowsSent());
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
        mockListener.messageForConsole(anyString(), anyBoolean());
        expect(expectLastCall());
        mockListener.fileStreamComplete("queued commands", true);
        expect(expectLastCall());

        expect(mockCommunicator.areActiveCommands()).andReturn(true);
        expect(mockCommunicator.areActiveCommands()).andReturn(false);

        replay(instance, mockCommunicator, mockListener);

        instance.commandComplete("ok");
        instance.commandComplete("ok");

        assertEquals(true, gc1.getValue().isDone());
        assertEquals(true, gc2.getValue().isDone());
        assertEquals("ok", gc1.getValue().getResponse());
        assertEquals("ok", gc2.getValue().getResponse());

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
