package com.willwinder.ugs.platform.probe.actions;

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UnitUtils;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class ProbeZActionTest {
    @Spy
    private ProbeZAction action;

    @Mock
    private IController controller;

    @Mock
    private BackendAPI backend;

    @Mock
    private IFirmwareSettings firmwareSettings;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(action.getBackend()).thenReturn(backend);
        when(backend.getController()).thenReturn(controller);
        when(controller.getFirmwareSettings()).thenReturn(firmwareSettings);
    }

    @Test
    public void calculateSafeProbeDistanceShouldReturnProbeDistanceIfDistanceToSoftLimitIsPositive() throws FirmwareSettingsException {
        when(firmwareSettings.isSoftLimitsEnabled()).thenReturn(true);
        doReturn(UnitUtils.Units.MM).when(action).getSettingsUnits();
        doReturn(true).when(action).getSettingsCompensateForSoftLimits();
        doReturn(-10d).when(action).getSettingProbeDistance();
        doReturn(5d).when(action).getDistanceToSoftLimit();
        assertEquals(-10, action.calculateSafeProbeDistance(), 0.1);
    }

    @Test
    public void calculateSafeProbeDistanceShouldReturnNotCompensateForProbeDistanceWhenNotUsingSoftLimits() throws FirmwareSettingsException {
        when(firmwareSettings.isSoftLimitsEnabled()).thenReturn(true);
        doReturn(UnitUtils.Units.MM).when(action).getSettingsUnits();
        doReturn(false).when(action).getSettingsCompensateForSoftLimits();
        doReturn(-10d).when(action).getSettingProbeDistance();
        doReturn(-5d).when(action).getDistanceToSoftLimit();
        assertEquals(-10, action.calculateSafeProbeDistance(), 0.1);
    }

    @Test
    public void calculateSafeProbeDistanceShouldSubtractDistanceToSoftLimit() {
        doReturn(UnitUtils.Units.MM).when(action).getSettingsUnits();
        doReturn(true).when(action).shouldCompensateForSoftLimits();
        doReturn(-10d).when(action).getSettingProbeDistance();
        doReturn(-5d).when(action).getDistanceToSoftLimit();
        assertEquals(-5, action.calculateSafeProbeDistance(), 0.1);
    }

    @Test
    public void calculateSafeProbeDistanceShouldReturnTheDistanceIfCompensationIsTurnedOff() {
        doReturn(false).when(action).shouldCompensateForSoftLimits();
        doReturn(-10d).when(action).getSettingProbeDistance();
        assertEquals(-10, action.calculateSafeProbeDistance(), 0.1);
    }

}