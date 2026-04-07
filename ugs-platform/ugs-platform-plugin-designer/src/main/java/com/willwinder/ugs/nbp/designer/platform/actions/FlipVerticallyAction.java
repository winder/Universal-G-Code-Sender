package com.willwinder.ugs.nbp.designer.platform.actions;

import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;

@ActionID(
        category = LocalizingService.CATEGORY_DESIGNER,
        id = "FlipVerticallyAction")
@ActionRegistration(
        iconBase = FlipVerticallyAction.SMALL_ICON_PATH,
        displayName = "Flip vertically",
        lazy = false)
public class FlipVerticallyAction extends com.willwinder.ugs.designer.actions.FlipVerticallyAction {

}
