package com.willwinder.ugs.platform.probe.actions;

import com.willwinder.universalgcodesender.model.UnitUtils;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.doReturn;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

public class ProbeZActionTest {
    @Spy
    private ProbeZAction action;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void calculateSafeProbeDistanceShouldReturnProbeDistanceIfDistanceToSoftLimitIsPositive() {
        doReturn(UnitUtils.Units.MM).when(action).getSettingsUnits();
        doReturn(true).when(action).getSettingCompensateForSoftLimits();
        doReturn(-10d).when(action).getSettingProbeDistance();
        doReturn(5d).when(action).getDistanceToSoftLimit();
        assertEquals(-10, action.calculateSafeProbeDistance(), 0.1);
    }

    @Test
    public void calculateSafeProbeDistanceShouldSubtractDistanceToSoftLimit() {
        doReturn(UnitUtils.Units.MM).when(action).getSettingsUnits();
        doReturn(true).when(action).getSettingCompensateForSoftLimits();
        doReturn(-10d).when(action).getSettingProbeDistance();
        doReturn(-5d).when(action).getDistanceToSoftLimit();
        assertEquals(-5, action.calculateSafeProbeDistance(), 0.1);
    }

    @Test
    public void calculateSafeProbeDistanceShouldReturnTheDistanceIfCompensationIsTurnedOff() {
        doReturn(false).when(action).getSettingCompensateForSoftLimits();
        doReturn(-10d).when(action).getSettingProbeDistance();
        assertEquals(-10, action.calculateSafeProbeDistance(), 0.1);
    }

}