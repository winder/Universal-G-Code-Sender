/*
    Copyright 2021-2023 Will Winder

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
package com.willwinder.ugs.designer.gui;

import com.willwinder.ugs.designer.actions.ClearSelectionAction;
import com.willwinder.ugs.designer.actions.CopyAction;
import com.willwinder.ugs.designer.actions.DeleteAction;
import com.willwinder.ugs.designer.actions.ExportGcodeAction;
import com.willwinder.ugs.designer.actions.ExportPngAction;
import com.willwinder.ugs.designer.actions.FlipHorizontallyAction;
import com.willwinder.ugs.designer.actions.FlipVerticallyAction;
import com.willwinder.ugs.designer.actions.NewAction;
import com.willwinder.ugs.designer.actions.OpenAction;
import com.willwinder.ugs.designer.actions.PasteAction;
import com.willwinder.ugs.designer.actions.PrintDesignAction;
import com.willwinder.ugs.designer.actions.QuitAction;
import com.willwinder.ugs.designer.actions.RedoAction;
import com.willwinder.ugs.designer.actions.SaveAction;
import com.willwinder.ugs.designer.actions.SelectAllAction;
import com.willwinder.ugs.designer.actions.SelectNextAction;
import com.willwinder.ugs.designer.actions.SelectPreviousAction;
import com.willwinder.ugs.designer.actions.ToggleHidden;
import com.willwinder.ugs.designer.actions.UndoAction;
import com.willwinder.ugs.designer.logic.Controller;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.Serial;

/**
 * Represents a main menu for a DrawGUI
 *
 * @author Alex Lagerstedt
 * @author Joacim Breiler
 */
public class MainMenu extends JMenuBar {

    @Serial
    private static final long serialVersionUID = 3391060173341949629L;

    public MainMenu(Controller controller) {
        JMenuItem newDrawing = new JMenuItem(new NewAction());
        JMenuItem open = new JMenuItem(new OpenAction());
        JMenuItem save = new JMenuItem(new SaveAction(controller));
        JMenuItem export = new JMenuItem(new ExportPngAction(controller));
        JMenuItem exportGcode = new JMenuItem(new ExportGcodeAction());
        JMenuItem printDesign = new JMenuItem(new PrintDesignAction());
        JMenuItem quit = new JMenuItem(new QuitAction());

        JMenuItem undo = new JMenuItem(new UndoAction());
        JMenuItem redo = new JMenuItem(new RedoAction());

        JMenuItem copy = new JMenuItem(new CopyAction());
        JMenuItem paste = new JMenuItem(new PasteAction());

        JMenuItem flipHorizontal = new JMenuItem(new FlipHorizontallyAction());
        JMenuItem flipVertical = new JMenuItem(new FlipVerticallyAction());

        JMenuItem all = new JMenuItem(new SelectAllAction());
        JMenuItem previous = new JMenuItem(new SelectPreviousAction());
        JMenuItem next = new JMenuItem(new SelectNextAction());
        JMenuItem clear = new JMenuItem(new ClearSelectionAction());
        JMenuItem delete = new JMenuItem(new DeleteAction());
        JMenuItem toggleHidden = new JMenuItem(new ToggleHidden());

        int menuShortcut = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();

        open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, menuShortcut));
        save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, menuShortcut));
        newDrawing.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, menuShortcut));
        undo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, menuShortcut));
        redo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, menuShortcut | InputEvent.SHIFT_DOWN_MASK));
        quit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, menuShortcut));
        export.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, menuShortcut));
        clear.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK));
        previous.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, menuShortcut | InputEvent.SHIFT_DOWN_MASK));
        all.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, menuShortcut));
        next.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, menuShortcut | InputEvent.SHIFT_DOWN_MASK));
        delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, menuShortcut));
        paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, menuShortcut));
        printDesign.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, menuShortcut));

        JMenu fileMenu = new JMenu("File");
        fileMenu.add(newDrawing);
        fileMenu.add(open);
        fileMenu.add(save);
        fileMenu.addSeparator();
        fileMenu.add(export);
        fileMenu.add(exportGcode);
        fileMenu.addSeparator();
        fileMenu.add(printDesign);
        fileMenu.addSeparator();
        fileMenu.add(quit);

        JMenu editMenu = new JMenu("Edit");
        editMenu.add(undo);
        editMenu.add(redo);
        editMenu.add(new JSeparator());
        editMenu.add(copy);
        editMenu.add(paste);
        editMenu.add(new JSeparator());
        editMenu.add(flipHorizontal);
        editMenu.add(flipVertical);
        editMenu.add(new JSeparator());
        editMenu.add(all);
        editMenu.add(previous);
        editMenu.add(next);
        editMenu.add(clear);
        editMenu.add(delete);
        editMenu.add(new JSeparator());
        editMenu.add(toggleHidden);

        add(fileMenu);
        add(editMenu);
    }
}
