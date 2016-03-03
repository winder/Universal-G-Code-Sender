/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.ugs.nbm.visualizer.util;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.willwinder.universalgcodesender.visualizer.VisualizerUtils;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class SizeDisplay extends Renderable {

    public SizeDisplay() {
        super(3);
    }

    @Override
    public boolean rotate() {
        return true;
    }

    @Override
    public boolean center() {
        return true;
    }

    @Override
    public void init(GLAutoDrawable drawable) {
    }

    @Override
    public void draw(GLAutoDrawable drawable, boolean idle, Point3d workCoord, Point3d focusMin, Point3d focusMax, double scaleFactor) {
        if (idle) return;

        double maxSide = VisualizerUtils.findMaxSide(focusMin, focusMax);
        double buffer = maxSide * 0.05;
        double offset = buffer;

        GL2 gl = drawable.getGL().getGL2();
        gl.glColor4f(1f,0f,0f,1f);

        //gl.glLineStipple(0, 0);
        gl.glLineWidth(1.75f);
            // X
            gl.glPushMatrix();
                gl.glTranslated(0, -offset, 0);
                gl.glBegin(gl.GL_LINE_STRIP);
                    gl.glVertex3d(focusMin.x, focusMin.y, 0);
                    gl.glVertex3d(focusMin.x, focusMin.y-buffer, 0);
                    gl.glVertex3d(focusMax.x, focusMin.y-buffer, 0);
                    gl.glVertex3d(focusMax.x, focusMin.y, 0);
                gl.glEnd();
            gl.glPopMatrix();

            // Y
            gl.glPushMatrix();
                gl.glTranslated(-offset, 0, 0);
                gl.glBegin(gl.GL_LINE_STRIP);
                    gl.glVertex3d(focusMin.x       , focusMin.y, 0);
                    gl.glVertex3d(focusMin.x-buffer, focusMin.y, 0);
                    gl.glVertex3d(focusMin.x-buffer, focusMax.y, 0);
                    gl.glVertex3d(focusMin.x       , focusMax.y, 0);
                gl.glEnd();
            gl.glPopMatrix();

            // Z
            gl.glPushMatrix();
                gl.glTranslated(offset, 0, 0);
                gl.glBegin(gl.GL_LINE_STRIP);
                    gl.glVertex3d(focusMax.x       , focusMin.y, focusMin.z);
                    gl.glVertex3d(focusMax.x+buffer, focusMin.y, focusMin.z);
                    gl.glVertex3d(focusMax.x+buffer, focusMin.y, focusMax.z);
                    gl.glVertex3d(focusMax.x       , focusMin.y, focusMax.z);
                gl.glEnd();
            gl.glPopMatrix();
    }
    
}
