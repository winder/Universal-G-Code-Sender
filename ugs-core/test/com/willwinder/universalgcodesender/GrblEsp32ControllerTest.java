/*
    Copyright 2022 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.communicator.ICommunicator;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.willwinder.universalgcodesender.CapabilitiesConstants.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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
