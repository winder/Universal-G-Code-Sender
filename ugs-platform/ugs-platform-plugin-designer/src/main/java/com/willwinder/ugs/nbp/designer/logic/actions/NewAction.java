package com.willwinder.ugs.nbp.designer.logic.actions;

import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class NewAction extends AbstractAction {

    public static final String SMALL_ICON_PATH = "img/new.svg";
    public static final String LARGE_ICON_PATH = "img/new32.svg";

    public NewAction() {
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
        putValue("menuText", "New");
        putValue(NAME, "New");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        UndoManager undoManager = CentralLookup.getDefault().lookup(UndoManager.class);
        undoManager.clear();

        SelectionManager selectionManager = CentralLookup.getDefault().lookup(SelectionManager.class);
        selectionManager.clearSelection();

        Controller controller = CentralLookup.getDefault().lookup(Controller.class);
        controller.newDrawing();
    }
}
