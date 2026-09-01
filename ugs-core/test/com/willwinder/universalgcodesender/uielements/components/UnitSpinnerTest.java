package com.willwinder.universalgcodesender.uielements.components;

import com.willwinder.universalgcodesender.model.Unit;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UnitSpinnerTest {

    @Test
    public void setValue_shouldConvertFromGivenUnitsToTheSpinnerUnits() {
        UnitSpinner spinner = new UnitSpinner(0, Unit.INCH);

        spinner.setValue(25.4, Unit.MM);

        assertEquals(1, spinner.getDoubleValue(), 0.001);
    }

    @Test
    public void setValue_shouldNotConvertWhenGivenTheSpinnerUnits() {
        UnitSpinner spinner = new UnitSpinner(0, Unit.MM);

        spinner.setValue(25.4, Unit.MM);

        assertEquals(25.4, spinner.getDoubleValue(), 0.001);
    }

    @Test
    public void getDoubleValue_shouldConvertToTheGivenUnits() {
        UnitSpinner spinner = new UnitSpinner(1, Unit.INCH);

        double millimeters = spinner.getDoubleValue(Unit.MM);

        assertEquals(25.4, millimeters, 0.001);
    }

    @Test
    public void getDoubleValue_shouldConvertFeedRatesToTheGivenUnits() {
        UnitSpinner spinner = new UnitSpinner(10, Unit.INCHES_PER_MINUTE);

        double millimetersPerMinute = spinner.getDoubleValue(Unit.MM_PER_MINUTE);

        assertEquals(254, millimetersPerMinute, 0.001);
    }

    @Test
    public void setValue_shouldConvertFeedRatesFromGivenUnits() {
        UnitSpinner spinner = new UnitSpinner(0, Unit.INCHES_PER_MINUTE);

        spinner.setValue(254, Unit.MM_PER_MINUTE);

        assertEquals(10, spinner.getDoubleValue(), 0.001);
    }

    @Test
    public void setUnits_shouldChangeTheUnitsUsedForConversion() {
        UnitSpinner spinner = new UnitSpinner(1, Unit.INCH);

        spinner.setUnits(Unit.MM);

        assertEquals(Unit.MM, spinner.getUnits());
        assertEquals(1, spinner.getDoubleValue(Unit.MM), 0.001);
    }
}
