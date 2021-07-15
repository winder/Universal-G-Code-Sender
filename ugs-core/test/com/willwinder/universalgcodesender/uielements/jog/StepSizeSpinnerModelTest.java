/*
    Copyright 2017 Will Winder

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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Joacim Breiler
 */
public class StepSizeSpinnerModelTest {

    @Test
    public void nextValueWhenMaxShouldNotIncrement() {
        StepSizeSpinnerModel model = new StepSizeSpinnerModel();
        model.setValue(StepSizeSpinnerModel.MAX_VALUE);
        Assert.assertEquals(null, model.getNextValue());
    }

    @Test
    public void previousValueWhenMaxShouldDecrement() {
        StepSizeSpinnerModel model = new StepSizeSpinnerModel();
        model.setValue(StepSizeSpinnerModel.MAX_VALUE);
        Assert.assertEquals(StepSizeSpinnerModel.MAX_VALUE - 10000, model.getPreviousValue());
    }

    @Test
    public void nextValueWhenMinShouldIncrement() {
        StepSizeSpinnerModel model = new StepSizeSpinnerModel();
        model.setValue(StepSizeSpinnerModel.MIN_VALUE);
        Assert.assertEquals(StepSizeSpinnerModel.MIN_VALUE + 0.001, model.getNextValue());
    }

    @Test
    public void previousValueWhenMinShouldNotDecrement() {
        StepSizeSpinnerModel model = new StepSizeSpinnerModel();
        model.setValue(StepSizeSpinnerModel.MIN_VALUE);
        Assert.assertEquals(null, model.getPreviousValue());
    }

    @Test
    public void stepSizesShouldChange() {
        StepSizeSpinnerModel model = new StepSizeSpinnerModel();
        model.setValue(StepSizeSpinnerModel.MIN_VALUE);
        Assert.assertEquals(0.002, model.getNextValue());

        model.setValue(StepSizeSpinnerModel.MIN_VALUE * 10);
        Assert.assertEquals(0.02, model.getNextValue());

        model.setValue(StepSizeSpinnerModel.MIN_VALUE * 100);
        Assert.assertEquals(0.2, model.getNextValue());

        model.setValue(StepSizeSpinnerModel.MIN_VALUE * 1000);
        Assert.assertEquals(2.0, model.getNextValue());

        model.setValue(StepSizeSpinnerModel.MIN_VALUE * 10000);
        Assert.assertEquals(20.0, model.getNextValue());

        model.setValue(StepSizeSpinnerModel.MIN_VALUE * 100000);
        Assert.assertEquals(200.0, model.getNextValue());

        model.setValue(StepSizeSpinnerModel.MIN_VALUE * 1000000000);
        Assert.assertEquals(null, model.getNextValue());
        Assert.assertEquals(1000000.0, model.getValue());
    }
}
