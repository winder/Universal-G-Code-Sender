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
        popupMenu.add(controller.getAction(SelectAllAction.class));
        popupMenu.add(controller.getAction(ClearSelectionAction.class));
        popupMenu.add(controller.getAction(DeleteAction.class));
        popupMenu.addSeparator();
        popupMenu.add(controller.getAction(CopyAction.class));
        popupMenu.add(controller.getAction(PasteAction.class));
        popupMenu.addSeparator();
        popupMenu.add(controller.getAction(UnionAction.class));
        popupMenu.add(controller.getAction(SubtractAction.class));
        popupMenu.add(controller.getAction(IntersectionAction.class));
        popupMenu.add(controller.getAction(BreakApartAction.class));
        popupMenu.addSeparator();
        popupMenu.add(controller.getAction(FlipHorizontallyAction.class));
        popupMenu.add(controller.getAction(FlipVerticallyAction.class));
        popupMenu.addSeparator();
        popupMenu.add(controller.getAction(JogMachineToCenterAction.class));
        return popupMenu;
    }
}
