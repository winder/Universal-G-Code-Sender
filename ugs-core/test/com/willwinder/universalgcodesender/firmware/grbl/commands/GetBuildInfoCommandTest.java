package com.willwinder.universalgcodesender.firmware.grbl.commands;


import com.willwinder.universalgcodesender.firmware.grbl.GrblVersion;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GetBuildInfoCommandTest {

    @Test
    public void getVersionWithMultilineShouldReturnTheVersionNumber() {
        GetBuildInfoCommand command = new GetBuildInfoCommand();
        command.appendResponse("[OPT: ABC]");
        command.appendResponse("[VER: 1.1f]");
        command.appendResponse("ok");
        GrblVersion version = command.getVersion().orElseThrow(RuntimeException::new);
        assertEquals(1.1, version.getVersionNumber(), 0.01);
    }

    @Test
    public void getVersionWithLegacySingleLineVersion() {
        GetBuildInfoCommand command = new GetBuildInfoCommand();
        command.appendResponse("[0.9j.20160303:]");
        command.appendResponse("ok");
        GrblVersion version = command.getVersion().orElseThrow(RuntimeException::new);
        assertEquals(0.9, version.getVersionNumber(), 0.01);
        assertEquals(Character.valueOf('j'), version.getVersionLetter());
    }
}
