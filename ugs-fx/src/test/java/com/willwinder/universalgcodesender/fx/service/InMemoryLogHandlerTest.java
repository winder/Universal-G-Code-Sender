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

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.assertj.core.api.Assertions.assertThat;

public class InMemoryLogHandlerTest {

    @Test
    public void publish_shouldStoreFormattedEntry() {
        InMemoryLogHandler handler = new InMemoryLogHandler(10);
        LogRecord record = new LogRecord(Level.INFO, "Hello {0}");
        record.setLoggerName("test.logger");
        record.setParameters(new Object[]{"world"});

        handler.publish(record);

        assertThat(handler.getEntries()).hasSize(1);
        assertThat(handler.getEntries().getFirst())
                .contains("INFO")
                .contains("[test.logger]")
                .contains("Hello world")
                .endsWith(System.lineSeparator());
    }

    @Test
    public void publish_shouldIncludeStackTraceWhenRecordHasThrowable() {
        InMemoryLogHandler handler = new InMemoryLogHandler(10);
        LogRecord record = new LogRecord(Level.SEVERE, "Failed");
        record.setThrown(new IllegalStateException("boom"));

        handler.publish(record);

        assertThat(handler.getLogText())
                .contains("Failed")
                .contains("java.lang.IllegalStateException: boom")
                .contains("InMemoryLogHandlerTest");
    }

    @Test
    public void publish_shouldDropOldestEntriesWhenExceedingMaxEntries() {
        InMemoryLogHandler handler = new InMemoryLogHandler(2);

        handler.publish(new LogRecord(Level.INFO, "first"));
        handler.publish(new LogRecord(Level.INFO, "second"));
        handler.publish(new LogRecord(Level.INFO, "third"));

        assertThat(handler.getEntries()).hasSize(2);
        assertThat(handler.getLogText())
                .doesNotContain("first")
                .contains("second")
                .contains("third");
    }

    @Test
    public void publish_shouldNotifyListeners() {
        InMemoryLogHandler handler = new InMemoryLogHandler(10);
        List<String> received = new ArrayList<>();
        handler.addListener(received::add);

        handler.publish(new LogRecord(Level.WARNING, "Careful"));

        assertThat(received).hasSize(1);
        assertThat(received.getFirst()).contains("WARNING").contains("Careful");
    }

    @Test
    public void publish_shouldNotNotifyRemovedListeners() {
        InMemoryLogHandler handler = new InMemoryLogHandler(10);
        List<String> received = new ArrayList<>();
        handler.addListener(received::add);
        handler.removeListener(received::add);

        handler.publish(new LogRecord(Level.WARNING, "Careful"));

        assertThat(handler.getEntries()).hasSize(1);
    }

    @Test
    public void publish_shouldIgnoreRecordsBelowHandlerLevel() {
        InMemoryLogHandler handler = new InMemoryLogHandler(10);
        handler.setLevel(Level.WARNING);

        handler.publish(new LogRecord(Level.INFO, "ignored"));

        assertThat(handler.getEntries()).isEmpty();
    }

    @Test
    public void clear_shouldRemoveAllEntries() {
        InMemoryLogHandler handler = new InMemoryLogHandler(10);
        handler.publish(new LogRecord(Level.INFO, "first"));

        handler.clear();

        assertThat(handler.getEntries()).isEmpty();
        assertThat(handler.getLogText()).isEmpty();
    }
}
