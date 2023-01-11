package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.actions.BreakApartAction;
import com.willwinder.ugs.nbp.designer.actions.ClearSelectionAction;
import com.willwinder.ugs.nbp.designer.actions.CopyAction;
import com.willwinder.ugs.nbp.designer.actions.DeleteAction;
import com.willwinder.ugs.nbp.designer.actions.FlipHorizontallyAction;
import com.willwinder.ugs.nbp.designer.actions.FlipVerticallyAction;
import com.willwinder.ugs.nbp.designer.actions.IntersectionAction;
import com.willwinder.ugs.nbp.designer.actions.JogMachineToCenterAction;
import com.willwinder.ugs.nbp.designer.actions.PasteAction;
import com.willwinder.ugs.nbp.designer.actions.SelectAllAction;
import com.willwinder.ugs.nbp.designer.actions.SubtractAction;
import com.willwinder.ugs.nbp.designer.actions.UnionAction;

import javax.swing.JPopupMenu;

/**
 * @author Joacim Breiler
 */
public class PopupMenuFactory {
    public JPopupMenu createPopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(new SelectAllAction());
        popupMenu.add(new ClearSelectionAction());
        popupMenu.add(new DeleteAction());
        popupMenu.addSeparator();
        popupMenu.add(new CopyAction());
        popupMenu.add(new PasteAction());
        popupMenu.addSeparator();
        popupMenu.add(new UnionAction());
        popupMenu.add(new SubtractAction());
        popupMenu.add(new IntersectionAction());
        popupMenu.add(new BreakApartAction());
        popupMenu.addSeparator();
        popupMenu.add(new FlipHorizontallyAction());
        popupMenu.add(new FlipVerticallyAction());
        popupMenu.addSeparator();
        popupMenu.add(new JogMachineToCenterAction());
        return popupMenu;
    }
}
