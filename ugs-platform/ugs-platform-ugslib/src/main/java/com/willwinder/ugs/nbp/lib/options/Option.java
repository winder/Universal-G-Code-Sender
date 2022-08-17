/*
    Copyright 2016-2022 Will Winder

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
package com.willwinder.ugs.nbp.lib.options;

import com.willwinder.universalgcodesender.i18n.Language;

/**
 * @author wwinder
 */
public class Option<T> {
    public String option;
    public String localized;
    public String description;
    public T value;

    public Option(String name, String l, String d) {
        option = name;
        localized = l;
        description = d;
    }

    public Option(String name, String l, String d, T v) {
        option = name;
        localized = l;
        description = d;
        value = v;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T v) {
        if (v.getClass() == Language.class) {
            System.out.println("What?");
        }
        value = v;
    }

    public void setRawValue(Object value) {
        this.value = (T) value;
    }

    public String getDescription() {
        return description;
    }
}
