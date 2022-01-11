/*
    Copyright 2015-2018 Will Winder

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

import com.willwinder.universalgcodesender.connection.Connection;
import com.willwinder.universalgcodesender.connection.ConnectionDriver;
import com.willwinder.universalgcodesender.listeners.CommunicatorListener;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import com.willwinder.universalgcodesender.utils.GcodeStreamTest;
import com.willwinder.universalgcodesender.utils.GcodeStreamWriter;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;
import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.concurrent.LinkedBlockingDeque;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author wwinder
 */
public class BufferedCommunicatorTest {

    private static File tempDir;
    private final static Connection mockConnection = EasyMock.createMock(Connection.class);
    private final static CommunicatorListener mockScl = EasyMock.createMock(CommunicatorListener.class);
    private BufferedCommunicator instance;
    private LinkedBlockingDeque<GcodeCommand> cb;
    private LinkedBlockingDeque<GcodeCommand> asl;

    public BufferedCommunicatorTest() {
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
    public void setUp() throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException {
        EasyMock.reset(mockConnection, mockScl);
        cb = new LinkedBlockingDeque<>();
        asl = new LinkedBlockingDeque<>();

        instance = new BufferedCommunicatorImpl(cb, asl);
        instance.setConnection(mockConnection);
        instance.addListener(mockScl);

        // Initialize private variable.
        Field f = AbstractCommunicator.class.getDeclaredField("launchEventsInDispatchThread");
        f.setAccessible(true);
        f.set(instance, false);

        EasyMock.reset(mockConnection, mockScl);
    }

    /**
     * Test of getBufferSize method, of class BufferedCommunicator.
     */
    @Test
    public void testGetBufferSize() {
        System.out.println("getBufferSize");
        assertEquals(101, instance.getBufferSize());
    }

    /**
     * Test of queueStringForComm method, of class BufferedCommunicator.
     */
    @Test
    public void testQueueStringForComm() {
        System.out.println("queueStringForComm");
        String input = "input";
        instance.queueCommand(new GcodeCommand(input));
        assertEquals(input, cb.getFirst().getCommandString());
    }

    /**
     * Test of streamCommands method, of class BufferedCommunicator.
     */
    @Test
    public void testSimpleQueueStringsStream() throws Exception {
        System.out.println("streamCommands");

        String input = "input";

        // Check events and connection:
        // console message, connection stream, sent event
        mockConnection.sendStringToComm(input + "\n");
        EasyMock.expect(EasyMock.expectLastCall()).times(2);
        mockScl.commandSent(EasyMock.anyObject(GcodeCommand.class));
        EasyMock.expect(EasyMock.expectLastCall()).times(2);

        EasyMock.replay(mockConnection, mockScl);

        // Test
        instance.queueCommand(new GcodeCommand(input));
        instance.queueCommand(new GcodeCommand(input));
        instance.streamCommands();

        EasyMock.verify(mockConnection, mockScl);
    }

    @Test
    public void testSimpleStreamStream() throws Exception {
        String[] inputs = {"input1", "input2"};

        for (String i : inputs) {
            mockConnection.sendStringToComm(i + "\n");
            EasyMock.expect(EasyMock.expectLastCall());

            mockScl.commandSent(EasyMock.<GcodeCommand>anyObject());
            EasyMock.expect(EasyMock.expectLastCall());
        }

        EasyMock.replay(mockConnection, mockScl);
        
        File f = new File(tempDir,"gcodeFile");

        try (GcodeStreamWriter gsw = new GcodeStreamWriter(f)) {
            for(String i : inputs) {
                gsw.addLine("blah", i, null, -1);
            }
        }

        IGcodeStreamReader gsr = new GcodeStreamReader(f);
 
        instance.queueStreamForComm(gsr);
        instance.streamCommands();

        assertEquals("input1, input2, 0 streaming commands.", instance.activeCommandSummary());

        EasyMock.verify(mockConnection, mockScl);
    }

    /**
     * Test of areActiveCommands method, of class BufferedCommunicator.
     */
    @Test
    public void testActiveCommands() throws Exception {
        System.out.println("areActiveCommands");

        String input = "input";

        // Setup 2 active commands.
        mockConnection.sendStringToComm(input + "\n");
        EasyMock.expect(EasyMock.expectLastCall()).times(2);

        mockScl.commandSent(EasyMock.<GcodeCommand>anyObject());
        EasyMock.expect(EasyMock.expectLastCall()).times(2);

        // Left an active command.

        // Active commands complete.
        mockScl.rawResponseListener("ok");
        EasyMock.expect(EasyMock.expectLastCall()).times(2);

        EasyMock.replay(mockConnection, mockScl);

        //////////////
        // THE TEST //
        //////////////

        // No active commands.
        assertEquals(false, instance.areActiveCommands());
        assertEquals(0, instance.numActiveCommands());

        // Leave active commands in pipeline.
        instance.queueCommand(new GcodeCommand(input));
        instance.queueCommand(new GcodeCommand(input));
        instance.streamCommands();

        assertEquals(true, instance.areActiveCommands());
        assertEquals(2, instance.numActiveCommands());
        assertEquals(input + ", " + input, instance.activeCommandSummary());

        // Clear out active commands.
        instance.handleResponseMessage("ok");
        assertEquals(true, instance.areActiveCommands());
        instance.handleResponseMessage("ok");
        assertEquals(false, instance.areActiveCommands());

        assertEquals(0, instance.numActiveCommands());
        EasyMock.verify(mockConnection, mockScl);
    }

    /**
     * Test of pauseSend method, of class BufferedCommunicator.
     */
    @Test
    public void testPauseSendResume() throws Exception {
        System.out.println("pauseSend");

        String input = "123456789";
        for (int i = 0; i < 11; i++) {
            mockConnection.sendStringToComm(input + "\n");
            EasyMock.expect(EasyMock.expectLastCall());
        }
        EasyMock.replay(mockConnection);

        // Send the first 10 commands, pause 11th
        for (int i = 0; i < 11; i++) {
            instance.queueCommand(new GcodeCommand(input));
        }
        instance.streamCommands();
        instance.pauseSend();

        assertEquals("First 10 commands sent.", 10, asl.size());
        for (int i = 0; i < 10; i++) {
            instance.handleResponseMessage("ok");
        }
        assertEquals("First 10 commands done.", 0, asl.size());

        // Resume and send the last command.
        instance.resumeSend();

        assertEquals("Last comamnd active.", 1, asl.size());
        EasyMock.verify(mockConnection);
    }

    /**
     * Test of cancelSend method, of class BufferedCommunicator.
     */
    @Test
    public void testCancelSend() {
        System.out.println("cancelSend");

        // Queue up 200 characters.
        String tenChar = "123456789";
        for (int i = 0; i < 20; i++) {
            instance.queueCommand(new GcodeCommand(tenChar));
        }
        instance.streamCommands();

        // Cancel the send, and clear out the 10 active commands.
        instance.cancelSend();

        for (int i = 0; i < 10; i++) {
            instance.handleResponseMessage("ok");
        }

        assertEquals(false, instance.areActiveCommands());
    }

    /**
     * Test of responseMessage method, of class BufferedCommunicator.
     */
    @Test
    public void testResponseMessage() throws Exception {
        System.out.println("responseMessage");

        String first = "not-handled";

        mockScl.rawResponseListener(first);
        EasyMock.expect(EasyMock.expectLastCall()).once();
        mockScl.rawResponseListener("ok");
        EasyMock.expect(EasyMock.expectLastCall()).once();

        EasyMock.replay(mockScl);

        asl.add(new GcodeCommand("command"));
        instance.handleResponseMessage(first);
        assertEquals(1, asl.size());
        instance.handleResponseMessage("ok");
        assertEquals(0, asl.size());
    }

    /**
     * Test of openCommPort method, of class BufferedCommunicator.
     */
    @Test
    public void testOpenCommPort() throws Exception {
        System.out.println("openCommPort");
        String name = "";
        int baud = 0;
        boolean expResult = true;

        mockConnection.addListener(EasyMock.<AbstractCommunicator>anyObject());
        EasyMock.expect(EasyMock.expectLastCall()).once();
        EasyMock.expect(mockConnection.openPort()).andReturn(true).once();
        EasyMock.replay(mockConnection);

        instance.connect(ConnectionDriver.JSSC, name, baud);

        EasyMock.verify(mockConnection);
    }

    /**
     * Test of closeCommPort method, of class BufferedCommunicator.
     */
    @Test
    public void testCloseCommPort() throws Exception {
        System.out.println("closeCommPort");
        boolean expResult = true;

        mockConnection.closePort();
        EasyMock.expect(EasyMock.expectLastCall()).once();
        EasyMock.replay(mockConnection);

        instance.disconnect();

        EasyMock.verify(mockConnection);
    }

    /**
     * Test of sendByteImmediately method, of class BufferedCommunicator.
     */
    @Test
    public void testSendByteImmediately() throws Exception {
        System.out.println("sendByteImmediately");
        byte b = 10;

        String tenChar = "123456789";
        for (int i = 0; i < 10; i++) {
            mockConnection.sendStringToComm(tenChar + "\n");
        }
        mockConnection.sendByteImmediately(b);

        EasyMock.replay(mockConnection);

        // Queue up 200 characters.
        for (int i = 0; i < 20; i++) {
            instance.queueCommand(new GcodeCommand(tenChar));
        }
        instance.streamCommands();

        // Make sure the byte is sent.
        instance.sendByteImmediately(b);

        EasyMock.verify(mockConnection);
    }

    /**
     * Test of sendingCommand method, of class BufferedCommunicatorImpl.
     */
    @Test
    public void testSendingCommand() {
        System.out.println("sendingCommand");
        System.out.println("-N/A for abstract class-");
    }

    /**
     * Test of processedCommand method, of class BufferedCommunicatorImpl.
     */
    @Test
    public void testProcessedCommand() {
        System.out.println("processedCommand");
        System.out.println("-N/A for abstract class-");
    }

    @Test
    public void testStreamCommandsOrderStringCommandsFirst() throws Exception {
        // Given
        Connection connection = mock(Connection.class);
        instance.setConnection(connection);

        ArgumentCaptor<String> commandCaptor = ArgumentCaptor.forClass(String.class);
        doNothing().when(connection).sendStringToComm(commandCaptor.capture());

        // Create a gcode file stream
        File gcodeFile = new File(tempDir,"gcodeFile");
        GcodeStreamWriter gcodeStreamWriter = new GcodeStreamWriter(gcodeFile);
        gcodeStreamWriter.addLine("G0", "G0", null, 0);
        gcodeStreamWriter.close();

        instance.queueStreamForComm(new GcodeStreamReader(gcodeFile));
        instance.queueCommand(new GcodeCommand("G1"));

        // When
        instance.streamCommands();

        // Then
        assertEquals(2, commandCaptor.getAllValues().size());
        assertEquals("The first command processed should be the string command", "G1\n", commandCaptor.getAllValues().get(0));
        assertEquals("The second command should be from the stream", "G0\n", commandCaptor.getAllValues().get(1));
    }

    @Test
    public void softResetShouldClearBuffersAndResumeOperation() {
        // Given
        Connection connection = mock(Connection.class);
        instance.setConnection(connection);
        asl.add(new GcodeCommand("G0"));
        cb.add(new GcodeCommand("G0"));

        instance.pauseSend();
        assertTrue(instance.isPaused());
        assertEquals(1, instance.numActiveCommands());
        assertEquals(1, instance.numBufferedCommands());

        // When
        instance.cancelSend();

        // Then
        assertFalse("The communicator should resume operation after a reset", instance.isPaused());
        assertEquals("There should be no active commands", 0, instance.numActiveCommands());
        assertEquals("There should be no buffered manual commands", 0, instance.numBufferedCommands());
    }

    @Test
    public void responseMessageOnErrorShouldPauseTheCommunicator() {
        // Given
        Connection connection = mock(Connection.class);
        instance.setConnection(connection);
        asl.add(new GcodeCommand("G0"));
        asl.add(new GcodeCommand("G0"));
        instance.streamCommands();

        // When
        instance.handleResponseMessage("error");

        // Then
        assertTrue(instance.isPaused());
    }

    @Test
    public void responseMessageOnErrorShouldDispatchPauseEvent() {
        // Given
        Connection connection = mock(Connection.class);
        instance.setConnection(connection);

        CommunicatorListener communicatorListener = mock(CommunicatorListener.class);
        instance.addListener(communicatorListener);

        asl.add(new GcodeCommand("G0"));
        asl.add(new GcodeCommand("G0"));
        instance.streamCommands();

        // When
        instance.handleResponseMessage("error");

        // Then
        assertTrue(instance.isPaused());
        verify(communicatorListener, times(1)).communicatorPausedOnError();
    }

    @Test
    public void responseMessageOnErrorOnManualCommandShouldPauseTheCommunicator() {
        // Given
        Connection connection = mock(Connection.class);
        instance.setConnection(connection);
        cb.add(new GcodeCommand("G0"));
        cb.add(new GcodeCommand("G0"));
        instance.streamCommands();

        // When
        instance.handleResponseMessage("error");

        // Then
        assertTrue(instance.isPaused());
    }

    @Test
    public void responseMessageOnErrorOnLastManualCommandShouldNotPauseTheCommunicator() {
        // Given
        Connection connection = mock(Connection.class);
        instance.setConnection(connection);
        cb.add(new GcodeCommand("G0"));
        instance.streamCommands();

        // When
        instance.handleResponseMessage("error");

        // Then
        assertFalse(instance.isPaused());
    }

    @Test
    public void responseMessageOnErrorOnLastCommandShouldNotPauseTheCommunicator() {
        // Given
        Connection connection = mock(Connection.class);
        instance.setConnection(connection);
        asl.add(new GcodeCommand("G0"));
        instance.streamCommands();

        // When
        instance.handleResponseMessage("error");

        // Then
        assertFalse(instance.isPaused());
    }

    @Test
    public void responseMessageOnErrorOnCommandStreamShouldPauseTheCommunicator() throws IOException, GcodeStreamReader.NotGcodeStreamFile {
        // Given
        Connection connection = mock(Connection.class);
        instance.setConnection(connection);

        // Create a gcode file stream
        File gcodeFile = new File(tempDir, "gcodeFile");
        GcodeStreamWriter gcodeStreamWriter = new GcodeStreamWriter(gcodeFile);
        gcodeStreamWriter.addLine("G0", "G0", null, 0);
        gcodeStreamWriter.addLine("G0", "G0", null, 0);
        gcodeStreamWriter.close();

        // Stream
        instance.queueStreamForComm(new GcodeStreamReader(gcodeFile));
        instance.streamCommands();

        // When
        instance.handleResponseMessage("error");

        // Then
        assertTrue(instance.isPaused());
    }

    @Test
    public void responseMessageOnErrorOnLastCommandStreamShouldNotPauseTheCommunicator() throws IOException, GcodeStreamReader.NotGcodeStreamFile {
        // Given
        Connection connection = mock(Connection.class);
        instance.setConnection(connection);

        // Create a gcode file stream
        File gcodeFile = new File(tempDir, "gcodeFile");
        GcodeStreamWriter gcodeStreamWriter = new GcodeStreamWriter(gcodeFile);
        gcodeStreamWriter.addLine("G0", "G0", null, 0);
        gcodeStreamWriter.close();

        // Stream
        instance.queueStreamForComm(new GcodeStreamReader(gcodeFile));
        instance.streamCommands();

        // When
        instance.handleResponseMessage("error");

        // Then
        assertFalse(instance.isPaused());
    }

    public class BufferedCommunicatorImpl extends BufferedCommunicator {
        BufferedCommunicatorImpl(LinkedBlockingDeque<GcodeCommand> cb, LinkedBlockingDeque<GcodeCommand> asl) {
            super(cb, asl);
        }

        public int getBufferSize() {
            return 101;
        }

        public void sendingCommand(String command) {
        }

        public boolean processedCommand(String response) {
            return (response != null &&
                    ("ok".equals(response) || response.startsWith("error")));
        }

        public boolean processedCommandIsError(String response) {
            return (response != null && response.startsWith("error"));
        }
    }
}
