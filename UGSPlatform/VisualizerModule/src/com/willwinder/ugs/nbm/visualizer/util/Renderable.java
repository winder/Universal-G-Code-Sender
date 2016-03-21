/*
    Copywrite 2016 Will Winder

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
package com.willwinder.ugs.nbm.visualizer.util;

import com.jogamp.opengl.GLAutoDrawable;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import java.util.Objects;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public abstract class Renderable implements Comparable<Renderable> {
    Integer priority;

    /**
     * Construct with a priority number. Objects should be rendered from highest
     * to lowest priority;
     */
    Renderable(int priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Renderable)) return false;
        Renderable that = (Renderable)obj;
        return Objects.equals(this.priority, that.priority);
    }

    @Override
    public int hashCode() {
        return priority.hashCode();
    }

    @Override
    public int compareTo(Renderable o) {
        return -1 * priority.compareTo(o.priority);
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
    abstract public void draw(GLAutoDrawable drawable, boolean idle, Point3d workCoord, Point3d focusMin, Point3d focusMax, double scaleFactor);
}
