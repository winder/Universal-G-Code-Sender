package com.willwinder.ugs.nbp.editor.actions;

import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

@ActionID(
        category = LocalizingService.CATEGORY_PROGRAM,
        id = "RotateThreeQuarterAction")
@ActionRegistration(
        iconBase = AbstractRotateAction.ICON_BASE,
        displayName = "Rotate 270°",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.MENU_PROGRAM,
                position = 1203)
})
public class RotateLeftAction extends AbstractRotateAction {

    public static final String ICON_BASE = "icons/rotate_left.svg";

    public RotateLeftAction() {
        super((Math.PI / 2) * 3);
        setIcon(ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue(NAME, "Rotate left");
    }
}
