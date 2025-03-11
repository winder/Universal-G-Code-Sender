/*
    Copyright 2016-2023 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.willwinder.ugs.nbm.workflow;

import com.willwinder.ugs.nbm.workflow.model.WorkflowFile;
import com.willwinder.ugs.nbp.core.actions.OpenFileAction;
import com.willwinder.ugs.nbp.lib.Mode;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.ugs.nbp.lib.services.TopComponentLocalizer;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.StreamEvent;
import com.willwinder.universalgcodesender.model.events.StreamEventType;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.modules.OnStart;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;

import javax.swing.JOptionPane;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.io.File;

/**
 * An interface to help organize a multi gcode file workflow.
 */
@ConvertAsProperties(
        dtd = "-//com.willwinder.ugs.nbm.workflow//WorkflowWindow//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "WorkflowWindowTopComponent"
)
@TopComponent.Registration(mode = Mode.OUTPUT, openAtStartup = false)
@ActionID(category = WorkflowWindowTopComponent.CATEGORY, id = WorkflowWindowTopComponent.ACTION_ID)
@ActionReference(path = LocalizingService.MENU_WINDOW_PLUGIN)
@TopComponent.OpenActionRegistration(
        displayName = "Workflow Helper",
        preferredID = "WorkflowWindowTopComponent"
)
public final class WorkflowWindowTopComponent extends TopComponent implements UGSEventListener, WorkflowPanelListener {
    public final static String ACTION_ID = "com.willwinder.ugs.nbm.workflow.WorkflowWindowTopComponent";
    public final static String TITLE = Localization.getString("platform.window.workflow");
    public final static String CATEGORY = LocalizingService.CATEGORY_WINDOW;
    private final BackendAPI backend;
    private final WorkflowPanel workflowPanel = new WorkflowPanel();

    /**
     * Initialize the WorkflowWindow, register with the UGS Backend and set some
     * of the required JTable settings.
     */
    public WorkflowWindowTopComponent() {

        // This is how to access the UGS backend and register the listener.
        // CentralLookup is used to get singleton instances of the UGS Settings
        // and BackendAPI objects.
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        setLayout(new BorderLayout());
        add(workflowPanel, BorderLayout.CENTER);
        setName(Localization.getString("platform.window.workflow"));

        workflowPanel.addListener(this);
    }

    @Override
    public void onSelectedFile(WorkflowFile workflowFile) {
        if (workflowFile == null) {
            return;
        }

        try {
            OpenFileAction action = new OpenFileAction(workflowFile.getFile());
            action.actionPerformed(null);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Events from backend. Take specific actions based on the control state.
     * File state change - FILE_LOADED: Add the file to the workflow, always do this if the workflow page is loaded.
     * File state change - FILE_STREAM_COMPLETE: When the file send job has finished.
     *
     * @param cse the event
     */
    @Override
    public void UGSEvent(UGSEvent cse) {
        if (cse instanceof StreamEvent streamEvent) {
            if (streamEvent.getType() == StreamEventType.STREAM_COMPLETE) {
                completeFile(backend.getGcodeFile());
            }
        }
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        backend.addUGSEventListener(this);
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        backend.removeUGSEventListener(this);
    }

    /**
     * Call when a file's work has been completed to progress to the next step
     * of the work flow.
     *
     * @param completedFile the file which is completing.
     */
    public void completeFile(File completedFile) {
        if (completedFile == null) return;

        // Make sure the file is loaded in the table.
        workflowPanel.markAsComplete(completedFile);
        String message = getMessage(completedFile);

        // Display a notification.
        EventQueue.invokeLater(() -> {
            requestActive();
            JOptionPane.showMessageDialog(this.getTopLevelAncestor(), message,
                    "Workflow Event", JOptionPane.PLAIN_MESSAGE);
        });

    }

    private String getMessage(File completedFile) {
        String message;

        // Make sure there is another command left.
        int nextFileIndex = workflowPanel.getFileIndex(completedFile) + 1;
        if (nextFileIndex < workflowPanel.getFileCount()) {
            String nextTool = workflowPanel.getToolName(nextFileIndex);
            String messageTemplate =
                    "Finished sending '%s'.\n"
                            + "The next file uses tool '%s'\n"
                            + "Load tool and move machine to its zero location\n"
                            + "and click send to continue this workflow.";
            message = String.format(messageTemplate, completedFile.getName(), nextTool);

            // Select the next row, this will trigger a selection event.
            workflowPanel.selectFileIndex(nextFileIndex);
        } else {
            // Use a different message if we're finished.
            message = "Finished sending the last file!";
        }
        return message;
    }

    public void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
    }

    public void readProperties(java.util.Properties p) {
        //String version = p.getProperty("version");
    }

    @OnStart
    public static class Localizer extends TopComponentLocalizer {
        public Localizer() {
            super(CATEGORY, ACTION_ID, TITLE);
        }
    }
}
