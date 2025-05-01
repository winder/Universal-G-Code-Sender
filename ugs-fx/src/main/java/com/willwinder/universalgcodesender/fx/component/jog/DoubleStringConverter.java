package com.willwinder.universalgcodesender.fx.component.jog;

import com.willwinder.universalgcodesender.Utils;
import javafx.util.StringConverter;

import java.text.ParseException;

public class DoubleStringConverter extends StringConverter<Double> {
    @Override
    public String toString(Double value) {
        return Utils.formatter.format(value);
    }

    @Override
    public Double fromString(String string) {
        try {
            return Utils.formatter.parse(string).doubleValue();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}