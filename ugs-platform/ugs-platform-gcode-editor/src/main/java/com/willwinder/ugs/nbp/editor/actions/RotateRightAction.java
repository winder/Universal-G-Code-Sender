package com.willwinder.ugs.nbp.editor.actions;

import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

@ActionID(
        category = LocalizingService.CATEGORY_PROGRAM,
        id = "RotateQuarterAction")
@ActionRegistration(
        iconBase = AbstractRotateAction.ICON_BASE,
        displayName = "Rotate 90°",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.MENU_PROGRAM,
                position = 1201)
})
public class RotateRightAction extends AbstractRotateAction {

    public static final String ICON_BASE = "icons/rotate_right.svg";

    public RotateRightAction() {
        super(Math.PI / 2);
        setIcon(ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue(NAME, "Rotate right");
    }
}
