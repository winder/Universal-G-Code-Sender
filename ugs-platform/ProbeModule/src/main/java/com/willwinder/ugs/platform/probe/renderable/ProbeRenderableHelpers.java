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
 * @author wwinder
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
}
