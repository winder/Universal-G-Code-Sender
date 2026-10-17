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
package com.willwinder.ugs.nbp.setupwizard.panels;

import com.willwinder.universalgcodesender.Capabilities;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JComponent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WizardPanelHardLimitsTest {
    private static final long REFRESH_TIMEOUT_MILLIS = 2000;

    private IFirmwareSettings firmwareSettings;
    private WizardPanelHardLimits panel;

    @Before
    public void setUp() throws Exception {
        BackendAPI backend = mock(BackendAPI.class);
        IController controller = mock(IController.class);
        Capabilities capabilities = mock(Capabilities.class);
        firmwareSettings = mock(IFirmwareSettings.class);

        when(backend.getController()).thenReturn(controller);
        when(controller.getCapabilities()).thenReturn(capabilities);
        when(controller.getFirmwareSettings()).thenReturn(firmwareSettings);
        when(capabilities.hasHardLimits()).thenReturn(true);
        when(firmwareSettings.isHardLimitsEnabled()).thenReturn(true);

        panel = new WizardPanelHardLimits(backend);
    }

    @Test
    public void refreshComponents_shouldShowSingleEndstopLabelsWhenFirmwareHasNoDualEndstops() throws Exception {
        invokeRefreshComponents();
        awaitVisible("labelLimitX");

        assertThat(component("labelLimitX").isVisible()).isTrue();
        assertThat(component("labelLimitY").isVisible()).isTrue();
        assertThat(component("labelLimitZ").isVisible()).isTrue();
        assertThat(component("labelLimitX0").isVisible()).isFalse();
        assertThat(component("labelLimitX1").isVisible()).isFalse();
        assertThat(component("pnlX0Spacer").isVisible()).isFalse();
        assertThat(component("pnlX1Spacer").isVisible()).isFalse();
    }

    @Test
    public void refreshComponents_shouldShowDualEndstopLabelsWhenAnAxisHasTwoEndstops() throws Exception {
        when(firmwareSettings.hasX0()).thenReturn(true);
        when(firmwareSettings.hasX1()).thenReturn(true);
        when(firmwareSettings.hasZ0()).thenReturn(true);

        invokeRefreshComponents();
        awaitVisible("labelLimitX0");

        assertThat(component("labelLimitX").isVisible()).isFalse();
        assertThat(component("labelLimitX0").isVisible()).isTrue();
        assertThat(component("labelLimitX1").isVisible()).isTrue();
        assertThat(component("labelLimitZ0").isVisible()).isTrue();
        assertThat(component("pnlZ0Spacer").isVisible()).isFalse();
        assertThat(component("labelLimitZ1").isVisible()).isFalse();
        assertThat(component("pnlZ1Spacer").isVisible()).isTrue();
    }

    private void invokeRefreshComponents() throws Exception {
        Method method = WizardPanelHardLimits.class.getDeclaredMethod("refreshComponents");
        method.setAccessible(true);
        method.invoke(panel);
    }

    private void awaitVisible(String fieldName) throws Exception {
        JComponent component = component(fieldName);
        await(component::isVisible);
    }

    private static void await(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.currentTimeMillis() + REFRESH_TIMEOUT_MILLIS;
        while (!condition.getAsBoolean() && System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }
    }

    private JComponent component(String fieldName) throws Exception {
        Field field = WizardPanelHardLimits.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (JComponent) field.get(panel);
    }
}
