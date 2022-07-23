package com.willwinder.universalgcodesender.firmware.fluidnc.commands;

import org.junit.Test;

import static org.junit.Assert.*;

public class GetFirmwareVersionCommandTest {

    @Test
    public void getFirmwareVersionWithNoResponseShouldNotCrash() {
        GetFirmwareVersionCommand getFirmwareVersionCommand = new GetFirmwareVersionCommand();
        assertNotNull(getFirmwareVersionCommand.getVersion());
        assertEquals(0, getFirmwareVersionCommand.getVersion().getMajor());
        assertEquals(0, getFirmwareVersionCommand.getVersion().getMinor());
        assertEquals(0, getFirmwareVersionCommand.getVersion().getPatch());
        assertEquals("Unknown", getFirmwareVersionCommand.getFirmware());
    }


    @Test
    public void getFirmwareVersionWithVersion() {
        GetFirmwareVersionCommand getFirmwareVersionCommand = new GetFirmwareVersionCommand();
        getFirmwareVersionCommand.appendResponse("[VER:3.5 FluidNC v3.5.0 (main-5db1039a):]");

        assertNotNull(getFirmwareVersionCommand.getVersion());
        assertEquals(3, getFirmwareVersionCommand.getVersion().getMajor());
        assertEquals(5, getFirmwareVersionCommand.getVersion().getMinor());
        assertEquals(0, getFirmwareVersionCommand.getVersion().getPatch());
        assertEquals("FluidNC", getFirmwareVersionCommand.getFirmware());
    }

    @Test
    public void getFirmwareVersionWithOldGRBLVersion() {
        GetFirmwareVersionCommand getFirmwareVersionCommand = new GetFirmwareVersionCommand();
        getFirmwareVersionCommand.appendResponse("[VER:1.1f.20170801:]");

        assertNotNull(getFirmwareVersionCommand.getVersion());
        assertEquals(1, getFirmwareVersionCommand.getVersion().getMajor());
        assertEquals(1, getFirmwareVersionCommand.getVersion().getMinor());
        assertEquals(0, getFirmwareVersionCommand.getVersion().getPatch());
        assertEquals("GRBL", getFirmwareVersionCommand.getFirmware());
    }
}
