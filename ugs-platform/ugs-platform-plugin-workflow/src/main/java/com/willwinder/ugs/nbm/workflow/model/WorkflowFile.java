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
package com.willwinder.ugs.nbm.workflow.model;

import java.io.File;

/**
 * A file that can be loaded into the workflow plugin
 *
 * @author Joacim Breiler
 */
public class WorkflowFile {
    private final File file;
    private WorkflowTool tool;

    private boolean isCompleted;

    public WorkflowFile(File file, WorkflowTool tool) {
        this.file = file;
        this.tool = tool;
        this.isCompleted = false;
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WorkflowFile)) {
            return false;
        }

        return ((WorkflowFile) obj).getFile().equals(file);
    }

    public String getFileName() {
        return file.getName();
    }

    public WorkflowTool getTool() {
        return tool;
    }

    public File getFile() {
        return file;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        this.isCompleted = completed;
    }

    public void setTool(WorkflowTool tool) {
        this.tool = tool;
    }
}
