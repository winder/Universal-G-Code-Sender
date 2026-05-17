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

import com.willwinder.ugs.designer.logic.ToolLibraryService;
import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.uielements.TextFieldWithUnit;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.text.ParseException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

/**
 * Visual end-to-end test for the Tool Library dialog. The dialog is shown in a real visible
 * window; keystrokes are simulated by dispatching synthetic {@link KeyEvent}s (KEY_PRESSED /
 * KEY_TYPED / KEY_RELEASED) directly to the focused component. This exercises the same Swing
 * event pipeline a real key press goes through (Component.dispatchEvent -> processKeyEvent ->
 * keymap action -> document insert), but without depending on OS-level keyboard focus, which is
 * unreliable when surefire launches the JVM in the background on macOS.
 *
 * <p>If the original per-keystroke focus-loss bug were still present, the first KEY_TYPED would
 * trigger the refresh-disable cycle, disable the JTextField mid-input, and the remaining
 * characters would not be inserted (a disabled JTextField drops key events). The test would then
 * see truncated text.
 *
 * <p>Skipped when {@link GraphicsEnvironment#isHeadless()} returns true.
 */
public class ToolLibraryDialogRenameVisualTest {

    private static final long DIALOG_VISIBLE_TIMEOUT_MS = 5_000;

    @Rule
    public TemporaryFolder temp = new TemporaryFolder();

    private JFrame ownerFrame;
    private ToolLibraryDialog dialog;
    private ToolLibraryService service;
    private String seedToolId;

    @Before
    public void setUp() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Path libraryPath = temp.newFolder().toPath().resolve("tools.json");
        LookupService.remove(ToolLibraryService.class);
        service = new ToolLibraryService(libraryPath);
        LookupService.register(service);
        seedToolId = service.addTool(buildSeed()).getId();

        SwingUtilities.invokeAndWait(() -> {
            ownerFrame = new JFrame("Visual test owner");
            ownerFrame.setSize(100, 100);
            ownerFrame.setLocation(50, 50);
            ownerFrame.setVisible(true);
            dialog = new ToolLibraryDialog(ownerFrame, service, UnitUtils.Units.MM);
        });

        Thread modalThread = new Thread(() -> dialog.setVisible(true), "modal-dialog-visual-test");
        modalThread.setDaemon(true);
        modalThread.start();

