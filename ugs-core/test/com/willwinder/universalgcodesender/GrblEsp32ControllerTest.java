package com.willwinder.universalgcodesender;

import org.junit.Test;

import static com.willwinder.universalgcodesender.CapabilitiesConstants.*;
import static org.junit.Assert.*;

public class GrblEsp32ControllerTest {
    @Test
    public void TestGetAxesCount() {
        GrblEsp32Controller  instance = new GrblEsp32Controller();

        instance.rawResponseHandler("[MSG:Axis count 6]");
        assertTrue(instance.getCapabilities().hasCapability(X_AXIS));
        assertTrue(instance.getCapabilities().hasCapability(Y_AXIS));
        assertTrue(instance.getCapabilities().hasCapability(Z_AXIS));
        assertTrue(instance.getCapabilities().hasCapability(A_AXIS));
        assertTrue(instance.getCapabilities().hasCapability(B_AXIS));
        assertTrue(instance.getCapabilities().hasCapability(C_AXIS));

        instance.rawResponseHandler("[MSG:Axis count 3]");
        assertTrue(instance.getCapabilities().hasCapability(X_AXIS));
        assertTrue(instance.getCapabilities().hasCapability(Y_AXIS));
        assertTrue(instance.getCapabilities().hasCapability(Z_AXIS));
        assertFalse(instance.getCapabilities().hasCapability(A_AXIS));
        assertFalse(instance.getCapabilities().hasCapability(B_AXIS));
        assertFalse(instance.getCapabilities().hasCapability(C_AXIS));

        instance.rawResponseHandler("[MSG:Axis count 4]");
        assertTrue(instance.getCapabilities().hasCapability(X_AXIS));
        assertTrue(instance.getCapabilities().hasCapability(Y_AXIS));
        assertTrue(instance.getCapabilities().hasCapability(Z_AXIS));
        assertTrue(instance.getCapabilities().hasCapability(A_AXIS));
        assertFalse(instance.getCapabilities().hasCapability(B_AXIS));
        assertFalse(instance.getCapabilities().hasCapability(C_AXIS));
    }

}