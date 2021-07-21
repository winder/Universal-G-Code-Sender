package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.actions.ClearSelectionAction;
import com.willwinder.ugs.nbp.designer.actions.DeleteAction;
import com.willwinder.ugs.nbp.designer.actions.SelectAllAction;
import com.willwinder.ugs.nbp.designer.logic.Controller;

import javax.swing.*;

public class PopupMenuFactory {
    public JPopupMenu createPopupMenu(Controller controller) {
        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(new SelectAllAction(controller));
        popupMenu.add(new ClearSelectionAction(controller));
        popupMenu.add(new DeleteAction(controller));
        return popupMenu;
    }
}
