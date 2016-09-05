/**
 * Draws a vertical line along the Z axis at the (X,Y) coordinate where the
 * mouse is considered to be.
 * 
 * Ray - Plane intersection: http://stackoverflow.com/a/21114992/204023
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

    /**
     * Get the near/far mouse locations in world space coordinates.
     */
    private Vector3[] getRayFromMouse(GLAutoDrawable drawable, Point mouseCoordinates) {

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

        double farDepth = 0.0;
        double nearDepth = 1.0;

        ///////////////////
        // Calculate Ray //
        ///////////////////

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

        return new Vector3[]{
                new Vector3(wcoordNear[0], wcoordNear[1], wcoordNear[2]),
                new Vector3(wcoordFar[0], wcoordFar[1], wcoordFar[2])};
    }

    public static boolean intersectRayWithSquare(Vector3 R1, Vector3 R2,
                                     Vector3 S1, Vector3 S2, Vector3 S3) {
        // 1.
        Vector3 dS21 = S2.sub(S1);
        Vector3 dS31 = S3.sub(S1);
        Vector3 n = dS21.cross(dS31);

        // 2.
        Vector3 dR = R1.sub(R2);

        double ndotdR = n.dot(dR);

        if (Math.abs(ndotdR) < 1e-6f) { // Choose your tolerance
            return false;
        }

        double t = -n.dot(R1.sub(S1)) / ndotdR;
        Vector3 M = R1.add(dR.scale(t));

        // 3.
        Vector3 dMS1 = M.sub(S1);
        double u = dMS1.dot(dS21);
        double v = dMS1.dot(dS31);

        // 4.
        return (u >= 0.0f && u <= dS21.dot(dS21)
             && v >= 0.0f && v <= dS31.dot(dS31));
    }

    @Override
    public void draw(GLAutoDrawable drawable, boolean idle, Point3d workCoord, Point3d objectMin, Point3d objectMax, double scaleFactor, Point3d rotation, Point mouseCoordinates) {
        if (mouseCoordinates == null) return;

        Vector3[] mouseRay = getRayFromMouse(drawable, mouseCoordinates);
        Vector3 R1 = mouseRay[0];
        Vector3 R2 = mouseRay[1];

        Vector3 S1 = new Vector3(objectMin.x, objectMax.y, 0);
        Vector3 S2 = new Vector3(objectMax.x, objectMax.y, 0);
        Vector3 S3 = new Vector3(objectMin.x, objectMin.y, 0);

        boolean hit = this.intersectRayWithSquare(R1, R2, S1, S2, S3);

        ///////////////////
        // Debug spheres //
        ///////////////////
        GL2 gl2 = drawable.getGL().getGL2();

        gl2.glPushMatrix();
            gl2.glColor4fv(VisualizerOptions.colorToFloatArray(hit ? Color.CYAN : Color.BLACK), 0);
            gl2.glTranslated(R1.x, R1.y, R1.z);
            GLU.gluSphere(GQ, 0.5, 10, 10);
        gl2.glPopMatrix();


        gl2.glPushMatrix();
            gl2.glColor4fv(VisualizerOptions.colorToFloatArray(hit ? Color.MAGENTA : Color.BLACK), 0);
            gl2.glTranslated(R2.x, R2.y, R2.z);
            GLU.gluSphere(GQ, 0.25, 10, 10);
        gl2.glPopMatrix();

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

    static class Vector3 {
        public double x, y, z;

        public Vector3(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Vector3 add(Vector3 other) {
            return new Vector3(x + other.x, y + other.y, z + other.z);
        }

        public Vector3 sub(Vector3 other) {
            return new Vector3(x - other.x, y - other.y, z - other.z);
        }

        public Vector3 scale(double f) {
            return new Vector3(x * f, y * f, z * f);
        }

        public Vector3 cross(Vector3 other) {
            return new Vector3(y * other.z - z * other.y,
                               z - other.x - x * other.z,
                               x - other.y - y * other.x);
        }

        public double dot(Vector3 other) {
            return x * other.x + y * other.y + z * other.z;
        }
    }
}
