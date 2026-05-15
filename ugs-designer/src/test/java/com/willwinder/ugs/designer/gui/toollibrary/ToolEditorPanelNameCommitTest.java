/*
    Copyright 2026 Damian Nikodem

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
package com.willwinder.ugs.designer.gui.toollibrary;

import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JTextField;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Regression guard for the Tool Library name-field focus-loss bug. Typing into the Name field used
 * to commit on every keystroke via a DocumentListener, which triggered a refresh cycle that
 * disabled the field mid-edit and kicked focus to the Shape combo. The fix moves commit to
 * Enter / focus-lost so the field stays focused while the user types.
 */
public class ToolEditorPanelNameCommitTest {

    private ToolEditorPanel panel;
    private JTextField nameField;
    private AtomicInteger fireCount;
    private AtomicReference<ToolDefinition> lastEdit;

    @Before
    public void setUp() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        panel = new ToolEditorPanel(UnitUtils.Units.MM);
        nameField = readField("nameField");

        fireCount = new AtomicInteger();
        lastEdit = new AtomicReference<>();
        panel.setChangeListener(t -> {
            fireCount.incrementAndGet();
            lastEdit.set(t);
        });

        ToolDefinition tool = new ToolDefinition();
        tool.setName("Initial");
        tool.setShape(EndmillShape.UPCUT);
        tool.setDiameter(3.0);
        tool.setDiameterUnit(UnitUtils.Units.MM);
        tool.setFeedSpeed(900);
        tool.setPlungeSpeed(300);
        tool.setDepthPerPass(1.0);
        tool.setStepOverPercent(0.4);
        tool.setMaxSpindleSpeed(18000);
        panel.setTool(tool, false);
        fireCount.set(0);
        lastEdit.set(null);
    }

    @Test
    public void typingDoesNotFireChange() throws Exception {
        nameField.getDocument().insertString(0, "A", null);
        nameField.getDocument().insertString(1, "B", null);
        nameField.getDocument().insertString(2, "C", null);
        assertEquals("Typing into the name field must not commit per keystroke.",
                0, fireCount.get());
    }

    @Test
    public void enterCommitsName() {
        nameField.setText("Renamed");
        nameField.getActionListeners()[0].actionPerformed(
                new ActionEvent(nameField, ActionEvent.ACTION_PERFORMED, ""));
        assertEquals(1, fireCount.get());
        assertNotNull(lastEdit.get());
        assertEquals("Renamed", lastEdit.get().getName());
    }

    @Test
    public void focusLostCommitsName() {
        nameField.setText("AfterTab");
        FocusEvent event = new FocusEvent(nameField, FocusEvent.FOCUS_LOST);
        for (FocusListener listener : nameField.getFocusListeners()) {
            if (listener instanceof FocusAdapter) {
                listener.focusLost(event);
            }
        }
        assertEquals(1, fireCount.get());
        assertNotNull(lastEdit.get());
        assertEquals("AfterTab", lastEdit.get().getName());
    }

    @SuppressWarnings("unchecked")
    private <T> T readField(String name) throws Exception {
        Field field = ToolEditorPanel.class.getDeclaredField(name);
        field.setAccessible(true);
        return (T) field.get(panel);
    }
}
