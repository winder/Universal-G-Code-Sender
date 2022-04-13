package com.willwinder.ugs.nbp.toolbox;

import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.ugs.nbp.toolbox.settings.ToolboxOptionsController;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;

import javax.swing.*;
import java.awt.event.ActionEvent;

@ActionID(
        category = LocalizingService.CATEGORY_EDIT,
        id = "com.willwinder.ugs.nbp.toolbox.ToolboxSettingsAction")
@ActionRegistration(
        displayName = "Toolbox settings",
        lazy = false)
public class ToolboxSettingsAction extends AbstractAction {

    public ToolboxSettingsAction() {
        putValue("menuText", LocalizingService.ToolboxSettingsTitle);
        putValue(NAME, LocalizingService.ToolboxSettingsTitle);
        setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        OptionsDisplayer.getDefault().open(ToolboxOptionsController.KEYWORDS_CATEGORY);
    }
}
