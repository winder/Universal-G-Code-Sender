/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.ugs.nbm.visualizer.util;

import static com.jogamp.opengl.GL.GL_LINES;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.willwinder.universalgcodesender.visualizer.VisualizerUtils;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class Grid extends Renderable {
    public Grid() {
        super(5);
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
    public void draw(GLAutoDrawable drawable, Point3d workCoord, Point3d focusMin, Point3d focusMax, double scaleFactor) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glPushMatrix();
            gl.glRotated(90, 1.0, 0.0, 0.0);
            gl.glColor4f(.3f,.3f,.3f, .5f);
            // floor - cover entire model and a little extra.
            double side = VisualizerUtils.findMaxSide(focusMin, focusMax) * 0.05;
            gl.glPushMatrix();
                gl.glBegin(gl.GL_QUADS);
                gl.glVertex3d(focusMin.x-side, 0,-focusMin.y+side);
                gl.glVertex3d(focusMin.x-side, 0,-focusMax.y-side);
                gl.glVertex3d(focusMax.x+side, 0,-focusMax.y-side);
                gl.glVertex3d(focusMax.x+side, 0,-focusMin.y+side);
                gl.glEnd();
            gl.glPopMatrix();

            // grid
            gl.glBegin(GL_LINES);
            for(double i=-side;i<=side;i++) {
                if (i==0) { gl.glColor3d(.6f,.3f,.3f); } else { gl.glColor3d(.25,.25,.25); };
                gl.glVertex3d(i,0.001,-side);
                gl.glVertex3d(i,0.001,side);
                gl.glVertex3d(i,-0.001,-side);
                gl.glVertex3d(i,-0.001,side);
                if (i==0) { gl.glColor3d(.3,.3,.6); } else { gl.glColor3d(.25,.25,.25); };
                gl.glVertex3d(-side,0.001,i);
                gl.glVertex3d(side,0.001,i);
                gl.glVertex3d(-side,-0.001,i);
                gl.glVertex3d(side,-0.001,i);
            };
            gl.glEnd();

        gl.glPopMatrix();
    }
    
}
