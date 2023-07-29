package com.willwinder.ugs.nbm.workflow.actions;

import com.willwinder.ugs.nbm.workflow.WorkflowPanel;
import com.willwinder.ugs.nbm.workflow.WorkflowWindowTopComponent;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ResetWorkflowAction extends AbstractAction {
    private final WorkflowPanel workflowPanel;
    public static final String ICON_BASE = "com/willwinder/ugs/nbm/workflow/icons/clear.svg";

    public ResetWorkflowAction(WorkflowPanel workflowPanel) {
        String title = NbBundle.getMessage(WorkflowWindowTopComponent.class, "WorkflowWindowTopComponent.resetButton.text");
        putValue(NAME, title);
        putValue("menuText", title);
        putValue(Action.SHORT_DESCRIPTION, title);
        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        this.workflowPanel = workflowPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        workflowPanel.resetWorkflow();
    }
}
