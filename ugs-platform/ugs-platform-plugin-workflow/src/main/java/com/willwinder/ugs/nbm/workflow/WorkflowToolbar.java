/*
    Copyright 2023 Will Winder

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

import com.willwinder.ugs.nbm.workflow.actions.AddGcodeFileAction;
import com.willwinder.ugs.nbm.workflow.actions.RemoveGcodeFileAction;
import com.willwinder.ugs.nbm.workflow.actions.ResetWorkflowAction;
import com.willwinder.ugs.nbp.core.ui.ToolBar;

import javax.swing.*;

/**
 * A toolbar for handling workflow actions
 *
 * @author Joacim Breiler
 */
public class WorkflowToolbar extends ToolBar {
    private final WorkflowPanel workflowPanel;

    public WorkflowToolbar(WorkflowPanel workflowPanel) {
        this.workflowPanel = workflowPanel;
        setFloatable(false);
        initComponents();
    }

    private void initComponents() {
        createAndAddButton(new AddGcodeFileAction(workflowPanel));
        createAndAddButton(new RemoveGcodeFileAction(workflowPanel));
        addSeparator();
        createAndAddButton(new ResetWorkflowAction(workflowPanel));
    }

    private void createAndAddButton(Action action) {
        JButton button = new JButton(action);
        button.setHideActionText(false);
        button.setToolTipText((String) action.getValue(Action.SHORT_DESCRIPTION));
        this.add(button);
    }
}
