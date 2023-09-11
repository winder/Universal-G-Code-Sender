package com.willwinder.universalgcodesender.firmware.grbl;

import org.junit.Test;

import static org.junit.Assert.*;

public class GrblVersionTest {

    @Test
    public void parseCompleteVersionString() {
        GrblVersion version = new GrblVersion("[VER:v1.1f.20170131:Some string]");
        assertEquals(1.1d, version.getVersionNumber(), 0.001);
        assertEquals('f', version.getVersionLetter().charValue());
    }

    @Test
    public void parseMillRightVersionString() {
        GrblVersion version = new GrblVersion("[VER:1.1i MegaV 4 Axis Router.20190120:]");
        assertEquals(1.1d, version.getVersionNumber(), 0.001);
        assertEquals('i', version.getVersionLetter().charValue());
    }

    @Test
    public void parseMillRightVersionString1() {
        GrblVersion version = new GrblVersion("[VER:1.1i MR MegaV Tri-CAM Open Frame Bed.V4-20230217:]");
        assertEquals(1.1d, version.getVersionNumber(), 0.001);
        assertEquals('i', version.getVersionLetter().charValue());
    }

    @Test
    public void parseUnknownMegaVersionString() {
        GrblVersion version = new GrblVersion("[VER:1.1g3.20211002.Mega:]");
        assertEquals(1.1d, version.getVersionNumber(), 0.001);
        assertEquals('g', version.getVersionLetter().charValue());
    }

    @Test
    public void parseCompleteVersionString2() {
        GrblVersion version = new GrblVersion("[VER:1.1f.20170801:MINIMILL]");
        assertEquals(1.1d, version.getVersionNumber(), 0.001);
        assertEquals('f', version.getVersionLetter().charValue());
    }

    @Test
    public void parseVersionStringWithoutName() {
        GrblVersion version = new GrblVersion("[VER:v1.1f.20170131]");
        assertEquals(1.1d, version.getVersionNumber(), 0.001);
        assertEquals('f', version.getVersionLetter().charValue());
    }

    @Test
    public void parseVersionStringWithoutBuildNumber() {
        GrblVersion version = new GrblVersion("[VER:v1.1f:Some string]");
        assertEquals(1.1d, version.getVersionNumber(), 0.001);
        assertEquals('f', version.getVersionLetter().charValue());
    }

    @Test
    public void parseVersionStringWithoutChar() {
        GrblVersion version = new GrblVersion("[VER:v1.1.20170131:Some string]");
        assertEquals(1.1d, version.getVersionNumber(), 0.001);
        assertEquals('-', version.getVersionLetter().charValue());
    }

    @Test
    public void parseVersionStringWithSnapshotBuildDateAndMachine() {
        GrblVersion version = new GrblVersion("[VER:1.1h-XCP.20220314a:abc]");
        assertEquals(1.1d, version.getVersionNumber(), 0.001);
        assertEquals('h', version.getVersionLetter().charValue());
    }

    @Test
    public void parseVersionStringWithSnapshot() {
        GrblVersion version = new GrblVersion("[VER:1.1h-XCP]");
        assertEquals(1.1d, version.getVersionNumber(), 0.001);
        assertEquals('h', version.getVersionLetter().charValue());
    }

    @Test
    public void parseOldVersion() {
        GrblVersion version = new GrblVersion("[VER:0.7]");
        assertEquals(0.7d, version.getVersionNumber(), 0.001);
        assertEquals('-', version.getVersionLetter().charValue());
    }

    @Test
    public void parseGenmitsu32VersionString() {
        GrblVersion version = new GrblVersion("[VER:GD32 V2.1.20220827:]");
        assertEquals(2.1d, version.getVersionNumber(), 0.001);
    }

    @Test
    public void parseGenmitsuArm32VersionString() {
        GrblVersion version = new GrblVersion("[VER:ARM32 V2.1.20220827:]");
        assertEquals(2.1d, version.getVersionNumber(), 0.001);
    }

    @Test
    public void parseLegacyGrblVersionString() {
        GrblVersion version = new GrblVersion("[0.9j.2016076:]");
        assertEquals(0.9d, version.getVersionNumber(), 0.001);
        assertEquals('j', version.getVersionLetter().charValue());
    }
}
