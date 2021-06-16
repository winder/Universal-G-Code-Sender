package com.willwinder.ugs.nbp.designer;

import com.willwinder.ugs.nbp.designer.gui.SelectionSettingsPanel;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import org.openide.windows.TopComponent;

@TopComponent.Description(
        preferredID = "SettingsTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED
)
@TopComponent.Registration(mode = "bottom_left", openAtStartup = false)
public class SettingsTopComponent extends TopComponent {

    private SelectionSettingsPanel selectionSettingsPanel;

    public SettingsTopComponent() {
        setMinimumSize(new java.awt.Dimension(50, 50));
        setPreferredSize(new java.awt.Dimension(200, 200));
        setLayout(new java.awt.BorderLayout());
        setDisplayName("Cut settings");
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        selectionSettingsPanel = new SelectionSettingsPanel();
        add(selectionSettingsPanel);
        validate();
    }

    public void updateController(Controller controller) {
        selectionSettingsPanel.updateController(controller);
    }
}
