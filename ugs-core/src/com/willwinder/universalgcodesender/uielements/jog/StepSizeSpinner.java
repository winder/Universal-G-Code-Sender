package com.willwinder.universalgcodesender.uielements.jog;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;

public class StepSizeSpinner extends JSpinner {

    double currentValue = 0.0;

    public StepSizeSpinner() {
        setModel(new StepSizeSpinnerModel());
    }

    @Override
    public Double getValue() {
        try {
            commitEdit();
        } catch (ParseException e) {
            setValue(currentValue);
        }

        BigDecimal bd = new BigDecimal(super.getValue().toString()).setScale(3, RoundingMode.HALF_EVEN);
        double stepSize = bd.doubleValue();
        return stepSize;
    }

    @Override
    public void setValue(Object value) {
        double val = Double.parseDouble(value.toString());
        BigDecimal bd = new BigDecimal(val).setScale(3, RoundingMode.HALF_EVEN);
        currentValue = bd.doubleValue();
        super.setValue(currentValue);
    }

    public void increaseStep() {
        Object nextValue = getNextValue();
        if (nextValue != null) {
            setValue(nextValue);
        }
    }

    public void decreaseStep() {
        Object previousValue = getPreviousValue();
        if (previousValue != null) {
            setValue(previousValue);
        }
    }

    public void divideStep() {
        double stepSize = getValue();
        setValue(bound(stepSize / 10.0));
    }

    public void multiplyStep() {
        double stepSize = getValue();
        setValue(bound(stepSize * 10.0));
    }

    private double bound(double val) {
        if (val <= StepSizeSpinnerModel.MIN_VALUE) {
            return StepSizeSpinnerModel.MIN_VALUE;
        } else if (val >= StepSizeSpinnerModel.MAX_VALUE) {
            return StepSizeSpinnerModel.MAX_VALUE;
        } else {
            return val;
        }
    }
}
