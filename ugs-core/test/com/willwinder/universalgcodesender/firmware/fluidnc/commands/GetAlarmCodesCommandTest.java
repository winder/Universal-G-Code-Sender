package com.willwinder.universalgcodesender.firmware.fluidnc.commands;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GetAlarmCodesCommandTest {
    @Test
    public void appendResponseWithAlarmCodes() throws InterruptedException {
        AtomicInteger eventsCounter = new AtomicInteger(0);
        GetErrorCodesCommand command = new GetErrorCodesCommand();
        command.addListener(e -> eventsCounter.incrementAndGet());
        command.appendResponse("0: None");
        command.appendResponse("1: Hard Limit");
        command.appendResponse("2: Soft Limit");
        command.appendResponse("3: Abort Cycle");
        command.appendResponse("4: Probe Fail Initial");
        command.appendResponse("5: Probe Fail Contact");
        command.appendResponse("6: Homing Fail Reset");
        command.appendResponse("7: Homing Fail Door");
        command.appendResponse("8: Homing Fail Pulloff");
        command.appendResponse("9: Homing Fail Approach");
        command.appendResponse("10: Spindle Control");
        command.appendResponse("11: Control Pin Initially On");
        command.appendResponse("12: Ambiguous Switch");
        command.appendResponse("[MSG:INFO: Huanyang PD0011, PD005 Freq range (133,400) Hz (7980,24000) RPM]");
        command.appendResponse("13: Hard Stop");
        command.appendResponse("14: Unhomed");
        command.appendResponse("15: Init");
        command.appendResponse("ok");

        // Wait for the listener to complete
        Thread.sleep(50);

        assertTrue(command.isOk());
        assertEquals(1, eventsCounter.get());
        assertEquals(16, command.getErrorCodes().size());
        assertEquals("Probe Fail Contact", command.getErrorCodes().get(5));
    }
}