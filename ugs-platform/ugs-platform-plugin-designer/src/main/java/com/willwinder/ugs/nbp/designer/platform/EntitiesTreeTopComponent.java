package com.willwinder.ugs.nbp.designer.platform;

import com.willwinder.ugs.nbp.designer.gui.EntitiesTree;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import org.openide.windows.TopComponent;

@TopComponent.Description(
        preferredID = "EntitiesTreeTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED
)
@TopComponent.Registration(mode = "bottom_left", openAtStartup = false)
public class EntitiesTreeTopComponent extends TopComponent {
    private static final long serialVersionUID = 432423498723987873L;

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
