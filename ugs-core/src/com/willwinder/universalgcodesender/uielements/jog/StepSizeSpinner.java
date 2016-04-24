package com.willwinder.universalgcodesender.uielements.jog;

import javax.swing.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;

public class StepSizeSpinner extends JSpinner {

    public static final double MIN_VALUE = 0.001;
    public static final double MAX_VALUE = 1000.0;

    @Override
    public Double getValue() {
        try {
            commitEdit();
        } catch (ParseException e) {
            setValue(0.0);
        }
        BigDecimal bd = new BigDecimal(super.getValue().toString()).setScale(3, RoundingMode.HALF_EVEN);
        double stepSize = bd.doubleValue();
        return stepSize;
    }

    @Override
    public void setValue(Object value) {
        double val = bound(Double.parseDouble(value.toString()));
        BigDecimal bd = new BigDecimal(val).setScale(3, RoundingMode.HALF_EVEN);
        val = bd.doubleValue();
        super.setValue(val);
    }

    @Override
    public Double getNextValue() {
        double stepSize = getValue();
        double increment = MAX_VALUE;
        while (increment >= MIN_VALUE) {
            if (stepSize >= increment) {
                stepSize += increment;
                break;
            } else {
                increment = increment / 10;
            }
        }
        return bound(stepSize);
    }

    @Override
    public Double getPreviousValue() {
        double stepSize = getValue();
        double increment = MAX_VALUE;
        while (increment >= MIN_VALUE) {
            if (stepSize > increment) {
                stepSize -= increment;
                break;
            } else {
                increment = increment / 10;
            }
        }
        return bound(stepSize);
    }

    public void increaseStep() {
        setValue(getNextValue());
    }

    public void decreaseStep() {
        setValue(getPreviousValue());
    }

    public void divideStep() {
        double stepSize = getValue();
        double increment = MAX_VALUE;
        while (increment >= MIN_VALUE) {
            if (stepSize >= increment) {
                stepSize = increment;
                break;
            } else {
                increment = increment / 10;
            }
        }
        setValue(stepSize);
    }

    public void multiplyStep() {
        double stepSize = getValue();
        double increment = MIN_VALUE;
        while (increment <= MAX_VALUE) {
            if (stepSize <= increment) {
                stepSize = increment;
                break;
            } else {
                increment = increment * 10;
            }
        }
        setValue(stepSize);
    }

    private double bound(double val) {
        if (val <= MIN_VALUE) {
            return MIN_VALUE;
        } else if (val >= MAX_VALUE) {
            return MAX_VALUE;
        } else {
            return val;
        }
    }
}
