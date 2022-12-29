package com.willwinder.universalgcodesender.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CommandHistoryTest {
    @Test
    public void nextWithEmptyHistoryShouldReturnEmptyString() {
        CommandHistory commandHistory = new CommandHistory();
        assertEquals("", commandHistory.next());
    }

    @Test
    public void previousWithEmptyHistoryShouldReturnEmptyString() {
        CommandHistory commandHistory = new CommandHistory();
        assertEquals("", commandHistory.previous());
    }

    @Test
    public void nextAndPreviousShouldReturnCommandFromHistory() {
        CommandHistory commandHistory = new CommandHistory();
        commandHistory.add("test1");
        commandHistory.add("test2");
        commandHistory.add("test3");

        assertEquals("", commandHistory.next());
        assertEquals("test3", commandHistory.previous());
        assertEquals("test2", commandHistory.previous());
        assertEquals("test1", commandHistory.previous());
        assertEquals("test1", commandHistory.previous());
        assertEquals("test2", commandHistory.next());
        assertEquals("test3", commandHistory.next());
        assertEquals("", commandHistory.next());
    }

    @Test
    public void addShouldResetHistoryIndex() {
        CommandHistory commandHistory = new CommandHistory();
        commandHistory.add("test1");
        assertEquals("test1", commandHistory.previous());
        commandHistory.add("test2");
        assertEquals("test2", commandHistory.previous());
        commandHistory.add("test3");
        assertEquals("test3", commandHistory.previous());
    }
}
