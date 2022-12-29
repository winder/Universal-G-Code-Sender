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
package com.willwinder.ugs.nbp.console;

import com.willwinder.universalgcodesender.utils.CommandHistory;
import org.apache.commons.lang3.StringUtils;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.function.Consumer;

/**
 * @author Joacim Breiler
 */
public class ConsoleKeyListener implements KeyListener {
    private final JTextComponent textComponent;
    private final Consumer<String> sendCommand;
    private final CommandHistory commandHistory = new CommandHistory();

    public ConsoleKeyListener(JTextComponent textComponent, Consumer<String> sendCommand) {
        this.textComponent = textComponent;
        this.sendCommand = sendCommand;
    }

    private boolean isArrowKey(KeyEvent e) {
        return e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN;
    }

    public String getCurrentLine() {
        int position = textComponent.getText().length();
        try {
            int start = Utilities.getRowStart(textComponent, position);
            int end = Utilities.getRowEnd(textComponent, position);
            return StringUtils.trimToEmpty(textComponent.getText(start, end - start));
        } catch (BadLocationException e) {
            return "";
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not needed
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            String currentLine = getCurrentLine();
            addToHistory(currentLine);
            sendCommand.accept(currentLine);
        } else if (isArrowKey(e)) {
            replaceTextFromHistory(e.getKeyCode());
            e.consume();
        }
    }

    private void addToHistory(String currentLine) {
        commandHistory.add(currentLine.trim());
    }

    private void replaceTextFromHistory(int keyCode) {
        if (keyCode == KeyEvent.VK_UP) {
            textComponent.setText(commandHistory.previous());
        } else if (keyCode == KeyEvent.VK_DOWN) {
            textComponent.setText(commandHistory.next());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Not needed
    }
}
