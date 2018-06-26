/*
    Copyright 2016-2018 Will Winder

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

/**
 *
 * @author wwinder
 */
public abstract class Renderable implements Comparable<Renderable> {
    Integer priority;
    Boolean enabled;
    String title;

    /**
     * Construct with a priority number. Objects should be rendered from highest
     * to lowest priority;
     */
    public Renderable(int priority, String title) {
        this.title = title;
        this.priority = priority;
        this.enabled = true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Renderable)) return false;
        Renderable that = (Renderable)obj;
        return 
                Objects.equals(this.title, that.title) &&
                Objects.equals(this.enabled, that.enabled) &&
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

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return this.enabled;
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
    abstract public boolean rotate();

    /**
     * Indicates whether the object should be centered prior to calling draw.
     */
    abstract public boolean center();

    abstract public void init(GLAutoDrawable drawable);
    abstract public void reloadPreferences(VisualizerOptions vo);
    abstract public void draw(GLAutoDrawable drawable, boolean idle, Position machineCoord, Position workCoord, Position objectMin, Position objectMax, double scaleFactor, Position mouseWorldCoordinates, Position rotation);
}
