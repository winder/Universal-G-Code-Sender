/*
    Copyright 2015-2023 Will Winder

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

import com.willwinder.universalgcodesender.communicator.AbstractCommunicator;
import com.willwinder.universalgcodesender.communicator.ICommunicator;
import com.willwinder.universalgcodesender.connection.ConnectionDriver;
import com.willwinder.universalgcodesender.gcode.DefaultCommandCreator;
import com.willwinder.universalgcodesender.gcode.ICommandCreator;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.services.MessageService;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.GcodeStreamTest;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SimpleGcodeStreamReader;
import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.easymock.IMockBuilder;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author wwinder
 */
public class AbstractControllerTest {
    
    private AbstractCommunicator mockCommunicator;
    private ControllerListener mockListener;
    private MessageService mockMessageService;
    private ICommandCreator gcodeCreator;

    private final Settings settings = new Settings();

    private static AbstractController instance;

    private static File tempDir = null;

    public void init() throws IllegalArgumentException {
        mockCommunicator = EasyMock.createMock(AbstractCommunicator.class);
        mockListener = EasyMock.createMock(ControllerListener.class);
        mockMessageService = EasyMock.createMock(MessageService.class);
        gcodeCreator = new DefaultCommandCreator();

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
                        "isCommOpen")
                .withConstructor(ICommunicator.class, ICommandCreator.class)
                .withArgs(mockCommunicator, gcodeCreator);

        instance = instanceBuilder.createMock();
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
        init();
        reset(mockCommunicator, mockListener, mockMessageService);
    }


    ///////////////
    // UTILITIES //
    ///////////////
    private void openInstanceExpectUtility(String port, int portRate) throws Exception {
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
    }
    private void streamInstanceExpectUtility() throws Exception {
        expect(mockCommunicator.areActiveCommands()).andReturn(false).anyTimes();
        instance.isReadyToStreamCommandsEvent();
        expect(expectLastCall()).once();
        mockCommunicator.streamCommands();
        expect(expectLastCall()).once();
    }
    private void startStreamExpectation(String port, int rate) throws Exception {
        openInstanceExpectUtility(port, rate);
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
        ICommandCreator result = instance.getCommandCreator();
        assertEquals(gcodeCreator, result);
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

        openInstanceExpectUtility(port, rate);
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
}
