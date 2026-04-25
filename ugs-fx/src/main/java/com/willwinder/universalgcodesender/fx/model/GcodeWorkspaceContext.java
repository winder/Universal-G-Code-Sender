package com.willwinder.universalgcodesender.fx.model;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.services.LookupService;

import java.io.File;

public class GcodeWorkspaceContext extends WorkspaceContext {
    public GcodeWorkspaceContext(File file) {
        super(file);
    }

    @Override
    public void open() {
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + file.getAbsolutePath());
        }

        try {
            BackendAPI backend = LookupService.lookup(BackendAPI.class);
            backend.setGcodeFile(file);
            backend.getSettings().setLastWorkingDirectory(file.getParent());
        } catch (Exception e) {
            throw new IllegalArgumentException("Not a valid gcode file: " + file.getAbsolutePath(), e);
        }
    }
}
