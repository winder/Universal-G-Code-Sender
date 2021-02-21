package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.logic.actions.ClearSelectionAction;
import com.willwinder.ugs.nbp.designer.logic.actions.DeleteAction;
import com.willwinder.ugs.nbp.designer.logic.actions.ExportGcodeAction;
import com.willwinder.ugs.nbp.designer.logic.actions.ExportPngAction;
import com.willwinder.ugs.nbp.designer.logic.actions.NewAction;
import com.willwinder.ugs.nbp.designer.logic.actions.OpenAction;
import com.willwinder.ugs.nbp.designer.logic.actions.QuitAction;
import com.willwinder.ugs.nbp.designer.logic.actions.RedoAction;
import com.willwinder.ugs.nbp.designer.logic.actions.SelectAllAction;
import com.willwinder.ugs.nbp.designer.logic.actions.UndoAction;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Represents a main menu for a DrawGUI
 *
 * @author Alex Lagerstedt
 */
public class MainMenu extends JMenuBar {

    private static final long serialVersionUID = 0;

    public MainMenu() {

        JMenuItem newdrawing = new JMenuItem(new NewAction());
        JMenuItem open = new JMenuItem(new OpenAction());
        JMenuItem export = new JMenuItem(new ExportPngAction());
        JMenuItem exportGcode = new JMenuItem(new ExportGcodeAction());
        JMenuItem quit = new JMenuItem(new QuitAction());

        JMenuItem undo = new JMenuItem(new UndoAction());
        JMenuItem redo = new JMenuItem(new RedoAction());

        JMenuItem all = new JMenuItem(new SelectAllAction());
        JMenuItem clear = new JMenuItem(new ClearSelectionAction());
        JMenuItem delete = new JMenuItem(new DeleteAction());

        redo.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
        open.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        newdrawing.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        undo.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        quit.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        export.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
        clear.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        all.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                InputEvent.CTRL_DOWN_MASK));
        delete.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_BACK_SPACE, 0));

        JMenu fileMenu = new JMenu("File");
        fileMenu.add(newdrawing);
        fileMenu.add(open);
        fileMenu.addSeparator();
        fileMenu.add(export);
        fileMenu.add(exportGcode);
        fileMenu.addSeparator();
        fileMenu.add(quit);

        JMenu editMenu = new JMenu("Edit");
        editMenu.add(undo);
        editMenu.add(redo);
        editMenu.add(new JSeparator());
        editMenu.add(all);
        editMenu.add(clear);
        editMenu.add(delete);

        add(fileMenu);
        add(editMenu);
    }
}
