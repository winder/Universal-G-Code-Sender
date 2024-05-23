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

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.text.DefaultFormatterFactory;

/**
 * Spinner that shows the given unit with its abbreviation
 *
 * @author Joacim Breiler
 */
public class UnitSpinner extends JSpinner {

    private SpinnerNumberModel spinnerNumberModel;
    private NumberEditor numberEditor;

    public UnitSpinner(double value, TextFieldUnit units) {
        this(value, units, null, null, 0.01d);
    }

    public UnitSpinner(double value, TextFieldUnit units, Double minimum, Double maximum, Double stepSize) {
        spinnerNumberModel = new SpinnerNumberModel(Double.valueOf(value), minimum, maximum, stepSize);
        setModel(spinnerNumberModel);
        setValue(value);
        setUnits(units);
    }

    public void setUnits(TextFieldUnit units) {
        if (units == TextFieldUnit.MM) {
            spinnerNumberModel.setStepSize(0.01);
        } else if (units == TextFieldUnit.INCH) {
            spinnerNumberModel.setStepSize(0.001);
        }

        numberEditor = new JSpinner.NumberEditor(this);
        numberEditor.getTextField().setFormatterFactory(new DefaultFormatterFactory(
                new TextFieldUnitFormatter(units, 3),
                new TextFieldUnitFormatter(units, 3),
                new TextFieldUnitFormatter(units, 3, false)));
        numberEditor.getTextField().setHorizontalAlignment(SwingConstants.LEFT);
        setEditor(numberEditor);
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
