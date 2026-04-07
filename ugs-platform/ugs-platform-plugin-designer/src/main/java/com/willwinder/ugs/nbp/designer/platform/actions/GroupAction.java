package com.willwinder.ugs.nbp.designer.platform.actions;

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

@ActionID(
        id = "com.willwinder.ugs.designer.actions.GroupAction",
        category = "Edit")
@ActionRegistration(
        iconBase = GroupAction.SMALL_ICON_PATH,
        displayName = "Group entities",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = "Shortcuts",
                name = "D-G")
})
public class GroupAction extends com.willwinder.ugs.designer.actions.GroupAction {

}
