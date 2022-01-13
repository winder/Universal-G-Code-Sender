package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.gui.ToolSettingsPanel;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static org.openide.NotifyDescriptor.OK_OPTION;

public class OpenToolSettingsAction implements ActionListener {
    private final Controller controller;

    public OpenToolSettingsAction(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ToolSettingsPanel toolSettingsPanel = new ToolSettingsPanel(controller);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(toolSettingsPanel, "Tool settings", true, null);
        if (DialogDisplayer.getDefault().notify(dialogDescriptor) == OK_OPTION) {
            ChangeToolSettingsAction changeStockSettings = new ChangeToolSettingsAction(controller, toolSettingsPanel.getSettings());
            changeStockSettings.actionPerformed(null);
            controller.getUndoManager().addAction(changeStockSettings);
        }
    }
}
