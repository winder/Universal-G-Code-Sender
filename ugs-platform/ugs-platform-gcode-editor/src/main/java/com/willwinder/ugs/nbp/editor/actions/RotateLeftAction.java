package com.willwinder.ugs.nbp.editor.actions;

import com.willwinder.ugs.nbp.editor.GcodeLanguageConfig;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import org.netbeans.api.editor.EditorActionRegistration;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

@ActionID(
        category = LocalizingService.CATEGORY_PROGRAM,
        id = "RotateThreeQuarterAction")
@ActionRegistration(
        iconBase = MirrorAction.ICON_BASE,
        displayName = "Rotate 270°",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.MENU_PROGRAM,
                position = 1203)
})
@EditorActionRegistration(
        name = "rotate-quarter-left",
        toolBarPosition = 11,
        mimeType = GcodeLanguageConfig.MIME_TYPE,
        iconResource = RotateLeftAction.ICON_BASE
)
public class RotateLeftAction extends AbstractRotateAction {

    public static final String ICON_BASE = "icons/rotate_left.svg";

    public RotateLeftAction() {
        super((Math.PI / 2) * 3);
        putValue(NAME, "Rotate left");
    }

    @Override
    protected String iconResource() {
        return ICON_BASE;
    }
}
