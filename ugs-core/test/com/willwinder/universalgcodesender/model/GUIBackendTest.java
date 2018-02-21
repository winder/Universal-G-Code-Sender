/*
    Copyright 2016-2018 Will Winder

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
package com.willwinder.universalgcodesender.model;

import com.willwinder.universalgcodesender.AbstractController;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.UGSEvent.ControlState;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.Settings;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit test for GUIBackend
 *
 * @author wwinder
 * @author Joacim Breiler
 */
public class GUIBackendTest {

    private static final String FIRMWARE = "GRBL";
    private static final String PORT = "/dev/ttyS0";
    private static final int BAUD_RATE = 9600;

    /**
     * An argument captor for any UGS event fired from the application.
     * Run your test and fetch any events using eventArgumentCaptor.getAllValues()
     */
    private ArgumentCaptor<UGSEvent> eventArgumentCaptor;

    private AbstractController controller;

    private Settings settings;

    private GUIBackend instance;

    @Before
    public void setUp() throws Exception {

        // We need to mock the method that loads the controller
        instance = spy(new GUIBackend());
        controller = mock(AbstractController.class);
        doReturn(controller).when(instance).fetchControllerFromFirmware(any());

        // Add a event listener that stores events in the argument captor
        UGSEventListener ugsEventListener = mock(UGSEventListener.class);
        eventArgumentCaptor = ArgumentCaptor.forClass(UGSEvent.class);
        doNothing().when(ugsEventListener).UGSEvent(eventArgumentCaptor.capture());
        instance.addUGSEventListener(ugsEventListener);

        // Add settings
        settings = new Settings();
        instance.applySettings(settings);
    }

    @Test
    public void adjustManualLocationShouldBeOk() throws Exception {
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
        instance.adjustManualLocation(1, 0, 0, 10, 10, UnitUtils.Units.MM);
        verify(controller, times(1)).jogMachine(1, 0, 0, 10, 10, UnitUtils.Units.MM);
    }

    @Test
    public void adjustManualLocationWithNoDirectionShouldNotMoveTheMachine() throws Exception {
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
        instance.adjustManualLocation(0, 0, 0, 10, 10, UnitUtils.Units.MM);
        verify(controller, times(0)).jogMachine(anyInt(), anyInt(), anyInt(), anyDouble(), anyDouble(), any(UnitUtils.Units.class));
    }

    @Test
    public void probeShouldBeOk() throws Exception {
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
        instance.probe("Z", 10, 10, UnitUtils.Units.MM);
        verify(controller, times(1)).probe(anyString(), anyDouble(), anyDouble(), any(UnitUtils.Units.class));
    }

    @Test
    public void pauseResumeWhenInIdleShouldThrowException() throws Exception {
        when(controller.getControlState()).thenReturn(ControlState.COMM_IDLE);
        instance.connect(FIRMWARE, PORT, BAUD_RATE);

        try {
            instance.pauseResume();
            fail("We should have had an exception here");
        } catch (Exception e) {
            // Expected!
        }

        verify(controller, times(0)).pauseStreaming();
        verify(controller, times(0)).resumeStreaming();
    }

    @Test
    public void pauseResumeWhenDisconnectedShouldThrowException() throws Exception {
        when(controller.getControlState()).thenReturn(ControlState.COMM_DISCONNECTED);
        instance.connect(FIRMWARE, PORT, BAUD_RATE);

        try {
            instance.pauseResume();
            fail("We should have had an exception here");
        } catch (Exception e) {
            // Expected!
        }

        verify(controller, times(0)).pauseStreaming();
        verify(controller, times(0)).resumeStreaming();
    }

    @Test
    public void pauseResumeWhenSendingShouldPause() throws Exception {
        when(controller.getControlState()).thenReturn(ControlState.COMM_SENDING);
        instance.connect(FIRMWARE, PORT, BAUD_RATE);

        instance.pauseResume();

        verify(controller, times(1)).pauseStreaming();
        verify(controller, times(0)).resumeStreaming();
    }

    @Test
    public void pauseResumeWhenPausedShouldResume() throws Exception {
        when(controller.getControlState()).thenReturn(ControlState.COMM_SENDING_PAUSED);
        instance.connect(FIRMWARE, PORT, BAUD_RATE);

        instance.pauseResume();

        verify(controller, times(0)).pauseStreaming();
        verify(controller, times(1)).resumeStreaming();
    }

    @Test
    public void getSendRemainingDuration() throws Exception {
        // Given
        when(controller.rowsSent()).thenReturn(10);
        when(controller.getSendDuration()).thenReturn(10L);
        when(controller.rowsInSend()).thenReturn(1000);
        instance.connect(FIRMWARE, PORT, BAUD_RATE);

        // When
        long remainingDuration = instance.getSendRemainingDuration();

        // Then
        assertEquals(990L, remainingDuration);
    }

    @Test
    public void offsetShouldBeOk() throws Exception {
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
        instance.offsetTool("Z", 10, UnitUtils.Units.MM);
        verify(controller, times(1)).offsetTool(anyString(), anyDouble(), any(UnitUtils.Units.class));
    }

    @Test
    public void connectShouldBeOk() throws Exception {
        instance.connect(FIRMWARE, PORT, BAUD_RATE);

        verify(controller).openCommPort(PORT, BAUD_RATE);
        assertEquals(controller, instance.getController());
        assertNull("The controller state is fetched from the controller which in this case is a mock", instance.getControlState());
        assertEquals("No events should have been fired", 0, eventArgumentCaptor.getAllValues().size());
    }

    @Test(expected = Exception.class)
    public void connectWithUnknownFirmwareShouldNotBeOk() throws Exception {
        instance.connect("unknown", PORT, BAUD_RATE);
    }

