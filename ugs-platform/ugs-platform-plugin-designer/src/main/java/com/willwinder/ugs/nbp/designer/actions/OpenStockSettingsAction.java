package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.gui.StockSettingsPanel;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static org.openide.NotifyDescriptor.OK_OPTION;

public class OpenStockSettingsAction implements ActionListener {
    private final Controller controller;

    public OpenStockSettingsAction(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        StockSettingsPanel stockSettingsPanel = new StockSettingsPanel(controller);
        DialogDescriptor dialogDescriptor = new DialogDescriptor(stockSettingsPanel, "Stock settings", true, null);
        if (DialogDisplayer.getDefault().notify(dialogDescriptor) == OK_OPTION) {
            double stockThickness = stockSettingsPanel.getStockThickness();
            ChangeStockSettingsAction changeStockSettingsAction = new ChangeStockSettingsAction(controller, stockThickness);
            changeStockSettingsAction.actionPerformed(null);
            controller.getUndoManager().addAction(changeStockSettingsAction);
        }
    }
}
