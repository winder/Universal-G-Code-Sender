package com.willwinder.ugs.nbp.designer.logic.actions;

import com.willwinder.ugs.nbp.designer.Utils;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;

public class ExportGcodeAction extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger(ExportGcodeAction.class.getSimpleName());
    private static final String SMALL_ICON_PATH = "img/export.svg";
    private static final String LARGE_ICON_PATH = "img/export32.svg";
    private final Controller controller;

    public ExportGcodeAction(Controller controller) {
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
        putValue("menuText", "Export Gcode");
        putValue(NAME, "Export Gcode");

        this.controller = controller;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String gcode = Utils.toGcode(controller, controller.getDrawing().getEntities());
        LOGGER.info(gcode);
    }
}
