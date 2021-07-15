/*
    Copyright 2016 Will Winder

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
package com.willwinder.universalgcodesender.uielements.jog;

import javax.swing.*;
import javax.swing.text.DefaultFormatter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;

public class StepSizeSpinner extends JSpinner {

    double currentValue = 0.0;

    public StepSizeSpinner() {
        setModel(new StepSizeSpinnerModel());

        // Make the editor fire update events when typing, not only after changing fields
        JComponent comp = getEditor();
        JFormattedTextField field = (JFormattedTextField) comp.getComponent(0);
        DefaultFormatter formatter = (DefaultFormatter) field.getFormatter();
        formatter.setCommitsOnValidEdit(true);
    }

    @Override
    public Double getValue() {
        try {
            commitEdit();
        } catch (ParseException e) {
            setValue(currentValue);
        }

        BigDecimal bd = new BigDecimal(super.getValue().toString()).setScale(3, RoundingMode.HALF_EVEN);
        return bd.doubleValue();
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
