/*
    Copyright 2017-2018 Will Winder

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

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.gl2.GLUT;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import com.willwinder.ugs.platform.probe.ProbeService.ProbeParameters;
import com.willwinder.ugs.platform.probe.renderable.ProbeRenderableHelpers.Side;
import static com.willwinder.ugs.platform.probe.renderable.ProbeRenderableHelpers.Side.NEGATIVE;
import static com.willwinder.ugs.platform.probe.renderable.ProbeRenderableHelpers.Side.POSITIVE;
import static com.willwinder.ugs.platform.probe.renderable.ProbeRenderableHelpers.drawTouchPlate;
import static com.willwinder.ugs.platform.probe.renderable.ProbeRenderableHelpers.drawArrow;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class CornerProbePathPreview extends Renderable {
    private final Point3d spacing = new Point3d(0, 0, 0);
    private final Point3d thickness = new Point3d(0, 0, 0);
    private Point3d startWork = null;
    private Point3d startMachine = null;
    private ProbeParameters pc = null;

    private final GLUT glut;

    public CornerProbePathPreview(String title) {
        super(10, title);
        glut = new GLUT();
    }

    public void setContext(ProbeParameters pc, Point3d startWork, Point3d startMachine) {
        this.pc = pc;
        this.startWork = startWork;
        this.startMachine = startMachine;
    }

    public void updateSpacing(
            double xSpacing, double ySpacing, double zSpacing,
            double xThickness, double yThickness, double zThickness) {
        this.spacing.x = xSpacing;
        this.spacing.y = ySpacing;
        this.spacing.z = zSpacing;
        this.thickness.x = xThickness;
        this.thickness.y = yThickness;
        this.thickness.z = zThickness;
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

    private boolean invalidSettings() {
        return this.spacing.x == 0
                && this.spacing.y == 0
                && this.spacing.z == 0
                && this.thickness.x == 0
                && this.thickness.y == 0
                && this.thickness.z == 0;
    }

    private void drawXY(GL2 gl, Side X, Side Y) {
        double previewSize = Math.max(5, Math.max(thickness.x * 4, thickness.y * 4));
        double previewDepth = Math.min(thickness.x, thickness.y);
        double inset = 2.5;
        double lip = 4;
        drawTouchPlate(gl, glut,
            new Point3d(spacing.x, spacing.y, spacing.z),
            inset,
            previewSize,
            thickness,
            previewDepth + lip,
            previewDepth,
            X, Y);

        // Everything is going to be red now!
        gl.glColor4d(8., 0., 0., 1);

        // y probe arrows
        drawArrow(gl, glut,
                new Point3d(0, 0, 0),
                new Point3d(spacing.x, 0, 0));
        drawArrow(gl, glut,
                new Point3d(spacing.x, 0, 0),
                new Point3d(spacing.x, spacing.y - inset, 0));

        // x probe arrows
        drawArrow(gl, glut,
                new Point3d(0, 0, 0),
                new Point3d(0, spacing.y, 0));
        drawArrow(gl, glut,
                new Point3d(0, spacing.y, 0),
                new Point3d(spacing.x - inset, spacing.y, 0));
    }

    private void drawXYZ(GL2 gl, Side X, Side Y) {
        double previewSize = Math.max(5, Math.max(thickness.x * 4, thickness.y * 4));
        double previewDepth = thickness.z;
        double inset = 2.5;
        double lip = 4;
        drawTouchPlate(gl, glut,
            new Point3d(0, 0, spacing.z),
            inset,
            previewSize,
            thickness,
            previewDepth + lip,
            previewDepth,
            X, Y);

        // Everything is going to be red now!
        gl.glColor4d(8., 0., 0., 1);

        // z arrow
        drawArrow(gl, glut,
                new Point3d(.25, .25, 0),
                new Point3d(.25, .25, spacing.z - Math.signum(spacing.z) * inset));
        drawArrow(gl, glut,
                new Point3d(-0.25, -0.25, spacing.z - Math.signum(spacing.z) * inset),
                new Point3d(-0.25, -0.25, 0.));

        // x probe arrows
        drawArrow(gl, glut,
                new Point3d(0, 0, 0),
                new Point3d(-spacing.x, 0, 0));
        drawArrow(gl, glut,
                new Point3d(-spacing.x, 0, 0),
                new Point3d(-spacing.x, 0, spacing.z));
        drawArrow(gl, glut,
                new Point3d(-spacing.x, 0, spacing.z),
                new Point3d(-X.side(inset), 0, spacing.z));

        // y probe arrows
        drawArrow(gl, glut,
                new Point3d(-spacing.x, 0, spacing.z),
                new Point3d(-spacing.x, -spacing.y, spacing.z));
        drawArrow(gl, glut,
                new Point3d(-spacing.x, -spacing.y, spacing.z),
                new Point3d(0, -spacing.y, spacing.z));
        drawArrow(gl, glut,
                new Point3d(0, -spacing.y, spacing.z),
                new Point3d(0, -Y.side(inset), spacing.z));
    }

    @Override
    public void draw(GLAutoDrawable drawable, boolean idle, Point3d machineCoord, Point3d workCoord, Point3d objectMin, Point3d objectMax, double scaleFactor, Point3d mouseWorldCoordinates, Point3d rotation) {
        if (invalidSettings()) return;

        GL2 gl = drawable.getGL().getGL2();

        if (startWork != null && pc.endPosition == null) {
            // The WCS is reset at the start of these operations.
            if (pc.startPosition != null) {
            }
            // Follow tool.
            else {
                gl.glTranslated(startWork.x, startWork.y, startWork.z);
            }
        }
        // Follow tool.
        else {
            gl.glTranslated(workCoord.x, workCoord.y, workCoord.z);
        }

        Side X = (spacing.x > 0) ? POSITIVE : NEGATIVE;
        Side Y = (spacing.y > 0) ? POSITIVE : NEGATIVE;
        if (spacing.z == 0) {
            drawXY(gl, X, Y);
        } else {
            drawXYZ(gl, X, Y);
        }
    }
}