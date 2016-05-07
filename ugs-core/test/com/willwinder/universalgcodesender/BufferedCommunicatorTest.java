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

import static com.willwinder.universalgcodesender.AbstractControllerTest.tempDir;
import com.willwinder.universalgcodesender.connection.Connection;
import com.willwinder.universalgcodesender.listeners.SerialCommunicatorListener;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import com.willwinder.universalgcodesender.utils.GcodeStreamTest;
import com.willwinder.universalgcodesender.utils.GcodeStreamWriter;
import java.io.File;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.lang.reflect.Field;
import java.util.concurrent.LinkedBlockingDeque;
import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author wwinder
 */
public class BufferedCommunicatorTest {

    BufferedCommunicator instance;
    LinkedBlockingDeque<String> cb;
    LinkedBlockingDeque<GcodeCommand> asl;

    final static Connection mockConnection = EasyMock.createMock(Connection.class);
    final static SerialCommunicatorListener mockScl = EasyMock.createMock(SerialCommunicatorListener.class);
    
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

        instance = new BufferedCommunicatorImpl();
        instance.setConnection(mockConnection);
        instance.setListenAll(mockScl);
        cb = new LinkedBlockingDeque<>();
        asl = new LinkedBlockingDeque<>();
        instance.setQueuesForTesting(cb, asl);

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
    @Ignore
    public void testSingleStepMode() {
        System.out.println("testSingleStepMode");
        fail("Not implemented yet.");
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
        instance.queueStringForComm(input);
        assertEquals(input, cb.getFirst());
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
        mockScl.messageForConsole(EasyMock.anyString());
        EasyMock.expect(EasyMock.expectLastCall()).times(2);
        mockConnection.sendStringToComm(input + "\n");
        EasyMock.expect(EasyMock.expectLastCall()).times(2);
        mockScl.commandSent(EasyMock.<GcodeCommand>anyObject());
        EasyMock.expect(EasyMock.expectLastCall()).times(2);

        EasyMock.replay(mockConnection, mockScl);

        // Test
        instance.queueStringForComm(input);
        instance.queueStringForComm(input);
        instance.streamCommands();

        EasyMock.verify(mockConnection, mockScl);
    }

    @Test
    public void testSimpleRawStreamStream() throws Exception {
        String[] inputs = {"input1", "input2"};


        for (String i : inputs) {
            mockScl.messageForConsole(EasyMock.anyString());
            EasyMock.expect(EasyMock.expectLastCall());

            mockConnection.sendStringToComm(i + "\n");
            EasyMock.expect(EasyMock.expectLastCall());

            mockScl.commandSent(EasyMock.<GcodeCommand>anyObject());
            EasyMock.expect(EasyMock.expectLastCall());
        }

        EasyMock.replay(mockConnection, mockScl);

        PipedReader in = new PipedReader();
        try (PipedWriter out = new PipedWriter(in)) {
            for(String i : inputs) {
                out.append(i + "\n");
            }
        }
 
        instance.queueRawStreamForComm(in);
        instance.streamCommands();

        EasyMock.verify(mockConnection, mockScl);
    }

    @Test
    public void testSimpleStreamStream() throws Exception {
        String[] inputs = {"input1", "input2"};

        for (String i : inputs) {
            mockScl.messageForConsole(EasyMock.anyString());
            EasyMock.expect(EasyMock.expectLastCall());

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

        GcodeStreamReader gsr = new GcodeStreamReader(f);
 
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
        mockScl.messageForConsole(EasyMock.anyString());
        EasyMock.expect(EasyMock.expectLastCall()).times(2);

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
        instance.queueStringForComm(input);
        instance.queueStringForComm(input);
        instance.streamCommands();

        assertEquals(true, instance.areActiveCommands());
        assertEquals(2, instance.numActiveCommands());
        assertEquals(input + ", " + input, instance.activeCommandSummary());

        // Clear out active commands.
        instance.responseMessage("ok");
        assertEquals(true, instance.areActiveCommands());
        instance.responseMessage("ok");
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
            instance.queueStringForComm(input);
        }
        instance.streamCommands();
        instance.pauseSend();

        assertEquals("First 10 commands sent.", 10, asl.size());
        for (int i = 0; i < 10; i++) {
            instance.responseMessage("ok");
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
            instance.queueStringForComm(tenChar);
        }
        instance.streamCommands();

        // Cancel the send, and clear out the 10 active commands.
        instance.cancelSend();

        for (int i = 0; i < 10; i++) {
            instance.responseMessage("ok");
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
        instance.responseMessage(first);
        assertEquals(1, asl.size());
        instance.responseMessage("ok");
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

        mockConnection.setCommunicator(EasyMock.<AbstractCommunicator>anyObject());
        EasyMock.expect(EasyMock.expectLastCall()).once();
        EasyMock.expect(mockConnection.openPort(name, baud)).andReturn(true).once();
        EasyMock.replay(mockConnection);

        boolean result = instance.openCommPort(name, baud);
        assertEquals(expResult, result);

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

        instance.closeCommPort();

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
            instance.queueStringForComm(tenChar);
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

    public class BufferedCommunicatorImpl extends BufferedCommunicator {

        public int getBufferSize() {
            return 101;
        }

        public void sendingCommand(String command) {
        }

        public boolean processedCommand(String response) {
            return (response != null && "ok".equals(response));
        }

        @Override
        public String getLineTerminator() {
            return "\r\n";
        }
    }
}
