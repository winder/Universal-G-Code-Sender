package com.willwinder.universalgcodesender.firmware.fluidnc;

import com.willwinder.universalgcodesender.Capabilities;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.firmware.fluidnc.commands.GetFirmwareVersionCommand;
import com.willwinder.universalgcodesender.firmware.fluidnc.commands.GetStatusCommand;
import com.willwinder.universalgcodesender.services.MessageService;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.SemanticVersion;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import static com.willwinder.universalgcodesender.CapabilitiesConstants.FILE_SYSTEM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FluidNCUtilsTest {

    @Test
    public void isMessageResponseShouldReturnTrueOnMessages() {
        assertTrue(FluidNCUtils.isMessageResponse("[MSG:INFO Test]"));
        assertFalse(FluidNCUtils.isMessageResponse("[GC: blapp]"));
        assertFalse(FluidNCUtils.isMessageResponse("[MSG:INFO Test"));
    }

    @Test
    public void parseMessageResponseShouldReturnMessage() {
        assertEquals("INFO Test", FluidNCUtils.parseMessageResponse("[MSG:INFO Test]").get());
        assertFalse(FluidNCUtils.parseMessageResponse("[MSG:INFO Test").isPresent());
    }

    @Test
    public void isWelcomeResponseShouldRecognizeWelcomeMessages() {
        assertTrue(FluidNCUtils.isWelcomeResponse("Grbl 3.4 [FluidNC v3.4.2 (wifi) '$' for help]"));
        assertTrue(FluidNCUtils.isWelcomeResponse("GrblHal 3.4 [FluidNC v3.4.2 (wifi) '$' for help]"));
        assertFalse(FluidNCUtils.isWelcomeResponse(""));
    }

    @Test
    public void parseVersionShouldReturnMajorMinorPatchVersions() {
        SemanticVersion semanticVersion = FluidNCUtils.parseSemanticVersion("Grbl 3.4 [FluidNC v3.4.2 (wifi) '$' for help]").get();
        assertEquals(semanticVersion.getMajor(), 3);
        assertEquals(semanticVersion.getMinor(), 4);
        assertEquals(semanticVersion.getPatch(), 2);
    }

    @Test
    public void parseVersionShouldReturnMajorMinorVersions() {
        SemanticVersion semanticVersion = FluidNCUtils.parseSemanticVersion("Grbl 3.4 [FluidNC v3.4 (wifi) '$' for help]").get();
        assertEquals(semanticVersion.getMajor(), 3);
        assertEquals(semanticVersion.getMinor(), 4);
        assertEquals(semanticVersion.getPatch(), 0);
    }

    @Test
    public void addCapabilitiesShouldAddFileSystem() {
        Capabilities capabilities = new Capabilities();
        IFirmwareSettings firmwareSettings = mock(IFirmwareSettings.class);
        FluidNCUtils.addCapabilities(capabilities, new SemanticVersion(3, 5, 1), firmwareSettings);
        assertFalse(capabilities.hasCapability(FILE_SYSTEM));

        FluidNCUtils.addCapabilities(capabilities, new SemanticVersion(3, 5, 2), firmwareSettings);
        assertTrue(capabilities.hasCapability(FILE_SYSTEM));
    }

    @Test
    public void queryFirmwareVersionShouldReturnTheFirmwareVersion() throws Exception {
        IController controller = mock(IController.class);
        MessageService messageService = mock(MessageService.class);

        // Simulate response from controller on any sent gcode commands
        doAnswer(answer -> {
            GcodeCommand command = answer.getArgument(0, GcodeCommand.class);
            command.appendResponse("[VER:3.6 FluidNC v3.6.6:]");
            command.appendResponse("ok");
            return null;
        }).when(controller).sendCommandImmediately(any());

        GetFirmwareVersionCommand firmwareVersionCommand = FluidNCUtils.queryFirmwareVersion(controller, messageService);
        SemanticVersion semanticVersion = firmwareVersionCommand.getVersion();
        assertEquals(semanticVersion.getMajor(), 3);
        assertEquals(semanticVersion.getMinor(), 6);
        assertEquals(semanticVersion.getPatch(), 6);
        assertEquals("FluidNC", firmwareVersionCommand.getFirmware());
    }

    @Test
    public void queryFirmwareVersionShouldThrowErrorIfVersionToLow() throws Exception {
        IController controller = mock(IController.class);
        MessageService messageService = mock(MessageService.class);

        // Simulate response from controller on any sent gcode commands
        doAnswer(answer -> {
            GcodeCommand command = answer.getArgument(0, GcodeCommand.class);
            command.appendResponse("[VER:3.6 FluidNC v3.2.9:]");
            command.appendResponse("ok");
            return null;
        }).when(controller).sendCommandImmediately(any());

        assertThrows(IllegalStateException.class, () -> FluidNCUtils.queryFirmwareVersion(controller, messageService));
    }

    @Test
    public void queryFirmwareVersionShouldThrowErrorIfNotFluidNC() throws Exception {
        IController controller = mock(IController.class);
        MessageService messageService = mock(MessageService.class);

        // Simulate response from controller on any sent gcode commands
        doAnswer(answer -> {
            GcodeCommand command = answer.getArgument(0, GcodeCommand.class);
            command.appendResponse("[VER:3.6 SomethingElse v3.6.6:]");
            command.appendResponse("ok");
            return null;
        }).when(controller).sendCommandImmediately(any());

        assertThrows(IllegalStateException.class, () -> FluidNCUtils.queryFirmwareVersion(controller, messageService));
    }

    @Test
    public void isControllerResponsiveWhenControllerRespondsAsIdle() throws Exception {
        IController controller = mock(IController.class);
        when(controller.isCommOpen()).thenReturn(true);
        MessageService messageService = mock(MessageService.class);

        doAnswer(answer -> {
            GcodeCommand command = answer.getArgument(0, GcodeCommand.class);
            command.appendResponse("<Idle>");
            return null;
        }).when(controller).sendCommandImmediately(any(GetStatusCommand.class));

        assertTrue(FluidNCUtils.isControllerResponsive(controller, messageService));
    }

    @Test
    public void isControllerResponsiveWhenControllerHold() throws Exception {
        IController controller = mock(IController.class);
        when(controller.isCommOpen()).thenReturn(true);
        MessageService messageService = mock(MessageService.class);

        // Respond with status hold
        doAnswer(answer -> {
            GcodeCommand command = answer.getArgument(0, GcodeCommand.class);
            command.appendResponse("<Hold>");
            return null;
        }).when(controller).sendCommandImmediately(any(GetStatusCommand.class));

        // Responds with ok on empty system command
        doAnswer(answer -> {
            GcodeCommand command = answer.getArgument(0, GcodeCommand.class);
            command.appendResponse("ok");
            return null;
        }).when(controller).sendCommandImmediately(ArgumentMatchers.argThat(command -> command.getCommandString().equals("")));

        assertTrue(FluidNCUtils.isControllerResponsive(controller, messageService));
    }

    @Test
    public void isControllerResponsiveWhenControllerInAlarm() throws Exception {
        IController controller = mock(IController.class);
        when(controller.isCommOpen()).thenReturn(true);
        MessageService messageService = mock(MessageService.class);

        // Respond with status hold
        doAnswer(answer -> {
            GcodeCommand command = answer.getArgument(0, GcodeCommand.class);
            command.appendResponse("<Alarm>");
            return null;
        }).when(controller).sendCommandImmediately(any(GetStatusCommand.class));

        // Responds with ok on empty system command
        doAnswer(answer -> {
            GcodeCommand command = answer.getArgument(0, GcodeCommand.class);
            command.appendResponse("ok");
            return null;
        }).when(controller).sendCommandImmediately(ArgumentMatchers.argThat(command -> command.getCommandString().equals("")));

        assertTrue(FluidNCUtils.isControllerResponsive(controller, messageService));
    }

    @Test
    public void isControllerResponsiveWhenControllerInAlarmAndNotResponsive() throws Exception {
        IController controller = mock(IController.class);
        when(controller.isCommOpen()).thenReturn(true);
        MessageService messageService = mock(MessageService.class);

        // Respond with status hold
        doAnswer(answer -> {
            GcodeCommand command = answer.getArgument(0, GcodeCommand.class);
            command.appendResponse("<Alarm>");
            return null;
        }).when(controller).sendCommandImmediately(any(GetStatusCommand.class));

        assertFalse(FluidNCUtils.isControllerResponsive(controller, messageService));
    }
}
