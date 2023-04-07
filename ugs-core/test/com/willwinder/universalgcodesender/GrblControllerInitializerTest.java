package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.firmware.grbl.commands.GetBuildInfoCommand;
import com.willwinder.universalgcodesender.firmware.grbl.commands.GetParserStateCommand;
import com.willwinder.universalgcodesender.firmware.grbl.commands.GetSettingsCommand;
import com.willwinder.universalgcodesender.firmware.grbl.commands.GetStatusCommand;
import com.willwinder.universalgcodesender.firmware.grbl.commands.GrblSystemCommand;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.services.MessageService;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GrblControllerInitializerTest {

    @Mock
    private GrblController controller;

    @Mock
    private MessageService messageService;

    private static void mockParserStateCommand(GrblController controller) throws Exception {
        doAnswer((arguments) -> {
            GcodeCommand command = arguments.getArgument(0);
            command.setDone(true);
            return null;
        }).when(controller).sendCommandImmediately(any(GetParserStateCommand.class));
    }

    private static void mockSettingsCommand(GrblController controller) throws Exception {
        doAnswer((arguments) -> {
            GcodeCommand command = arguments.getArgument(0);
            command.setDone(true);
            return null;
        }).when(controller).sendCommandImmediately(any(GetSettingsCommand.class));
    }

    private static void mockBuildInfoCommand(GrblController controller) throws Exception {
        doAnswer((arguments) -> {
            GcodeCommand command = arguments.getArgument(0);
            command.setResponse("[VER:1.1f]\nok\n");
            command.setDone(true);
            return null;
        }).when(controller).sendCommandImmediately(any(GetBuildInfoCommand.class));
    }

    private static void mockStatusCommand(GrblController controller, ControllerState state) throws Exception {
        doAnswer((arguments) -> {
            GcodeCommand command = arguments.getArgument(0);
            command.setResponse("<" + state.name() + ">");
            command.setDone(true);
            return null;
        }).when(controller).sendCommandImmediately(any(GetStatusCommand.class));
    }

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        when(controller.isCommOpen()).thenReturn(true);
    }

    @Test
    public void initializeShouldReturnTrueWhenSuccessFullyInitializedController() throws Exception {
        mockStatusCommand(controller, ControllerState.IDLE);
        mockBuildInfoCommand(controller);
        mockSettingsCommand(controller);
        mockParserStateCommand(controller);

        GrblControllerInitializer instance = new GrblControllerInitializer(controller, messageService);
        assertTrue(instance.initialize());
        assertEquals(1.1d, instance.getVersion().getVersionNumber(), 0.01);
        assertEquals(Character.valueOf('f'), instance.getVersion().getVersionLetter());
        verify(controller, times(0)).issueSoftReset();
    }

    @Test
    public void initializeShouldOnlyInitializeOnce() throws Exception {
        mockStatusCommand(controller, ControllerState.IDLE);
        mockBuildInfoCommand(controller);
        mockSettingsCommand(controller);
        mockParserStateCommand(controller);

        GrblControllerInitializer instance = new GrblControllerInitializer(controller, messageService);
        assertTrue(instance.initialize());
        assertFalse(instance.initialize());
    }

    @Test
    public void initializeShouldThrowErrorWhenNoStatusResponseFromController() throws Exception {
        GrblControllerInitializer instance = new GrblControllerInitializer(controller, messageService);
        RuntimeException exception = assertThrows(RuntimeException.class, instance::initialize);
        assertEquals("Could not query the device status", exception.getMessage());

        verify(controller, times(3)).sendCommandImmediately(any(GetStatusCommand.class));
        verify(controller, times(0)).issueSoftReset();
    }

    @Test
    public void initializeShouldResetControllerIfTheControllerIsNotResponsive() throws Exception {
        GrblControllerInitializer instance = new GrblControllerInitializer(controller, messageService);

        // Mock status as HOLD which will force it to send command with empty line break to see if it is still responsive
        mockStatusCommand(controller, ControllerState.HOLD);

        assertFalse(instance.initialize());
        verify(controller, times(1)).sendCommandImmediately(any(GetStatusCommand.class));
        verify(controller, times(1)).sendCommandImmediately(any(GrblSystemCommand.class));
        verify(controller, times(1)).issueSoftReset();
    }
}
