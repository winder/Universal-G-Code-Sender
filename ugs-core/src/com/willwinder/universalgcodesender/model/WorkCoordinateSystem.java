/*
    Copyright 2017 Will Winder

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
 *
 * @author wwinder
 */
public enum WorkCoordinateSystem {
    G54(1),
    G55(2),
    G56(3),
    G57(4),
    G58(5),
    G59(6);

    final int pValue;
    private WorkCoordinateSystem(int pValue) {
        this.pValue = pValue;
    }
    
    public int getPValue() {
        return pValue;
    }
}
