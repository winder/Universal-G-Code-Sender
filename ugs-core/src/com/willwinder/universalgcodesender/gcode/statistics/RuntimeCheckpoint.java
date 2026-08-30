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
package com.willwinder.universalgcodesender.gcode.statistics;

/**
 * How far into a program the machine is expected to be once a given row has been reached. A
 * program is described by a handful of these, spread evenly over its estimated runtime, which is
 * enough to tell how far off an estimate is while a program is running without having to remember
 * anything about the individual rows.
 *
 * @param row           the row of the program
 * @param elapsedMillis the estimated time in milliseconds it takes to reach that row
 * @author Joacim Breiler
 */
public record RuntimeCheckpoint(int row, long elapsedMillis) {
}
