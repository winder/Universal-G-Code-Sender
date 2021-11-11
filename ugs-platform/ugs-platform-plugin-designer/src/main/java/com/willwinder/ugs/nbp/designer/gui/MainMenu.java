/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.actions.*;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import org.openide.util.Utilities;

import javax.swing.*;

/**
 * Represents a main menu for a DrawGUI
 *
 * @author Alex Lagerstedt
 * @author Joacim Breiler
 */
public class MainMenu extends JMenuBar {

    private static final long serialVersionUID = 0;

    public MainMenu(Controller controller) {

        JMenuItem newdrawing = new JMenuItem(new NewAction());
        JMenuItem open = new JMenuItem(new OpenAction());
        JMenuItem save = new JMenuItem(new SaveAction(controller));
        JMenuItem export = new JMenuItem(new ExportPngAction(controller));
        JMenuItem exportGcode = new JMenuItem(new ExportGcodeAction(controller));
        JMenuItem quit = new JMenuItem(new QuitAction());

        JMenuItem undo = new JMenuItem(new UndoAction());
        JMenuItem redo = new JMenuItem(new RedoAction());

        JMenuItem copy = new JMenuItem(new CopyAction(controller));
        JMenuItem paste = new JMenuItem(new PasteAction(controller));

        JMenuItem flipHorizontal = new JMenuItem(new FlipHorizontallyAction(controller));
        JMenuItem flipVertical = new JMenuItem(new FlipVerticallyAction(controller));

        JMenuItem all = new JMenuItem(new SelectAllAction(controller));
        JMenuItem clear = new JMenuItem(new ClearSelectionAction(controller));
        JMenuItem delete = new JMenuItem(new DeleteAction(controller));

        open.setAccelerator(Utilities.stringToKey("D-O"));
        save.setAccelerator(Utilities.stringToKey("D-S"));
        newdrawing.setAccelerator(Utilities.stringToKey("D-N"));
        undo.setAccelerator(Utilities.stringToKey("D-Z"));
        redo.setAccelerator(Utilities.stringToKey("SD-Z"));
        quit.setAccelerator(Utilities.stringToKey("D-Q"));
        export.setAccelerator(Utilities.stringToKey("D-E"));
        clear.setAccelerator(Utilities.stringToKey("O-C"));
        all.setAccelerator(Utilities.stringToKey("D-A"));
        delete.setAccelerator(Utilities.stringToKey("BACK_SPACE"));
        copy.setAccelerator(Utilities.stringToKey("D-C"));
        paste.setAccelerator(Utilities.stringToKey("D-V"));

        JMenu fileMenu = new JMenu("File");
        fileMenu.add(newdrawing);
        fileMenu.add(open);
        fileMenu.add(save);
        fileMenu.addSeparator();
        fileMenu.add(export);
        fileMenu.add(exportGcode);
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
        editMenu.add(clear);
        editMenu.add(delete);

        add(fileMenu);
        add(editMenu);
    }
}
