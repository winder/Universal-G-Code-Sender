package com.willwinder.universalgcodesender.firmware.grbl;

import org.junit.Test;

import static org.junit.Assert.*;

public class GrblVersionTest {

    @Test
    public void parseCompleteVersionString() {
        GrblVersion version = new GrblVersion("[VER:v1.1f.20170131:Some string]");
        assertEquals(1.1d, version.getVersionNumber(), 0.001);
        assertEquals('f', version.getVersionLetter().charValue());
        assertEquals("20170131", version.getBuildDate());
        assertEquals("Some string", version.getMachineName());
    }

    @Test
    public void parseCompleteVersionString2() {
        GrblVersion version = new GrblVersion("[VER:1.1f.20170801:MINIMILL]");
        assertEquals(1.1d, version.getVersionNumber(), 0.001);
        assertEquals('f', version.getVersionLetter().charValue());
        assertEquals("20170801", version.getBuildDate());
        assertEquals("MINIMILL", version.getMachineName());
    }

    @Test
    public void parseVersionStringWithoutName() {
        GrblVersion version = new GrblVersion("[VER:v1.1f.20170131]");
        assertEquals(1.1d, version.getVersionNumber(), 0.001);
        assertEquals('f', version.getVersionLetter().charValue());
        assertEquals("20170131", version.getBuildDate());
        assertEquals("", version.getMachineName());
    }

    @Test
    public void parseVersionStringWithoutBuildNumber() {
        GrblVersion version = new GrblVersion("[VER:v1.1f:Some string]");
        assertEquals(1.1d, version.getVersionNumber(), 0.001);
        assertEquals('f', version.getVersionLetter().charValue());
        assertEquals("", version.getBuildDate());
        assertEquals("Some string", version.getMachineName());
    }

    @Test
    public void parseVersionStringWithoutChar() {
        GrblVersion version = new GrblVersion("[VER:v1.1.20170131:Some string]");
        assertEquals(1.1d, version.getVersionNumber(), 0.001);
        assertEquals('-', version.getVersionLetter().charValue());
        assertEquals("20170131", version.getBuildDate());
        assertEquals("Some string", version.getMachineName());
    }

    @Test
    public void parseOldVersion() {
        GrblVersion version = new GrblVersion("[VER:0.7]");
        assertEquals(0.7d, version.getVersionNumber(), 0.001);
        assertEquals('-', version.getVersionLetter().charValue());
    }
}
