/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.ugs.nbm.visualizer.util;

import com.jogamp.opengl.GLAutoDrawable;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public abstract class Renderable implements Comparable<Renderable> {
    Point3d workCoord;
    double scaleFactor;
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
        return this.priority == that.priority;
    }

    @Override
    public int hashCode() {
        return priority.hashCode();
    }

    @Override
    public int compareTo(Renderable o) {
        return -1 * priority.compareTo(o.priority);
    }

    public void setWorkCoord(Point3d p) {
        workCoord = p;
    }

    public void setScaleFactor(double sf) {
        scaleFactor = sf;
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
    abstract public void draw(GLAutoDrawable drawable);
}
