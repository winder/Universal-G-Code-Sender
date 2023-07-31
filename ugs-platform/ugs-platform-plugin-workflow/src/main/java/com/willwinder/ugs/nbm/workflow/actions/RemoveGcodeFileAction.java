package com.willwinder.ugs.nbm.workflow.actions;

import com.willwinder.ugs.nbm.workflow.WorkflowPanel;
import com.willwinder.ugs.nbm.workflow.WorkflowPanelListener;
import com.willwinder.ugs.nbm.workflow.WorkflowWindowTopComponent;
import com.willwinder.ugs.nbm.workflow.model.WorkflowFile;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.event.ActionEvent;


public class RemoveGcodeFileAction extends AbstractAction implements WorkflowPanelListener {
    public static final String ICON_BASE = "com/willwinder/ugs/nbm/workflow/icons/remove.svg";

    private final WorkflowPanel workflowPanel;

    public RemoveGcodeFileAction(WorkflowPanel workflowPanel) {
        String title = NbBundle.getMessage(WorkflowWindowTopComponent.class, "WorkflowWindowTopComponent.removeButton.text");
        putValue(NAME, title);
        putValue("menuText", title);
        putValue(Action.SHORT_DESCRIPTION, title);
        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        this.workflowPanel = workflowPanel;
        this.workflowPanel.addListener(this);
        setEnabled(this.workflowPanel.getSelectedIndex() > 0);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.workflowPanel.removeSelectedFile();
    }

    @Override
    public void onSelectedFile(WorkflowFile workflowFile) {
        setEnabled(workflowFile != null);
    }
}
