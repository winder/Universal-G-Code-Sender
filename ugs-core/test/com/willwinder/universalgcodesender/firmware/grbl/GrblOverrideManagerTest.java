package com.willwinder.universalgcodesender.firmware.grbl;

import com.willwinder.universalgcodesender.Capabilities;
import com.willwinder.universalgcodesender.CapabilitiesConstants;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.communicator.ICommunicator;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.ControllerStatusBuilder;
import com.willwinder.universalgcodesender.listeners.OverridePercents;
import com.willwinder.universalgcodesender.listeners.OverrideType;
import com.willwinder.universalgcodesender.model.Overrides;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.anyByte;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import java.util.List;

public class GrblOverrideManagerTest {

    @Mock
    private IController controller;

    @Mock
    private ICommunicator communicator;
    private GrblOverrideManager overrideManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        overrideManager = new GrblOverrideManager(controller, communicator);
    }

    @Test
    public void isAvailableShouldReturnFalseIfControllerStatusIsNull() {
        when(controller.getControllerStatus()).thenReturn(null);
        assertFalse(overrideManager.isAvailable());
    }

    @Test
    public void isAvailableShouldReturnFalseIfControllerCapabilitiesIsNull() {
        when(controller.getControllerStatus()).thenReturn(null);
        assertFalse(overrideManager.isAvailable());
    }

    @Test
    public void isAvailableShouldReturnTrueIfControllerIsInHoldState() {
        mockOverrideCapabilities();
        mockControllerStatus(ControllerState.HOLD);

        assertTrue(overrideManager.isAvailable());
    }

    @Test
    public void isAvailableShouldReturnTrueIfControllerIsInIdleState() {
        mockOverrideCapabilities();
        mockControllerStatus(ControllerState.IDLE);

        assertTrue(overrideManager.isAvailable());
    }

    @Test
    public void isAvailableShouldReturnTrueIfControllerIsInRunState() {
        mockOverrideCapabilities();
        mockControllerStatus(ControllerState.RUN);

        assertTrue(overrideManager.isAvailable());
    }

    @Test
    public void sendOverrideCommandShouldSendCommandByteToCommunicator() throws Exception {
        mockOverrideCapabilities();
        mockControllerStatus(ControllerState.RUN);

        overrideManager.sendOverrideCommand(Overrides.CMD_TOGGLE_SPINDLE);

        verify(communicator, times(1)).sendByteImmediately(anyByte());
    }

    @Test
    public void getSpeedValuesShouldReturnBasedOnOverrideType() {
        mockOverrideCapabilities();
        mockControllerStatus(ControllerState.RUN);

        assertEquals(100, overrideManager.getSpeedDefault(OverrideType.SPINDLE_SPEED));
        assertEquals(100, overrideManager.getSpeedDefault(OverrideType.FEED_SPEED));
        assertEquals(0, overrideManager.getSpeedDefault(OverrideType.RAPID_SPEED));
        assertEquals(0, overrideManager.getSpeedDefault(OverrideType.MIST_TOGGLE));

        assertEquals(200, overrideManager.getSpeedMax(OverrideType.SPINDLE_SPEED));
        assertEquals(200, overrideManager.getSpeedMax(OverrideType.FEED_SPEED));
        assertEquals(0, overrideManager.getSpeedMax(OverrideType.RAPID_SPEED));
        assertEquals(0, overrideManager.getSpeedMax(OverrideType.MIST_TOGGLE));

        assertEquals(10, overrideManager.getSpeedMin(OverrideType.SPINDLE_SPEED));
        assertEquals(10, overrideManager.getSpeedMin(OverrideType.FEED_SPEED));
        assertEquals(0, overrideManager.getSpeedMin(OverrideType.RAPID_SPEED));
        assertEquals(0, overrideManager.getSpeedMin(OverrideType.MIST_TOGGLE));

        assertEquals(100, overrideManager.getSpeedTargetValue(OverrideType.SPINDLE_SPEED));
        assertEquals(100, overrideManager.getSpeedTargetValue(OverrideType.FEED_SPEED));
        assertEquals(0, overrideManager.getSpeedTargetValue(OverrideType.RAPID_SPEED));
        assertEquals(0, overrideManager.getSpeedTargetValue(OverrideType.MIST_TOGGLE));

        assertEquals(1, overrideManager.getSpeedStep(OverrideType.SPINDLE_SPEED));
        assertEquals(1, overrideManager.getSpeedStep(OverrideType.FEED_SPEED));
        assertEquals(0, overrideManager.getSpeedStep(OverrideType.RAPID_SPEED));
        assertEquals(0, overrideManager.getSpeedStep(OverrideType.MIST_TOGGLE));

        assertEquals(10, overrideManager.getSpeedMajorStep(OverrideType.SPINDLE_SPEED));
        assertEquals(10, overrideManager.getSpeedMajorStep(OverrideType.FEED_SPEED));

        assertEquals(1, overrideManager.getSpeedMinorStep(OverrideType.SPINDLE_SPEED));
        assertEquals(1, overrideManager.getSpeedMinorStep(OverrideType.FEED_SPEED));

        assertEquals(List.of(OverrideType.FEED_SPEED, OverrideType.SPINDLE_SPEED), overrideManager.getSpeedTypes());
    }

    @Test
    public void setSpeedTargetShouldSendCommand() throws Exception {
        mockControllerStatus(ControllerState.IDLE);
        mockOverrideCapabilities();

        overrideManager.setSpeedTarget(OverrideType.FEED_SPEED, 10);
        verify(communicator, times(1)).sendByteImmediately(anyByte());
        assertFalse(overrideManager.hasSettled());


        ControllerStatus controllerStatus = ControllerStatusBuilder.newInstance().setState(ControllerState.RUN).setOverrides(new OverridePercents(10, 0, 0)).build();
        when(controller.getControllerStatus()).thenReturn(controllerStatus);
        assertFalse(overrideManager.hasSettled());
    }

    @Test
    public void hasSettledShouldReturnTrueWhenOverridePercentReachesTarget() {
        mockControllerStatus(ControllerState.IDLE);
        mockOverrideCapabilities();

        overrideManager.setSpeedTarget(OverrideType.FEED_SPEED, 10);
        ControllerStatus controllerStatus = ControllerStatusBuilder.newInstance().setState(ControllerState.RUN).setOverrides(new OverridePercents(10, 100, 100)).build();
        when(controller.getControllerStatus()).thenReturn(controllerStatus);
        overrideManager.onControllerStatus(controllerStatus);

        assertTrue(overrideManager.hasSettled());
    }

    private void mockControllerStatus(ControllerState controllerState) {
        ControllerStatus controllerStatus = ControllerStatusBuilder.newInstance().setOverrides(new OverridePercents(100, 100, 100)).setState(controllerState).build();
        when(controller.getControllerStatus()).thenReturn(controllerStatus);
    }

    private void mockOverrideCapabilities() {
        Capabilities capabilites = new Capabilities();
        capabilites.addCapability(CapabilitiesConstants.OVERRIDES);
        when(controller.getCapabilities()).thenReturn(capabilites);
    }
}