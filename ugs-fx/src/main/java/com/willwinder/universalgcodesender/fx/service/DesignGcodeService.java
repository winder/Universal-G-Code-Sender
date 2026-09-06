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

import com.google.common.io.Files;
import com.willwinder.ugs.designer.entities.EntityListener;
import com.willwinder.ugs.designer.entities.EventType;
import com.willwinder.ugs.designer.io.gcode.GcodeDesignWriter;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerEventType;
import com.willwinder.ugs.designer.logic.ControllerListener;
import com.willwinder.ugs.designer.logic.SettingsListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.File;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Keeps the loaded G-code in step with the design being edited. Every change to the design or
 * its settings schedules a regeneration into the workspace's temporary G-code file, debounced so
 * that a drag regenerates once when it settles rather than on every mouse move. Generation and
 * the backend's parsing both run on the service's own thread; the backend's events reach the UI
 * on the JavaFX thread through {@link FxEventDispatcher}.
 */
public final class DesignGcodeService {
    private static final Logger LOGGER = Logger.getLogger(DesignGcodeService.class.getName());
    private static final long DEBOUNCE_MILLIS = 500;
    private static final Set<EventType> DESIGN_CHANGE_EVENTS = EnumSet.of(
            EventType.MOVED, EventType.RESIZED, EventType.ROTATED, EventType.PATH_CHANGED,
            EventType.CHILD_ADDED, EventType.CHILDREN_ADDED, EventType.CHILD_REMOVED,
            EventType.CHILDREN_REMOVED, EventType.SETTINGS_CHANGED);

    private final Controller controller;
    private final BackendAPI backend;
    private final File gcodeFile;
    private final Executor uiExecutor;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "design-gcode");
        thread.setDaemon(true);
        return thread;
    });
    private final EntityListener designListener = event -> {
        if (DESIGN_CHANGE_EVENTS.contains(event.getType())) {
            requestRegeneration();
        }
    };
    private final SettingsListener settingsListener = this::requestRegeneration;
    private final ControllerListener controllerListener = event -> {
        if (event == ControllerEventType.NEW_DRAWING) {
            requestRegeneration();
        }
    };
    private final BooleanProperty busy = new SimpleBooleanProperty(false);
    private final AtomicInteger generation = new AtomicInteger();
    private ScheduledFuture<?> pending;
    private boolean bound;

    /**
     * @param name the design's name, used for the temporary G-code file so the program shows
     *             under a recognisable name
     */
    public DesignGcodeService(Controller controller, BackendAPI backend, String name) {
        this(controller, backend, name, Platform::runLater);
    }

    /**
     * @param uiExecutor runs the updates of {@link #busyProperty()}; the JavaFX thread in the
     *                   application
     */
    DesignGcodeService(Controller controller, BackendAPI backend, String name, Executor uiExecutor) {
        this.controller = controller;
        this.backend = backend;
        this.gcodeFile = new File(Files.createTempDir(), name + ".gcode");
        this.uiExecutor = uiExecutor;
    }

    /**
     * Starts following the design. Nothing is generated until a change happens or
     * {@link #regenerateNow()} is called.
     */
    public synchronized void bind() {
        if (bound) {
            return;
        }
        bound = true;
        controller.getModel().getRootEntity().addListener(designListener);
        controller.getSettings().addListener(settingsListener);
        controller.addListener(controllerListener);
    }

    /**
     * Stops following the design and drops any scheduled regeneration.
     */
    public synchronized void unbind() {
        if (!bound) {
            return;
        }
        bound = false;
        controller.getModel().getRootEntity().removeListener(designListener);
        controller.getSettings().removeListener(settingsListener);
        controller.removeListener(controllerListener);
        cancelPending();
        executor.shutdownNow();
        uiExecutor.execute(() -> busy.set(false));
    }

    /**
     * Whether a regeneration is pending or running. Changes on the UI executor's thread, the
     * JavaFX thread in the application, so it can be bound to the UI directly.
     */
    public ReadOnlyBooleanProperty busyProperty() {
        return busy;
    }

    /**
     * Generates the G-code and loads it right away, on the calling thread.
     */
    public void regenerateNow() throws Exception {
        synchronized (this) {
            cancelPending();
        }
        new GcodeDesignWriter().write(gcodeFile, controller);
        backend.setGcodeFile(gcodeFile);
    }

    /**
     * Schedules a regeneration once the design has been quiet for the debounce period.
     */
    public synchronized void requestRegeneration() {
        schedule(DEBOUNCE_MILLIS);
    }

    /**
     * Schedules a regeneration right away on the service's thread, for a freshly opened design.
     */
    public synchronized void regenerateAsync() {
        schedule(0);
    }

    private void schedule(long delayMillis) {
        if (!bound) {
            return;
        }
        cancelPending();
        generation.incrementAndGet();
        pending = executor.schedule(this::regenerate, delayMillis, TimeUnit.MILLISECONDS);
        uiExecutor.execute(() -> busy.set(true));
    }

    /**
     * Writes and loads the G-code on the service's own thread, keeping the parsing off the
     * JavaFX thread.
     */
    private void regenerate() {
        int started = generation.get();
        if (backend.isSendingFile()) {
            // Never swap the program out from under a running job; try again once it has settled.
            requestRegeneration();
            return;
        }
        try {
            new GcodeDesignWriter().write(gcodeFile, controller);
            backend.setGcodeFile(gcodeFile);
        } catch (Exception e) {
            if (generation.get() == started) {
                LOGGER.log(Level.WARNING, "Could not regenerate the design G-code", e);
            } else {
                LOGGER.log(Level.FINE, "Abandoned a regeneration superseded by a newer change");
            }
        } finally {
            uiExecutor.execute(() -> finish(started));
        }
    }

    /**
     * Clears the busy flag unless another regeneration was requested after this one started.
     */
    private void finish(int started) {
        if (generation.get() == started) {
            busy.set(false);
        }
    }

    /**
     * Drops a regeneration that has not started and interrupts one that is running; both the
     * generation and the backend's preprocessing give up when their thread is interrupted.
     */
    private void cancelPending() {
        if (pending != null) {
            pending.cancel(true);
            pending = null;
        }
    }
}
