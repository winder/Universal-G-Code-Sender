package com.willwinder.universalgcodesender.fx.model;

import java.io.File;

public final class WorkspaceContextFactory {
    public static WorkspaceContext create(File file) {
        String name = file.getName().toLowerCase();

        if (name.endsWith(".gcode") || name.endsWith(".nc") || name.endsWith(".ngc") || name.endsWith(".txt")) {
            return new GcodeWorkspaceContext(file);
        }

        if (name.endsWith(".ugsd")) {
            return new UgsdWorkspaceContext(file);
        }

        throw new IllegalArgumentException("Unsupported file type: " + file.getName());
    }
}
