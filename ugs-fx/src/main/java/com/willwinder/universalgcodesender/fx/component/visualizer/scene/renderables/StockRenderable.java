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
import com.willwinder.universalgcodesender.fx.component.visualizer.simulation.CutSegment;
import com.willwinder.universalgcodesender.fx.component.visualizer.simulation.HeightField;
import com.willwinder.universalgcodesender.fx.component.visualizer.simulation.HeightFieldMesher;
import com.willwinder.universalgcodesender.fx.component.visualizer.simulation.StockDefinition;
import com.willwinder.universalgcodesender.fx.component.visualizer.simulation.StockSimulation;
import com.willwinder.universalgcodesender.fx.component.visualizer.simulation.StockSpec;
import com.willwinder.universalgcodesender.fx.component.visualizer.simulation.ToolResolver;
import com.willwinder.universalgcodesender.fx.service.ProgramToolResolution;
import com.willwinder.universalgcodesender.fx.service.StockService;
import com.willwinder.universalgcodesender.fx.service.StockSpecs;
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
import com.willwinder.universalgcodesender.visualizer.LineSegment;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.scene.paint.Color;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Draws the virtual block of material the loaded program cuts into. When no program is streaming
 * the finished result is shown. While streaming, a second simulation follows the commands the
 * controller reports finished, so the block is carved as the machine works; once the stream ends
 * or is cancelled the finished result is shown again.
 *
 * <p>Simulation and meshing run on a single background thread. Only the mesh upload touches the
 * JavaFX thread. Nothing is computed while the stock is hidden; the work is done when it is shown.
 */
public final class StockRenderable implements Renderable {
    private static final Logger LOGGER = Logger.getLogger(StockRenderable.class.getName());
    private static final int NOTHING_COMPLETED = -1;
    /**
     * How often, at most, the carved block is re-meshed while streaming.
     */
    private static final long PROGRESS_INTERVAL_MS = 150;
    private static final float[] IDENTITY = Mat4.identity();

    /**
     * Everything about a loaded program the simulations need, produced once per load.
     */
    private record Program(List<CutSegment> cuts, StockDefinition stock, ToolResolver tools, List<HeightFieldMesher.Mesh> finalMesh) {
    }

    /**
     * How many depth bands the cut surface is split into when coloured by depth: one for untouched
     * material and the rest dividing the thickness of the block.
     */
    private static final int DEPTH_BANDS = 8;

    /**
     * The shallowest cut band already differs this much from the stock colour, so any cut shows.
     */
    private static final float SHALLOW_MIX = 0.35f;

    private final UGSEventListener eventListener = this::onEvent;
    private final InvalidationListener colorListener = observable -> {
        readColor();
        requestRender();
    };
    private final InvalidationListener visibilityListener = observable -> onVisibilityChanged();
    private final InvalidationListener reloadListener = observable -> load(currentFile());
    private final float[][] bandColors = new float[DEPTH_BANDS][4];
    /**
     * Counts loads and unloads so a simulation that finishes after a newer load or an unload
     * started is dropped.
     */
    private final AtomicInteger loadGeneration = new AtomicInteger();
    private final AtomicBoolean progressScheduled = new AtomicBoolean();
    private volatile int completedCommand = NOTHING_COMPLETED;
    private volatile Program program;
    private volatile boolean stale = true;
    private volatile ScheduledExecutorService simulator;
    /**
     * The simulation following the running stream. Touched only on the simulator thread.
     */
    private StockSimulation progress;
    private Scene scene;
    private final List<MeshHandle> meshes = new ArrayList<>();
    private final List<Integer> meshBands = new ArrayList<>();
    private Bounds3 bounds;

    @Override
    public SceneLayer layer() {
        return SceneLayer.STOCK;
    }

    @Override
    public boolean isVisible() {
        return !meshes.isEmpty() && isEnabled();
    }

    @Override
    public Optional<Bounds3> bounds() {
        return Optional.ofNullable(bounds);
    }

