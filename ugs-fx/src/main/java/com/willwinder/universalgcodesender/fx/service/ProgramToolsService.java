/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.service;

import com.willwinder.ugs.designer.logic.ToolLibraryListener;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.renderables.GcodeLines;
import com.willwinder.universalgcodesender.fx.component.visualizer.simulation.CutSegment;
import com.willwinder.universalgcodesender.fx.component.visualizer.simulation.ProgramTools;
import com.willwinder.universalgcodesender.fx.component.visualizer.simulation.ProgramTools.ProgramTool;
import com.willwinder.universalgcodesender.fx.component.visualizer.simulation.ToolResolver;
import com.willwinder.universalgcodesender.fx.model.WorkspaceContext;
import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.FileState;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Keeps track of which tools the loaded program is simulated with, so the user can see them. The
 * list is recomputed when a program is loaded, when the tool library or the default tool changes
 * and when the workspace changes, all off the JavaFX thread.
 */
public final class ProgramToolsService {
    private static final Logger LOGGER = Logger.getLogger(ProgramToolsService.class.getName());
    private static ProgramToolsService instance;

    private final ReadOnlyObjectWrapper<List<ProgramTool>> tools = new ReadOnlyObjectWrapper<>(this, "tools", List.of());
    private final AtomicInteger generation = new AtomicInteger();
    private final AtomicBoolean libraryListenerInstalled = new AtomicBoolean();
    private final ToolLibraryListener libraryListener = this::refresh;

    private ProgramToolsService() {
        LookupService.lookup(BackendAPI.class).addUGSEventListener(this::onEvent);
        VisualizerSettings.getInstance().stockDefaultToolIdProperty().addListener(observable -> refresh());
        WorkspaceManager.getInstance().addListener(new WorkspaceManager.WorkspaceListener() {
            @Override
            public void onWorkspaceOpened(WorkspaceContext workspace) {
                refresh();
            }

            @Override
            public void onWorkspaceClosed() {
                refresh();
            }

            @Override
            public void onWorkspaceDirtyStateChanged(WorkspaceContext workspace, boolean dirty) {
                // The tools only depend on the program and the settings
            }
        });
        refresh();
    }

    public static synchronized ProgramToolsService getInstance() {
        if (instance == null) {
            instance = new ProgramToolsService();
        }
        return instance;
    }

    /**
     * The tools of the loaded program in order of first use, empty when no program is loaded.
     * Updated on the JavaFX thread.
     */
    public ReadOnlyObjectProperty<List<ProgramTool>> toolsProperty() {
        return tools.getReadOnlyProperty();
    }

    public List<ProgramTool> getTools() {
        return tools.get();
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof FileStateEvent fileStateEvent) {
            if (fileStateEvent.getFileState() == FileState.FILE_LOADING) {
                refresh();
            } else if (fileStateEvent.getFileState() == FileState.FILE_UNLOADED) {
                generation.incrementAndGet();
                publish(List.of());
            }
        }
    }

    /**
     * Recomputes the tools for the loaded program. A result from a refresh that was superseded
     * while it ran is dropped.
     */
    public void refresh() {
        int current = generation.incrementAndGet();
        File file = LookupService.lookup(BackendAPI.class).getGcodeFile();
        if (file == null || !file.exists()) {
            publish(List.of());
            return;
        }
        String defaultToolId = VisualizerSettings.getInstance().stockDefaultToolIdProperty().get();
        ThreadHelper.invokeLater(() -> {
            try {
                installLibraryListener();
                ToolResolver resolver = ProgramToolResolution.forProgram(file, defaultToolId);
                List<CutSegment> cuts = CutSegment.fromLineSegments(GcodeLines.parseSegments(file));
                List<ProgramTool> result = List.copyOf(ProgramTools.collect(cuts, resolver));
                if (current == generation.get()) {
                    publish(result);
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Could not determine the tools of " + file, e);
            }
        });
    }

    /**
     * Opening the tool library reads a file, so the listener that follows its changes is only
     * installed once a program is actually being looked at.
     */
    private void installLibraryListener() {
        if (libraryListenerInstalled.compareAndSet(false, true)) {
            ToolLibraryProvider.getInstance().addListener(libraryListener);
        }
    }

    private void publish(List<ProgramTool> result) {
        if (Platform.isFxApplicationThread()) {
            tools.set(result);
        } else {
            Platform.runLater(() -> tools.set(result));
        }
    }
}
