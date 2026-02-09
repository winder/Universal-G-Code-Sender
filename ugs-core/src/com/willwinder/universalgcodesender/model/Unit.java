/*
    Copyright 2021-2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.model;

import com.willwinder.universalgcodesender.i18n.Localization;

/**
 * Describes different units of measurement and their conversion
 * factors in respect to the SI units.
 *
 * @author Joacim Breiler
 */
public enum Unit {
    MM("mm", Localization.getString("millimeters"), 0.001),
    INCH("in", Localization.getString("inch"), 0.0254),
    FEET("ft", Localization.getString("feet"), INCH.inStandardUnit * 12),
    METERS_PER_SECOND("m/sec", Localization.getString("metersPerSecond"), 1),
    MM_PER_SECOND("mm/sec", Localization.getString("millimetersPerSecond"), MM.inStandardUnit),
    MM_PER_MINUTE("mm/min", Localization.getString("millimetersPerMinute"), MM.inStandardUnit / 60.0),
    INCHES_PER_MINUTE("inch/min", Localization.getString("inchesPerMinute"), INCH.inStandardUnit / 60.0),
    REVOLUTIONS_PER_MINUTE("rpm", Localization.getString("revolutionsPerMinute"), 1.0 / 60.0),
    PERCENT("%", Localization.getString("percent"), 1.0),
    DEGREE("°", Localization.getString("degrees"), 1.0),
    TIMES("times", Localization.getString("times"), 1.0),
    SECONDS("s", Localization.getString("seconds"), 1.0),
    MILLISECONDS("ms", Localization.getString("milliseconds") , 0.001 ),
    MINUTES("m", Localization.getString("minutes"), 1d/60d);

    private final String abbreviation;
    private final String name;
    private final double inStandardUnit;

    Unit(String abbreviation, String name, double standardUnit) {
        this.abbreviation = abbreviation;
        this.name = name;
        this.inStandardUnit = standardUnit;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public String getName() {
        return name;
    }

    public double getInStandardUnit() {
        return inStandardUnit;
    }

    public double convertTo(Unit unit) {
        return this.inStandardUnit / unit.inStandardUnit;
    }
}
