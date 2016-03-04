/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.ugs.nbm.visualizer.util;

import static com.jogamp.opengl.GL.GL_CULL_FACE;
import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.willwinder.universalgcodesender.visualizer.VisualizerUtils;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class SizeDisplay extends Renderable {

    private static DecimalFormat formatter = new DecimalFormat("#.##");
    private TextRenderer renderer;

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
        renderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 72));
        renderer.setColor(0.2f, 0.2f, 0.2f, 1f);
    }

    @Override
    public void draw(GLAutoDrawable drawable, boolean idle, Point3d workCoord, Point3d focusMin, Point3d focusMax, double scaleFactor) {
        if (idle) return;

        double maxSide = VisualizerUtils.findMaxSide(focusMin, focusMax);
        double buffer = maxSide * 0.03;
        double offset = buffer*2;

        GL2 gl = drawable.getGL().getGL2();
        gl.glColor4f(0.5f,0.5f,0.5f,1f);
        gl.glLineWidth(2f);

            // X
            gl.glPushMatrix();
                gl.glTranslated(0, -offset, 0);
                gl.glBegin(GL2.GL_LINES);
                    gl.glVertex3d(focusMin.x, focusMin.y, 0);
                    gl.glVertex3d(focusMin.x, focusMin.y-offset, 0);
                    gl.glVertex3d(focusMin.x, focusMin.y-buffer, 0);
                    gl.glVertex3d(focusMax.x, focusMin.y-buffer, 0);
                    gl.glVertex3d(focusMax.x, focusMin.y-offset, 0);
                    gl.glVertex3d(focusMax.x, focusMin.y, 0);
                gl.glEnd();
                
                {
                renderer.begin3DRendering();
                double xSize = focusMax.x-focusMin.x;
                String text = formatter.format(xSize) + " mm";
                Rectangle2D bounds = renderer.getBounds(text);
                float w = (float) bounds.getWidth();
                float h = (float) bounds.getHeight();

                float textScaleFactor = (float)(buffer/h);
                // Center text and move to line.
                gl.glTranslated(
                        (focusMin.x+focusMax.x)/2-(w*textScaleFactor/2),
                        focusMin.y-offset, 0);
                renderer.draw3D(text,
                        0f, 0f,
                        0f, textScaleFactor);
                renderer.end3DRendering();
                }
            gl.glPopMatrix();

            // Y
            gl.glPushMatrix();
                gl.glTranslated(-offset, 0, 0);
                gl.glBegin(GL2.GL_LINES);
                    gl.glVertex3d(focusMin.x       , focusMin.y, 0);
                    gl.glVertex3d(focusMin.x-offset, focusMin.y, 0);
                    gl.glVertex3d(focusMin.x-buffer, focusMin.y, 0);
                    gl.glVertex3d(focusMin.x-buffer, focusMax.y, 0);
                    gl.glVertex3d(focusMin.x-offset, focusMax.y, 0);
                    gl.glVertex3d(focusMin.x       , focusMax.y, 0);
                gl.glEnd();

                {
                renderer.begin3DRendering();
                double ySize = focusMax.y-focusMin.y;
                String text = formatter.format(ySize) + " mm";
                Rectangle2D bounds = renderer.getBounds(text);
                float w = (float) bounds.getWidth();
                float h = (float) bounds.getHeight();

                float textScaleFactor = (float)(buffer/h);
                // Center text and move to line.
                gl.glRotated(90,0,0,1);
                gl.glTranslated(
                        (focusMin.y+focusMax.y)/2-(w*textScaleFactor/2),
                        -focusMin.x+buffer*1.1, 0);
                renderer.draw3D(text,
                        0f, 0f,
                        0f, textScaleFactor);
                renderer.end3DRendering();
                }
            gl.glPopMatrix();

            // Z
            gl.glPushMatrix();
                gl.glTranslated(offset, 0, 0);
                gl.glBegin(gl.GL_LINES);
                    gl.glVertex3d(focusMax.x       , focusMin.y, focusMin.z);
                    gl.glVertex3d(focusMax.x+offset, focusMin.y, focusMin.z);
                    gl.glVertex3d(focusMax.x+buffer, focusMin.y, focusMin.z);
                    gl.glVertex3d(focusMax.x+buffer, focusMin.y, focusMax.z);
                    gl.glVertex3d(focusMax.x+offset, focusMin.y, focusMax.z);
                    gl.glVertex3d(focusMax.x       , focusMin.y, focusMax.z);
                gl.glEnd();

                {
                renderer.begin3DRendering();
                double zSize = focusMax.z-focusMin.z;
                String text = formatter.format(zSize) + " mm";
                Rectangle2D bounds = renderer.getBounds(text);
                float w = (float) bounds.getWidth();
                float h = (float) bounds.getHeight();

                float textScaleFactor = (float)(buffer/h);
                // Center text and move to line.
                gl.glRotated(90,1,0,0);
                gl.glTranslated(
                        focusMax.x + buffer*1.1,
                        (focusMin.z+focusMax.z)/2-(h*textScaleFactor/2),
                        //focusMin.y-offset,
                        -focusMin.y);
                renderer.draw3D(text,
                        0f, 0f,
                        0f, textScaleFactor);
                renderer.end3DRendering();
                }
            gl.glPopMatrix();
    }
    
}
