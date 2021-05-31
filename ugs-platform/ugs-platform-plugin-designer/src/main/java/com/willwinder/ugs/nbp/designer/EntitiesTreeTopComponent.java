package com.willwinder.ugs.nbp.designer;

import com.willwinder.ugs.nbp.designer.gui.EntitiesTree;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import org.openide.windows.TopComponent;

public class EntitiesTreeTopComponent extends TopComponent {
    private EntitiesTree entitiesTree;

    public EntitiesTreeTopComponent() {
        setMinimumSize(new java.awt.Dimension(50, 50));
        setPreferredSize(new java.awt.Dimension(200, 200));
        setLayout(new java.awt.BorderLayout());
        setDisplayName("Design objects");
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        entitiesTree = new EntitiesTree();
        add(entitiesTree);
        validate();
    }

    public void updateController(Controller controller) {
        entitiesTree.updateController(controller);
    }
}
