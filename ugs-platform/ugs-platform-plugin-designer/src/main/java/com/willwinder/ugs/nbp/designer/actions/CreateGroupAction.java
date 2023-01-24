package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;

import java.awt.event.ActionEvent;

public class CreateGroupAction extends AbstractDesignAction {
    public CreateGroupAction() {
        putValue("menuText", "Create group");
        putValue(NAME, "Create group");
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        new AddAction(ControllerFactory.getController(), new Group()).actionPerformed(e);
    }
}
