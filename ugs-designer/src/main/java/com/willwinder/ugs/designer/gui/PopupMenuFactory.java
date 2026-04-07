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
package com.willwinder.ugs.designer.gui;

import com.willwinder.ugs.designer.actions.BreakApartAction;
import com.willwinder.ugs.designer.actions.ClearSelectionAction;
import com.willwinder.ugs.designer.actions.DeleteAction;
import com.willwinder.ugs.designer.actions.FlipHorizontallyAction;
import com.willwinder.ugs.designer.actions.FlipVerticallyAction;
import com.willwinder.ugs.designer.actions.GroupAction;
import com.willwinder.ugs.designer.actions.IntersectionAction;
import com.willwinder.ugs.designer.actions.JogMachineToCenterAction;
import com.willwinder.ugs.designer.actions.JogMachineToLowerLeftCornerAction;
import com.willwinder.ugs.designer.actions.JogMachineToLowerRightCornerAction;
import com.willwinder.ugs.designer.actions.JogMachineToTopLeftCornerAction;
import com.willwinder.ugs.designer.actions.JogMachineToTopRightCornerAction;
import com.willwinder.ugs.designer.actions.RenameAction;
import com.willwinder.ugs.designer.actions.SelectAllAction;
import com.willwinder.ugs.designer.actions.StitchAction;
import com.willwinder.ugs.designer.actions.SubtractAction;
import com.willwinder.ugs.designer.actions.ToggleHidden;
import com.willwinder.ugs.designer.actions.UnionAction;
import com.willwinder.ugs.designer.actions.CopyAction;
import com.willwinder.ugs.designer.actions.PasteAction;

import javax.swing.JPopupMenu;

/**
 * @author Joacim Breiler
 */
public class PopupMenuFactory {

    private static JPopupMenu popupMenu;

    private PopupMenuFactory() {
        // Should not be instanced
    }

    public static JPopupMenu createPopupMenu() {
        if (popupMenu != null) {
            return popupMenu;
        }

        popupMenu = new JPopupMenu();
        popupMenu.add(new SelectAllAction());
        popupMenu.add(new ClearSelectionAction());
        popupMenu.add(new DeleteAction());
        popupMenu.add(new RenameAction());
        popupMenu.add(new GroupAction());
        popupMenu.addSeparator();
        popupMenu.add(new CopyAction());
        popupMenu.add(new PasteAction());
        popupMenu.addSeparator();
        popupMenu.add(new UnionAction());
        popupMenu.add(new SubtractAction());
        popupMenu.add(new IntersectionAction());
        popupMenu.add(new BreakApartAction());
        popupMenu.add(new StitchAction());
        popupMenu.addSeparator();
        popupMenu.add(new FlipHorizontallyAction());
        popupMenu.add(new FlipVerticallyAction());
        popupMenu.addSeparator();
        popupMenu.add(new ToggleHidden());
        popupMenu.addSeparator();
        popupMenu.add(new JogMachineToCenterAction());
        popupMenu.add(new JogMachineToLowerLeftCornerAction());
        popupMenu.add(new JogMachineToLowerRightCornerAction());
        popupMenu.add(new JogMachineToTopLeftCornerAction());
        popupMenu.add(new JogMachineToTopRightCornerAction());
        return popupMenu;
    }
}
