/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.utils.MathUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.text.ParseException;

/**
 * @author Joacim Breiler
 */
public class UnitFormatter extends JFormattedTextField.AbstractFormatter {

    private final Unit unit;
    private final int numberOfDecimals;
    private final boolean showAbbreviation;

    public UnitFormatter(Unit unit, int numberOfDecimals) {
        this(unit, numberOfDecimals, true);
    }

    public UnitFormatter(Unit unit, int numberOfDecimals, boolean showAbbreviation) {
        this.unit = unit;
        this.numberOfDecimals = numberOfDecimals;
        this.showAbbreviation = showAbbreviation;
    }

    @Override
    public Object stringToValue(String text) throws ParseException {
        String value = StringUtils.removeEnd(text, Unit.MM.getAbbreviation());
        value = StringUtils.replace(value, ",", ".");
        value = StringUtils.removeEnd(value, Unit.INCH.getAbbreviation());
        value = StringUtils.removeEnd(value, Unit.INCHES_PER_MINUTE.getAbbreviation());
        value = StringUtils.removeEnd(value, Unit.MM_PER_MINUTE.getAbbreviation());
        value = StringUtils.removeEnd(value, Unit.ROTATIONS_PER_MINUTE.getAbbreviation());
        value = StringUtils.removeEnd(value, Unit.PERCENT.getAbbreviation());
        value = StringUtils.trim(value);
        double numericValue = MathUtils.round(Utils.formatter.parse(value).doubleValue(), numberOfDecimals);

        if (unit == Unit.PERCENT) {
            numericValue = numericValue / 100d;
        }
        return numericValue;
    }

    @Override
    public String valueToString(Object text) throws ParseException {
        double value = Utils.formatter.parse(text.toString()).doubleValue();
        if (unit == Unit.PERCENT) {
            value = value * 100d;
        }

        String result = Utils.formatter.format(MathUtils.round(value, numberOfDecimals));
        if (showAbbreviation) {
            result += " " + unit.getAbbreviation();
        }
        return result;
    }
}
