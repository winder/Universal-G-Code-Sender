/*
    Copyright 2022 Will Winder

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
package com.willwinder.universalgcodesender.connection.xmodem;

import java.util.Arrays;

/**
 * @author Joacim Breiler
 */
public class XModemUtils {
    public static final int EOF = 0x1A;

    /**
     * Trims any trailing EOF bytes from the byte buffer
     *
     * @param buffer a byte array buffer that should be trimmed
     * @return a trimmed byte buffer
     */
    public static byte[] trimEOF(byte[] buffer) {
        // Trim any trailing EOF
        int i = buffer.length - 1;
        while (i >= 0 && buffer[i] == EOF) {
            i--;
        }

        return Arrays.copyOfRange(buffer, 0, i + 1);
    }
}
