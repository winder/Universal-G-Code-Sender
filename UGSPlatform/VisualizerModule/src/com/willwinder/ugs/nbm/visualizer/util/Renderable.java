/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.ugs.nbm.visualizer.util;

import com.jogamp.opengl.GLAutoDrawable;

/**
 *
 * @author wwinder
 */
public interface Renderable {
    /**
     * Indicates whether the object should be rotated prior to calling draw.
     */
    public boolean rotate();

    /**
     * Indicates whether the object should be centered prior to calling draw.
     */
    public boolean center();

    public void init(GLAutoDrawable drawable);
    public void draw(GLAutoDrawable drawable);
}
