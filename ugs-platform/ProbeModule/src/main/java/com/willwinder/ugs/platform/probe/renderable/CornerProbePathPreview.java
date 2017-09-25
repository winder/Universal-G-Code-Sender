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
import com.jogamp.opengl.util.gl2.GLUT;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import com.willwinder.ugs.platform.probe.ProbeService.ProbeContext;
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
    private Point3d startWork = null;
    private Point3d startMachine = null;
    private ProbeContext pc = null;

    private final GLUT glut;

    public CornerProbePathPreview(String title) {
        super(10, title);
        glut = new GLUT();
    }

    public void setContext(ProbeContext pc, Point3d startWork, Point3d startMachine) {
        this.pc = pc;
        this.startWork = startWork;
        this.startMachine = startMachine;
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


        if (startWork != null) {
            // After the probe, move it back to the original location
            if (pc != null && pc.xWcsOffset != null && pc.yWcsOffset != null && pc.zWcsOffset != null) {
                //Point3d originalOffset = new Point3d(this.startMachine);
                //originalOffset.sub(this.startWork);
                gl.glTranslated(
                        pc.xWcsOffset,
                        pc.yWcsOffset,
                        pc.zWcsOffset);
            } else {
                gl.glTranslated(startWork.x, startWork.y, startWork.z);
            }
        } else {
            gl.glTranslated(workCoord.x, workCoord.y, workCoord.z);
        }

        // touch plate
        gl.glPushMatrix();
            // big piece
            gl.glTranslated(this.xSpacing, this.ySpacing, 0);

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

        /*
        // If we haven't done both probes, don't render the offset arrow.
        if (pc == null || pc.xWcsOffset == null || pc.yWcsOffset == null) return;

        // offset arrow is yellow
        gl.glColor3d(1., 1., 0);

        double vx = pc.xWcsOffset;
        double vy = pc.yWcsOffset;
        double vz = pc.yWcsOffset;

        //handle the degenerate case of z1 == z2 with an approximation
        if(vz == 0)
            vz = .0001;

        double v = Math.sqrt( vx*vx + vy*vy + vz*vz );
        double ax = 57.2957795*Math.acos( vz/v );
        if ( vz < 0.0 )
            ax = -ax;
        double rx = -vy*vz;
        double ry = vx*vz;

        gl.glRotated(ax, rx, ry, 0.0);
        glut.glutSolidCylinder(.1, v - 0.5, slices, stacks);
        gl.glTranslated(0, 0, v - 1);
        glut.glutSolidCone(.2, 1, slices, stacks);
        */
    }
}