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
package com.willwinder.universalgcodesender.firmware.grblhal;

/**
 * An error or alarm code as reported by the enumeration commands $EE and $EA.
 *
 * @param code        the numeric code, ie {@code 1}
 * @param message     a short message for the code, ie {@code Hard limit}
 * @param description a long description of the cause. May be empty
 * @author Joacim Breiler
 */
public record GrblHalCode(String code, String message, String description) {
}
