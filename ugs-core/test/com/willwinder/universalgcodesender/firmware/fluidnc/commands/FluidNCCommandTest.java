package com.willwinder.universalgcodesender.firmware.fluidnc.commands;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class FluidNCCommandTest {

    @Test
    public void appendResponseShouldNotFilterMessages() {
        FluidNCCommand command = new FluidNCCommand("$$");
        command.appendResponse("[MSG: a message]");
        command.appendResponse("ok");
        assertEquals("[MSG: a message]\nok", command.getResponse());
    }
}