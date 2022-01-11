package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.actions.*;
import com.willwinder.ugs.nbp.designer.logic.Controller;

import javax.swing.*;

/**
 * @author Joacim Breiler
 */
public class PopupMenuFactory {
    public JPopupMenu createPopupMenu(Controller controller) {
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(new SelectAllAction(controller));
        popupMenu.add(new ClearSelectionAction(controller));
        popupMenu.add(new DeleteAction(controller));
        popupMenu.addSeparator();
        popupMenu.add(new CopyAction(controller));
        popupMenu.add(new PasteAction(controller));
        popupMenu.addSeparator();
        popupMenu.add(new UnionAction(controller));
        popupMenu.add(new SubtractAction(controller));
        popupMenu.add(new IntersectionAction(controller));
        popupMenu.add(new BreakApartAction(controller));
        popupMenu.addSeparator();
        popupMenu.add(new FlipHorizontallyAction(controller));
        popupMenu.add(new FlipVerticallyAction(controller));
        return popupMenu;
    }
}
