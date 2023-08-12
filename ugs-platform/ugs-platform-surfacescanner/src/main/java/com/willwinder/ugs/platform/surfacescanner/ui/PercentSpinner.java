package com.willwinder.ugs.platform.surfacescanner.ui;

import com.willwinder.universalgcodesender.uielements.TextFieldUnit;
import com.willwinder.universalgcodesender.uielements.TextFieldUnitFormatter;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;

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