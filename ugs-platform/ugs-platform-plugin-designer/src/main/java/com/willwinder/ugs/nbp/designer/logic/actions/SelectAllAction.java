package com.willwinder.ugs.nbp.designer.logic.actions;

import com.willwinder.ugs.nbp.designer.gui.DrawingEvent;
import com.willwinder.ugs.nbp.designer.gui.DrawingListener;
import com.willwinder.ugs.nbp.designer.gui.entities.Entity;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.selection.SelectionManager;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class SelectAllAction extends AbstractAction implements DrawingListener {

    public static final String SMALL_ICON_PATH = "img/select-all.svg";
    public static final String LARGE_ICON_PATH = "img/select-all32.svg";

    private final SelectionManager selectionManager;
    private final Controller controller;

    public SelectAllAction() {
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
        putValue("menuText", "Select all");
        putValue(NAME, "Select all");

        selectionManager = CentralLookup.getDefault().lookup(SelectionManager.class);
        controller = CentralLookup.getDefault().lookup(Controller.class);

        setEnabled(!controller.getDrawing().getEntities().isEmpty());
        controller.getDrawing().addListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        selectionManager.removeAll();
        for (Entity sh : controller.getDrawing().getEntities()) {
            selectionManager.add(sh);
        }
        controller.getDrawing().repaint();
    }

    @Override
    public void onDrawingEvent(DrawingEvent event) {
        setEnabled(!controller.getDrawing().getEntities().isEmpty());
    }
}
