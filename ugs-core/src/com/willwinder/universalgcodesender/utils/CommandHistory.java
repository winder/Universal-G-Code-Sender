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
package com.willwinder.universalgcodesender.utils;

import org.apache.commons.lang3.StringUtils;

import java.awt.Toolkit;
import java.util.LinkedList;

/**
 * A class responsible for keeping a command history that can be browsed using next/previous.
 * When a new command is added to the command history it will reset to the latest item.
 *
 * @author Joacim Breiler
 */
public class CommandHistory {
    private static final int MAX_COMMAND_HISTORY = 20;
    private final LinkedList<String> history = new LinkedList<>();
    private int currentCommandIndex = -1;

    public void add(String command) {
        currentCommandIndex = -1;

        boolean isDuplicateCommand = StringUtils.equalsIgnoreCase(getLatestCommand(), command);
        boolean isCommandEmpty = StringUtils.isEmpty(StringUtils.trimToEmpty(command));
        if (isCommandEmpty || isDuplicateCommand) {
            return;
        }

        history.addFirst(command);
        while (history.size() > MAX_COMMAND_HISTORY) {
            history.removeLast();
        }
    }

    public String next() {
        currentCommandIndex--;
        if (currentCommandIndex < 0) {
            currentCommandIndex = -1;
            Toolkit.getDefaultToolkit().beep();
        }

        return getCommand();
    }

    public String previous() {
        currentCommandIndex++;
        if (currentCommandIndex > (history.size() - 1)) {
            currentCommandIndex = history.size() - 1;
            Toolkit.getDefaultToolkit().beep();
        }

        return getCommand();
    }

    private String getLatestCommand() {
        if (history.isEmpty()) {
            return "";
        }

        return history.peekFirst();
    }

    private String getCommand() {
        if (currentCommandIndex < 0) {
            return "";
        }
        return history.get(currentCommandIndex);
    }
}
