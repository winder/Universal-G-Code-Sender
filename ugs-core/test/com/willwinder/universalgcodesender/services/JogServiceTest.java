package com.willwinder.universalgcodesender.services;

import com.willwinder.universalgcodesender.AbstractController;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.*;
import com.willwinder.universalgcodesender.utils.Settings;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.*;

public class JogServiceTest {

    private ArgumentCaptor<UGSEvent> eventArgumentCaptor;
    private Settings settings;


    private JogService instance;
    private BackendAPI backendAPI;
    private AbstractController controller;

    @Before
    public void setUp() {

        // We need to mock the method that loads the controller
        backendAPI = spy(new GUIBackend());
        instance = spy(new JogService(backendAPI));

        IFirmwareSettings firmwareSettings = mock(IFirmwareSettings.class);
        controller = mock(AbstractController.class);
        doReturn(controller).when(backendAPI).getController();
        doReturn(firmwareSettings).when(controller).getFirmwareSettings();

        // Add a event listener that stores events in the argument captor
        UGSEventListener ugsEventListener = mock(UGSEventListener.class);
        eventArgumentCaptor = ArgumentCaptor.forClass(UGSEvent.class);
        doNothing().when(ugsEventListener).UGSEvent(eventArgumentCaptor.capture());
        backendAPI.addUGSEventListener(ugsEventListener);

        // Add settings
        settings = new Settings();
        settings.setJogFeedRate(200);
        doReturn(settings).when(backendAPI).getSettings();
    }

    @Test
    public void testJogTo3D() throws Exception {
        // Given
        ControllerStatus status = new ControllerStatus(ControllerState.IDLE, new Position(0, 0, 0, UnitUtils.Units.MM), new Position(11, 11,11, UnitUtils.Units.MM));

        // when
        instance.jogTo(new PartialPosition(1.0, 2.0, 3.0, UnitUtils.Units.MM));

        // check
        verify(controller, times(1)).jogMachineTo(new PartialPosition(1.0, 2.0, 3.0, UnitUtils.Units.MM), 200);
    }

    @Test
    public void testJogTo2D() throws Exception {
        // Given
        ControllerStatus status = new ControllerStatus(ControllerState.IDLE, new Position(0, 0, 0, UnitUtils.Units.MM), new Position(11, 11,11, UnitUtils.Units.MM));

        // when
        instance.jogTo(new PartialPosition(1.0, 2.0, UnitUtils.Units.MM));

        // check
        verify(controller, times(1)).jogMachineTo(new PartialPosition(1.0, 2.0, UnitUtils.Units.MM), 200);
    }
}