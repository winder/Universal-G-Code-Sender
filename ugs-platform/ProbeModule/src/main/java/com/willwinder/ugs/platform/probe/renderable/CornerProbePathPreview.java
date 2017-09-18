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
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.jogamp.opengl.util.gl2.GLUT;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class CornerProbePathPreview extends Renderable {
    private Double xSpacing = null;
    private Double ySpacing = null;
    private Double xThickness = null;
    private Double yThickness = null;
    private final double previewSize = 5;

    private final GLUT glut;
    private GLU glu;
    private GLUquadric gq;

    public CornerProbePathPreview(String title) {
        super(10, title);
        glut = new GLUT();
    }

    public void updateSpacing(double xSpacing, double ySpacing, double xThickness, double yThickness) {
        this.xSpacing = xSpacing;
        this.ySpacing = ySpacing;
        this.xThickness = xThickness;
        this.yThickness = yThickness;
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
        glu = new GLU();
        gq = glu.gluNewQuadric();
    }

    @Override
    public void reloadPreferences(VisualizerOptions vo) {
    }

    @Override
    public void draw(GLAutoDrawable drawable, boolean idle, Point3d workCoord, Point3d objectMin, Point3d objectMax, double scaleFactor, Point3d mouseWorldCoordinates, Point3d rotation) {
        if (xSpacing == null || ySpacing == null) return;

        final int slices = 10;
        final int stacks = 10;

        GL2 gl = drawable.getGL().getGL2();

        int xDir = (this.xSpacing > 0) ? 1 : -1;
        int yDir = (this.ySpacing > 0) ? 1 : -1;
        double xAbs = Math.abs(this.xSpacing);
        double yAbs = Math.abs(this.ySpacing);

        // touch plate
        gl.glPushMatrix();
            // big piece
            gl.glTranslated(this.xSpacing, this.ySpacing, workCoord.z);

            gl.glColor4d(.8, .8, .8, 1);
            // y bump
            gl.glPushMatrix();
                gl.glTranslated(0, yDir * -this.previewSize/2 + yDir * this.yThickness / 2, 0);
                gl.glScaled(previewSize, this.yThickness, 2.);
                glut.glutSolidCube(1);
            gl.glPopMatrix();

            // x bump
            gl.glPushMatrix();
                gl.glTranslated(xDir * -this.previewSize/2 + xDir * this.xThickness / 2, 0, 0);
                gl.glScaled(this.xThickness, previewSize, 2.);
                glut.glutSolidCube(1);
            gl.glPopMatrix();

            gl.glColor4d(1, 1, 1, 1);
            gl.glColorMaterial(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT);

            gl.glPushMatrix();
                gl.glScaled(previewSize-0.1, previewSize-0.1, 1.);
                glut.glutSolidCube(1);
            gl.glPopMatrix();

        gl.glPopMatrix();

        // Everything is going to be red now!
        gl.glColor4d(8., 0., 0., 1);

        // y probe arrows
        gl.glPushMatrix();
            gl.glRotated(90, 0, xDir, 0);
            glut.glutSolidCylinder(.1, xAbs - 0.5, slices, stacks);
            gl.glTranslated(0, 0, xAbs - 1);
            glut.glutSolidCone(.2, 1, slices, stacks);
            gl.glTranslated(0, 0, 1);
            gl.glRotated(-90, 0, xDir, 0);
            gl.glRotated(-90, yDir, 0, 0);
            glut.glutSolidCylinder(.1, yAbs - previewSize / 2 - 0.5, slices, stacks);
            gl.glTranslated(0, 0, yAbs - previewSize / 2 - 1);
            glut.glutSolidCone(.2, 1, slices, stacks);
        gl.glPopMatrix();

        // x probe arrows
        gl.glPushMatrix();
            gl.glRotated(-90, yDir, 0, 0);
            glut.glutSolidCylinder(.1, yAbs - 0.5, slices, stacks);
            gl.glTranslated(0, 0, yAbs - 1);
            glut.glutSolidCone(.2, 1, slices, stacks);
            gl.glTranslated(0, 0, 1);
            gl.glRotated(90, yDir, 0, 0);
            gl.glRotated(90, 0, xDir, 0);
            glut.glutSolidCylinder(.1, xAbs - previewSize / 2 - 0.5, slices, stacks);
            gl.glTranslated(0, 0, xAbs - previewSize / 2 - 1);
            glut.glutSolidCone(.2, 1, slices, stacks);
        gl.glPopMatrix();
    }
}