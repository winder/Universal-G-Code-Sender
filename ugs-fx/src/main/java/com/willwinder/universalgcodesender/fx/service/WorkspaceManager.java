package com.willwinder.universalgcodesender.fx.service;

import com.willwinder.universalgcodesender.fx.model.WorkspaceContext;
import com.willwinder.universalgcodesender.fx.model.WorkspaceContextFactory;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class WorkspaceManager {
    public interface WorkspaceListener {
        void onWorkspaceOpened(WorkspaceContext workspace);

        void onWorkspaceClosed();

        void onWorkspaceDirtyStateChanged(WorkspaceContext workspace, boolean dirty);
    }

    private static final WorkspaceManager INSTANCE = new WorkspaceManager();

    private final List<WorkspaceListener> listeners = new CopyOnWriteArrayList<>();
    private WorkspaceContext activeWorkspace;

    private WorkspaceManager() {
    }

    public static WorkspaceManager getInstance() {
        return INSTANCE;
    }

    public void addListener(WorkspaceListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    public void removeListener(WorkspaceListener listener) {
        listeners.remove(listener);
    }

    public synchronized WorkspaceContext open(File file) {
        WorkspaceContext workspace = WorkspaceContextFactory.create(file);
        activeWorkspace = workspace;
        workspace.open();
        notifyWorkspaceOpened(workspace);
        return workspace;
    }

    public synchronized WorkspaceContext openWorkspace(File file) {
        return open(file);
    }

    public synchronized Optional<WorkspaceContext> getActiveWorkspace() {
        return Optional.ofNullable(activeWorkspace);
    }

    public synchronized void closeActiveWorkspace() {
        if (activeWorkspace != null) {
            activeWorkspace = null;
            notifyWorkspaceClosed();
        }
    }

    public synchronized boolean hasActiveWorkspace() {
        return activeWorkspace != null;
    }

    public synchronized void markActiveWorkspaceDirty(boolean dirty) {
        if (activeWorkspace != null) {
            activeWorkspace.setDirty(dirty);
            notifyWorkspaceDirtyStateChanged(activeWorkspace, dirty);
        }
    }

    private void notifyWorkspaceOpened(WorkspaceContext workspace) {
        for (WorkspaceListener listener : listeners) {
            listener.onWorkspaceOpened(workspace);
        }
    }

    private void notifyWorkspaceClosed() {
        for (WorkspaceListener listener : listeners) {
            listener.onWorkspaceClosed();
        }
    }

    private void notifyWorkspaceDirtyStateChanged(WorkspaceContext workspace, boolean dirty) {
        for (WorkspaceListener listener : listeners) {
            listener.onWorkspaceDirtyStateChanged(workspace, dirty);
        }
    }
}
