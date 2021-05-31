package com.willwinder.ugs.nbp.designer.logic.actions;

import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class ClearSelectionAction extends AbstractAction implements SelectionListener {

    public static final String SMALL_ICON_PATH = "img/clear-selection.svg";
    public static final String LARGE_ICON_PATH = "img/clear-selection32.svg";

    private final SelectionManager selectionManager;

    public ClearSelectionAction() {
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
        putValue("menuText", "Clear selection");
        putValue(NAME, "Clear selection");

        selectionManager = CentralLookup.getDefault().lookup(SelectionManager.class);
        selectionManager.addSelectionListener(this);
        setEnabled(!selectionManager.getSelection().isEmpty());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        selectionManager.clearSelection();

        Controller controller = CentralLookup.getDefault().lookup(Controller.class);
        controller.getDrawing().repaint();
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        setEnabled(!selectionManager.getSelection().isEmpty());
    }
}
