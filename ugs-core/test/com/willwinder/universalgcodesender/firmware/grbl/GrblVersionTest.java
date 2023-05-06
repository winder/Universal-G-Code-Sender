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
    public void parseMillRightVersionString() {
        GrblVersion version = new GrblVersion("[VER:1.1i MegaV 4 Axis Router.20190120:]");
        assertEquals(1.1d, version.getVersionNumber(), 0.001);
        assertEquals('i', version.getVersionLetter().charValue());
        assertEquals("20190120", version.getBuildDate());
        assertEquals("", version.getMachineName());
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
    public void parseVersionStringWithSnapshotBuildDateAndMachine() {
        GrblVersion version = new GrblVersion("[VER:1.1h-XCP.20220314a:abc]");
        assertEquals(1.1d, version.getVersionNumber(), 0.001);
        assertEquals('h', version.getVersionLetter().charValue());
        assertEquals("20220314a", version.getBuildDate());
        assertEquals("abc", version.getMachineName());
    }

    @Test
    public void parseVersionStringWithSnapshot() {
        GrblVersion version = new GrblVersion("[VER:1.1h-XCP]");
        assertEquals(1.1d, version.getVersionNumber(), 0.001);
        assertEquals('h', version.getVersionLetter().charValue());
        assertEquals("", version.getBuildDate());
        assertEquals("", version.getMachineName());
    }

    @Test
    public void parseOldVersion() {
        GrblVersion version = new GrblVersion("[VER:0.7]");
        assertEquals(0.7d, version.getVersionNumber(), 0.001);
        assertEquals('-', version.getVersionLetter().charValue());
    }
}
