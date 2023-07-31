package com.willwinder.ugs.nbm.workflow.actions;

import com.willwinder.ugs.nbm.workflow.WorkflowPanel;
import com.willwinder.ugs.nbm.workflow.WorkflowWindowTopComponent;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.uielements.components.GcodeFileTypeFilter;
import com.willwinder.universalgcodesender.utils.Settings;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

public class AddGcodeFileAction extends AbstractAction {
    public static final String ICON_BASE = "com/willwinder/ugs/nbm/workflow/icons/add.svg";

    private final WorkflowPanel workflowPanel;

    public AddGcodeFileAction(WorkflowPanel workflowPanel) {
        String title = NbBundle.getMessage(WorkflowWindowTopComponent.class, "WorkflowWindowTopComponent.addButton.text");
        putValue(NAME, title);
        putValue("menuText", title);
        putValue(Action.SHORT_DESCRIPTION, title);
        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        this.workflowPanel = workflowPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Settings settings = CentralLookup.getDefault().lookup(Settings.class);
        JFileChooser fileChooser = GcodeFileTypeFilter.getGcodeFileChooser(settings.getLastOpenedFilename());

        int returnVal = fileChooser.showOpenDialog(workflowPanel);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File gcodeFile = fileChooser.getSelectedFile();
            settings.setLastOpenedFilename(gcodeFile.getParent());
            workflowPanel.addFileToWorkflow(gcodeFile);
        }
    }
}
