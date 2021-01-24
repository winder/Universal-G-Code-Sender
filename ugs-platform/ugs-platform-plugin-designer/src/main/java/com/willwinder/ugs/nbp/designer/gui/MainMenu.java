package com.willwinder.ugs.nbp.designer.gui;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Represents a main menu for a DrawGUI
 *
 * @author Alex Lagerstedt
 */
public class MainMenu extends JMenuBar {

    private static final long serialVersionUID = 0;

    public MainMenu(MenuListener listener) {

        JMenu fileMenu = new JMenu("File");
        JMenuItem newdrawing = new JMenuItem("New", new ImageIcon(
                MainMenu.class.getResource("/img/document-new.png")));
        JMenuItem open = new JMenuItem("Open", new ImageIcon(
                MainMenu.class.getResource(
                        "/img/document-open.png")));
        JMenuItem saveas = new JMenuItem("Save as", new ImageIcon(MainMenu.class.getResource(
                "/img/document-save-as.png")));
        JMenuItem export = new JMenuItem("Export PNG", new ImageIcon(
                MainMenu.class.getResource(
                        "/img/document-save-as.png")));
        JMenuItem exportGcode = new JMenuItem("Export GCode", new ImageIcon(
                MainMenu.class.getResource(
                        "/img/document-save-as.png")));
        JMenuItem quit = new JMenuItem("Quit", new ImageIcon(
                MainMenu.class.getResource(
                        "/img/system-log-out.png")));

        JMenu editMenu = new JMenu("Edit");
        JMenuItem undo = new JMenuItem("Undo", new ImageIcon(MainMenu.class.getResource(
                "/img/edit-undo.png")));
        JMenuItem redo = new JMenuItem("Redo", new ImageIcon(MainMenu.class.getResource(
                "/img/edit-redo.png")));

        JMenu selectionMenu = new JMenu("Selection");
        JMenuItem all = new JMenuItem("Select all", new ImageIcon(
                MainMenu.class.getResource(
                        "/img/edit-select-all.png")));
        JMenuItem clear = new JMenuItem("Clear selection", new ImageIcon(
                MainMenu.class.getResource(
                        "/img/edit-clear.png")));
        JMenuItem delete = new JMenuItem("Delete", new ImageIcon(
                MainMenu.class.getResource(
                        "/img/edit-delete.png")));
        delete.setActionCommand("Delete");


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
        saveas.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        clear.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));
        all.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                InputEvent.CTRL_DOWN_MASK));
        delete.setAccelerator(KeyStroke.getKeyStroke(
                KeyEvent.VK_BACK_SPACE, 0));


        quit.addActionListener(listener);
        all.addActionListener(listener);
        undo.addActionListener(listener);
        redo.addActionListener(listener);
        delete.addActionListener(listener);
        clear.addActionListener(listener);
        newdrawing.addActionListener(listener);
        open.addActionListener(listener);
        saveas.addActionListener(listener);
        export.addActionListener(listener);
        exportGcode.addActionListener(listener);

        fileMenu.add(newdrawing);
        fileMenu.add(open);
        fileMenu.addSeparator();
        fileMenu.add(saveas);
        fileMenu.add(export);
        fileMenu.add(exportGcode);
        fileMenu.addSeparator();
        fileMenu.add(quit);

        editMenu.add(undo);
        editMenu.add(redo);

        selectionMenu.add(all);
        selectionMenu.add(clear);
        selectionMenu.add(delete);


        add(fileMenu);
        add(editMenu);
        add(selectionMenu);
    }
}