    @Test(expected = Exception.class)
    public void connectWhenFailingToOpenControllerConnectionShouldNotBeOk() throws Exception {
        when(controller.openCommPort(PORT, BAUD_RATE)).thenThrow(new Exception());
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
    }

    @Test
    public void connectWhenOpenControllerConnectionWasNotPossibleShouldNotBeOk() throws Exception {
        // Given
        when(controller.openCommPort(PORT, BAUD_RATE)).thenReturn(false);

        // When
        instance.connect(FIRMWARE, PORT, BAUD_RATE);

        //Then
        assertFalse(instance.isConnected());
        assertNotNull(instance.getController());
    }

    @Test
    public void isConnectedShouldReturnTrueIfConnected() throws Exception {
        // Given
        when(controller.isCommOpen()).thenReturn(true);
        instance.connect(FIRMWARE, PORT, BAUD_RATE);

        assertTrue(instance.isConnected());
    }

    @Test
    public void isConnectedShouldReturnFalseIfNotConnected() throws Exception {
        // Given
        when(controller.isCommOpen()).thenReturn(false);
        instance.connect(FIRMWARE, PORT, BAUD_RATE);

        assertFalse(instance.isConnected());
    }

    @Test
    public void isConnectedShouldReturnFalseIfNeverConnected() throws Exception {
        assertFalse(instance.isConnected());
    }


    @Test
    public void disconnectShouldCloseTheConnection() throws Exception {
        // Given
        instance.connect(FIRMWARE, PORT, BAUD_RATE);

        // When
        instance.disconnect();

        // Then
        verify(controller).closeCommPort();
        assertNull("The instance should now be null", instance.getController());
        assertEquals(ControlState.COMM_DISCONNECTED, instance.getControlState());
        assertFalse(instance.isConnected());

        assertEquals("Only one event should have been fired", 1, eventArgumentCaptor.getAllValues().size());
        assertTrue(eventArgumentCaptor.getValue().isStateChangeEvent());
        assertEquals(ControlState.COMM_DISCONNECTED, eventArgumentCaptor.getValue().getControlState());
    }

    @Test
    public void sendGcodeCommandWhenConnectedShouldBeOk() throws Exception {
        // Given
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
        when(controller.isCommOpen()).thenReturn(true);

        GcodeCommand gcodeCommand = new GcodeCommand("G00");
        when(controller.createCommand(any())).thenReturn(gcodeCommand);

        // When
        instance.sendGcodeCommand("G00");

        // Then
        verify(controller, times(1)).sendCommandImmediately(any());
        verify(controller, times(1)).sendCommandImmediately(gcodeCommand);
        verify(controller, times(0)).restoreParserModalState();
    }

    @Test(expected = Exception.class)
    public void sendGcodeCommandWhenNotConnectedShouldThrowException() throws Exception {
        // Given
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
        when(controller.isCommOpen()).thenReturn(false);

        GcodeCommand gcodeCommand = new GcodeCommand("G00");
        when(controller.createCommand(any())).thenReturn(gcodeCommand);

        // When
        instance.sendGcodeCommand("G00");
    }

    @Test
    public void getSettingsShouldBeOk() {
        Settings result = instance.getSettings();
        assertEquals(settings, result);
    }

    @Test
    public void getControlStateShouldBeOkWhenConnected() throws Exception {
        instance.connect(FIRMWARE, PORT, BAUD_RATE);

        when(controller.getControlState()).thenReturn(ControlState.COMM_IDLE);
        ControlState result = instance.getControlState();
        assertEquals(ControlState.COMM_IDLE, result);
    }

    @Test
    public void getControlStateShouldReturnStateDisconnectedWhenNotConnected() {
        ControlState result = instance.getControlState();
        assertEquals(ControlState.COMM_DISCONNECTED, result);
    }

    @Test
    public void getControllerShouldBeOkWhenConnected() throws Exception {
        // Given
        instance.connect(FIRMWARE, PORT, BAUD_RATE);

        // When
        IController result = instance.getController();

        // Then
        assertEquals(controller, result);
    }

    @Test
    public void setGcodeFileShouldBeOk() throws Exception {
        // Given
        instance.connect(FIRMWARE, PORT, BAUD_RATE);

        File tempFile = File.createTempFile("ugs-", ".gcode");
        FileUtils.writeStringToFile(tempFile, "G0 X0 Y0\n");

        // When
        instance.setGcodeFile(tempFile);

        // Then
        List<UGSEvent> events = eventArgumentCaptor.getAllValues();
        assertEquals(3, events.size());
        assertEquals(UGSEvent.FileState.FILE_LOADING, events.get(0).getFileState());
        assertEquals(UGSEvent.EventType.SETTING_EVENT, events.get(1).getEventType());
        assertEquals(UGSEvent.FileState.FILE_LOADED, events.get(2).getFileState());

        assertNotNull(instance.getProcessedGcodeFile());
    }

    @Test(expected = IOException.class)
    public void getGcodeFileThatDoesNotExistShouldThrowException() throws Exception {
        // Given
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
        instance.setGcodeFile(new File("does_not_exist.gcode"));
    }

    @Test
    public void getNumRowsShouldReturnNumberFromController() throws Exception {
        // Given
        when(controller.rowsInSend()).thenReturn(42);
        instance.connect(FIRMWARE, PORT, BAUD_RATE);

        // When
        long numRows = instance.getNumRows();

        // Then
        assertEquals(42, numRows);
    }

    @Test
    public void getNumSentRowsShouldReturnNumberFromController() throws Exception {
        // Given
        when(controller.rowsSent()).thenReturn(21);
        instance.connect(FIRMWARE, PORT, BAUD_RATE);

        // When
        long numRows = instance.getNumSentRows();

        // Then
        assertEquals(21, numRows);
    }
}
