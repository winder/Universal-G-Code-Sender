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
package com.willwinder.universalgcodesender.uielements;

import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.utils.MathUtils;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.text.ParseException;

/**
 * @author Joacim Breiler
 */
public class TextFieldUnitFormatter extends JFormattedTextField.AbstractFormatter {

    private final TextFieldUnit unit;
    private final int numberOfDecimals;
    private final boolean showAbbreviation;

    public TextFieldUnitFormatter(TextFieldUnit unit, int numberOfDecimals) {
        this(unit, numberOfDecimals, true);
    }

    public TextFieldUnitFormatter(TextFieldUnit unit, int numberOfDecimals, boolean showAbbreviation) {
        this.unit = unit;
        this.numberOfDecimals = numberOfDecimals;
        this.showAbbreviation = showAbbreviation;
    }

    @Override
    public Object stringToValue(String text) throws ParseException {
        String value = StringUtils.removeEnd(text, TextFieldUnit.MM.getAbbreviation());
        value = StringUtils.replace(value, ",", ".");
        value = StringUtils.removeEnd(value, TextFieldUnit.INCH.getAbbreviation());
        value = StringUtils.removeEnd(value, TextFieldUnit.INCHES_PER_MINUTE.getAbbreviation());
        value = StringUtils.removeEnd(value, TextFieldUnit.MM_PER_MINUTE.getAbbreviation());
        value = StringUtils.removeEnd(value, TextFieldUnit.ROTATIONS_PER_MINUTE.getAbbreviation());
        value = StringUtils.removeEnd(value, TextFieldUnit.PERCENT.getAbbreviation());
        value = StringUtils.trim(value);
        double numericValue = MathUtils.round(Utils.formatter.parse(value).doubleValue(), numberOfDecimals);

        if (unit == TextFieldUnit.PERCENT) {
            numericValue = numericValue / 100d;
        }
        return numericValue;
    }

    @Override
    public String valueToString(Object text) throws ParseException {
        double value = Utils.formatter.parse(text.toString()).doubleValue();
        if (unit == TextFieldUnit.PERCENT) {
            value = value * 100d;
        }

        String result = Utils.formatter.format(MathUtils.round(value, numberOfDecimals));
        if (showAbbreviation) {
            result += " " + unit.getAbbreviation();
        }
        return result;
    }
}
