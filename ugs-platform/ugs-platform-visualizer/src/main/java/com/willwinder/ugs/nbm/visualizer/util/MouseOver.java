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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Point3d;
import jogamp.nativewindow.macosx.OSXUtil;

/**
 *
 * @author wwinder
 */
public class MouseOver extends Renderable {
    private static final Logger logger = Logger.getLogger(MouseOver.class.getName());

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
     * 
     * Utilize gluUnProject to get the points.
     */
    private static Vector3[] getRayFromMouse(GLAutoDrawable drawable, int mouseX, int mouseY) {

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
        // NEAR
        GLU.gluUnProject((double)mouseX, (double)realy, nearDepth,
                modelViewMatrix, 0,
                projectionMatrix, 0,
                viewPort, 0,
                wcoordNear, 0);

        return new Vector3[]{
                new Vector3(wcoordNear[0], wcoordNear[1], wcoordNear[2]),
                new Vector3(wcoordFar[0], wcoordFar[1], wcoordFar[2])};
    }

    /**
     * Returns a point where a ray intersects with the XY plane.
     * @param R1 Start point of the mouse ray
     * @param R2 End point of the mouse ray
     * @param S1 Top-left corner of a box on the plane.
     * @param S2 Top-right corner of a box on the plane.
     * @param S3 Bottom-left corner of a box on the plane.
     * @return The X, Y coordinate at Z=0
     */
    private static Point3d intersectPointWithPlane(Vector3 R1, Vector3 R2,
                                     Vector3 S1, Vector3 S2, Vector3 S3) {

        // TODO: The plane S1, S2, S3 is the XY plane by definition, so this
        //       could be simplified if I spent more time understanding the
        //       trig. 

        // 1.
        Vector3 dS21 = S2.sub(S1);
        Vector3 dS31 = S3.sub(S1);
        Vector3 n = dS21.cross(dS31);

        // 2.
        Vector3 dR = R1.sub(R2);

        double ndotdR = n.dot(dR);

        // If the ray is parallel to the plane return 0
        if (Math.abs(ndotdR) < 1e-6f) { // Choose your tolerance
            return new Point3d(0,0,0);
        }

        double t = -n.dot(R1.sub(S1)) / ndotdR;
        Vector3 M = R1.add(dR.scale(t));
        logger.log(Level.INFO, String.format("Intersection at: (%f,%f)", M.x, M.y));

        return new Point3d(M.x, M.y, 0);

        /*
        // The below will calculate if the intersection is also within the
        // bounds of the plane. Since our plane is along the XY plane it is not
        // necessary to do any of this.

        // 3.
        Vector3 dMS1 = M.sub(S1);
        double u = dMS1.dot(dS21);
        double v = dMS1.dot(dS31);

        // 4.
        return (u >= 0.0f && u <= dS21.dot(dS21)
             && v >= 0.0f && v <= dS31.dot(dS31));
        */
    }

    static private boolean inBounds(Point3d point, Point3d bottomLeft, Point3d topRight) {
        if (point.x > topRight.x || point.x < bottomLeft.x) return false;
        if (point.y > topRight.y || point.y < bottomLeft.y) return false;
        return true;
    }

    @Override
    public void draw(GLAutoDrawable drawable, boolean idle, Point3d workCoord, Point3d objectMin, Point3d objectMax, double scaleFactor, Point3d rotation, Point mouseCoordinates) {
        if (mouseCoordinates == null) return;

        int[] raw = {mouseCoordinates.x, mouseCoordinates.y};
        int[] coords = drawable.getNativeSurface().convertToPixelUnits(raw);
        int mouseX = coords[0];
        int mouseY = coords[1];

        Vector3[] mouseRay = getRayFromMouse(drawable, mouseX, mouseY);
        Vector3 R1 = mouseRay[0];
        Vector3 R2 = mouseRay[1];

        Vector3 S1 = new Vector3(objectMin.x, objectMax.y, 0);
        Vector3 S2 = new Vector3(objectMax.x, objectMax.y, 0);
        Vector3 S3 = new Vector3(objectMin.x, objectMin.y, 0);

        Point3d hit = intersectPointWithPlane(R1, R2, S1, S2, S3);

        if (inBounds(hit, objectMin, objectMax)) {
            GL2 gl = drawable.getGL().getGL2();

            double scale = scaleFactor * 2;

            gl.glPushMatrix();
                gl.glEnable(GL2.GL_LIGHTING); 
                gl.glTranslated(hit.x, hit.y, hit.z);
                gl.glScaled(1./scale, 1./scale, 1./scale);

                gl.glColor4fv(VisualizerOptions.colorToFloatArray(Color.LIGHT_GRAY), 0);
                GLU.gluQuadricNormals(GQ, GLU.GLU_SMOOTH);
                GLU.gluCylinder(GQ, 0f, .03f, .2, 16, 1);
                gl.glTranslated(0, 0, 0.2);
                GLU.gluCylinder(GQ, 0.03f, .0f, .01, 16, 1);
                gl.glDisable(GL2.GL_LIGHTING); 
            gl.glPopMatrix();
        }
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
