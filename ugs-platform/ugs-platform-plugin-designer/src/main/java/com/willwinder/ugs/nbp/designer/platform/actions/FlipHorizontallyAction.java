package com.willwinder.ugs.nbp.designer.platform.actions;

import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;

@ActionID(
        category = LocalizingService.CATEGORY_DESIGNER,
        id = "FlipHorizontallyAction")
@ActionRegistration(
        iconBase = FlipHorizontallyAction.SMALL_ICON_PATH,
        displayName = "Flip horizontally",
        lazy = false)
public class FlipHorizontallyAction extends com.willwinder.ugs.nbp.designer.actions.FlipHorizontallyAction {

}
