package com.willwinder.universalgcodesender.firmware.grbl.commands;

import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import org.junit.Test;

import static org.junit.Assert.*;

public class GetStatusCommandTest {
    @Test
    public void getStatusFromLegacyVersionString() {
        GetStatusCommand statusCommand = new GetStatusCommand();
        statusCommand.appendResponse("<Idle,MPos:5.529,0.560,7.000,WPos:1.529,-5.440,-0.000>");
        statusCommand.appendResponse("ok");

        ControllerStatus controllerStatus = statusCommand.getControllerStatus();
        assertEquals(ControllerState.IDLE, controllerStatus.getState());
        assertEquals(5.529d, controllerStatus.getMachineCoord().getX(), 0.001);
        assertEquals(0.560d, controllerStatus.getMachineCoord().getY(), 0.001);
        assertEquals(7.000d, controllerStatus.getMachineCoord().getZ(), 0.001);
        assertEquals(1.529d, controllerStatus.getWorkCoord().getX(), 0.001);
        assertEquals(-5.440d, controllerStatus.getWorkCoord().getY(), 0.001);
        assertEquals(-0.000d, controllerStatus.getWorkCoord().getZ(), 0.001);
    }

    @Test
    public void getStatusFromNewVersionString() {
        GetStatusCommand statusCommand = new GetStatusCommand();
        statusCommand.appendResponse("<Idle|MPos:5.529,0.560,7.000|WPos:1.529,-5.440,-0.000>");
        statusCommand.appendResponse("ok");

        ControllerStatus controllerStatus = statusCommand.getControllerStatus();
        assertEquals(ControllerState.IDLE, controllerStatus.getState());
        assertEquals(5.529d, controllerStatus.getMachineCoord().getX(), 0.001);
        assertEquals(0.560d, controllerStatus.getMachineCoord().getY(), 0.001);
        assertEquals(7.000d, controllerStatus.getMachineCoord().getZ(), 0.001);
        assertEquals(1.529d, controllerStatus.getWorkCoord().getX(), 0.001);
        assertEquals(-5.440d, controllerStatus.getWorkCoord().getY(), 0.001);
        assertEquals(-0.000d, controllerStatus.getWorkCoord().getZ(), 0.001);
    }

    @Test
    public void getStatusFromUnknownString() {
        GetStatusCommand statusCommand = new GetStatusCommand();
        statusCommand.appendResponse("Not a status");
        statusCommand.appendResponse("ok");

        ControllerStatus controllerStatus = statusCommand.getControllerStatus();
        assertEquals(ControllerState.UNKNOWN, controllerStatus.getState());
    }
}
