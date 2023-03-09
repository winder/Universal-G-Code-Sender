package com.willwinder.universalgcodesender.services;

/*
    Copyright 2023 Will Winder

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

import com.willwinder.universalgcodesender.utils.GUIHelpers;

import javax.swing.KeyStroke;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static java.awt.event.KeyEvent.VK_ALT;
import static java.awt.event.KeyEvent.VK_CONTROL;
import static java.awt.event.KeyEvent.VK_META;
import static java.awt.event.KeyEvent.VK_SHIFT;

/**
 * A service for dispatching key events to the host OS.
 *
 * @author Joacim Breiler
 */
public class KeyboardService {
    private static final Logger LOGGER = Logger.getLogger(KeyboardService.class.getSimpleName());
    private static final KeyboardService instance = new KeyboardService();

    private final Executor executor = Executors.newSingleThreadExecutor();

    private KeyboardService() {
    }

    public static KeyboardService getInstance() {
        return instance;
    }

    private void dispatchKeyPressEvents(List<String> keyPressList) {
        LOGGER.info(() -> String.format("Sending keys '%s'", String.join(",", keyPressList)));
        try {
            Robot robot = new Robot();
            robot.setAutoWaitForIdle(true);
            keyPressList.forEach(keyPress -> {
                KeyStroke keyStroke = KeyStroke.getKeyStroke(keyPress);
                if (keyStroke == null) {
                    GUIHelpers.displayErrorDialog("Could not generate a key press event for key: " + keyPress);
                    return;
                }

                boolean isShift = (keyStroke.getModifiers() & InputEvent.SHIFT_DOWN_MASK) != 0;
                boolean isControl = (keyStroke.getModifiers() & InputEvent.CTRL_DOWN_MASK) != 0;
                boolean isMeta = (keyStroke.getModifiers() & InputEvent.META_DOWN_MASK) != 0;
                boolean isAlt = (keyStroke.getModifiers() & InputEvent.ALT_DOWN_MASK) != 0;

                if (isShift) {
                    robot.keyPress(VK_SHIFT);
                }

                if (isControl) {
                    robot.keyPress(VK_CONTROL);
                }

                if (isAlt) {
                    robot.keyPress(VK_ALT);
                }

                if (isMeta) {
                    robot.keyPress(VK_META);
                }

                robot.keyPress(keyStroke.getKeyCode());
                robot.keyRelease(keyStroke.getKeyCode());

                if (isShift) {
                    robot.keyRelease(VK_SHIFT);
                }

                if (isControl) {
                    robot.keyRelease(VK_CONTROL);
                }

                if (isAlt) {
                    robot.keyRelease(VK_ALT);
                }

                if (isMeta) {
                    robot.keyRelease(VK_META);
                }
            });
        } catch (AWTException e) {
            GUIHelpers.displayErrorDialog("Could not generate key press event", true);
        }
    }

    /**
     * Dispatch keyboard press events on the host system. Keys are defined using AWT format, see {@link KeyStroke#getKeyStroke(String)}
     *
     * @param keyPressList a list of one or multiple key presses
     */
    public void sendKeys(List<String> keyPressList) {
        executor.execute(() -> dispatchKeyPressEvents(keyPressList));
    }
}
