/*
 * A custom spinner that will step full integers unless less than or equal to 1
 * at which point it will step tenths unless less than or equal to .1 at which
 * point it will step hundredths, etc.
 */

/*
    Copywrite 2012-2016 Will Winder

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

package com.willwinder.universalgcodesender.uielements;

import javax.swing.SpinnerNumberModel;

/**
 *
 * @author wwinder
 */
public class StepSizeSpinnerModel extends SpinnerNumberModel {

    Double maxStepSize = null;

    public StepSizeSpinnerModel(Number value, Comparable min, Comparable max, Number size) {
        super (value, min, max, size);
        maxStepSize = size.doubleValue();
    }    
    
    private Double getPreviousStepSize() {
        Number stepSize = this.getStepSize();
        
        Double size =  stepSize.doubleValue();

        // Determine if step size needs adjusting
        if (getNumber().doubleValue() == this.getStepSize().doubleValue()) {
            size /= 10.0;
        }

        return size;
    }

    private Double getNextStepSize() {
        Double size = this.getStepSize().doubleValue();
        
        if (getNumber().doubleValue() >= (10 * size)) {
            if (this.getStepSize().doubleValue() < maxStepSize) {

                size *= 10;
                if (size > maxStepSize) {
                    size = maxStepSize;
                }
            }
        }

        if (this.getNumber().doubleValue() == 0) {
            size = .0001;
        }

        return size;
    }

    /**
     * Returns the next value, or
     * <code>null</code> if adding the step size to the current value results in
     * a value greater than the maximum value. The current value is not changed.
     *
     * @return The next value, or
     * <code>null</code> if the current value is the maximum value represented
     * by this model.
     */
    @Override
    public Object getNextValue() {
        Double num;
        this.setStepSize(getNextStepSize());

        num = this.getNumber().doubleValue() + this.getStepSize().doubleValue();

        // check upper bound if set
        if ((this.getMaximum() != null) && this.getMaximum().compareTo(num) < 0) {
            num = null;
        }

        return num;
    }

    /**
     * Returns the previous value, or
     * <code>null</code> if subtracting the step size from the current value
     * results in a value less than the minimum value. The current value is not
     * changed.
     *
     * @return The previous value, or
     * <code>null</code> if the current value is the minimum value represented
     * by this model.
     */
    @Override
    public Object getPreviousValue() {
        Double num;
        this.setStepSize(getPreviousStepSize());

        num = this.getNumber().doubleValue() - this.getStepSize().doubleValue();

        // check lower bound if set
        if ((this.getMinimum() != null) && this.getMinimum().compareTo(num) > 0) {
            num = null;
        }

        return num;
    }
}