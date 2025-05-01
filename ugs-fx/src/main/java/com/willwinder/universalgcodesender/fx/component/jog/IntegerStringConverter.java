package com.willwinder.universalgcodesender.fx.component.jog;

import com.willwinder.universalgcodesender.Utils;
import javafx.util.StringConverter;

import java.text.ParseException;

public class IntegerStringConverter extends StringConverter<Integer> {
    @Override
    public String toString(Integer value) {
        return Utils.formatter.format(value);
    }

    @Override
    public Integer fromString(String string) {
        try {
            return Utils.formatter.parse(string).intValue();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}