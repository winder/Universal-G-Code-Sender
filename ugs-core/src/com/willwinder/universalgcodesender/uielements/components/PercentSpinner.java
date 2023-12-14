/*
    Copyright 2023 Will Winder

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
package com.willwinder.universalgcodesender.uielements.components;

import com.willwinder.universalgcodesender.uielements.TextFieldUnit;
import com.willwinder.universalgcodesender.uielements.TextFieldUnitFormatter;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;

/**
 * A spinner that shows the percent character at the end when not editing.
 *
 * @author Joacim Breiler
 */
public class PercentSpinner extends JSpinner {

    private final SpinnerNumberModel spinnerNumberModel;

    public PercentSpinner(double value) {
        spinnerNumberModel = new SpinnerNumberModel(value, 0, 1, 0.01d);
        setModel(spinnerNumberModel);
        JSpinner.NumberEditor numberEditor = new JSpinner.NumberEditor(this);
        numberEditor.getTextField().setFormatterFactory(new DefaultFormatterFactory(
                new TextFieldUnitFormatter(TextFieldUnit.PERCENT, 1),
                new TextFieldUnitFormatter(TextFieldUnit.PERCENT, 1),
                new TextFieldUnitFormatter(TextFieldUnit.PERCENT, 1, false)));
        numberEditor.getTextField().setHorizontalAlignment(SwingConstants.LEFT);
        setEditor(numberEditor);
    }

    public PercentSpinner(double value, double min) {
        this(value);
        setMinimum(min);
    }

    public double getDoubleValue() {
        return (Double) getModel().getValue();
    }


    public void setMinimum(double min) {
        spinnerNumberModel.setMinimum(min);
        if (getDoubleValue() < min) {
            setValue(min);
        }
    }

    @Override
    public Object getNextValue() {
        if (super.getNextValue() == null) {
            return null;
        }

        double power = 1d / spinnerNumberModel.getStepSize().doubleValue();
        return Math.round((Double) super.getNextValue() * power) / power;
    }

    @Override
    public Object getPreviousValue() {
        if (super.getPreviousValue() == null) {
            return null;
        }

        double power = 1d / spinnerNumberModel.getStepSize().doubleValue();
        return Math.round((Double) super.getPreviousValue() * power) / power;
    }
}