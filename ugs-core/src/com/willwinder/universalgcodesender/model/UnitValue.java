/*
    Copyright 2026 Joacim Breiler

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

/**
 * Represents a value with a unit.
 *
 * @author Joacim Breiler
 */
public record UnitValue(Unit unit, Number value) {
    public Number convertTo(Unit unit) {
        return value().doubleValue() * this.unit.getInStandardUnit() / unit.getInStandardUnit();
    }

    public UnitValue invert() {
        return new UnitValue(unit, -value.doubleValue());
    }
}
