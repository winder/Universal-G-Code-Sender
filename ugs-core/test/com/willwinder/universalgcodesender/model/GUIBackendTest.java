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
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.FileState;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.Settings;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.Assert.*;
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
        IFirmwareSettings firmwareSettings = mock(IFirmwareSettings.class);
        controller = mock(AbstractController.class);
        doReturn(controller).when(instance).fetchControllerFromFirmware(any());
        doReturn(firmwareSettings).when(controller).getFirmwareSettings();

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
        PartialPosition p = new PartialPosition(10., 0., 0., UnitUtils.Units.MM);
        instance.adjustManualLocation(p, 10);
        verify(controller, times(1)).jogMachine(p, 10);
    }

    @Test
    public void adjustManualLocationWithNoDirectionShouldNotMoveTheMachine() throws Exception {
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
        PartialPosition p = new PartialPosition(0., 0., 0., UnitUtils.Units.MM);
        instance.adjustManualLocation(p, 10);
        verify(controller, times(0)).jogMachine(any(), anyDouble());
    }

    @Test
    public void probeShouldBeOk() throws Exception {
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
        instance.probe("Z", 10, 10, UnitUtils.Units.MM);
        verify(controller, times(1)).probe(anyString(), anyDouble(), anyDouble(), any(UnitUtils.Units.class));
    }


    @Test
    public void pauseResumeWhenInIdleShouldThrowException() throws Exception {
        when(controller.getControlState()).thenReturn(CommunicatorState.COMM_IDLE);
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
        when(controller.getControlState()).thenReturn(CommunicatorState.COMM_DISCONNECTED);
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
        when(controller.getControlState()).thenReturn(CommunicatorState.COMM_SENDING);
        instance.connect(FIRMWARE, PORT, BAUD_RATE);

        instance.pauseResume();

        verify(controller, times(1)).pauseStreaming();
        verify(controller, times(0)).resumeStreaming();
    }

    @Test
    public void pauseResumeWhenPausedShouldResume() throws Exception {
        when(controller.getControlState()).thenReturn(CommunicatorState.COMM_SENDING_PAUSED);
        instance.connect(FIRMWARE, PORT, BAUD_RATE);

        instance.pauseResume();

        verify(controller, times(0)).pauseStreaming();
        verify(controller, times(1)).resumeStreaming();
    }

    @Test
    public void getSendRemainingDuration() throws Exception {
        // Given
        when(controller.rowsCompleted()).thenReturn(10);
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

        verify(controller).openCommPort(settings.getConnectionDriver(), PORT, BAUD_RATE);
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
        when(controller.openCommPort(settings.getConnectionDriver(), PORT, BAUD_RATE)).thenThrow(new Exception());
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
    }

    @Test
    public void connectWhenOpenControllerConnectionWasNotPossibleShouldNotBeOk() throws Exception {
        // Given
        when(controller.openCommPort(settings.getConnectionDriver(), PORT, BAUD_RATE)).thenReturn(false);

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
    public void isIdleShouldReturnFalseIfNotConnected() {
        assertFalse(instance.isIdle());
    }

    @Test
    public void isIdleShouldReturnTrueIfControllerIsInStateIdle() throws Exception {
        // Given
        when(controller.isCommOpen()).thenReturn(true);
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
        ControllerStatus controllerStatus = new ControllerStatus(ControllerState.IDLE, new Position(0,0,0), new Position(0,0,0));
        when(controller.getControllerStatus()).thenReturn(controllerStatus);

        assertTrue(instance.isIdle());
    }

    @Test
    public void isIdleShouldReturnTrueIfControllerIsInStateCheck() throws Exception {
        // Given
        when(controller.isCommOpen()).thenReturn(true);
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
        ControllerStatus controllerStatus = new ControllerStatus(ControllerState.CHECK, new Position(0,0,0), new Position(0,0,0));
        when(controller.getControllerStatus()).thenReturn(controllerStatus);

        assertTrue(instance.isIdle());
    }

    @Test
    public void isIdleShouldReturnFalseIfControllerIsInStateRun() throws Exception {
        // Given
        when(controller.isCommOpen()).thenReturn(true);
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
        ControllerStatus controllerStatus = new ControllerStatus(ControllerState.RUN, new Position(0,0,0), new Position(0,0,0));
        when(controller.getControllerStatus()).thenReturn(controllerStatus);

        assertFalse(instance.isIdle());
    }

    @Test
    public void canSendShouldReturnTrueIfIdleAndFileLoaded() throws Exception {
        // Given
        when(controller.isCommOpen()).thenReturn(true);
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
        ControllerStatus controllerStatus = new ControllerStatus(ControllerState.IDLE, new Position(0,0,0), new Position(0,0,0));
        when(controller.getControllerStatus()).thenReturn(controllerStatus);

        File tempFile = File.createTempFile("ugs-", ".gcode");
        FileUtils.writeStringToFile(tempFile, "G0 X0 Y0\n", StandardCharsets.UTF_8);
        instance.setGcodeFile(tempFile);

        assertTrue(instance.canSend());
    }

    @Test
    public void canSendShouldReturnFalseIfIdleAndNoFileLoaded() throws Exception {
        // Given
        when(controller.isCommOpen()).thenReturn(true);
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
        ControllerStatus controllerStatus = new ControllerStatus(ControllerState.IDLE, new Position(0,0,0), new Position(0,0,0));
        when(controller.getControllerStatus()).thenReturn(controllerStatus);

        assertFalse(instance.canSend());
    }

    @Test
    public void canSendShouldReturnFalseIfNotConnectedAndFileLoaded() throws Exception {
        // Given
        File tempFile = File.createTempFile("ugs-", ".gcode");
        FileUtils.writeStringToFile(tempFile, "G0 X0 Y0\n", StandardCharsets.UTF_8);
        instance.setGcodeFile(tempFile);

        assertFalse(instance.canSend());
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
        assertEquals(CommunicatorState.COMM_DISCONNECTED, instance.getControlState());
        assertFalse(instance.isConnected());

        assertEquals("Only one event should have been fired", 1, eventArgumentCaptor.getAllValues().size());
        assertEquals(ControllerStateEvent.class, eventArgumentCaptor.getAllValues().get(0).getClass());
        assertEquals(ControllerState.DISCONNECTED, ((ControllerStateEvent) eventArgumentCaptor.getAllValues().get(0)).getState());
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

        when(controller.getControlState()).thenReturn(CommunicatorState.COMM_IDLE);
        CommunicatorState result = instance.getControlState();
        assertEquals(CommunicatorState.COMM_IDLE, result);
    }

    @Test
    public void getControlStateShouldReturnStateDisconnectedWhenNotConnected() {
        CommunicatorState result = instance.getControlState();
        assertEquals(CommunicatorState.COMM_DISCONNECTED, result);
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
        FileUtils.writeStringToFile(tempFile, "G0 X0 Y0\n", StandardCharsets.UTF_8);

        // When
        instance.setGcodeFile(tempFile);

        // Then
        List<UGSEvent> events = eventArgumentCaptor.getAllValues();
        assertEquals(4, events.size());
        assertEquals(FileState.OPENING_FILE, ((FileStateEvent)events.get(0)).getFileState());
        assertEquals(FileState.FILE_LOADING, ((FileStateEvent)events.get(1)).getFileState());
        assertEquals(SettingChangedEvent.class, events.get(2).getClass());
        assertEquals(FileState.FILE_LOADED, ((FileStateEvent) events.get(3)).getFileState());

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

    @Test
    public void setWorkPositionWithValueExpressionShouldSetPosition() throws Exception {
        // Given
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
        ControllerStatus status = new ControllerStatus(ControllerState.IDLE, new Position(0, 0, 0, UnitUtils.Units.MM), new Position(11, 11,11, UnitUtils.Units.MM));
        instance.statusStringListener(status);

        // When
        instance.setWorkPositionUsingExpression(Axis.X, "10.1");

        // Then
        verify(controller, times(1)).setWorkPosition(PartialPosition.from(Axis.X, 10.1, UnitUtils.Units.MM));
    }

    @Test
    public void setWorkPositionWithExpressionShouldSetPosition() throws Exception {
        // Given
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
        ControllerStatus status = new ControllerStatus(ControllerState.IDLE, new Position(0, 0, 0, UnitUtils.Units.MM), new Position(11, 11,11, UnitUtils.Units.MM));
        instance.statusStringListener(status);

        // When
        instance.setWorkPositionUsingExpression(Axis.Y, "10.1 * 10");

        // Then
        verify(controller, times(1)).setWorkPosition(PartialPosition.from(Axis.Y, 101.0, UnitUtils.Units.MM));
    }

    @Test
    public void setWorkPositionWithExpressionShouldSetNegativePosition() throws Exception {
        // Given
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
        ControllerStatus status = new ControllerStatus(ControllerState.IDLE, new Position(0, 0, 0, UnitUtils.Units.MM), new Position(11, 11,11, UnitUtils.Units.MM));
        instance.statusStringListener(status);

        // When
        instance.setWorkPositionUsingExpression(Axis.Y, "-10.1");

        // Then
        verify(controller, times(1)).setWorkPosition(PartialPosition.from(Axis.Y, -10.1, UnitUtils.Units.MM));
    }

    @Test
    public void setWorkPositionWithAdditionExpression() throws Exception {
        // Given
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
        ControllerStatus status = new ControllerStatus(ControllerState.IDLE, new Position(0, 0, 0, UnitUtils.Units.MM), new Position(11, 11,11, UnitUtils.Units.MM));
        instance.statusStringListener(status);

        // When
        instance.setWorkPositionUsingExpression(Axis.Y, "# + 10");

        // Then
        verify(controller, times(1)).setWorkPosition(PartialPosition.from(Axis.Y, 21.0, UnitUtils.Units.MM));
    }

    @Test
    public void setWorkPositionWithMultiplicationExpression() throws Exception {
        // Given
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
        ControllerStatus status = new ControllerStatus(ControllerState.IDLE, new Position(0, 0, 0, UnitUtils.Units.MM), new Position(11, 11,11, UnitUtils.Units.MM));
        instance.statusStringListener(status);

        // When
        instance.setWorkPositionUsingExpression(Axis.Z, "# * 10");

        // Then
        verify(controller, times(1)).setWorkPosition(PartialPosition.from(Axis.Z, 110.0, UnitUtils.Units.MM));
    }

    @Test
    public void setWorkPositionWithMultiplicationExpressionWithoutValue() throws Exception {
        // Given
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
        ControllerStatus status = new ControllerStatus(ControllerState.IDLE, new Position(0, 0, 0, UnitUtils.Units.MM), new Position(11, 11,11, UnitUtils.Units.MM));
        instance.statusStringListener(status);

        // When
        instance.setWorkPositionUsingExpression(Axis.Z, "* 10");

        // Then
        verify(controller, times(1)).setWorkPosition(PartialPosition.from(Axis.Z, 110.0, UnitUtils.Units.MM));
    }

    @Test
    public void setWorkPositionWithDivisionExpression() throws Exception {
        // Given
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
        ControllerStatus status = new ControllerStatus(ControllerState.IDLE, new Position(0, 0, 0, UnitUtils.Units.MM), new Position(11, 11,11, UnitUtils.Units.MM));
        instance.statusStringListener(status);

        // When
        instance.setWorkPositionUsingExpression(Axis.Z, "# / 10");

        // Then
        verify(controller, times(1)).setWorkPosition(PartialPosition.from(Axis.Z, 1.1, UnitUtils.Units.MM));
    }

    @Test
    public void setWorkPositionWithDivisionExpressionhouldConvertHashToWorkPositionUnits() throws Exception {
        // Given
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
        ControllerStatus status = new ControllerStatus(ControllerState.IDLE, new Position(0, 0, 0, UnitUtils.Units.MM), new Position(10, 10,10, UnitUtils.Units.INCH));
        instance.statusStringListener(status);

        // When
        instance.setWorkPositionUsingExpression(Axis.Z, "# / 10");

        // Then
        verify(controller, times(1)).setWorkPosition(PartialPosition.from(Axis.Z, 25.4, UnitUtils.Units.MM));
    }

    @Test
    public void setWorkPositionWithDivisionExpressionWithoutValue() throws Exception {
        // Given
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
        ControllerStatus status = new ControllerStatus(ControllerState.IDLE, new Position(0, 0, 0, UnitUtils.Units.MM), new Position(11, 11,11, UnitUtils.Units.MM));
        instance.statusStringListener(status);

        // When
        instance.setWorkPositionUsingExpression(Axis.Z, "/ 10");

        // Then
        verify(controller, times(1)).setWorkPosition(PartialPosition.from(Axis.Z, 1.1, UnitUtils.Units.MM));
    }

    @Test
    public void setWorkPositionWithDivisionExpressionShouldConvertToWorkPositionUnits() throws Exception {
        // Given
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
        ControllerStatus status = new ControllerStatus(ControllerState.IDLE, new Position(0, 0, 0, UnitUtils.Units.INCH), new Position(10, 10,10, UnitUtils.Units.INCH));
        instance.statusStringListener(status);

        // When
        instance.setWorkPositionUsingExpression(Axis.Z, "/ 10");

        // Then
        verify(controller, times(1)).setWorkPosition(PartialPosition.from(Axis.Z, 25.4, UnitUtils.Units.MM));
    }


    @Test
    public void setWorkPositionWithSubtractionExpression() throws Exception {
        // Given
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
        ControllerStatus status = new ControllerStatus(ControllerState.IDLE, new Position(0, 0, 0, UnitUtils.Units.MM), new Position(11, 11,11, UnitUtils.Units.MM));
        instance.statusStringListener(status);

        // When
        instance.setWorkPositionUsingExpression(Axis.X, "# - 10");

        // Then
        verify(controller, times(1)).setWorkPosition(PartialPosition.from(Axis.X, 1.0, UnitUtils.Units.MM));
    }

    @Test
    public void setWorkPositionMultipleAxes() throws Exception {
        // Given
        instance.connect(FIRMWARE, PORT, BAUD_RATE);
        ControllerStatus status = new ControllerStatus(ControllerState.IDLE, new Position(0, 0, 0, UnitUtils.Units.MM), new Position(11, 11,11, UnitUtils.Units.MM));
        instance.statusStringListener(status);

        // When
        instance.setWorkPosition(new PartialPosition(25.0,99.0, UnitUtils.Units.MM));

        // Then
        verify(controller, times(1)).setWorkPosition(new PartialPosition(25.0,99.0, UnitUtils.Units.MM));
    }
}
