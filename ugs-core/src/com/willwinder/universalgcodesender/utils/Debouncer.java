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

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Coalesces a burst of calls into a single execution of a task on the given executor. While an
 * execution is pending, further calls are dropped; calls made while the task is running will
 * schedule a new execution.
 * <p>
 * Typical use is to keep a UI in sync with a rapidly changing model, such as the entities being
 * dragged in the designer, without running the update once per event:
 * <pre>
 * Debouncer refresh = new Debouncer(SwingUtilities::invokeLater, this::refreshValues);
 * </pre>
 */
public class Debouncer {
    private final Executor executor;
    private final Runnable task;
    private final AtomicBoolean scheduled = new AtomicBoolean(false);

    /**
     * Creates a debouncer that executes the given task on the given executor.
     *
     * @param executor the executor to run the task on, typically {@code SwingUtilities::invokeLater}
     *                 or {@code Platform::runLater}
     * @param task     the task to execute
     */
    public Debouncer(Executor executor, Runnable task) {
        this.executor = executor;
        this.task = task;
    }

    /**
     * Requests an execution of the task, unless one is already pending.
     */
    public void call() {
        if (!scheduled.compareAndSet(false, true)) {
            return;
        }

        executor.execute(() -> {
            scheduled.set(false);
            task.run();
        });
    }
}