        long deadline = System.currentTimeMillis() + DIALOG_VISIBLE_TIMEOUT_MS;
        while (!dialog.isShowing() && System.currentTimeMillis() < deadline) {
            Thread.sleep(50);
        }
        if (!dialog.isShowing()) {
            fail("Dialog never became visible within " + DIALOG_VISIBLE_TIMEOUT_MS + "ms");
        }
        SwingUtilities.invokeAndWait(() -> {
            dialog.setLocation(100, 100);
            dialog.toFront();
        });
        flushEdt();
    }

    @After
    public void tearDown() throws Exception {
        if (dialog != null) {
            SwingUtilities.invokeAndWait(() -> dialog.dispose());
        }
        if (ownerFrame != null) {
            SwingUtilities.invokeAndWait(() -> ownerFrame.dispose());
        }
        LookupService.remove(ToolLibraryService.class);
    }

    @Test
    public void renamingViaKeystrokesUpdatesListLabelAndPreservesText() throws Exception {
        @SuppressWarnings("unchecked")
        JList<ToolDefinition> list = (JList<ToolDefinition>) readField(dialog, "toolList");
        ToolEditorPanel editor = (ToolEditorPanel) readField(dialog, "editorPanel");
        JTextField nameField = (JTextField) readField(editor, "nameField");

        selectSeedTool(list);

        SwingUtilities.invokeAndWait(() -> {
            nameField.requestFocusInWindow();
            nameField.selectAll();
        });
        flushEdt();

        typeString(nameField, "Renamed");

        // If the per-keystroke refresh cycle were still firing, the first KEY_TYPED would
        // disable nameField, the remaining 6 characters would be dropped, and getText() would
        // be "R" (or some partial prefix). This assertion is the regression check.
        SwingUtilities.invokeAndWait(() ->
                assertEquals("All typed characters must land in the name field.",
                        "Renamed", nameField.getText()));

        commitField(nameField);
        drainDeferred();

        SwingUtilities.invokeAndWait(() -> {
            int idx = list.getSelectedIndex();
            assertNotEquals("Selection should remain valid after commit.", -1, idx);
            assertEquals("List row label should reflect the renamed tool.",
                    "Renamed", list.getModel().getElementAt(idx).getName());
        });
        assertEquals("Service should persist the renamed tool.",
                "Renamed", reloadSeed().getName());
    }

    @Test
    public void editingMultipleFieldsPersistsAllValues() throws Exception {
        @SuppressWarnings("unchecked")
        JList<ToolDefinition> list = (JList<ToolDefinition>) readField(dialog, "toolList");
        ToolEditorPanel editor = (ToolEditorPanel) readField(dialog, "editorPanel");
        JTextField nameField = (JTextField) readField(editor, "nameField");

        selectSeedTool(list);

        TextFieldWithUnit diameterField = (TextFieldWithUnit) readField(editor, "diameterField");
        TextFieldWithUnit feedField = (TextFieldWithUnit) readField(editor, "feedField");

        editField(nameField, "MultiEdit");
        editField(diameterField, "4.5");
        editField(feedField, "1200");

        // Move focus off the last edited field so JFormattedTextField commits via focusLost.
        SwingUtilities.invokeAndWait(() -> dialog.getRootPane().requestFocusInWindow());
        drainDeferred();

        ToolDefinition persisted = reloadSeed();
        assertEquals("Name should persist after multi-field edit.",
                "MultiEdit", persisted.getName());
        assertEquals("Diameter should persist.", 4.5, persisted.getDiameter(), 1e-6);
        assertEquals("Feed speed should persist.", 1200, persisted.getFeedSpeed());
        SwingUtilities.invokeAndWait(() -> {
            int idx = list.getSelectedIndex();
            assertEquals("List row label should reflect the new name.",
                    "MultiEdit", list.getModel().getElementAt(idx).getName());
        });
    }

    private void selectSeedTool(JList<ToolDefinition> list) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            for (int i = 0; i < list.getModel().getSize(); i++) {
                if (seedToolId.equals(list.getModel().getElementAt(i).getId())) {
                    list.setSelectedIndex(i);
                    return;
                }
            }
            fail("Seed tool not present in list");
        });
        flushEdt();
    }

    private void editField(JTextComponent field, String newValue) throws Exception {
        // Grant focus, clear the document, type each char, then explicitly commit. We don't
        // rely on focus-lost to commit because focus transfer between rebuilt JFormattedTextField
        // instances is racy when the EDT is busy (which it is during the full test suite).
        SwingUtilities.invokeAndWait(() -> {
            field.requestFocusInWindow();
            try {
                field.getDocument().remove(0, field.getDocument().getLength());
            } catch (BadLocationException ignored) {
            }
        });
        flushEdt();
        typeString(field, newValue);
        SwingUtilities.invokeAndWait(() -> {
            if (field instanceof JFormattedTextField) {
                try {
                    ((JFormattedTextField) field).commitEdit();
                } catch (ParseException ignored) {
                }
            } else if (field instanceof JTextField) {
                ((JTextField) field).postActionEvent();
            }
        });
        flushEdt();
    }

    /**
     * Commit a JTextField as if the user pressed Enter: dispatch a synthetic ActionEvent via
     * {@link JTextField#postActionEvent()} which synchronously fires registered ActionListeners.
     */
    private void commitField(JTextField field) throws Exception {
        SwingUtilities.invokeAndWait(field::postActionEvent);
        flushEdt();
    }

    private void typeString(JTextComponent target, String s) throws Exception {
        for (int i = 0; i < s.length(); i++) {
            typeChar(target, s.charAt(i));
        }
    }

    private void typeChar(JTextComponent target, char c) throws Exception {
        int keyCode = keyCodeFor(c);
        SwingUtilities.invokeAndWait(() -> {
            long time = System.currentTimeMillis();
            target.dispatchEvent(new KeyEvent(target, KeyEvent.KEY_PRESSED, time, 0, keyCode, c));
            target.dispatchEvent(new KeyEvent(target, KeyEvent.KEY_TYPED, time, 0, KeyEvent.VK_UNDEFINED, c));
            target.dispatchEvent(new KeyEvent(target, KeyEvent.KEY_RELEASED, time, 0, keyCode, c));
        });
    }

    private static int keyCodeFor(char c) {
        if (c == '.') return KeyEvent.VK_PERIOD;
        if (Character.isDigit(c)) return KeyEvent.VK_0 + (c - '0');
        if (Character.isLetter(c)) return KeyEvent.VK_A + (Character.toLowerCase(c) - 'a');
        throw new IllegalArgumentException("Unsupported test character: " + c);
    }

    private ToolDefinition reloadSeed() {
        return service.getTools().stream()
                .filter(t -> seedToolId.equals(t.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Seed tool missing from service after edit"));
    }

    private void drainDeferred() throws Exception {
        for (int i = 0; i < 5; i++) {
            flushEdt();
            Thread.sleep(20);
        }
    }

    private static void flushEdt() throws Exception {
        SwingUtilities.invokeAndWait(() -> { });
    }

    private static ToolDefinition buildSeed() {
        ToolDefinition seed = new ToolDefinition();
        seed.setName("BeforeRename");
        seed.setShape(EndmillShape.UPCUT);
        seed.setDiameter(3.0);
        seed.setDiameterUnit(UnitUtils.Units.MM);
        seed.setFeedSpeed(900);
        seed.setPlungeSpeed(300);
        seed.setDepthPerPass(1.0);
        seed.setStepOverPercent(0.4);
        seed.setMaxSpindleSpeed(18000);
        return seed;
    }

    private static Object readField(Object target, String name) throws Exception {
        Class<?> cls = target.getClass();
        while (cls != null) {
            try {
                Field field = cls.getDeclaredField(name);
                field.setAccessible(true);
                return field.get(target);
            } catch (NoSuchFieldException e) {
                cls = cls.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }
}
