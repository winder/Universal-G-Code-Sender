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
        double side = VisualizerUtils.findMaxSide(focusMin, focusMax) * 0.05;
        Point3d bottomLeft = new Point3d(focusMin);
        Point3d topRight = new Point3d(focusMax);

        bottomLeft.x -= side;
        bottomLeft.y -= side;
        topRight.x += side;
        topRight.y += side;

        GL2 gl = drawable.getGL().getGL2();
        gl.glPushMatrix();
            gl.glRotated(90, 1.0, 0.0, 0.0);
            gl.glColor4f(.3f,.3f,.3f, .4f);

            // floor - cover entire model and a little extra.
            gl.glPushMatrix();
                gl.glBegin(gl.GL_QUADS);
                    gl.glVertex3d(bottomLeft.x, 0, -bottomLeft.y);
                    gl.glVertex3d(bottomLeft.x, 0, -topRight.y);
                    gl.glVertex3d(topRight.x  , 0, -topRight.y);
                    gl.glVertex3d(topRight.x  , 0, -bottomLeft.y);
                gl.glEnd();
            gl.glPopMatrix();
            
            double width = Math.abs(bottomLeft.x) + Math.abs(topRight.x);
            double height = Math.abs(bottomLeft.y) + Math.abs(topRight.y);

            double stepSize = Math.min(width/20, height/20);
            // grid
            gl.glBegin(GL_LINES);
            for(double x=bottomLeft.x;x<=topRight.x;x+=stepSize) {
                for (double y=bottomLeft.y; y<=topRight.y; y+=stepSize) {
                    if (x==0) { gl.glColor3d(.6f,.3f,.3f); } else { gl.glColor3d(.25,.25,.25); };
                    gl.glVertex3d(x,  0.001, -bottomLeft.y);
                    gl.glVertex3d(x,  0.001, -topRight.y);

                    gl.glVertex3d(x, -0.001, -bottomLeft.y);
                    gl.glVertex3d(x, -0.001, -topRight.y);
                    
                    if (x==0) { gl.glColor3d(.3,.3,.6); } else { gl.glColor3d(.25,.25,.25); };
                    gl.glVertex3d(bottomLeft.x,  0.001, -y);
                    gl.glVertex3d(topRight.x  ,  0.001, -y);

                    gl.glVertex3d(bottomLeft.x, -0.001, -y);
                    gl.glVertex3d(topRight.x  , -0.001, -y);
                }
            };
            gl.glEnd();
        gl.glPopMatrix();
    }
}
