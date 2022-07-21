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
package com.willwinder.universalgcodesender.connection;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AbstractConnectionTest {

    @Test
    public void trimEOFOnEmptyBuffer() {
        byte[] buffer = new byte[0];
        byte[] bytes = AbstractConnection.trimEOF(buffer);
        assertEquals(0, bytes.length);
    }

    @Test
    public void trimEOFWithoutEOF() {
        byte[] buffer = new byte[]{0x00, 0x01, 0x03};
        byte[] bytes = AbstractConnection.trimEOF(buffer);
        assertEquals(3, bytes.length);
    }

    @Test
    public void trimEOFOnBufferWithOneEOF() {
        byte[] buffer = new byte[]{0x1A};
        byte[] bytes = AbstractConnection.trimEOF(buffer);
        assertEquals(0, bytes.length);
    }

    @Test
    public void trimEOFOnBufferWithMultipleEOF() {
        byte[] buffer = new byte[]{0x1A, 0x1A, 0x1A};
        byte[] bytes = AbstractConnection.trimEOF(buffer);
        assertEquals(0, bytes.length);
    }

    @Test
    public void trimEOFOnlyTrailingEOF() {
        byte[] buffer = new byte[]{0x1A, 0x1A, 0x00, 0x1A};
        byte[] bytes = AbstractConnection.trimEOF(buffer);
        assertEquals(3, bytes.length);
    }

    @Test
    public void trimEOFAllTrailingEOF() {
        byte[] buffer = new byte[]{0x1A, 0x1A, 0x00, 0x1A, 0x1A, 0x1A, 0x1A};
        byte[] bytes = AbstractConnection.trimEOF(buffer);
        assertEquals(3, bytes.length);
    }
}
