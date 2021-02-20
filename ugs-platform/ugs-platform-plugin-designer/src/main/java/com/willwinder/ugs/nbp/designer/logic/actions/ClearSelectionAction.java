package com.willwinder.ugs.nbp.designer.logic.actions;

import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.selection.SelectionManager;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

@ActionID(
        category = LocalizingService.CATEGORY_EDIT,
        id = "com.willwinder.ugs.nbp.designer.logic.actions.ClearSelection"
)
@ActionRegistration(
        displayName = "Clear selection"
)
public class ClearSelectionAction extends AbstractAction {

    private static final String ICON_BASE = "img/edit-clear.png";

    public ClearSelectionAction() {
        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue("menuText", "Clear selection");
        putValue(NAME, "Clear selection");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SelectionManager selectionManager = CentralLookup.getDefault().lookup(SelectionManager.class);
        selectionManager.removeAll();

        Controller controller = CentralLookup.getDefault().lookup(Controller.class);
        controller.getDrawing().repaint();
    }
}
