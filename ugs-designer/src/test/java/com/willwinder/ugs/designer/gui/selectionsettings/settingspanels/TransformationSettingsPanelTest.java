/*
    Copyright 2026 Damian Nikodem

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
package com.willwinder.ugs.designer.gui.selectionsettings.settingspanels;

import com.willwinder.ugs.designer.gui.expression.ExpressionAwareTextFieldFormatter;
import com.willwinder.universalgcodesender.uielements.TextFieldWithUnit;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JFormattedTextField;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Integration test that locks in the wiring between {@link TransformationSettingsPanel}
 * and {@link ExpressionAwareTextFieldFormatter}. Without this, a future edit could
 * silently swap the field constructor back to plain {@code TextFieldWithUnit} and
 * the expression feature would regress without any unit test failing.
 */
public class TransformationSettingsPanelTest {

    private static final String[] EXPRESSION_FIELD_NAMES = {
            "posXTextField", "posYTextField", "widthTextField", "heightTextField", "rotationTextField"
    };

    private TransformationSettingsPanel panel;

    @Before
    public void setUp() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        panel = new TransformationSettingsPanel();
    }

    @Test
    public void allTransformFieldsUseExpressionAwareFormatter() throws Exception {
        for (String name : EXPRESSION_FIELD_NAMES) {
            TextFieldWithUnit field = readField(name);
            assertNotNull(name + " was not constructed", field);

            JFormattedTextField.AbstractFormatterFactory factory = field.getFormatterFactory();
            JFormattedTextField.AbstractFormatter formatter = factory.getFormatter(field);
            assertTrue(name + " must use ExpressionAwareTextFieldFormatter, was " + formatter.getClass().getName(),
                    formatter instanceof ExpressionAwareTextFieldFormatter);
        }
    }

    @Test
    public void typingAnExpressionIntoTheWidthFieldEvaluatesIt() throws Exception {
        TextFieldWithUnit width = readField("widthTextField");

        width.setText("3/2");
        width.commitEdit();

        assertEquals(1.5, ((Number) width.getValue()).doubleValue(), 1e-9);
    }

    @Test
    public void typingAnExpressionWithUnitSuffixWorks() throws Exception {
        TextFieldWithUnit height = readField("heightTextField");

        height.setText("25.4 * 2 mm");
        height.commitEdit();

        assertEquals(50.8, ((Number) height.getValue()).doubleValue(), 1e-9);
    }

    @Test
    public void rotationFieldAcceptsExpression() throws Exception {
        TextFieldWithUnit rotation = readField("rotationTextField");

        rotation.setText("360/8");
        rotation.commitEdit();

        assertEquals(45.0, ((Number) rotation.getValue()).doubleValue(), 1e-9);
    }

    @Test
    public void plainNumericInputStillCommitsCleanly() throws Exception {
        TextFieldWithUnit posX = readField("posXTextField");

        posX.setText("42");
        posX.commitEdit();

        assertEquals(42.0, ((Number) posX.getValue()).doubleValue(), 1e-9);
    }

    @SuppressWarnings("unchecked")
    private <T> T readField(String name) throws Exception {
        Field field = TransformationSettingsPanel.class.getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(panel);
    }
}
