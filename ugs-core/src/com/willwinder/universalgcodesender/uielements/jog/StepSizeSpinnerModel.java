package com.willwinder.universalgcodesender.uielements.jog;

import javax.swing.*;

public class StepSizeSpinnerModel extends SpinnerNumberModel {

    public static final double MIN_VALUE = 0.001;
    public static final double MAX_VALUE = 1000.0;

    public StepSizeSpinnerModel() {
        super(1.0, MIN_VALUE, MAX_VALUE, 1.0);
    }




    @Override
    public Object getNextValue() {
        double value = (double) getValue();
        double stepSize = MAX_VALUE;
        while (stepSize >= MIN_VALUE) {
            if (value >= stepSize) {
                break;
            } else {
                stepSize = stepSize / 10;
            }
        }
        setStepSize(stepSize);
        return super.getNextValue();
    }

    @Override
    public Object getPreviousValue() {
        double value = (double) getValue();
        double stepSize = MAX_VALUE;
        while (stepSize >= MIN_VALUE) {
            if (value > stepSize) {
                break;
            } else {
                stepSize = stepSize / 10;
            }
        }
        setStepSize(stepSize);
        return super.getPreviousValue();
    }

}
