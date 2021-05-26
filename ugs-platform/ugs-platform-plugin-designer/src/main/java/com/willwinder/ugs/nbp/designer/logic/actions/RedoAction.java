package com.willwinder.ugs.nbp.designer.logic.actions;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class RedoAction extends AbstractAction implements UndoManagerListener {
    public static final String SMALL_ICON_PATH = "img/redo.svg";
    public static final String LARGE_ICON_PATH = "img/redo32.svg";

    private final UndoManager undoManager;

    public RedoAction() {
        undoManager = CentralLookup.getDefault().lookup(UndoManager.class);
        undoManager.addListener(this);
        setEnabled(undoManager.canRedo());

        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
        putValue("menuText", "Redo");
        putValue(NAME, "Redo");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        undoManager.redo();
    }

    @Override
    public void onChanged() {
        setEnabled(undoManager.canRedo());
    }
}
