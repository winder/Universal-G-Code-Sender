package com.willwinder.universalgcodesender.fx.service;

import com.willwinder.universalgcodesender.fx.model.UgsdWorkspaceContext;
import com.willwinder.universalgcodesender.fx.model.WorkspaceContext;
import com.willwinder.universalgcodesender.fx.model.WorkspaceContextFactory;
import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;
import javafx.application.Platform;

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
        activate(workspace);
        return workspace;
    }

    /**
     * Makes the workspace the active one. Loading a design fires change events on the shared
     * designer controller while listeners of the previous workspace may still be attached, so
     * whatever dirtiness those events left behind is cleared: a workspace is clean when opened.
     */
    private void activate(WorkspaceContext workspace) {
        if (activeWorkspace != null) {
            activeWorkspace.close();
        }
        activeWorkspace = workspace;
        workspace.open();
        workspace.setDirty(false);
        showWorkspaceLayer(workspace);
        notifyWorkspaceOpened(workspace);
    }

    /**
     * Opening a file is a request to see it, so the visualizer layer it lives on is turned on even
     * if it was hidden earlier: the design shapes for a design, the tool path for a program.
     */
    private static void showWorkspaceLayer(WorkspaceContext workspace) {
        Runnable show = () -> {
            VisualizerSettings settings = VisualizerSettings.getInstance();
            if (workspace instanceof UgsdWorkspaceContext) {
                settings.showDesignProperty().set(true);
            } else {
                settings.showGcodeModelProperty().set(true);
            }
        };
        if (Platform.isFxApplicationThread()) {
            show.run();
        } else {
            Platform.runLater(show);
        }
    }

    public synchronized WorkspaceContext openWorkspace(File file) {
        return open(file);
    }

    public synchronized Optional<WorkspaceContext> getActiveWorkspace() {
        return Optional.ofNullable(activeWorkspace);
    }

    public synchronized void setWorkspace(WorkspaceContext workspace) {
        activate(workspace);
    }

    public synchronized void closeActiveWorkspace() {
        if (activeWorkspace != null) {
            activeWorkspace.close();
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
