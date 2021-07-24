package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.actions.*;
import com.willwinder.ugs.nbp.designer.logic.Controller;

import javax.swing.*;

public class PopupMenuFactory {
    public JPopupMenu createPopupMenu(Controller controller) {
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(new SelectAllAction(controller));
        popupMenu.add(new ClearSelectionAction(controller));
        popupMenu.add(new DeleteAction(controller));
        popupMenu.add(new JPopupMenu.Separator());
        popupMenu.add(new FlipHorizontallyAction(controller));
        popupMenu.add(new FlipVerticallyAction(controller));
        return popupMenu;
    }
}
