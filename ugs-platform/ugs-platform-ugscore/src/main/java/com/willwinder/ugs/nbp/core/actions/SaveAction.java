package com.willwinder.ugs.nbp.core.actions;

import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

@ActionID(
        category = LocalizingService.SaveCategory,
        id = LocalizingService.SaveActionId)
@ActionRegistration(
        iconBase = SaveAction.ICON_BASE,
        displayName = "resources.MessagesBundle#" + LocalizingService.SaveTitleKey,
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = "Toolbars/File",
                position = 10)
})
public class SaveAction extends org.openide.actions.SaveAction {
    public static final String ICON_BASE = "resources/icons/save.svg";

    public SaveAction() {
        super();
        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue("menuText", LocalizingService.SaveTitle);
        putValue(NAME, LocalizingService.SaveTitle);
        putValue("noIconInMenu", Boolean.FALSE);
    }

    @Override
    protected String iconResource() {
        return ICON_BASE;
    }
}
