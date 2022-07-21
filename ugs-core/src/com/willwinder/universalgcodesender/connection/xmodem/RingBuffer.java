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

import java.io.IOException;
import java.io.InputStream;

/**
 * A simple ring buffer borrowed from here:
 * https://en.wikibooks.org/wiki/Serial_Programming/Serial_Java#A_simple,_thread-safe_Ring_Buffer_Implementation
 */
public class RingBuffer extends InputStream {

    /**
     * internal buffer to hold the data
     **/
    private final byte[] buffer;

    /**
     * size of the buffer
     **/
    private final int size;

    /**
     * current start of data area
     **/
    private int start;

    /**
     * current end of data area
     **/
    private int end;

    /**
     * Construct a RingBuffer with a certain buffer size.
     *
     * @param size Buffer size in bytes
     */
    public RingBuffer(int size) {
        this.size = size;
        buffer = new byte[size];
        clear();
    }

    /**
     * Clear the buffer contents. All data still in the buffer is lost.
     */
    public void clear() {
        // Just reset the pointers. The remaining data fragments, if any,
        // will be overwritten during normal operation.
        start = end = 0;
    }

    /**
     * Write as much data as possible to the buffer.
     *
     * @param data Data to be written
     * @return Amount of data actually written
     */
    public int write(byte[] data) {
        return write(data, 0, data.length);
    }

    /**
     * Write as much data as possible to the buffer.
     *
     * @param data   Array holding data to be written
     * @param off    Offset of data in array
     * @param length Amount of data to write, starting from <code>off</code>.
     * @return Amount of data actually written
     */
    public int write(byte[] data, int off, int length) {
        if (length <= 0) return 0;
        int remain = length;

        int i = Math.min(remain, (end < start ? start : buffer.length) - end);
        if (i > 0) {
            System.arraycopy(data, off, buffer, end, i);
            off += i;
            remain -= i;
            end += i;
        }

        i = Math.min(remain, end >= start ? start : 0);
        if (i > 0) {
            System.arraycopy(data, off, buffer, 0, i);
            remain -= i;
            end = i;
        }
        return length - remain;
    }

    @Override
    public int read() throws IOException {
        if (available() == 0) {
            return -1;
        }
        byte[] buffer = new byte[1];
        int bytesRead = read(buffer);
        if (bytesRead == 0) {
            return -1;
        }
        return buffer[0];
    }

    @Override
    public int available() throws IOException {
        return start <= end
                ? end - start
                : end - start + size;
    }

    @Override
    public int read(byte[] data) {
        return read(data, 0, data.length);
    }

    @Override
    public int read(byte[] data, int off, int n) {
        if (n <= 0) return 0;
        int remain = n;
        // @todo check if off is valid: 0= <= off < data.length; throw exception if not

        int i = Math.min(remain, (end < start ? buffer.length : end) - start);
        if (i > 0) {
            System.arraycopy(buffer, start, data, off, i);
            off += i;
            remain -= i;
            start += i;
            if (start >= buffer.length) {
                start = 0;
            }
        }

        i = Math.min(remain, end >= start ? 0 : end);
        if (i > 0) {
            System.arraycopy(buffer, 0, data, off, i);
            remain -= i;
            start = i;
        }
        return n - remain;
    }
}
