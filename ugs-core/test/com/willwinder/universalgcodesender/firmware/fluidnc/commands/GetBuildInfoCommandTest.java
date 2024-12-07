package com.willwinder.universalgcodesender.firmware.fluidnc.commands;

import org.junit.Test;

import static org.junit.Assert.*;

public class GetBuildInfoCommandTest {

    @Test
    public void getFirmwareVersionWithNoResponseShouldNotCrash() {
        GetBuildInfoCommand getBuildInfoCommand = new GetBuildInfoCommand();
        assertNotNull(getBuildInfoCommand.getVersion());
        assertEquals(0, getBuildInfoCommand.getVersion().getMajor());
        assertEquals(0, getBuildInfoCommand.getVersion().getMinor());
        assertEquals(0, getBuildInfoCommand.getVersion().getPatch());
        assertEquals("Unknown", getBuildInfoCommand.getFirmware());
    }


    @Test
    public void getFirmwareVersionWithVersion() {
        GetBuildInfoCommand getBuildInfoCommand = new GetBuildInfoCommand();
        getBuildInfoCommand.appendResponse("[VER:3.5 FluidNC v3.5.0 (main-5db1039a):]");

        assertNotNull(getBuildInfoCommand.getVersion());
        assertEquals(3, getBuildInfoCommand.getVersion().getMajor());
        assertEquals(5, getBuildInfoCommand.getVersion().getMinor());
        assertEquals(0, getBuildInfoCommand.getVersion().getPatch());
        assertEquals("FluidNC", getBuildInfoCommand.getFirmware());
    }

    @Test
    public void getFirmwareVersionWithoutLeadingV() {
        GetBuildInfoCommand getBuildInfoCommand = new GetBuildInfoCommand();
        getBuildInfoCommand.appendResponse("[VER:3.7 FluidNC 3.7.10:]");

        assertNotNull(getBuildInfoCommand.getVersion());
        assertEquals(3, getBuildInfoCommand.getVersion().getMajor());
        assertEquals(7, getBuildInfoCommand.getVersion().getMinor());
        assertEquals(10, getBuildInfoCommand.getVersion().getPatch());
        assertEquals("FluidNC", getBuildInfoCommand.getFirmware());
    }

    @Test
    public void getFirmwareVersionWithOldGRBLVersion() {
        GetBuildInfoCommand getBuildInfoCommand = new GetBuildInfoCommand();
        getBuildInfoCommand.appendResponse("[VER:1.1f.20170801:]");

        assertNotNull(getBuildInfoCommand.getVersion());
        assertEquals(1, getBuildInfoCommand.getVersion().getMajor());
        assertEquals(1, getBuildInfoCommand.getVersion().getMinor());
        assertEquals(0, getBuildInfoCommand.getVersion().getPatch());
        assertEquals("GRBL", getBuildInfoCommand.getFirmware());
    }
}
