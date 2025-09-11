package com.willwinder.universalgcodesender.firmware.grbl.commands;


import com.willwinder.universalgcodesender.firmware.grbl.GrblBuildOption;
import com.willwinder.universalgcodesender.firmware.grbl.GrblBuildOptions;
import com.willwinder.universalgcodesender.firmware.grbl.GrblVersion;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
    public void getVersionWithoutVersionStringShouldAssumeGrbl1_1() {
        GetBuildInfoCommand command = new GetBuildInfoCommand();
        command.appendResponse("[OPT: ABC]");
        command.appendResponse("[VER:]");
        command.appendResponse("ok");
        GrblVersion version = command.getVersion().orElseThrow(RuntimeException::new);
        assertEquals(1.1, version.getVersionNumber(), 0.01);
    }

    @Test
    public void getVersionAndOptionsWithLegacySingleLineVersion() {
        GetBuildInfoCommand command = new GetBuildInfoCommand();
        command.appendResponse("[0.9j.20160303:]");
        command.appendResponse("ok");

        GrblVersion version = command.getVersion().orElseThrow(RuntimeException::new);
        assertEquals(0.9, version.getVersionNumber(), 0.01);
        assertEquals(Character.valueOf('j'), version.getVersionLetter());

        GrblBuildOptions options = command.getBuildOptions();
        for (GrblBuildOption option : GrblBuildOption.values()) {
            assertFalse(options.isEnabled(option));
        }
    }

    @Test
    public void getBuildOptionsShouldReturnDefinedOptions() {
        GetBuildInfoCommand command = new GetBuildInfoCommand();
        command.appendResponse("[OPT: V]");
        command.appendResponse("[VER:]");
        command.appendResponse("ok");
        GrblBuildOptions options = command.getBuildOptions();
        assertTrue(options.isEnabled(GrblBuildOption.VARIABLE_SPINDLE_ENABLED));
        assertFalse(options.isEnabled(GrblBuildOption.CORE_XY_ENABLED));
    }

    @Test
    public void getBuildOptionsWhenNoOptionsReturnedShouldReturnDefaultOptions() {
        GetBuildInfoCommand command = new GetBuildInfoCommand();
        command.appendResponse("[VER:]");
        command.appendResponse("ok");
        GrblBuildOptions options = command.getBuildOptions();
        for (GrblBuildOption option : GrblBuildOption.values()) {
            assertFalse(options.isEnabled(option));
        }
    }

    @Test
    public void getBuildOptionsWithoutAnyResponseFromController() {
        GetBuildInfoCommand command = new GetBuildInfoCommand();
        command.appendResponse("ok");
        GrblBuildOptions options = command.getBuildOptions();
        for (GrblBuildOption option : GrblBuildOption.values()) {
            assertFalse(options.isEnabled(option));
        }

        GrblVersion version = command.getVersion().orElseThrow(RuntimeException::new);
        assertEquals(1.1, version.getVersionNumber(), 0.01);
    }
}
