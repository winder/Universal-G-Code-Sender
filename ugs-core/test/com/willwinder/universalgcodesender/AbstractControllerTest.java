/*
    Copyright 2015-2020 Will Winder

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

import com.willwinder.universalgcodesender.connection.ConnectionDriver;
import com.willwinder.universalgcodesender.gcode.GcodeCommandCreator;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.model.*;
import com.willwinder.universalgcodesender.services.MessageService;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import com.willwinder.universalgcodesender.utils.GcodeStreamTest;
import com.willwinder.universalgcodesender.utils.GcodeStreamWriter;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SimpleGcodeStreamReader;
import org.apache.commons.io.FileUtils;
import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.EasyMock;
import org.easymock.IMockBuilder;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author wwinder
 */
public class AbstractControllerTest {
    
    private final static AbstractCommunicator mockCommunicator = EasyMock.createMock(AbstractCommunicator.class);
    private final static ControllerListener mockListener = EasyMock.createMock(ControllerListener.class);
    private final static MessageService mockMessageService = EasyMock.createMock(MessageService.class);
    private final static GcodeCommandCreator gcodeCreator = new GcodeCommandCreator();

    private Settings settings = new Settings();

    private static AbstractController instance;
    private static AbstractController niceInstance;

    private static File tempDir = null;

    //@BeforeClass
    public static void init() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        IMockBuilder<AbstractController> instanceBuilder = EasyMock
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
                .withConstructor(ICommunicator.class)
                .withArgs(mockCommunicator);

        instance = instanceBuilder.createMock();
        niceInstance = instanceBuilder.createNiceMock();

        // Initialize private variable.
        Field f = AbstractController.class.getDeclaredField("commandCreator");
        f.setAccessible(true);
        f.set(instance, gcodeCreator);
        f.set(niceInstance, gcodeCreator);
        
