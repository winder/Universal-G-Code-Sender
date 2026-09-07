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
package com.willwinder.universalgcodesender.fx.component.visualizer.scene.renderables;

import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Bounds3;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Mat4;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.MeshHandle;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.RenderContext;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Renderable;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Scene;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.SceneLayer;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.VertexLayout;
import com.willwinder.universalgcodesender.fx.service.VisualizerService;
import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.CommandEvent;
import com.willwinder.universalgcodesender.model.events.FileState;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.model.events.StreamEvent;
import com.willwinder.universalgcodesender.model.events.StreamEventType;
import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The toolpath of the loaded program. The vertex buffer never changes while a program runs;
 * showing progress is one number pushed with the next frame, and the shader recolours every
 * segment the controller has finished.
 */
public final class GcodeToolpathRenderable implements Renderable {
    private static final Logger LOGGER = Logger.getLogger(GcodeToolpathRenderable.class.getName());
    private static final int NOTHING_COMPLETED = -1;
    /**
     * Lifts the toolpath just above the design fills and outlines drawn on the work plane.
     */
    private static final double Z_OFFSET = 0.1;
    private static final float[] Z_OFFSET_MODEL = Mat4.translation(0, 0, Z_OFFSET);
    private static final float LINE_WIDTH_PX = 1;

    private final UGSEventListener eventListener = this::onEvent;
    private final InvalidationListener redrawListener = observable -> requestRender();
    private final InvalidationListener reloadListener = observable -> load(currentFile());
    private final float[] completedColor = new float[4];
    private Scene scene;
    private MeshHandle mesh;
    private Bounds3 bounds;
    private int completedCommand = NOTHING_COMPLETED;
    /**
     * Counts loads and unloads so a parse that finishes after a newer load or an unload started
     * is dropped. Bumped on the event thread, read back on the JavaFX thread.
     */
    private final AtomicInteger loadGeneration = new AtomicInteger();

    @Override
    public SceneLayer layer() {
        return SceneLayer.GCODE;
    }

    @Override
    public boolean isVisible() {
        return mesh != null && VisualizerSettings.getInstance().showGcodeModelProperty().get();
    }

    @Override
    public Optional<Bounds3> bounds() {
        return Optional.ofNullable(bounds);
    }

    @Override
    public void onAttached(Scene scene) {
        this.scene = scene;
        VisualizerSettings settings = VisualizerSettings.getInstance();
        readCompletedColor();
        settings.colorCompletedProperty().addListener(redrawListener);
        settings.showGcodeModelProperty().addListener(redrawListener);
        paletteProperties().forEach(property -> property.addListener(reloadListener));
        LookupService.lookup(BackendAPI.class).addUGSEventListener(eventListener);
        load(currentFile());
    }

    @Override
    public void onDetached(Scene scene) {
        loadGeneration.incrementAndGet();
        VisualizerSettings settings = VisualizerSettings.getInstance();
        settings.colorCompletedProperty().removeListener(redrawListener);
        settings.showGcodeModelProperty().removeListener(redrawListener);
        paletteProperties().forEach(property -> property.removeListener(reloadListener));
        LookupService.lookup(BackendAPI.class).removeUGSEventListener(eventListener);
        releaseMesh();
        this.scene = null;
    }

    @Override
    public void render(RenderContext context) {
        context.drawToolpath(mesh, Z_OFFSET_MODEL, LINE_WIDTH_PX, completedCommand, completedColor);
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof FileStateEvent fileStateEvent) {
            onFileState(fileStateEvent.getFileState());
        } else if (event instanceof CommandEvent commandEvent) {
            if (commandEvent.getCommand().isDone()) {
                completedCommand = commandEvent.getCommand().getCommandNumber();
                requestRender();
            }
        } else if (event instanceof StreamEvent streamEvent) {
            if (streamEvent.getType() == StreamEventType.STREAM_COMPLETE
                    || streamEvent.getType() == StreamEventType.STREAM_CANCELED) {
                completedCommand = NOTHING_COMPLETED;
                requestRender();
            }
        }
    }

    private void onFileState(FileState fileState) {
        if (fileState == FileState.FILE_LOADING) {
            VisualizerService.getInstance().setToolpathLoading(true);
            load(currentFile());
        } else if (fileState == FileState.FILE_UNLOADED) {
            // Opening a file unloads the previous one right before loading the new one. Bumping
            // the generation makes any load still running for the old file drop its result.
            loadGeneration.incrementAndGet();
            releaseMesh();
            VisualizerService.getInstance().setToolpathLoading(false);
            requestRender();
        }
    }

    /**
     * Parses the program off the JavaFX thread, since a large one takes a noticeable while, and
     * hands the finished vertices back on the JavaFX thread. A result from a load that was
     * superseded while it ran is dropped.
     */
    private void load(File file) {
        if (file == null || !file.exists() || scene == null) {
            return;
        }
        int generation = loadGeneration.incrementAndGet();
        VisualizerService.getInstance().setToolpathLoading(true);
        GcodeLines.Palette palette = GcodeLines.Palette.fromSettings();
        ThreadHelper.invokeLater(() -> {
            try {
                GcodeLines.Model model = GcodeLines.load(file, palette);
                Platform.runLater(() -> {
                    if (generation != loadGeneration.get()) {
                        // A newer load has taken over and will clear the loading state itself.
                        return;
                    }
                    if (scene != null) {
                        releaseMesh();
                        if (model.vertexCount() > 0) {
                            mesh = scene.context().upload(model.vertices(), VertexLayout.LINE);
                            bounds = model.bounds();
                        }
                        completedCommand = NOTHING_COMPLETED;
                        requestRender();
                    }
                    VisualizerService.getInstance().setToolpathLoading(false);
                });
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Could not load the G-code model", e);
                Platform.runLater(() -> {
                    if (generation == loadGeneration.get()) {
                        VisualizerService.getInstance().setToolpathLoading(false);
                    }
                });
            }
        });
    }

    private void releaseMesh() {
        if (mesh != null && scene != null) {
            scene.context().release(mesh);
        }
        mesh = null;
        bounds = null;
    }

    private void readCompletedColor() {
        Color color = Color.web(VisualizerSettings.getInstance().colorCompletedProperty().get());
        completedColor[0] = (float) color.getRed();
        completedColor[1] = (float) color.getGreen();
        completedColor[2] = (float) color.getBlue();
        completedColor[3] = (float) color.getOpacity();
    }

    private static File currentFile() {
        return LookupService.lookup(BackendAPI.class).getGcodeFile();
    }

    private static List<StringProperty> paletteProperties() {
        VisualizerSettings settings = VisualizerSettings.getInstance();
        return List.of(
                settings.colorRapidProperty(),
                settings.colorArcProperty(),
                settings.colorPlungeProperty(),
                settings.colorFeedMinProperty(),
                settings.colorFeedMaxProperty(),
                settings.colorSpindleMinProperty(),
                settings.colorSpindleMaxProperty());
    }

    private void requestRender() {
        readCompletedColor();
        if (scene != null) {
            scene.requestRender();
        }
    }
}
