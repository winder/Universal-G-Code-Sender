package com.willwinder.ugs.nbp.designer.platform.actions;

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;

@ActionID(
        id = "com.willwinder.ugs.designer.actions.CopyAction",
        category = "Edit")
@ActionReferences({
        @ActionReference(
                path = "Shortcuts",
                name = "D-C")
})
public class CopyAction extends com.willwinder.ugs.nbp.designer.actions.CopyAction {
}