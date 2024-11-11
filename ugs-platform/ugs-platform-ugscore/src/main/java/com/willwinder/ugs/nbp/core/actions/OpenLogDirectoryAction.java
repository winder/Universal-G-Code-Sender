package com.willwinder.ugs.nbp.core.actions;

import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.i18n.Localization;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.modules.Places;

import javax.swing.AbstractAction;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@ActionID(
        category = LocalizingService.CATEGORY_TOOLS,
        id = "com.willwinder.ugs.nbp.core.actions.OpenLogDirectoryAction")
@ActionRegistration(
        displayName = "resources/MessagesBundle#platform.action.openLogDirectory",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.MENU_TOOLS,
                position = 2100)
})
public class OpenLogDirectoryAction extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger(OpenLogDirectoryAction.class.getName());

    public OpenLogDirectoryAction() {
        putValue(NAME, Localization.getString("platform.action.openLogDirectory"));
    }


    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        try {
            Desktop.getDesktop().open(new File(Places.getUserDirectory().getAbsolutePath() + File.separator + "var" + File.separator + "log"));
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not open the log directory", ex);
        }
    }
}
