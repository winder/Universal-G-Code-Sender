/*
    Copyright 2026 Will Winder

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

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.ugs.nbp.setupwizard.NavigationButtons;
import org.junit.Test;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class WizardPanelStepCalibrationTest {

    @Test
    public void inchReportingDisplaysMillimetersAndUsesMillimetersForEstimate() throws Exception {
        BackendAPI backend = mock(BackendAPI.class);
        IController controller = mock(IController.class);
        IFirmwareSettings firmwareSettings = mock(IFirmwareSettings.class);
        Settings settings = mock(Settings.class);
        Position oneInch = new Position(1, 1, 1, UnitUtils.Units.INCH);

        when(backend.getSettings()).thenReturn(settings);
        when(settings.getJogFeedRate()).thenReturn(100.0);
        when(backend.getController()).thenReturn(controller);
        when(controller.getFirmwareSettings()).thenReturn(firmwareSettings);
        when(backend.getWorkPosition()).thenReturn(oneInch);
        when(firmwareSettings.getStepsPerMillimeter(Axis.X)).thenReturn(80.0);
        when(firmwareSettings.isSoftLimitsEnabled()).thenReturn(true);
        when(firmwareSettings.getSoftLimit(Axis.X)).thenReturn(20.0);

        WizardPanelStepCalibration panel = new WizardPanelStepCalibration(backend);
        invoke(panel, "updatePosition", new Class<?>[]{Position.class, Position.class}, oneInch, oneInch);

        JLabel positionLabel = field(panel, "labelPositionX", JLabel.class);
        assertTrue(positionLabel.getText(), positionLabel.getText().contains("25.4 mm"));

        JTextField measurementField = field(panel, "textFieldMeasuredX", JTextField.class);
        measurementField.setText("25.4");
        JLabel estimateLabel = field(panel, "labelEstimatedStepsX", JLabel.class);
        invoke(panel, "updateMeasurementEstimatesFields", new Class<?>[0]);

        assertTrue(estimateLabel.getText(), estimateLabel.getText().startsWith("80 steps/mm"));

        Position machinePosition = new Position(-0.75, 0, 0, UnitUtils.Units.INCH);
        invoke(panel, "updatePosition", new Class<?>[]{Position.class, Position.class}, oneInch, machinePosition);
        NavigationButtons navigationButtons = field(panel, "navigationButtons", NavigationButtons.class);
        JButton negativeXButton = navigationButtons.getButtonXneg();
        assertFalse("Negative X jog must stop at the 20 mm soft limit", negativeXButton.isEnabled());
    }

    private static void invoke(Object target, String name, Class<?>[] parameterTypes, Object... arguments) throws Exception {
        Method method = target.getClass().getDeclaredMethod(name, parameterTypes);
        method.setAccessible(true);
        method.invoke(target, arguments);
    }

    private static <T> T field(Object target, String name, Class<T> type) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return type.cast(field.get(target));
    }
}
