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

import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Renderable;
import com.willwinder.universalgcodesender.fx.model.WorkspaceBounds;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.function.Consumer;

/**
 * Lets components outside the visualizer add things to it and ask it to frame the workspace,
 * without holding a reference to the visualizer itself.
 */
public class VisualizerService {
    private static final VisualizerService INSTANCE = new VisualizerService();
    private final ObservableList<Renderable> renderables = FXCollections.observableArrayList();
    private volatile Consumer<WorkspaceBounds> centerOnBoundsHandler;
    private final BooleanProperty toolpathLoading = new SimpleBooleanProperty(false);
    private final StringProperty rendererDescription = new SimpleStringProperty("");

    public static VisualizerService getInstance() {
        return INSTANCE;
    }

    /**
     * Whether the visualizer is still turning a loaded program into its toolpath. Set on the
     * JavaFX thread, so it can be bound to the UI.
     */
    public ReadOnlyBooleanProperty toolpathLoadingProperty() {
        return toolpathLoading;
    }

    public void setToolpathLoading(boolean loading) {
        if (Platform.isFxApplicationThread()) {
            toolpathLoading.set(loading);
        } else {
            Platform.runLater(() -> toolpathLoading.set(loading));
        }
    }

    /**
     * Describes the graphics device and settings the visualizer renders with, for example
     * {@code "NVIDIA GeForce RTX 3060 (Vulkan, 4x MSAA)"}. Empty until a visualizer has been
     * created, and holds the failure reason if the renderer could not be initialized.
     */
    public ReadOnlyStringProperty rendererDescriptionProperty() {
        return rendererDescription;
    }

    public void setRendererDescription(String description) {
        if (Platform.isFxApplicationThread()) {
            rendererDescription.set(description);
        } else {
            Platform.runLater(() -> rendererDescription.set(description));
        }
    }

    /**
     * The renderables registered from outside the visualizer. Components add and remove theirs
     * here without knowing whether a visualizer is showing; the visualizer picks up whatever is
     * registered.
     */
    public ObservableList<Renderable> getRenderables() {
        return renderables;
    }

    public void addRenderable(Renderable renderable) {
        if (!renderables.contains(renderable)) {
            renderables.add(renderable);
        }
    }

    public void removeRenderable(Renderable renderable) {
        renderables.remove(renderable);
    }

    /**
     * Registers the handler that knows how to recenter and fit the view on a bounding box. Pass
     * null to unregister. Registered by the visualizer pane; invoked by the center camera action.
     */
    public void setCenterOnBoundsHandler(Consumer<WorkspaceBounds> handler) {
        this.centerOnBoundsHandler = handler;
    }

    /**
     * Asks the visualizer to recenter and fit the view on the given workspace bounds. No-op when
     * no visualizer is registered.
     */
    public void centerOnBounds(WorkspaceBounds bounds) {
        Consumer<WorkspaceBounds> handler = centerOnBoundsHandler;
        if (handler == null || bounds == null) {
            return;
        }
        if (Platform.isFxApplicationThread()) {
            handler.accept(bounds);
        } else {
            Platform.runLater(() -> handler.accept(bounds));
        }
    }
}
