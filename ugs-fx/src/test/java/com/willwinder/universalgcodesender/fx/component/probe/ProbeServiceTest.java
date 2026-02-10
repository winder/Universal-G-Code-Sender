/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.component.probe;

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.fx.service.ProbeService;
import com.willwinder.universalgcodesender.fx.settings.ProbeSettings;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.ControllerStatusBuilder;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitValue;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

public class ProbeServiceTest {

    @Mock
    private IFirmwareSettings firmwareSettings;

    @Mock
    private IController controller;

    @Mock
    private BackendAPI backend;

    @Mock
    private ProbeSettings settings;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(backend.getController()).thenReturn(controller);
        when(controller.getFirmwareSettings()).thenReturn(firmwareSettings);
    }

    @Test
    public void getSafeProbeZDistance_shouldSub() throws FirmwareSettingsException {
        ControllerStatus controllerStatus = ControllerStatusBuilder.newInstance()
                .setMachineCoord(new Position(0, 0, -10))
                .build();

        ObjectProperty<UnitValue> probeZDistanceProperty = new SimpleObjectProperty<>(new UnitValue(Unit.MM, 100));
        when(firmwareSettings.isSoftLimitsEnabled()).thenReturn(true);
        when(controller.getControllerStatus()).thenReturn(controllerStatus);
        when(settings.probeZDistanceProperty()).thenReturn(probeZDistanceProperty);

        ProbeService probeService = new ProbeService(backend, settings);

        UnitValue safeProbeZDistance = probeService.getSafeProbeZDistance();

        assertEquals(9.99999d, safeProbeZDistance.value().doubleValue(), 0.001);
    }
}