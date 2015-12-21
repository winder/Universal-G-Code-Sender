/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.connection.Connection;
import com.willwinder.universalgcodesender.listeners.SerialCommunicatorListener;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.Reader;
import java.util.concurrent.LinkedBlockingDeque;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author wwinder
 */
public class BufferedCommunicatorTest {

    BufferedCommunicator instance;
    LinkedBlockingDeque<String> cb;
    LinkedBlockingDeque<String> asl;

    final static Connection mockConnection = EasyMock.createMock(Connection.class);
    final static SerialCommunicatorListener mockScl = EasyMock.createMock(SerialCommunicatorListener.class);
    
    public BufferedCommunicatorTest() {
    }
    
    @Before
    public void setUp() {
        EasyMock.reset(mockConnection, mockScl);

        instance = new BufferedCommunicatorImpl();
        instance.setConnection(mockConnection);
        instance.setListenAll(mockScl);
        cb = new LinkedBlockingDeque<>();
        asl = new LinkedBlockingDeque<>();
        instance.setQueuesForTesting(cb, asl);
    }

    /**
     * Test of getBufferSize method, of class BufferedCommunicator.
     */
    @Test
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
        assertEquals(input + "\n", cb.getFirst());
    }

    /**
     * Test of streamCommands method, of class BufferedCommunicator.
     */
    @Test
    public void testSimpleStream() throws Exception {
        System.out.println("streamCommands");

        String input = "input\n";

        // Check events and connection:
        // console message, connection stream, sent event
        mockScl.messageForConsole(EasyMock.anyString());
        EasyMock.expect(EasyMock.expectLastCall()).times(2);
        mockConnection.sendStringToComm(input);
        EasyMock.expect(EasyMock.expectLastCall()).times(2);
        mockScl.commandSent(EasyMock.anyString());
        EasyMock.expect(EasyMock.expectLastCall()).times(2);

        EasyMock.replay(mockConnection, mockScl);

        // Test
        instance.queueStringForComm(input);
        instance.queueStringForComm(input);
        instance.streamCommands();

        // Verify
        Thread.sleep(1000);
        EasyMock.verify(mockConnection, mockScl);
    }

    @Test
    public void testSimpleStreamStream() throws Exception {
        String[] inputs = {"input1\n", "input2\n"};


        for (String i : inputs) {
            mockScl.messageForConsole(EasyMock.anyString());;
            EasyMock.expect(EasyMock.expectLastCall());

            mockConnection.sendStringToComm(i);
            EasyMock.expect(EasyMock.expectLastCall());

            mockScl.commandSent(EasyMock.anyString());
            EasyMock.expect(EasyMock.expectLastCall());
        }

        EasyMock.replay(mockConnection, mockScl);

        PipedReader in = new PipedReader();
        PipedWriter out = new PipedWriter(in);

        for(String i : inputs) {
            out.append(i);
        }
 
        instance.queueStreamForComm(in);
        instance.streamCommands();

        Thread.sleep(500);
        
        EasyMock.verify(mockConnection, mockScl);
    }

    /**
     * Test of areActiveCommands method, of class BufferedCommunicator.
     */
    @Test
    public void testAreActiveCommands() throws Exception {
        System.out.println("areActiveCommands");

        String input = "input\n";

        // Setup 2 active commands.
        mockScl.messageForConsole(EasyMock.anyString());
        EasyMock.expect(EasyMock.expectLastCall()).times(2);

        mockConnection.sendStringToComm(input);
        EasyMock.expect(EasyMock.expectLastCall()).times(2);

        mockScl.commandSent(EasyMock.anyString());
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

        // Leave active commands in pipeline.
        instance.queueStringForComm(input);
        instance.queueStringForComm(input);
        instance.streamCommands();

        assertEquals(true, instance.areActiveCommands());

        // Clear out active commands.
        instance.responseMessage("ok");
        assertEquals(true, instance.areActiveCommands());
        instance.responseMessage("ok");
        assertEquals(false, instance.areActiveCommands());

        Thread.sleep(500);
        EasyMock.verify(mockConnection, mockScl);
    }

    /**
     * Test of pauseSend method, of class BufferedCommunicator.
     */
    @Test
    public void testPauseSendResume() throws Exception {
        System.out.println("pauseSend");

        String input = "123456789\n";
        for (int i = 0; i < 11; i++) {
            mockConnection.sendStringToComm(input);
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
        String tenChar = "123456789\n";
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

        /*
        mockScl.rawResponseListener(first);
        EasyMock.expect(EasyMock.expectLastCall()).once();
        mockScl.rawResponseListener("ok");
        EasyMock.expect(EasyMock.expectLastCall()).once();
        */

        EasyMock.replay(mockScl);

        asl.add("command");
        instance.responseMessage(first);
        assertEquals(1, asl.size());
        instance.responseMessage("ok");
        assertEquals(0, asl.size());

        /*
        Thread.sleep(1000);
        EasyMock.verify(mockScl);
        */
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

        String tenChar = "123456789\n";
        for (int i = 0; i < 10; i++) {
            mockConnection.sendStringToComm(tenChar);
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
