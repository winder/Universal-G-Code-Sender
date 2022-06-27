package com.willwinder.universalgcodesender.firmware.fluidnc.commands;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GetFirmwareSettingsCommandTest {
    @Test
    public void appendResponseWithYamlConfigShouldParseSettingsAsYaml() {
        GetFirmwareSettingsCommand command = new GetFirmwareSettingsCommand();
        command.appendResponse("axis:");
        command.appendResponse("  shared_stepper_disable_pin: gpio.13:low");
        command.appendResponse("  shared_stepper_reset_pin: NO_PIN");
        command.appendResponse("  x:");
        command.appendResponse("    steps_per_mm: 800");
        command.appendResponse("  y:");
        command.appendResponse("    steps_per_mm: 800");
        command.appendResponse("ok");

        Map<String, String> settings = command.getSettings();
        assertEquals(4, settings.keySet().size());
        assertEquals("gpio.13:low", settings.get("axis/shared_stepper_disable_pin"));
        assertEquals("NO_PIN", settings.get("axis/shared_stepper_reset_pin"));
        assertEquals("800", settings.get("axis/x/steps_per_mm"));
        assertEquals("800", settings.get("axis/y/steps_per_mm"));
    }
}
