package com.willwinder.ugs.nbp.designer.logic.actions;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;


@ActionID(
        category = LocalizingService.CATEGORY_EDIT,
        id = "com.willwinder.ugs.nbp.designer.logic.actions.UndoAction"
)
@ActionRegistration(
        displayName = "Undo"
)
public class UndoAction extends AbstractAction implements UndoManagerListener {
    public static final String ICON_BASE = "img/edit-undo.png";
    private final UndoManager undoManager;

    public UndoAction() {
        undoManager = CentralLookup.getDefault().lookup(UndoManager.class);
        undoManager.addListener(this);
        setEnabled(undoManager.canUndo());

        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue("menuText", "Undo");
        putValue(NAME, "Undo");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        undoManager.undo();
    }

    @Override
    public void onChanged() {
        setEnabled(undoManager.canUndo());
    }
}
