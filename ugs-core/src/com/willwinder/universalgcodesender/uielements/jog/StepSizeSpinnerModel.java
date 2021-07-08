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

public class StepSizeSpinnerModel extends SpinnerNumberModel {

    public static final double MIN_VALUE = 0.001;
    public static final double MAX_VALUE = 100000.0;

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

        if( value >= MAX_VALUE) {
            return null;
        }
        return value + stepSize;
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

        if( value <= MIN_VALUE) {
            return null;
        }
        return value - stepSize;
    }

}
