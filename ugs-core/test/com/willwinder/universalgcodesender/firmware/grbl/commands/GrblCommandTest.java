package com.willwinder.universalgcodesender.firmware.grbl.commands;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class GrblCommandTest {

    @Test
    public void appendResponseShouldNotAppendStatusString() {
        GrblCommand command = new GrblCommand("G01");
        command.appendResponse("<Idle>");
        assertNull(command.getResponse());
    }

    @Test
    public void appendResponseShouldAppendStatusStringOnStatusCommand() {
        GrblCommand command = new GrblCommand("?");
        command.appendResponse("<Idle>");
        assertEquals("<Idle>", command.getResponse());
    }
}
