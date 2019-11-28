package com.willwinder.universalgcodesender.services;

import com.willwinder.universalgcodesender.AbstractController;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.ControllerStatusBuilder;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.GUIBackend;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.utils.Settings;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
        setupStatus();

        // when
        instance.jogTo(new PartialPosition(1.0, 2.0, 3.0, UnitUtils.Units.MM));

        // check
        verify(controller, times(1)).jogMachineTo(new PartialPosition(1.0, 2.0, 3.0, UnitUtils.Units.MM), 200);
    }

    @Test
    public void testJogTo2D() throws Exception {
        // Given
        setupStatus();

        // when
        instance.jogTo(new PartialPosition(1.0, 2.0, UnitUtils.Units.MM));

        // check
        verify(controller, times(1)).jogMachineTo(new PartialPosition(1.0, 2.0, UnitUtils.Units.MM), 200);
    }

    private void setupStatus() {
        ControllerStatus status = new ControllerStatusBuilder().setStateString("idle")
                                                               .setState(ControllerState.IDLE)
                                                               .setMachineCoord(new Position(0, 0, 0, UnitUtils.Units.MM))
                                                               .setWorkCoord(new Position(11, 11, 11, UnitUtils.Units.MM))
                                                               .build();
    }
}