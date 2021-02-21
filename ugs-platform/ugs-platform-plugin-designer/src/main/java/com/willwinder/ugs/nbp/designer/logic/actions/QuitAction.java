package com.willwinder.ugs.nbp.designer.logic.actions;

import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class QuitAction extends AbstractAction {
    private static final String ICON_SMALL_PATH = "img/exit.svg";
    private static final String ICON_LARGE_PATH = "img/exit32.svg";

    public QuitAction() {
        putValue("iconBase", ICON_SMALL_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_SMALL_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(ICON_LARGE_PATH, false));
        putValue("menuText", "Quit");
        putValue(NAME, "Quit");
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        System.exit(0);
    }
}
