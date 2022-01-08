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
        id = "RotateQuarterAction")
@ActionRegistration(
        iconBase = MirrorAction.ICON_BASE,
        displayName = "Rotate 90°",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.MENU_PROGRAM,
                position = 1201)
})
@EditorActionRegistration(
        name = "rotate-quarter-right",
        toolBarPosition = 10,
        mimeType = GcodeLanguageConfig.MIME_TYPE,
        iconResource = RotateRightAction.ICON_BASE
)
public class RotateRightAction extends AbstractRotateAction {

    public static final String ICON_BASE = "icons/rotate_right.svg";

    public RotateRightAction() {
        super(Math.PI / 2);
        putValue(NAME, "Rotate right");
    }

    @Override
    protected String iconResource() {
        return ICON_BASE;
    }
}
