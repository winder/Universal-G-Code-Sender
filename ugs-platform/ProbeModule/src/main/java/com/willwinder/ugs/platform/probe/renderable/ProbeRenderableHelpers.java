/*
    Copyright 2017 Will Winder

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
package com.willwinder.ugs.platform.probe.renderable;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.gl2.GLUT;
import com.willwinder.universalgcodesender.model.Position;

/**
 *
 * @author wwinder, risototh
 */
public class ProbeRenderableHelpers {
    private static int slices = 20;
    private static int stacks = 20;

    public enum Side {
        POSITIVE(1),
        NEGATIVE(-1);

        final int multiplier;
        Side(int mult) {
            multiplier = mult;
        }

        public double multiplier() {
            return multiplier;
        }

        public double side(double offset) {
            return offset * multiplier;
        }
    }

    public static class Triangle {
        public double x1;
        public double y1;
        public double z1;
        public double x2;
        public double y2;
        public double z2;
        public double x3;
        public double y3;
        public double z3;
        public double xn;
        public double yn;
        public double zn;

        public Triangle(double x1, double y1, double z1, double x2, double y2, double z2, double x3, double y3, double z3) {
            this.x1 = x1;
            this.y1 = y1;
            this.z1 = z1;
            this.x2 = x2;
            this.y2 = y2;
            this.z2 = z2;
            this.x3 = x3;
            this.y3 = y3;
            this.z3 = z3;

            // triangle normal calculation
            double Ux, Uy, Uz, Vx, Vy, Vz, Nx, Ny, Nz, length;

            Ux = x2 - x1;
            Uy = y2 - y1;
            Uz = z2 - z1;

            Vx = x3 - x1;
            Vy = y3 - y1;
            Vz = z3 - z1;

            Nx = Uy * Vz - Uz * Vy;
            Ny = Uz * Vx - Ux * Vz;
            Nz = Ux * Vy - Uy * Vx;

            length = Math.sqrt(Nx * Nx + Ny * Ny + Nz * Nz);

            this.xn = Nx / length;
            this.yn = Ny / length;
            this.zn = Nz / length;
        }
    }

    public static void drawTouchPlate(GL2 gl, GLUT glut, Position at, double inset, double size, Position offsets,
            double bumpThickness, double plateThickness, Side X, Side Y) {
        gl.glPushMatrix();
            gl.glTranslated(
                    at.x + X.side(size-2*inset)/2,
                    at.y + Y.side(size-2*inset)/2,
                    at.z + Math.signum(at.z) * (bumpThickness/2 - inset));

            gl.glColor4d(.8, .8, .8, 1);
            // y bump
            gl.glPushMatrix();
                gl.glTranslated(0, Y.side((offsets.y-size)/2), 0);
                gl.glScaled(size, offsets.y, bumpThickness);
                glut.glutSolidCube(1);
            gl.glPopMatrix();

            // x bump
            gl.glColor4d(.8, .8, .8, 1);
            gl.glPushMatrix();
                gl.glTranslated(X.side((offsets.x-size)/2), 0, 0);
                gl.glScaled(offsets.x, size, bumpThickness);
                glut.glutSolidCube(1);
            gl.glPopMatrix();

            gl.glColor4d(1, 1, 1, 1);
            gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT);

            // big piece
            gl.glPushMatrix();
                gl.glScaled(size-0.1, size-0.1, plateThickness);
                glut.glutSolidCube(1);
            gl.glPopMatrix();

        gl.glPopMatrix();
    }

    public static void drawArrow(GL2 gl, GLUT glut, Position from, Position to) {
        double vx = to.x - from.x;
        double vy = to.y - from.y;
        double vz = to.z - from.z;

        //handle the degenerate case of z1 == z2 with an approximation
        if(vz == 0)
            vz = .0001;

        double v = Math.sqrt( vx*vx + vy*vy + vz*vz );
        double ax = 57.2957795*Math.acos( vz/v );
        if ( vz < 0.0 )
            ax = -ax;
        double rx = -vy*vz;
        double ry = vx*vz;

        gl.glPushMatrix();
            gl.glTranslated(from.x, from.y, from.z);
            gl.glRotated(ax, rx, ry, 0.0);
            glut.glutSolidCylinder(.1, v - 0.5, slices, stacks);
            gl.glTranslated(0, 0, v - 1);
            glut.glutSolidCone(.2, 1, slices, stacks);
        gl.glPopMatrix();
    }

    /**
     * Renders the list of triangles. Usefull to render complex models from STL data.
     *
     * @param GL2 gl
     * @param Triangle[] triangles
     */
    public static void drawTriangleSet(GL2 gl, Triangle[] triangles) {
        if (triangles.length == 0) return;

        gl.glBegin(GL2.GL_TRIANGLES);

        for (Triangle triangle : triangles) {
          gl.glNormal3d(triangle.xn, triangle.yn, triangle.zn);
          gl.glVertex3d(triangle.x1, triangle.y1, triangle.z1);
          gl.glVertex3d(triangle.x2, triangle.y2, triangle.z2);
          gl.glVertex3d(triangle.x3, triangle.y3, triangle.z3);
        }

        gl.glEnd();
    }
}
