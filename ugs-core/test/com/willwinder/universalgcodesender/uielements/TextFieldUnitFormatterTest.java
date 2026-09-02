package com.willwinder.universalgcodesender.uielements;

import com.willwinder.universalgcodesender.model.Unit;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TextFieldUnitFormatterTest {

    @Test
    public void valueToString_shouldFormatNumbersWithoutParsingThem() throws Exception {
        TextFieldUnitFormatter formatter = new TextFieldUnitFormatter(Unit.MM, 3);

        String result = formatter.valueToString(2.5);

        assertEquals("2.5 mm", result);
    }

    @Test
    public void valueToString_shouldFormatSmallNumbersWrittenInScientificNotation() throws Exception {
        TextFieldUnitFormatter formatter = new TextFieldUnitFormatter(Unit.INCH, 3);

        String result = formatter.valueToString(1.0E-4);

        assertEquals("0 in", result);
    }

    @Test
    public void valueToString_shouldReturnEmptyStringForNullValue() throws Exception {
        TextFieldUnitFormatter formatter = new TextFieldUnitFormatter(Unit.MM, 3);

        String result = formatter.valueToString(null);

        assertEquals("", result);
    }

    @Test
    public void valueToString_shouldConvertPercentToWholePercent() throws Exception {
        TextFieldUnitFormatter formatter = new TextFieldUnitFormatter(Unit.PERCENT, 1);

        String result = formatter.valueToString(0.5);

        assertEquals("50 %", result);
    }

    @Test
    public void valueToString_shouldFormatStringValues() throws Exception {
        TextFieldUnitFormatter formatter = new TextFieldUnitFormatter(Unit.MM, 3);

        String result = formatter.valueToString("2.5");

        assertEquals("2.5 mm", result);
    }

    @Test
    public void valueToString_shouldOmitAbbreviationWhenNotRequested() throws Exception {
        TextFieldUnitFormatter formatter = new TextFieldUnitFormatter(Unit.MM, 3, false);

        String result = formatter.valueToString(2.5);

        assertEquals("2.5", result);
    }
}
