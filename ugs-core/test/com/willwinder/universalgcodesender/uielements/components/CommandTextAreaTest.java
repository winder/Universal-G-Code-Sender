/*
    Copyright 2016 Will Winder

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
package com.willwinder.universalgcodesender.uielements.components;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 *
 * @author wwinder
 */
public class CommandTextAreaTest {
    
    CommandTextArea cta;
    BackendAPI backend = EasyMock.mock(BackendAPI.class);
    private static int[] directions = {
        KeyEvent.VK_UP,
        KeyEvent.VK_DOWN,
        KeyEvent.VK_LEFT,
        KeyEvent.VK_RIGHT
    };

    @BeforeClass
    static public void testSetup() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        // Initialize private variable.
        Field f = GUIHelpers.class.getDeclaredField("unitTestMode");
        f.setAccessible(true);
        f.set(null, true);
    }

    @AfterClass
    static public void testTeardown() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        // Initialize private variable.
        Field f = GUIHelpers.class.getDeclaredField("unitTestMode");
        f.setAccessible(true);
        f.set(null, false);
    }

    @Before
    public void setup() {
        cta = new CommandTextArea(backend);
        cta.focusNotNeeded = true;
        EasyMock.reset(backend);
    }

    @After
    public void teardown() {
        cta = null;
    }

    private static void sendArrowEvent(int e, CommandTextArea source) {
        switch (e) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_RIGHT:
                KeyEvent key = new KeyEvent(
                        source,
                        KeyEvent.KEY_PRESSED,
                        System.currentTimeMillis(),
                        0,
                        e,
                        'Z');

                source.dispatchKeyEvent(key);
        }
    }

    /**
     * Verify that the arrow keys don't do anything when there are no commands,
     * and when you try to go past the last commands.
     */
    @Test
    public void noCommands() throws InterruptedException {
        // No commands should be sent.
        EasyMock.replay(backend);

        for (int dir : directions) {
            sendArrowEvent(dir, cta);
            assertEquals("", cta.getText());
        }

        EasyMock.verify(backend);
    }

    /**
     * Send one command.
     * @throws Exception 
     */
    @Test
    public void commandAction() throws Exception {
        String command = "some-command";
        backend.sendGcodeCommand(command);
        EasyMock.expect(EasyMock.expectLastCall()).once();
        EasyMock.replay(backend);

        cta.setText(command);
        cta.action(null);

        assertEquals("", cta.getText());

        EasyMock.verify(backend);
    }

    /**
     * Send 10 commands, verify history works.
     * @throws Exception 
     */
    @Test
    public void multipleCommands() throws Exception {
        int num = 10;
        for (int i = 0 ; i < num; i++) {
            String command = "" + i;
            backend.sendGcodeCommand(command);
            EasyMock.expect(EasyMock.expectLastCall()).once();
        }

        cta.hasFocus();
        EasyMock.replay(backend);

        for (int i = 0; i < num; i++) {
            cta.setText("" + i);
            cta.action(null);
        }
        
        assertEquals("", cta.getText());

        // Go all the way back.
        for (int i = 0; i < num; i++) {
            sendArrowEvent(KeyEvent.VK_UP, cta);
            assertEquals("" + (num-1-i), cta.getText());
        }

        // Can't go past the end.
        sendArrowEvent(KeyEvent.VK_UP, cta);
        assertEquals("0", cta.getText());
        sendArrowEvent(KeyEvent.VK_UP, cta);
        assertEquals("0", cta.getText());

        // Go back to the front.
        for (int i = 8; i >= 0; i--) {
            sendArrowEvent(KeyEvent.VK_DOWN, cta);
            assertEquals("" + (num-1-i), cta.getText());
        }

        // Go back to an empty command.
        sendArrowEvent(KeyEvent.VK_DOWN, cta);
        assertEquals("", cta.getText());

        EasyMock.verify(backend);
    }
}
