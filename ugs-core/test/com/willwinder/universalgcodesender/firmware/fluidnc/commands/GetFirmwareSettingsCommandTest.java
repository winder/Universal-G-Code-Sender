package com.willwinder.universalgcodesender.firmware.fluidnc.commands;

import org.junit.Test;

import static org.junit.Assert.*;

public class GetFirmwareSettingsCommandTest {


    @Test
    public void appendResponseOkShouldMakeTheCommandDone() {
        GetFirmwareSettingsCommand command = new GetFirmwareSettingsCommand();
        command.appendResponse("ok");
        assertEquals("ok", command.getResponse());

        assertTrue(command.isDone());
        assertTrue(command.isOk());
        assertFalse(command.isError());
    }

    @Test
    public void appendResponseWithYamlConfigShould() {
        GetFirmwareSettingsCommand command = new GetFirmwareSettingsCommand();
        command.appendResponse("axis:");
        command.appendResponse("  shared_stepper_disable_pin: gpio.13:low");
        command.appendResponse("  shared_stepper_reset_pin: NO_PIN");
        command.appendResponse("  x:");
        command.appendResponse("    steps_per_mm: 800");
        command.appendResponse("  y:");
        command.appendResponse("    steps_per_mm: 800");
        command.appendResponse("ok");
        command.getResponse();
    }
}
