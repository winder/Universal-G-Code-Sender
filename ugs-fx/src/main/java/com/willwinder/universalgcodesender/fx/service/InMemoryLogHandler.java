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
package com.willwinder.universalgcodesender.fx.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * A log handler that keeps the most recent log records in memory so they can be shown to the
 * user from within the application, for example in the about dialog. Only the latest
 * {@link #getMaxEntries()} entries are retained; older entries are discarded as new ones arrive.
 *
 * <p>Listeners are notified on the thread that emitted the log record, so UI listeners must
 * hand the entry over to the JavaFX application thread themselves.
 */
public class InMemoryLogHandler extends Handler {
    private static final int DEFAULT_MAX_ENTRIES = 5000;
    private static InMemoryLogHandler instance;

    private final Deque<String> entries = new ArrayDeque<>();
    private final List<Consumer<String>> listeners = new CopyOnWriteArrayList<>();
    private final int maxEntries;

    public InMemoryLogHandler() {
        this(DEFAULT_MAX_ENTRIES);
    }

    public InMemoryLogHandler(int maxEntries) {
        this.maxEntries = maxEntries;
        setLevel(Level.ALL);
        setFormatter(new SingleLineFormatter());
    }

    public static synchronized InMemoryLogHandler getInstance() {
        if (instance == null) {
            instance = new InMemoryLogHandler();
        }
        return instance;
    }

    /**
     * Attaches the shared handler to the root logger so that every log record emitted by the
     * application is captured. Calling it more than once has no effect.
     */
    public static synchronized void install() {
        Logger rootLogger = Logger.getLogger("");
        InMemoryLogHandler handler = getInstance();
        for (Handler existing : rootLogger.getHandlers()) {
            if (existing == handler) {
                return;
            }
        }
        rootLogger.addHandler(handler);
    }

    @Override
    public void publish(LogRecord record) {
        if (record == null || !isLoggable(record)) {
            return;
        }

        String entry = getFormatter().format(record);
        synchronized (entries) {
            entries.addLast(entry);
            while (entries.size() > maxEntries) {
                entries.removeFirst();
            }
        }
        listeners.forEach(listener -> listener.accept(entry));
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
        clear();
        listeners.clear();
    }

    public int getMaxEntries() {
        return maxEntries;
    }

    public List<String> getEntries() {
        synchronized (entries) {
            return new ArrayList<>(entries);
        }
    }

    public String getLogText() {
        return String.join("", getEntries());
    }

    public void clear() {
        synchronized (entries) {
            entries.clear();
        }
    }

    public void addListener(Consumer<String> listener) {
        listeners.add(listener);
    }

    public void removeListener(Consumer<String> listener) {
        listeners.remove(listener);
    }

    private static class SingleLineFormatter extends Formatter {
        private static final String FORMAT = "%1$tF %1$tT.%1$tL %2$-7s [%3$s] %4$s%5$s%n";

        @Override
        public String format(LogRecord record) {
            return String.format(FORMAT,
                    record.getInstant().toEpochMilli(),
                    record.getLevel().getName(),
                    record.getLoggerName(),
                    formatMessage(record),
                    formatThrowable(record));
        }

        private String formatThrowable(LogRecord record) {
            if (record.getThrown() == null) {
                return "";
            }
            StringWriter stringWriter = new StringWriter();
            try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
                printWriter.println();
                record.getThrown().printStackTrace(printWriter);
            }
            return stringWriter.toString().stripTrailing();
        }
    }
}
