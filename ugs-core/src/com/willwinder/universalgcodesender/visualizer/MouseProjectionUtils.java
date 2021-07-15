/**
 * Helpers for converting a window X/Y coordinate into the XY plane coordinate.
 */
/*
    Copyright 2016 Will Winder

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
package com.willwinder.universalgcodesender.visualizer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;
import com.willwinder.universalgcodesender.model.Position;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wwinder
 */
public class MouseProjectionUtils {
    private static final Logger logger = Logger.getLogger(MouseProjectionUtils.class.getName());

    private static final GLU GLU = new GLU();

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
        int realy = viewPort[3] - mouseY - 1;
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

    public static Position intersectPointWithXYPlane(GLAutoDrawable drawable,
            int rawMouseX, int rawMouseY) {

        int[] raw = {rawMouseX, rawMouseY};
        int[] coords = drawable.getNativeSurface().convertToPixelUnits(raw);
        int mouseX = coords[0];
        int mouseY = coords[1];

        Vector3[] mouseRay = getRayFromMouse(drawable, mouseX, mouseY);

        Vector3 R1 = mouseRay[0];
        Vector3 R2 = mouseRay[1];

        Vector3 S1 = new Vector3(-1., 1., 0);
        Vector3 S2 = new Vector3(1., 1., 0);
        Vector3 S3 = new Vector3(-1., -1., 0);

        return intersectPointWithPlane(R1, R2, S1, S2, S3);
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
    private static Position intersectPointWithPlane(Vector3 R1, Vector3 R2,
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
            return new Position(0,0,0);
        }

        double t = -n.dot(R1.sub(S1)) / ndotdR;
        Vector3 M = R1.add(dR.scale(t));
        //logger.log(Level.INFO, String.format("Intersection at: (%f,%f)", M.x, M.y));

        return new Position(M.x, M.y, 0);

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
