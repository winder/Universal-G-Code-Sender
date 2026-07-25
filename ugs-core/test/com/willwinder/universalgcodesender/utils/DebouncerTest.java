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
package com.willwinder.universalgcodesender.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class DebouncerTest {
    private final List<Runnable> pendingTasks = new ArrayList<>();
    private final AtomicInteger executions = new AtomicInteger();

    @Test
    public void call_shouldNotRunTheTaskUntilTheExecutorRunsIt() {
        Debouncer debouncer = new Debouncer(pendingTasks::add, executions::incrementAndGet);

        debouncer.call();

        assertEquals(0, executions.get());
        assertEquals(1, pendingTasks.size());
    }

    @Test
    public void call_shouldCoalesceMultipleCallsIntoOneExecution() {
        Debouncer debouncer = new Debouncer(pendingTasks::add, executions::incrementAndGet);

        debouncer.call();
        debouncer.call();
        debouncer.call();
        runPendingTasks();

        assertEquals(1, executions.get());
    }

    @Test
    public void call_shouldScheduleANewExecutionAfterThePreviousHasRun() {
        Debouncer debouncer = new Debouncer(pendingTasks::add, executions::incrementAndGet);

        debouncer.call();
        runPendingTasks();
        debouncer.call();
        runPendingTasks();

        assertEquals(2, executions.get());
    }

    @Test
    public void call_shouldScheduleANewExecutionWhenCalledFromTheTask() {
        Debouncer[] debouncer = new Debouncer[1];
        debouncer[0] = new Debouncer(pendingTasks::add, () -> {
            if (executions.incrementAndGet() == 1) {
                debouncer[0].call();
            }
        });

        debouncer[0].call();
        runPendingTasks();
        runPendingTasks();

        assertEquals(2, executions.get());
    }

    private void runPendingTasks() {
        List<Runnable> tasks = new ArrayList<>(pendingTasks);
        pendingTasks.clear();
        tasks.forEach(Runnable::run);
    }
}
