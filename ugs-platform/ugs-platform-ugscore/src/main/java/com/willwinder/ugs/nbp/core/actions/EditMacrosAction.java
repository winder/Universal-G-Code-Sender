package com.willwinder.ugs.nbp.core.actions;

import com.willwinder.ugs.nbp.core.options.MacrosOptionsPanelController;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;


import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

@ActionID(
        category = LocalizingService.EditMacrosActionCategory,
        id = LocalizingService.EditMacrosActionId)
@ActionRegistration(
        iconBase = EditMacrosAction.ICON_BASE,
        displayName = "resources.MessagesBundle#" + LocalizingService.EditMacrosTitleKey,
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.EditMacrosWindowPath,
                position = 2030)
})
public class EditMacrosAction extends AbstractAction {
    public static final String ICON_BASE = "resources/icons/macro.svg";

    public EditMacrosAction() {
        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue("menuText", LocalizingService.EditMacrosTitle);
        putValue(NAME, LocalizingService.EditMacrosTitle);
        setEnabled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        OptionsDisplayer.getDefault().open(MacrosOptionsPanelController.KEYWORDS_CATEGORY);
    }
}
