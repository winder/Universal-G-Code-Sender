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

import com.willwinder.universalgcodesender.fx.component.visualizer.DragHandler;
import com.willwinder.universalgcodesender.fx.component.visualizer.models.Model;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.input.MouseEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class VisualizerService {
    private static final VisualizerService INSTANCE = new VisualizerService();

    private final ObservableList<Model> models = FXCollections.observableArrayList();
    private final List<Consumer<MouseEvent>> backgroundClickHandlers = new CopyOnWriteArrayList<>();
    private volatile DrawGestureProvider drawGestureProvider;

    public static VisualizerService getInstance() {
        return INSTANCE;
    }

    public ObservableList<Model> getModels() {
        return models;
    }

    public void addModel(Model model) {
        models.add(model);
    }

    public void removeModel(Model model) {
        models.remove(model);
    }

    public void onZoomChange(double zoom) {
        if (Platform.isFxApplicationThread()) {
            models.forEach(model -> model.onZoomChange(zoom));
        } else {
            Platform.runLater(() -> models.forEach(model -> model.onZoomChange(zoom)));
        }
    }

    public void addBackgroundClickHandler(Consumer<MouseEvent> handler) {
        backgroundClickHandlers.add(handler);
    }

    public void removeBackgroundClickHandler(Consumer<MouseEvent> handler) {
        backgroundClickHandlers.remove(handler);
    }

    public void fireBackgroundClick(MouseEvent event) {
        backgroundClickHandlers.forEach(h -> h.accept(event));
    }

    /**
     * Registers a provider that can begin a designer "draw" drag gesture. Pass null to
     * unregister. Used so the generic visualizer can hand off a primary-button press to
     * the designer when a drawing tool is active, without knowing about designer tools.
     */
    public void setDrawGestureProvider(DrawGestureProvider provider) {
        this.drawGestureProvider = provider;
    }

    /**
     * Asks the registered provider (if any) to begin a draw gesture at the given designer
     * (model mm) coordinates. Returns a {@link DragHandler} driving the gesture, or null
     * when no drawing tool is active and the press should be handled normally.
     */
    public DragHandler beginDrawGesture(double designerX, double designerY) {
        DrawGestureProvider provider = drawGestureProvider;
        return provider == null ? null : provider.beginGesture(designerX, designerY);
    }

    @FunctionalInterface
    public interface DrawGestureProvider {
        DragHandler beginGesture(double designerX, double designerY);
    }
}
