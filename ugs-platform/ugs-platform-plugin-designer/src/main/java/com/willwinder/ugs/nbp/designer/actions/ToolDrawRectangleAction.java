package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.Tool;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class ToolDrawRectangleAction extends AbstractAction {
    private static final String SMALL_ICON_PATH = "img/rectangle.png";
    private static final String LARGE_ICON_PATH = "img/rectangle32.png";
    private final Controller controller;

    public ToolDrawRectangleAction(Controller controller) {
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
        putValue("menuText", "Draw rectangle");
        putValue(NAME, "Draw rectangle");
        this.controller = controller;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        controller.setTool(Tool.RECTANGLE);
    }
}
