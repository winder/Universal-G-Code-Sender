/**
 * Draws a vertical line along the Z axis at the (X,Y) coordinate where the
 * mouse is considered to be.
 */
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

import com.jogamp.nativewindow.NativeSurface;
import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_LINES;
import static com.jogamp.opengl.GL.GL_VIEWPORT;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import java.awt.Color;
import java.awt.Point;
import javax.vecmath.Point3d;
import jogamp.nativewindow.macosx.OSXUtil;

/**
 *
 * @author wwinder
 */
public class MouseOver extends Renderable {
    private static final GLU GLU = new GLU();
    private static GLUquadric GQ;

    public MouseOver() {
        super(8);
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
        GQ = GLU.gluNewQuadric();
    }

    @Override
    public void reloadPreferences(VisualizerOptions vo) {
    }

    public Point3d findGridLocation(GLAutoDrawable drawable, Point3d rotation, Point mouseCoordinates) {

        String formatMouseCoords = "Mouse coords before (%d,%d), mouse coords after (%d, %d), translate coords (%d,%d)";
        String formatWorldCoords = "World coords at z=%s are (%f,%f,%f)";

        int[] raw= {mouseCoordinates.x, mouseCoordinates.y};
        int[] coords = drawable.getNativeSurface().convertToPixelUnits(raw);
        int mouseX = coords[0];
        int mouseY = coords[1];
        int width = drawable.getSurfaceWidth();
        int height = drawable.getSurfaceHeight();

        //System.out.println("native width: " + panel.getNativeSurface().getSurfaceWidth());

        int translateX = mouseX - width/2;
        int translateY = mouseY - height/2;
        System.out.println("Width: " + width + ", Height: " + height);
        System.out.println(String.format(formatMouseCoords,
                mouseCoordinates.x, mouseCoordinates.y,
                mouseX, mouseY,
                translateX, translateY));
        GL gl1 = drawable.getGL();
        GL2 gl2 = drawable.getGL().getGL2();

        int[] viewPort = new int[4];
        double[] modelViewMatrix = new double[16];
        double[] projectionMatrix = new double[16];
        double wcoordNear[] = new double[4];
        double wcoordFar[] = new double[4];

        gl2.glGetIntegerv(GL.GL_VIEWPORT, viewPort, 0);
        gl2.glGetDoublev( GL2.GL_MODELVIEW_MATRIX, modelViewMatrix, 0);
        gl2.glGetDoublev( GL2.GL_PROJECTION_MATRIX, projectionMatrix, 0);

        //GL y coord pos - note viewport[3] is height of window in pixels
        int realy = viewPort[3] - (int)mouseY - 1;
        //realy = viewport[3] - (GLint) y - 1;

        double nearDepth = 0.0;
        double farDepth = 1.0;

        // FAR
        GLU.gluUnProject((double)mouseX, (double)realy, farDepth,
                modelViewMatrix, 0,
                projectionMatrix, 0,
                viewPort, 0,
                wcoordFar, 0);
        System.out.println(String.format(formatWorldCoords, farDepth,
                wcoordFar[0], wcoordFar[1], wcoordFar[2]));

        // NEAR
        GLU.gluUnProject((double)mouseX, (double)realy, nearDepth,
                modelViewMatrix, 0,
                projectionMatrix, 0,
                viewPort, 0,
                wcoordNear, 0);
        System.out.println(String.format(formatWorldCoords, nearDepth,
                wcoordNear[0], wcoordNear[1], wcoordNear[2]));

        gl2.glPushMatrix();
            gl2.glColor4fv(VisualizerOptions.colorToFloatArray(Color.CYAN), 0);
            gl2.glTranslated(wcoordFar[0], wcoordFar[1], wcoordFar[2]);
            GLU.gluSphere(GQ, 0.5, 10, 10);
        gl2.glPopMatrix();


        gl2.glPushMatrix();
            gl2.glColor4fv(VisualizerOptions.colorToFloatArray(Color.MAGENTA), 0);
            gl2.glTranslated(wcoordNear[0], wcoordNear[1], wcoordNear[2]);
            GLU.gluSphere(GQ, 0.25, 10, 10);
        gl2.glPopMatrix();

        

        /*
        gl2.glColor4fv(VisualizerOptions.colorToFloatArray(Color.GRAY), 0);
        gl2.glPushMatrix();
        gl2.glBegin(GL_LINES);
            gl2.glVertex3dv(wcoordFar, 0);
            gl2.glVertex3dv(wcoordNear, 0);
            //gl2.glVertex3d(0,0,0);
        gl2.glPopMatrix();
        */
        
        
        /*
        gl2.glColor4fv(VisualizerOptions.colorToFloatArray(Color.BLACK), 0);
        gl2.glPushMatrix();
            gl2.glTranslated(wcoordNear[0], wcoordNear[1], wcoordFar[2]);
            GLU.gluSphere(GQ, 10, 10, 10);
        gl2.glPopMatrix();
        */
        

        return null;
    }

    @Override
    public void draw(GLAutoDrawable drawable, boolean idle, Point3d workCoord, Point3d focusMin, Point3d focusMax, double scaleFactor, Point3d rotation, Point mouseCoordinates) {
        if (mouseCoordinates == null) return;

        findGridLocation(drawable, rotation, mouseCoordinates);
        

        /*
        GL2 gl = drawable.getGL().getGL2();
        gl.glPushMatrix();
            gl.glLineWidth(1.5f);
            // grid
            //gl.glTranslated(mouseCoordinates.x-width, mouseCoordinates.y-height, workCoord.z);
            gl.glScaled(1./scaleFactor, 1./scaleFactor, 1./scaleFactor);
            gl.glBegin(GL_LINES);
                // Z Axis Line
                gl.glColor4fv(VisualizerOptions.colorToFloatArray(Color.BLACK), 0);
                gl.glVertex3d(0, 0, -1000);
                gl.glVertex3d(0, 0,1000);
            gl.glEnd();
        
        gl.glPopMatrix();
        */

    }
}
