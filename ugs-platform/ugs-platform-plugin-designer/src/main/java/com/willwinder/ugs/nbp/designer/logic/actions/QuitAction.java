package com.willwinder.ugs.nbp.designer.logic.actions;

import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class QuitAction extends AbstractAction {
    private static final String ICON_BASE = "img/system-log-out.png";

    public QuitAction() {
        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue("menuText", "Quit");
        putValue(NAME, "Quit");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        System.exit(0);
    }
}
