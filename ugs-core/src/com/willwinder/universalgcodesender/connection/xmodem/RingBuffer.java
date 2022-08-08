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

import java.io.InputStream;
import java.nio.BufferOverflowException;

public class RingBuffer extends InputStream {

    private static final int DEFAULT_CAPACITY = 8;

    private final int capacity;
    private final byte[] data;
    private int writeSequence;
    private int readSequence;

    public RingBuffer(int capacity) {
        this.capacity = (capacity < 1) ? DEFAULT_CAPACITY : capacity;
        this.data = new byte[this.capacity];
        this.readSequence = 0;
        this.writeSequence = -1;
    }

    public int read() {
        if (!isEmpty()) {
            byte nextValue = data[readSequence % capacity];
            readSequence++;
            return nextValue;
        }

        return -1;
    }

    public void write(byte element) {
        if (isFull()) {
            throw new BufferOverflowException();
        }

        int nextWriteSeq = writeSequence + 1;
        data[nextWriteSeq % capacity] = element;
        writeSequence++;
    }

    public void write(byte[] buffer, int offset, int length) {
        for (int i = 0; i < length; i++) {
            write(buffer[offset + i]);
        }
    }

    public void write(byte[] buffer) {
        write(buffer, 0, buffer.length);
    }

    public int capacity() {
        return capacity;
    }

    @Override
    public int available() {
        return (writeSequence - readSequence) + 1;
    }

    public boolean isEmpty() {
        return writeSequence < readSequence;
    }

    public boolean isFull() {
        return available() >= capacity;
    }
}
