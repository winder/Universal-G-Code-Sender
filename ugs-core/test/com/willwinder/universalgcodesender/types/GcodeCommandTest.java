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
package com.willwinder.universalgcodesender.types;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class GcodeCommandTest {

    @Test
    public void shouldNotifyListenerWhenCommandIsDone() throws InterruptedException {
        AtomicInteger timesListenerCalled = new AtomicInteger(0);
        GcodeCommand command = new GcodeCommand("TEST");
        command.addListener(c -> timesListenerCalled.incrementAndGet());

        command.setOk(true);
        assertEquals(0, timesListenerCalled.get());

        command.setOk(false);
        assertEquals(0, timesListenerCalled.get());

        command.setSent(true);
        assertEquals(0, timesListenerCalled.get());

        command.setSent(false);
        assertEquals(0, timesListenerCalled.get());

        command.setSkipped(true);
        assertEquals(0, timesListenerCalled.get());

        command.setSkipped(false);
        assertEquals(0, timesListenerCalled.get());

        command.setError(false);
        assertEquals(0, timesListenerCalled.get());

        command.setError(true);
        assertEquals(0, timesListenerCalled.get());

        command.setDone(false);
        assertEquals(0, timesListenerCalled.get());

        command.setDone(true);

        Thread.sleep(100);
        assertEquals(1, timesListenerCalled.get());
    }

    @Test
    public void shouldNotifyListenerInOwnThread() throws InterruptedException {
        final long threadId = Thread.currentThread().getId();
        AtomicInteger timesListenerCalled = new AtomicInteger(0);
        GcodeCommand command = new GcodeCommand("TEST");
        command.addListener(c -> {
            if(threadId != Thread.currentThread().getId()) {
                timesListenerCalled.incrementAndGet();
            }
        });

        command.setDone(true);

        Thread.sleep(100);
        assertEquals(1, timesListenerCalled.get());
    }

    @Test
    public void shouldOnlyNotifyListenersOnceWhenDone() throws InterruptedException {
        AtomicInteger timesListenerCalled = new AtomicInteger(0);
        GcodeCommand command = new GcodeCommand("TEST");
        command.addListener(c -> timesListenerCalled.incrementAndGet());

        command.setDone(true);
        Thread.sleep(100);
        assertEquals(1, timesListenerCalled.get());

        command.setDone(true);
        Thread.sleep(100);
        assertEquals(1, timesListenerCalled.get());
    }

    @Test
    public void shouldNotifyAllListenersOnceWhenDone() throws InterruptedException {
        AtomicInteger timesListenerCalled = new AtomicInteger(0);
        GcodeCommand command = new GcodeCommand("TEST");
        command.addListener(c -> timesListenerCalled.incrementAndGet());
        command.addListener(c -> timesListenerCalled.incrementAndGet());

        command.setDone(true);
        Thread.sleep(100);
        assertEquals(2, timesListenerCalled.get());
    }
}
