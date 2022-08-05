package com.willwinder.universalgcodesender.firmware.fluidnc;

import com.willwinder.universalgcodesender.Capabilities;
import com.willwinder.universalgcodesender.utils.SemanticVersion;
import org.junit.Test;

import static com.willwinder.universalgcodesender.CapabilitiesConstants.FILE_SYSTEM;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FluidNCUtilsTest {

    @Test
    public void isMessageResponseShouldReturnTrueOnMessages() {
        assertTrue(FluidNCUtils.isMessageResponse("[MSG:INFO Test]"));
        assertFalse(FluidNCUtils.isMessageResponse("[GC: blapp]"));
        assertFalse(FluidNCUtils.isMessageResponse("[MSG:INFO Test"));
    }

    @Test
    public void parseMessageResponseShouldReturnMessage() {
        assertEquals("INFO Test", FluidNCUtils.parseMessageResponse("[MSG:INFO Test]").get());
        assertFalse(FluidNCUtils.parseMessageResponse("[MSG:INFO Test").isPresent());
    }

    @Test
    public void isWelcomeResponseShouldRecognizeWelcomeMessages() {
        assertTrue(FluidNCUtils.isWelcomeResponse("Grbl 3.4 [FluidNC v3.4.2 (wifi) '$' for help]"));
        assertTrue(FluidNCUtils.isWelcomeResponse("GrblHal 3.4 [FluidNC v3.4.2 (wifi) '$' for help]"));
        assertFalse(FluidNCUtils.isWelcomeResponse(""));
    }

    @Test
    public void parseVersionShouldReturnMajorMinorPatchVersions() {
        SemanticVersion semanticVersion = FluidNCUtils.parseSemanticVersion("Grbl 3.4 [FluidNC v3.4.2 (wifi) '$' for help]").get();
        assertEquals(semanticVersion.getMajor(), 3);
        assertEquals(semanticVersion.getMinor(), 4);
        assertEquals(semanticVersion.getPatch(), 2);
    }

    @Test
    public void parseVersionShouldReturnMajorMinorVersions() {
        SemanticVersion semanticVersion = FluidNCUtils.parseSemanticVersion("Grbl 3.4 [FluidNC v3.4 (wifi) '$' for help]").get();
        assertEquals(semanticVersion.getMajor(), 3);
        assertEquals(semanticVersion.getMinor(), 4);
        assertEquals(semanticVersion.getPatch(), 0);
    }

    @Test
    public void addCapabilitiesShouldAddFileSystem() {
        Capabilities capabilities = new Capabilities();
        FluidNCUtils.addCapabilities(capabilities, new SemanticVersion(3, 5, 1));
        assertFalse(capabilities.hasCapability(FILE_SYSTEM));

        FluidNCUtils.addCapabilities(capabilities, new SemanticVersion(3, 5, 2));
        assertTrue(capabilities.hasCapability(FILE_SYSTEM));
    }
}
