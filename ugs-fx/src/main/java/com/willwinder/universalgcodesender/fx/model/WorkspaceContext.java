package com.willwinder.universalgcodesender.fx.model;

import java.io.File;
import java.util.Objects;
import java.util.UUID;

public abstract class WorkspaceContext {
    protected final File file;
    protected final UUID id = UUID.randomUUID();
    protected final String displayName;
    protected boolean dirty;

    protected WorkspaceContext(File file) {
        this.file = Objects.requireNonNull(file);
        this.displayName = file.getName();
    }

    public abstract void open();

    public File getFile() { return file; }
    public UUID getId() { return id; }
    public String getDisplayName() { return displayName; }
    public boolean isDirty() { return dirty; }
    public void setDirty(boolean dirty) { this.dirty = dirty; }
}
