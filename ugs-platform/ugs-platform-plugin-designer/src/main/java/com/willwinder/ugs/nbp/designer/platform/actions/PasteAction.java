package com.willwinder.ugs.nbp.designer.platform.actions;

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;

@ActionID(
        id = "com.willwinder.ugs.designer.actions.PasteAction",
        category = "Edit")
@ActionReferences({
        @ActionReference(
                path = "Shortcuts",
                name = "D-V")
})
public class PasteAction extends com.willwinder.ugs.nbp.designer.actions.PasteAction {
}
