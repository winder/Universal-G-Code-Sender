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
public abstract class Renderable {
    Point3d workCoord;
    double scaleFactor;

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
