package com.willwinder.ugs.nbp.designer.logic.actions;

import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.Tool;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class ToolDrawCircleAction extends AbstractAction {
    private static final String ICON_SMALL_PATH = "img/circle.svg";
    private static final String ICON_LARGE_PATH = "img/circle32.svg";

    public ToolDrawCircleAction() {
        putValue("iconBase", ICON_SMALL_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_SMALL_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(ICON_LARGE_PATH, false));
        putValue("menuText", "Draw circle");
        putValue(NAME, "Draw circle");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Controller controller = CentralLookup.getDefault().lookup(Controller.class);
        controller.setTool(Tool.CIRCLE);
    }
}
