package com.willwinder.ugs.nbp.core.actions;

import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

@ActionID(
        category = LocalizingService.CATEGORY_PROGRAM,
        id = "RotateHalfAction")
@ActionRegistration(
        iconBase = AbstractRotateAction.ICON_BASE,
        displayName = "Rotate 180°",
        lazy = false)
@ActionReferences({
        //@ActionReference(
        //        path = "Toolbars/Run",
        //        position = 980),
        @ActionReference(
                path = LocalizingService.MENU_PROGRAM,
                position = 1002)
})
public class RotateHalfAction extends AbstractRotateAction {

    public static final String ICON_BASE = "resources/icons/rotate180.svg";

    public RotateHalfAction() {
        super(Math.PI);
        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue("menuText", "Rotate 180°");
        putValue(NAME, "Rotate 180°");
    }
}
