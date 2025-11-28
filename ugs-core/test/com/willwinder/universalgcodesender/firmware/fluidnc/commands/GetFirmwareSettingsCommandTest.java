package com.willwinder.universalgcodesender.firmware.fluidnc.commands;

import com.willwinder.universalgcodesender.types.CommandException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.Map;

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

    @Test
    public void appendResponseShouldIgnoreMessages() {
        GetFirmwareSettingsCommand command = new GetFirmwareSettingsCommand();
        command.appendResponse("axis:");
        command.appendResponse("  shared_stepper_disable_pin: gpio.13:low");
        command.appendResponse("  shared_stepper_reset_pin: NO_PIN");
        command.appendResponse("  x:");
        command.appendResponse("[MSG:INFO: Huanyang PD014 Accel:6.000]");
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

    @Test
    public void getSettings_shouldNotResolveFloatValuesFromMathematicalNotations() {
        GetFirmwareSettingsCommand command = new GetFirmwareSettingsCommand();
        command.appendResponse("uart1:");
        command.appendResponse("  passthrough_mode: 8E1");
        command.appendResponse("ok");

        Map<String, String> settings = command.getSettings();
        assertEquals(1, settings.keySet().size());
        assertEquals("8E1", settings.get("uart1/passthrough_mode"));
    }

    @Test
    public void getSettingsShouldThrowExceptionOnFaultyYaml() {
        GetFirmwareSettingsCommand command = new GetFirmwareSettingsCommand();
        command.appendResponse("meta: a command with nested colons : should not be allowed");
        command.appendResponse("ok");

        CommandException exception = assertThrows(CommandException.class, command::getSettings);
        assertTrue(exception.getMessage().startsWith("mapping values are not allowed here"));
    }
}
