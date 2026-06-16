package com.willwinder.universalgcodesender.fx.model;

import java.io.File;
import java.util.UUID;

public abstract class WorkspaceContext {
    protected final UUID id = UUID.randomUUID();
    protected File file;
    protected boolean dirty;

    /**
     * Creates a workspace context. The file may be {@code null} for a workspace that has not been
     * saved yet (e.g. a brand new design); a location is then assigned later via {@link #setFile}.
     */
    protected WorkspaceContext(File file) {
        this.file = file;
    }

    public abstract void open();

    public File getFile() { return file; }
    public void setFile(File file) { this.file = file; }
    public UUID getId() { return id; }
    public String getDisplayName() { return file != null ? file.getName() : "Untitled"; }

    /**
     * Returns the file extension for this type of workspace {@code "ugsd"}.
     */
    public abstract String getFileExtension();
    public boolean isDirty() { return dirty; }
    public void setDirty(boolean dirty) { this.dirty = dirty; }
}
