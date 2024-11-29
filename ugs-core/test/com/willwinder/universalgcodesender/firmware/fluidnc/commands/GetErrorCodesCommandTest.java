package com.willwinder.universalgcodesender.firmware.fluidnc.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

public class GetErrorCodesCommandTest {
    @Test
    public void appendResponseWithErrorCodes() throws InterruptedException {
        AtomicInteger eventsCounter = new AtomicInteger(0);

        GetErrorCodesCommand command = new GetErrorCodesCommand();
        command.addListener(e -> eventsCounter.incrementAndGet());
        command.appendResponse("0: No error. Streaming has been paused.");
        command.appendResponse("1: Expected GCodecommand letter");
        command.appendResponse("2: Bad GCode number format");
        command.appendResponse("3: Invalid $ statement");
        command.appendResponse("4: Negative value");
        command.appendResponse("5: Setting disabled");
        command.appendResponse("6: Step pulse too short");
        command.appendResponse("7: Failed to read settings");
        command.appendResponse("8: Command requires idle state");
        command.appendResponse("9: GCode cannot be executed in lock or alarm state");
        command.appendResponse("11: Line too long");
        command.appendResponse("12: Max step rate exceeded");
        command.appendResponse("13: Check door");
        command.appendResponse("14: Startup line too long");
        command.appendResponse("15: Max travel exceeded during jog");
        command.appendResponse("16: Invalid jog command");
        command.appendResponse("17: Laser mode requires PWM output");
        command.appendResponse("18: No Homing/Cycle defined in settings");
        command.appendResponse("19: Single axis homing not allowed");
        command.appendResponse("20: Unsupported GCode command");
        command.appendResponse("21: Gcode modal group violation");
        command.appendResponse("22: Gcode undefined feed rate");
        command.appendResponse("23: Gcode command value not integer");
        command.appendResponse("24: Gcode axis command conflict");
        command.appendResponse("25: Gcode word repeated");
        command.appendResponse("26: Gcode no axis words");
        command.appendResponse("27: Gcode invalid line number");
        command.appendResponse("28: Gcode value word missing");
        command.appendResponse("29: Gcode unsupported coordinate system");
        command.appendResponse("30: Gcode G53 invalid motion mode");
        command.appendResponse("31: Gcode extra axis words");
        command.appendResponse("32: Gcode no axis words in plane");
        command.appendResponse("33: Gcode invalid target");
        command.appendResponse("34: Gcode arc radius error");
        command.appendResponse("35: Gcode no offsets in plane");
        command.appendResponse("36: Gcode unused words");
        command.appendResponse("37: Gcode G43 dynamic axis error");
        command.appendResponse("38: Gcode max value exceeded");
        command.appendResponse("39: P param max exceeded");
        command.appendResponse("40: Check control pins");
        command.appendResponse("60: Failed to mount device");
        command.appendResponse("61: Read failed");
        command.appendResponse("62: Failed to open directory");
        command.appendResponse("63: Directory not found");
        command.appendResponse("64: File empty");
        command.appendResponse("65: File not found");
        command.appendResponse("66: Failed to open file");
        command.appendResponse("67: Device is busy");
        command.appendResponse("68: Failed to delete directory");
        command.appendResponse("69: Failed to delete file");
        command.appendResponse("70: Failed to rename file");
        command.appendResponse("80: Number out of range for setting");
        command.appendResponse("81: Invalid value for setting");
        command.appendResponse("82: Failed to create file");
        command.appendResponse("83: Failed to format filesystem");
        command.appendResponse("90: Failed to send message");
        command.appendResponse("100: Failed to store setting");
        command.appendResponse("101: Failed to get setting status");
        command.appendResponse("110: Authentication failed!");
        command.appendResponse("111: End of line");
        command.appendResponse("112: End of file");
        command.appendResponse("[MSG:INFO: Huanyang PD0011, PD005 Freq range (133,400) Hz (7980,24000) RPM]");
        command.appendResponse("113: System Reset");
        command.appendResponse("120: Another interface is busy");
        command.appendResponse("130: Jog Cancelled");
        command.appendResponse("150: Bad Pin Specification");
        command.appendResponse("152: Configuration is invalid. Check boot messages for ERR's.");
        command.appendResponse("160: File Upload Failed");
        command.appendResponse("161: File Download Failed");
        command.appendResponse("ok");

        // Wait for the listener to complete
        long startTime = System.currentTimeMillis();
        while(eventsCounter.get() == 0 && startTime + 1000 > System.currentTimeMillis()) {
            // Wait for command to complete
            Thread.sleep(10);
        }

        assertTrue(command.isOk());
        assertEquals(1, eventsCounter.get());
        assertEquals(68, command.getErrorCodes().size());
        assertEquals("Gcode G43 dynamic axis error", command.getErrorCodes().get(37));
    }
}