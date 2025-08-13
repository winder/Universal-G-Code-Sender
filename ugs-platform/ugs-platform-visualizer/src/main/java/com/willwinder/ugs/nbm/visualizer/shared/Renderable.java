/*
    Copyright 2016-2025 Will Winder

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
package com.willwinder.ugs.nbm.visualizer.shared;

import com.jogamp.opengl.GLAutoDrawable;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.universalgcodesender.model.Position;

import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author wwinder
 */
public abstract class Renderable implements Comparable<Renderable> {
    private final Integer priority;
    private final String title;

    /**
     * A locally cached setting for if the renderable is enabled.
     * This is needed to make the rendering fast enough to not slow down the rendering thread.
     */
    private boolean isEnabled;

    private final Set<RenderableListener> listeners = ConcurrentHashMap.newKeySet();
    private final String enabledOptionKey;

    /**
     * Construct with a priority number. Objects should be rendered from highest
     * to lowest priority;
     */
    protected Renderable(int priority, String title, String enabledOptionKey) {
        this.title = title;
        this.priority = priority;
        this.isEnabled = VisualizerOptions.getBooleanOption(enabledOptionKey, true);
        this.enabledOptionKey = enabledOptionKey;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Renderable)) return false;
        Renderable that = (Renderable)obj;
        return
                Objects.equals(this.title, that.title) &&
                Objects.equals(this.isEnabled, that.isEnabled) &&
                Objects.equals(this.priority, that.priority);
    }

    @Override
    public int hashCode() {
        return this.priority.hashCode();
    }

    @Override
    public int compareTo(Renderable o) {
        return -1 * this.priority.compareTo(o.priority);
    }

    /**
     * Enables the feature in the configuration
     *
     * @param enabled set to true for enabling it in the configuration
     */
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
        VisualizerOptions.setBooleanOption(enabledOptionKey, enabled);
        notifyListeners();
    }

    /**
     * Returns true if the setting for the renderable is enabled
     *
     * @return true if enabled in the configuration
     */
    public boolean isEnabled() {
        return isEnabled;
    }

    public String getTitle() {
        return this.title;
    }

    /**
     * Indicates whether the object should have lighting enabled.
     */
    public boolean enableLighting() {
        return true;
    }

    /**
     * Indicates whether the object should be rotated prior to calling draw.
     */
    public abstract boolean rotate();

    /**
     * Indicates whether the object should be centered prior to calling draw.
     */
    public abstract boolean center();

    public abstract void init(GLAutoDrawable drawable);

    public void reloadPreferences(VisualizerOptions vo) {
        isEnabled = VisualizerOptions.getBooleanOption(enabledOptionKey, true);
    }

    public abstract void draw(GLAutoDrawable drawable, boolean idle, Position machineCoord, Position workCoord, Position objectMin, Position objectMax, double scaleFactor, Position mouseWorldCoordinates, Position rotation);

    public void addListener(RenderableListener listener) {
        listeners.add(listener);
    }

    protected void notifyListeners() {
        listeners.forEach(RenderableListener::onChanged);
    }
}
