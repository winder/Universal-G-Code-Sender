package com.willwinder.ugs.nbp.designer.logic.actions;

import com.willwinder.ugs.nbp.designer.gui.entities.Entity;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.selection.SelectionManager;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class SelectAllAction extends AbstractAction {

    private static final String ICON_BASE = "img/edit-select-all.png";

    public SelectAllAction() {
        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue("menuText", "Select all");
        putValue(NAME, "Select all");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SelectionManager selectionManager = CentralLookup.getDefault().lookup(SelectionManager.class);
        Controller controller = CentralLookup.getDefault().lookup(Controller.class);
        selectionManager.removeAll();
        for (Entity sh : controller.getDrawing().getShapes()) {
            selectionManager.add(sh);
        }
        controller.getDrawing().repaint();
    }
}
