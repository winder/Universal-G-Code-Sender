package com.willwinder.universalgcodesender.connection.xmodem;

import org.junit.Test;

import java.io.IOException;
import java.nio.BufferOverflowException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class RingBufferTest {

    @Test
    public void readShouldOnlyReadAvailableBytes() throws IOException {
        RingBuffer buffer = new RingBuffer(10);
        buffer.write(new byte[]{1, 2, 3});

        byte[] readBuffer = new byte[10];
        int read = buffer.read(readBuffer, 0, 10);

        assertEquals(3, read);
        assertEquals(0, buffer.available());

        assertEquals(1, readBuffer[0]);
        assertEquals(2, readBuffer[1]);
        assertEquals(3, readBuffer[2]);
    }

    @Test(expected = BufferOverflowException.class)
    public void writeShouldThrowBufferOverflowWhenNoMoreSpace() throws IOException {
        RingBuffer buffer = new RingBuffer(10);
        buffer.write(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11});
    }

    @Test
    public void writeShouldThrowBufferOverflowWhenNoMoreSpaceInMultipleCalls() throws IOException {
        RingBuffer buffer = new RingBuffer(10);

        byte[] readBuffer = new byte[10];
        buffer.write(new byte[]{1, 2, 3});
        buffer.read(readBuffer, 0, 2);
        assertEquals(1, buffer.available());

        buffer.write(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9});
        assertEquals(10, buffer.available());

        assertThrows(BufferOverflowException.class, () -> buffer.write(new byte[]{10}));
    }
}