        instance.addListener(mockListener);
        instance.setMessageService(mockMessageService);
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
        reset(mockCommunicator, mockListener, mockMessageService);
        init();
        reset(mockCommunicator, mockListener, mockMessageService);
    }


    ///////////////
    // UTILITIES //
    ///////////////
    private void openInstanceExpectUtility(String port, int portRate, boolean handleStateChange) throws Exception {
        instance.openCommAfterEvent();
        expect(expectLastCall()).anyTimes();
        mockMessageService.dispatchMessage(anyObject(), anyString());
        expect(expectLastCall()).anyTimes();
        instance.setControllerState(eq(ControllerState.CONNECTING));
        expect(expectLastCall()).once();
        expect(mockCommunicator.isConnected()).andReturn(true).anyTimes();
        mockCommunicator.connect(or(eq(ConnectionDriver.JSERIALCOMM), eq(ConnectionDriver.JSSC)), eq(port), eq(portRate));
        expect(instance.isCommOpen()).andReturn(false).once();
        expect(instance.isCommOpen()).andReturn(true).anyTimes();
        expect(instance.handlesAllStateChangeEvents()).andReturn(handleStateChange).anyTimes();
    }
    private void streamInstanceExpectUtility() throws Exception {
        expect(mockCommunicator.areActiveCommands()).andReturn(false).anyTimes();
        instance.isReadyToStreamCommandsEvent();
        expect(expectLastCall()).once();
        mockCommunicator.streamCommands();
        expect(expectLastCall()).once();
    }
    private void startStreamExpectation(String port, int rate) throws Exception {
        openInstanceExpectUtility(port, rate, false);
        streamInstanceExpectUtility();
        
        // Making sure the commands get queued.
        mockCommunicator.queueStreamForComm(anyObject(IGcodeStreamReader.class));
        expect(expectLastCall()).times(1);
    }
    private void startStream(String port, int rate, String command) throws Exception {
        // Open port, send some commands, make sure they are streamed.
        instance.openCommPort(getSettings().getConnectionDriver(), port, rate);
        instance.queueStream(new SimpleGcodeStreamReader(command, command));
        instance.beginStreaming();
    }
    private Settings getSettings() {
        return settings;
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
     * Test of isStreamingFile method, of class AbstractController.
     */
    @Test
    public void testIsStreaming() throws Exception {
        System.out.println("isStreaming");

        assertEquals(false, instance.isStreaming());
        testQueueCommands();
        assertEquals(true, instance.isStreaming());
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

        startStreamExpectation(port, rate);
        expect(mockCommunicator.numActiveCommands()).andReturn(1);
        expect(mockCommunicator.numActiveCommands()).andReturn(0);
        instance.updateCommandFromResponse(anyObject(), anyString());
        expect(expectLastCall()).times(2);
        expect(instance.getControllerStatus()).andReturn(new ControllerStatus(ControllerState.IDLE, new Position(0,0,0, UnitUtils.Units.MM), new Position(0,0,0, UnitUtils.Units.MM)));
        expect(instance.getControllerStatus()).andReturn(new ControllerStatus(ControllerState.IDLE, new Position(0,0,0, UnitUtils.Units.MM), new Position(0,0,0, UnitUtils.Units.MM)));
        replay(instance, mockCommunicator);

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

        verify(mockCommunicator, instance);
    }

    @Test
    public void testRowStats() throws Exception {
        testQueueStreamForComm();

        Assert.assertEquals(0, instance.rowsSent());
        Assert.assertEquals(2, instance.rowsRemaining());
        Assert.assertEquals(2, instance.rowsInSend());
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

            try (IGcodeStreamReader gsr = new GcodeStreamReader(f)) {
                openInstanceExpectUtility(port, rate, false);
                streamInstanceExpectUtility();

                // TODO Fix this
                // Making sure the commands get queued.
                mockCommunicator.queueStreamForComm(gsr);

                expect(expectLastCall()).times(1);

                replay(instance, mockCommunicator);

                // Open port, send some commands, make sure they are streamed.
                instance.openCommPort(getSettings().getConnectionDriver(), port, rate);
                instance.queueStream(gsr);
                instance.beginStreaming();
            }

            verify(mockCommunicator, instance);
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

        openInstanceExpectUtility(port, rate, false);
        streamInstanceExpectUtility();
        
        // Making sure the commands get queued.
        mockCommunicator.queueStreamForComm(anyObject(IGcodeStreamReader.class));
        expect(expectLastCall()).times(1);

        replay(instance, mockCommunicator);

        // Open port, send some commands, make sure they are streamed.
        instance.openCommPort(getSettings().getConnectionDriver(), port, rate);
        instance.queueStream(new SimpleGcodeStreamReader(command, command));
        instance.beginStreaming();

        verify(mockCommunicator, instance);
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

        openInstanceExpectUtility(port, rate, false);
        streamInstanceExpectUtility();
        
        // Making sure the commands get queued.
        mockCommunicator.queueStreamForComm(anyObject(IGcodeStreamReader.class));
        expect(expectLastCall()).times(1);

        replay(instance, mockCommunicator);

        // Open port, send some commands, make sure they are streamed.
        instance.openCommPort(getSettings().getConnectionDriver(), port, rate);
        instance.queueStream(new SimpleGcodeStreamReader(command, command));
        instance.beginStreaming();

        verify(mockCommunicator, instance);
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
        startStreamExpectation(port, baud);
        replay(instance, mockCommunicator);
        startStream(port, baud, command);
        reset(instance, mockCommunicator, mockListener);

        // Make sure the events are triggered.
        Capture<GcodeCommand> gc1 = EasyMock.newCapture();
        Capture<GcodeCommand> gc2 = EasyMock.newCapture();
        mockListener.commandSent(EasyMock.capture(gc1));
        expect(expectLastCall());
        mockListener.commandSent(EasyMock.capture(gc2));
        expect(expectLastCall());

        replay(instance, mockCommunicator, mockListener);

        // Run test.
        //assertEquals(0, instance.rowsSent());
        instance.commandSent(new GcodeCommand(command));
        //assertEquals(1, instance.rowsSent());
        instance.commandSent(new GcodeCommand(command));
        //assertEquals(2, instance.rowsSent());
        assertTrue(gc1.getValue().isSent());
        assertTrue(gc2.getValue().isSent());

        verify(mockListener);
    }

    /**
     * Test of commandComplete method, of class AbstractController.
     */
    @Test
    public void testCommandComplete() throws Exception {
        // Setup test with commands sent by communicator waiting on response.
        testCommandSent();
        reset(instance, mockCommunicator, mockListener, mockMessageService);
        expect(mockCommunicator.isConnected()).andReturn(true).anyTimes();
        expect(instance.handlesAllStateChangeEvents()).andReturn(true).anyTimes();
        instance.updateCommandFromResponse(anyObject(), eq("ok"));
        expect(expectLastCall()).times(2);

        // Make sure the events are triggered.
        Capture<GcodeCommand> gc1 = newCapture();
        Capture<GcodeCommand> gc2 = newCapture();
        mockListener.commandComplete(capture(gc1));
        expect(expectLastCall());
        mockListener.commandComplete(capture(gc2));
        expect(expectLastCall());
        expect(expectLastCall());
        mockMessageService.dispatchMessage(anyObject(), anyString());
        expect(expectLastCall());
        mockListener.fileStreamComplete("queued commands", true);
        mockListener.controlStateChange(CommunicatorState.COMM_IDLE);
        expect(expectLastCall());

        expect(instance.getControllerStatus()).andReturn(new ControllerStatus(ControllerState.IDLE, new Position(0,0,0, UnitUtils.Units.MM), new Position(0,0,0, UnitUtils.Units.MM)));
        expect(instance.getControllerStatus()).andReturn(new ControllerStatus(ControllerState.IDLE, new Position(0,0,0, UnitUtils.Units.MM), new Position(0,0,0, UnitUtils.Units.MM)));
        expect(mockCommunicator.areActiveCommands()).andReturn(true);
        expect(mockCommunicator.areActiveCommands()).andReturn(false);
        expect(mockCommunicator.numActiveCommands()).andReturn(0);
        replay(instance, mockCommunicator, mockListener);

        GcodeCommand first = instance.getActiveCommand().orElseThrow(() -> new RuntimeException("Couldn't find first command"));
        instance.commandComplete("ok");
        GcodeCommand second = instance.getActiveCommand().orElseThrow(() -> new RuntimeException("Couldn't find second command"));
        instance.commandComplete("ok");

        verify(mockListener);
    }

    /**
     * Test of jogMachine method, of class AbstractController.
     */
    @Test
    public void testJogMachine() throws Exception {
        System.out.println("jogMachine");

        expect(niceInstance.handlesAllStateChangeEvents()).andReturn(true).anyTimes();
        expect(niceInstance.isCommOpen()).andReturn(true).anyTimes();
        mockCommunicator.streamCommands();
        expect(expectLastCall()).anyTimes();

        Capture<GcodeCommand> gcodeCommandCapture = EasyMock.newCapture(CaptureType.ALL);
        mockCommunicator.queueCommand(capture(gcodeCommandCapture));
        expect(expectLastCall()).times(4);

        replay(niceInstance, mockCommunicator);

        niceInstance.setDistanceModeCode("G90");
        niceInstance.setUnitsCode("G21");

        niceInstance.jogMachine(new PartialPosition(-10., null, 10., UnitUtils.Units.INCH), 11);
        niceInstance.jogMachine(new PartialPosition(null, 10., null, UnitUtils.Units.MM), 11);

        assertEquals(4, gcodeCommandCapture.getValues().size());
        assertEquals("G20G91G1X-10Z10F11", gcodeCommandCapture.getValues().get(0).getCommandString());
        assertTrue(gcodeCommandCapture.getValues().get(0).isTemporaryParserModalChange());

        assertEquals("G90 G21 ", gcodeCommandCapture.getValues().get(1).getCommandString());
        assertEquals("G21G91G1Y10F11", gcodeCommandCapture.getValues().get(2).getCommandString());
        assertEquals("G90 G21 ", gcodeCommandCapture.getValues().get(3).getCommandString());
        assertTrue(gcodeCommandCapture.getValues().get(3).isTemporaryParserModalChange());
    }
}
