/*
    Copyright 2021-2023 Will Winder

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

/**
 * @author Joacim Breiler
 */
public enum TextFieldUnit {
    MM("mm"),
    INCH("\""),
    MM_PER_MINUTE("mm/min"),
    INCHES_PER_MINUTE("inch/min"),
    ROTATIONS_PER_MINUTE("rpm"),
    PERCENT("%"),
    DEGREE("°"),
    TIMES("times"),
    SECONDS("s");

    private final String abbreviation;

    TextFieldUnit(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return abbreviation;
    }
}