    @Override
    public void onAttached(Scene scene) {
        this.scene = scene;
        simulator = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "StockSimulation");
            thread.setDaemon(true);
            return thread;
        });
        VisualizerSettings settings = VisualizerSettings.getInstance();
        readColor();
        settings.colorStockProperty().addListener(colorListener);
        settings.colorStockDeepProperty().addListener(colorListener);
        settings.showStockProperty().addListener(visibilityListener);
        settings.stockDefaultToolIdProperty().addListener(reloadListener);
        settings.stockProperties().forEach(property -> property.addListener(reloadListener));
        LookupService.lookup(BackendAPI.class).addUGSEventListener(eventListener);
        load(currentFile());
    }

    @Override
    public void onDetached(Scene scene) {
        loadGeneration.incrementAndGet();
        VisualizerSettings settings = VisualizerSettings.getInstance();
        settings.colorStockProperty().removeListener(colorListener);
        settings.colorStockDeepProperty().removeListener(colorListener);
        settings.showStockProperty().removeListener(visibilityListener);
        settings.stockDefaultToolIdProperty().removeListener(reloadListener);
        settings.stockProperties().forEach(property -> property.removeListener(reloadListener));
        LookupService.lookup(BackendAPI.class).removeUGSEventListener(eventListener);
        simulator.shutdownNow();
        simulator = null;
        program = null;
        releaseMesh();
        this.scene = null;
    }

    @Override
    public void render(RenderContext context) {
        for (int i = 0; i < meshes.size(); i++) {
            context.drawTriangles(meshes.get(i), IDENTITY, bandColors[meshBands.get(i)], true);
        }
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof FileStateEvent fileStateEvent) {
            onFileState(fileStateEvent.getFileState());
        } else if (event instanceof CommandEvent commandEvent) {
            if (commandEvent.getCommand().isDone()) {
                completedCommand = commandEvent.getCommand().getCommandNumber();
                scheduleProgress();
            }
        } else if (event instanceof StreamEvent streamEvent) {
            if (streamEvent.getType() == StreamEventType.STREAM_COMPLETE
                    || streamEvent.getType() == StreamEventType.STREAM_CANCELED) {
                completedCommand = NOTHING_COMPLETED;
                showFinalResult();
            }
        }
    }

    private void onFileState(FileState fileState) {
        if (fileState == FileState.FILE_LOADING) {
            load(currentFile());
        } else if (fileState == FileState.FILE_UNLOADED) {
            loadGeneration.incrementAndGet();
            program = null;
            stale = true;
            Platform.runLater(() -> {
                releaseMesh();
                requestRender();
            });
        }
    }

    private void onVisibilityChanged() {
        if (isEnabled() && stale) {
            load(currentFile());
        }
        requestRender();
    }

    /**
     * Simulates the whole program off the JavaFX thread and uploads the resulting block. Skipped
     * while the stock is hidden; the load is redone when it is shown.
     */
    private void load(File file) {
        int generation = loadGeneration.incrementAndGet();
        stale = true;
        if (file == null || !file.exists() || scene == null || simulator == null || !isEnabled()) {
            return;
        }
        stale = false;
        String defaultToolId = VisualizerSettings.getInstance().stockDefaultToolIdProperty().get();
        StockSpec spec = StockSpecs.fromSettings();
        simulator.execute(() -> {
            try {
                progress = null;
                ToolResolver tools = ProgramToolResolution.forProgram(file, defaultToolId);
                List<LineSegment> segments = GcodeLines.parseSegments(file);
                List<CutSegment> cuts = CutSegment.fromLineSegments(segments);
                Optional<StockDefinition> stock = StockDefinition.resolve(spec, cuts, tools);
                if (stock.isEmpty()) {
                    Platform.runLater(() -> {
                        if (generation == loadGeneration.get()) {
                            releaseMesh();
                            requestRender();
                        }
                    });
                    return;
                }
                long start = System.currentTimeMillis();
                HeightField field = stock.get().createHeightField();
                new StockSimulation(field, cuts, tools).runAll();
                List<HeightFieldMesher.Mesh> finalMesh = HeightFieldMesher.meshByDepth(field, DEPTH_BANDS);
                LOGGER.fine(() -> "Simulated " + cuts.size() + " cuts into " + field.columns() + "x" + field.rows()
                        + " nodes and " + triangleCount(finalMesh) + " triangles in "
                        + (System.currentTimeMillis() - start) + " ms");
                if (generation != loadGeneration.get()) {
                    return;
                }
                program = new Program(cuts, stock.get(), tools, finalMesh);
                upload(generation, finalMesh, stock.get().bounds());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Could not simulate the stock for " + file, e);
            }
        });
    }

    /**
     * Coalesces the flood of completed commands into one progress step per interval.
     */
    private void scheduleProgress() {
        if (program == null || simulator == null || !isEnabled()) {
            return;
        }
        if (progressScheduled.compareAndSet(false, true)) {
            try {
                simulator.schedule(this::progressStep, PROGRESS_INTERVAL_MS, TimeUnit.MILLISECONDS);
            } catch (RejectedExecutionException e) {
                // Detached while a command finished
                progressScheduled.set(false);
            }
        }
    }

    private void progressStep() {
        progressScheduled.set(false);
        Program current = program;
        int completed = completedCommand;
        if (current == null || completed == NOTHING_COMPLETED) {
            return;
        }
        try {
            if (progress == null || progress.cuts() != current.cuts()) {
                progress = new StockSimulation(current.stock().createHeightField(), current.cuts(), current.tools());
            }
            if (progress.advanceTo(completed)) {
                List<HeightFieldMesher.Mesh> mesh = HeightFieldMesher.meshByDepth(progress.field(), current.finalMesh().size());
                upload(loadGeneration.get(), mesh, current.stock().bounds());
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not update the stock progress", e);
        }
    }

    private void showFinalResult() {
        Program current = program;
        if (current == null || simulator == null) {
            return;
        }
        simulator.execute(() -> {
            progress = null;
            if (program == current) {
                upload(loadGeneration.get(), current.finalMesh(), current.stock().bounds());
            }
        });
    }

    private void upload(int generation, List<HeightFieldMesher.Mesh> bandMeshes, Bounds3 stockBounds) {
        Platform.runLater(() -> {
            if (generation != loadGeneration.get() || scene == null) {
                return;
            }
            releaseMesh();
            for (int band = 0; band < bandMeshes.size(); band++) {
                HeightFieldMesher.Mesh mesh = bandMeshes.get(band);
                if (!mesh.isEmpty()) {
                    meshes.add(scene.context().upload(mesh.vertices(), VertexLayout.MESH));
                    meshBands.add(Math.min(band, DEPTH_BANDS - 1));
                }
            }
            if (!meshes.isEmpty()) {
                bounds = stockBounds;
            }
            StockService.getInstance().setBounds(Optional.ofNullable(bounds));
            requestRender();
        });
    }

    private void releaseMesh() {
        if (scene != null) {
            meshes.forEach(scene.context()::release);
        }
        meshes.clear();
        meshBands.clear();
        bounds = null;
        StockService.getInstance().setBounds(Optional.empty());
    }

    private static int triangleCount(List<HeightFieldMesher.Mesh> bandMeshes) {
        return bandMeshes.stream().mapToInt(HeightFieldMesher.Mesh::triangleCount).sum();
    }

    private void requestRender() {
        if (scene != null) {
            scene.requestRender();
        }
    }

    /**
     * Band zero is the stock colour. The cut bands run from a shade already some way towards the
     * deep cut colour, so the shallowest cut is visibly different from untouched material, down to
     * the deep cut colour itself at the bottom.
     */
    private void readColor() {
        VisualizerSettings settings = VisualizerSettings.getInstance();
        Color stock = Color.web(settings.colorStockProperty().get());
        Color deep = Color.web(settings.colorStockDeepProperty().get());
        for (int band = 0; band < DEPTH_BANDS; band++) {
            double mix = band == 0 ? 0 : SHALLOW_MIX + (1 - SHALLOW_MIX) * (band - 1) / (double) (DEPTH_BANDS - 2);
            Color color = stock.interpolate(deep, mix);
            bandColors[band][0] = (float) color.getRed();
            bandColors[band][1] = (float) color.getGreen();
            bandColors[band][2] = (float) color.getBlue();
            bandColors[band][3] = (float) color.getOpacity();
        }
    }

    private static boolean isEnabled() {
        return VisualizerSettings.getInstance().showStockProperty().get();
    }

    private static File currentFile() {
        return LookupService.lookup(BackendAPI.class).getGcodeFile();
    }
}
