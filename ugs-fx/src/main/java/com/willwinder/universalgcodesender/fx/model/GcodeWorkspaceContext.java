package com.willwinder.universalgcodesender.fx.model;

import com.willwinder.universalgcodesender.gcode.GcodeStats;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.services.LookupService;

import java.io.File;
import java.util.Optional;

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

    @Override
    public String getFileExtension() {
        return "gcode";
    }

    /**
     * The workspace size is the bounding box of the loaded gcode. Only available once the file has
     * been parsed (i.e. after a {@code FILE_LOADED} event).
     */
    @Override
    public Optional<WorkspaceBounds> getBounds() {
        BackendAPI backend = LookupService.lookup(BackendAPI.class);
        GcodeStats stats = backend.getGcodeStats();
        if (stats == null || stats.getMin() == null || stats.getMax() == null) {
            return Optional.empty();
        }

        Position min = stats.getMin();
        Position max = stats.getMax();
        return Optional.of(new WorkspaceBounds(min.getX(), min.getY(), max.getX(), max.getY()));
    }
}
