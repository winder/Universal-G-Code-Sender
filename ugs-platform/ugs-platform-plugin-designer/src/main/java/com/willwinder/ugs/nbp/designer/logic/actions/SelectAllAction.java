package com.willwinder.ugs.nbp.designer.logic.actions;

import com.willwinder.ugs.nbp.designer.gui.DrawingEvent;
import com.willwinder.ugs.nbp.designer.gui.DrawingListener;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class SelectAllAction extends AbstractAction implements DrawingListener {

    public static final String SMALL_ICON_PATH = "img/select-all.svg";
    public static final String LARGE_ICON_PATH = "img/select-all32.svg";

    private final Controller controller;

    public SelectAllAction(Controller controller) {
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
        putValue("menuText", "Select all");
        putValue(NAME, "Select all");

        this.controller = controller;

        setEnabled(!this.controller.getDrawing().getEntities().isEmpty());
        this.controller.getDrawing().addListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SelectionManager selectionManager = controller.getSelectionManager();
        ThreadHelper.invokeLater(() -> {
            selectionManager.clearSelection();
            selectionManager.setSelection(controller.getDrawing().getEntities());
            controller.getDrawing().repaint();
        });
    }

    @Override
    public void onDrawingEvent(DrawingEvent event) {
        setEnabled(!controller.getDrawing().getEntities().isEmpty());
    }
}
