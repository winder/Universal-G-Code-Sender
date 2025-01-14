package com.willwinder.universalgcodesender.firmware.grbl;

import com.willwinder.universalgcodesender.Capabilities;
import com.willwinder.universalgcodesender.CapabilitiesConstants;
import com.willwinder.universalgcodesender.GrblUtils;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.communicator.ICommunicator;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.ControllerStatusBuilder;
import com.willwinder.universalgcodesender.listeners.OverridePercents;
import com.willwinder.universalgcodesender.listeners.OverrideType;
import com.willwinder.universalgcodesender.model.Overrides;
import com.willwinder.universalgcodesender.services.MessageService;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.anyByte;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Captor;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
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

    @Captor
    private ArgumentCaptor<ControllerListener> controllerListenerCaptor;

    private GrblOverrideManager overrideManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        doNothing().when(controller).addListener(controllerListenerCaptor.capture());
        overrideManager = new GrblOverrideManager(controller, communicator, new MessageService());
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

        assertEquals(100, overrideManager.getSliderDefault(OverrideType.SPINDLE_SPEED));
        assertEquals(100, overrideManager.getSliderDefault(OverrideType.FEED_SPEED));
        assertEquals(100, overrideManager.getSliderDefault(OverrideType.RAPID_SPEED));
        assertEquals(100, overrideManager.getRadioDefault(OverrideType.RAPID_SPEED));
        assertEquals(0, overrideManager.getSliderDefault(OverrideType.MIST_TOGGLE));

        assertEquals(200, overrideManager.getSliderMax(OverrideType.SPINDLE_SPEED));
        assertEquals(200, overrideManager.getSliderMax(OverrideType.FEED_SPEED));
        assertEquals(100, overrideManager.getSliderMax(OverrideType.RAPID_SPEED));
        assertEquals(0, overrideManager.getSliderMax(OverrideType.MIST_TOGGLE));

        assertEquals(10, overrideManager.getSliderMin(OverrideType.SPINDLE_SPEED));
        assertEquals(10, overrideManager.getSliderMin(OverrideType.FEED_SPEED));
        assertEquals(25, overrideManager.getSliderMin(OverrideType.RAPID_SPEED));
        assertEquals(0, overrideManager.getSliderMin(OverrideType.MIST_TOGGLE));

        assertEquals(100, overrideManager.getSliderTargetValue(OverrideType.SPINDLE_SPEED));
        assertEquals(100, overrideManager.getSliderTargetValue(OverrideType.FEED_SPEED));
        assertEquals(100, overrideManager.getSliderTargetValue(OverrideType.RAPID_SPEED));
        assertEquals(0, overrideManager.getSliderTargetValue(OverrideType.MIST_TOGGLE));

        assertEquals(1, overrideManager.getSliderStep(OverrideType.SPINDLE_SPEED));
        assertEquals(1, overrideManager.getSliderStep(OverrideType.FEED_SPEED));
        assertEquals(25, overrideManager.getSliderStep(OverrideType.RAPID_SPEED));
        assertEquals(0, overrideManager.getSliderStep(OverrideType.MIST_TOGGLE));

        assertEquals(10, overrideManager.getSpeedMajorStep(OverrideType.SPINDLE_SPEED));
        assertEquals(10, overrideManager.getSpeedMajorStep(OverrideType.FEED_SPEED));

        assertEquals(1, overrideManager.getSpeedMinorStep(OverrideType.SPINDLE_SPEED));
        assertEquals(1, overrideManager.getSpeedMinorStep(OverrideType.FEED_SPEED));

        assertEquals(List.of(OverrideType.FEED_SPEED, OverrideType.SPINDLE_SPEED), overrideManager.getSliderTypes());
    }

    @Test
    public void setSpeedTargetShouldSendCommand() throws Exception {
        mockControllerStatus(ControllerState.IDLE);
        mockOverrideCapabilities();

        overrideManager.setSliderTarget(OverrideType.FEED_SPEED, 10);
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

        overrideManager.setSliderTarget(OverrideType.FEED_SPEED, 10);
        ControllerStatus controllerStatus = ControllerStatusBuilder.newInstance().setState(ControllerState.RUN).setOverrides(new OverridePercents(10, 100, 100)).build();
        when(controller.getControllerStatus()).thenReturn(controllerStatus);
        overrideManager.onControllerStatus(controllerStatus);

        assertTrue(overrideManager.hasSettled());
    }

    @Test
    public void onStreamCanceledShouldResetOverrides() throws Exception {
        mockControllerStatus(ControllerState.IDLE);
        mockOverrideCapabilities();

        ControllerListener controllerListener = controllerListenerCaptor.getValue();
        controllerListener.streamCanceled();

        assertOverridesResetted();
    }

    @Test
    public void onStreamCompleteShouldResetOverrides() throws Exception {
        mockControllerStatus(ControllerState.IDLE);
        mockOverrideCapabilities();

        ControllerListener controllerListener = controllerListenerCaptor.getValue();
        controllerListener.streamComplete();

        assertOverridesResetted();
    }

    @Test
    public void onStreamStartedShouldResetOverrides() throws Exception {
        mockControllerStatus(ControllerState.IDLE);
        mockOverrideCapabilities();

        ControllerListener controllerListener = controllerListenerCaptor.getValue();
        controllerListener.streamStarted();

        assertOverridesResetted();
    }

    private void assertOverridesResetted() throws Exception {
        verify(communicator, times(1)).sendByteImmediately(eq(getOverrideCommand(Overrides.CMD_RAPID_OVR_RESET)));
        verify(communicator, times(1)).sendByteImmediately(eq(getOverrideCommand(Overrides.CMD_FEED_OVR_RESET)));
        verify(communicator, times(1)).sendByteImmediately(eq(getOverrideCommand(Overrides.CMD_SPINDLE_OVR_RESET)));
    }

    private byte getOverrideCommand(Overrides overrides) {
        return GrblUtils.getOverrideForEnum(overrides, controller.getCapabilities());
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