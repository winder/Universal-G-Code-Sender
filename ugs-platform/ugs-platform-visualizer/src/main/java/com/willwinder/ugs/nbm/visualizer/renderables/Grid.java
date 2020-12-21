/*
    Copyright 2016-2017 Will Winder

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
package com.willwinder.ugs.nbm.visualizer.renderables;

import static com.jogamp.opengl.GL.GL_LINES;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_X;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_XY_GRID;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_XY_PLANE;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_Y;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_Z;
import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.visualizer.VisualizerUtils;

/**
 *
 * @author wwinder
 */
public class Grid extends Renderable {
    private float[] gridLineColor;
    private float[] gridPlaneColor;
    private float[] xAxisColor;
    private float[] yAxisColor;
    private float[] zAxisColor;

    public Grid(String title) {
        super(5, title);
        reloadPreferences(new VisualizerOptions());
    }

    @Override
    final public void reloadPreferences(VisualizerOptions vo) {
        gridLineColor = VisualizerOptions.colorToFloatArray(vo.getOptionForKey(VISUALIZER_OPTION_XY_GRID).value);
        gridPlaneColor = VisualizerOptions.colorToFloatArray(vo.getOptionForKey(VISUALIZER_OPTION_XY_PLANE).value);
        xAxisColor = VisualizerOptions.colorToFloatArray(vo.getOptionForKey(VISUALIZER_OPTION_X).value);
        yAxisColor = VisualizerOptions.colorToFloatArray(vo.getOptionForKey(VISUALIZER_OPTION_Y).value);
        zAxisColor = VisualizerOptions.colorToFloatArray(vo.getOptionForKey(VISUALIZER_OPTION_Z).value);
    }

    @Override
    public boolean enableLighting() {
        return false;
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

    private double getBestStepSize(double maxSide) {
        return maxSide/20;
    }

    private double getDistFromZeroForStepSize(double stepSize, double point, boolean min) {
        if (stepSize < 0.01) return min ? -1 : 1;

        // Get remainder.
        double remainder = Math.abs(point);
        while (remainder >= stepSize/2) {
            remainder -= stepSize;
        }

        if (point <= 0) {
            if (min) {
                return point - (stepSize - remainder);
            } else {
                return point + remainder;
            }
        }
        else {
            if (min) {
                return point - remainder;
            } else {
                return point + (stepSize - remainder);
            }
        }
    }

    @Override
    public void draw(GLAutoDrawable drawable, boolean idle, Position machineCoord, Position workCoord, Position focusMin, Position focusMax, double scaleFactor, Position mouseCoordinates, Position rotation) {
        double maxSide = VisualizerUtils.findMaxSide(focusMin, focusMax);
        if (maxSide == 0) {
            maxSide = 1;
        }
        double buffer = maxSide * 0.05;
        Position bottomLeft = new Position(focusMin);
        Position topRight = new Position(focusMax);

        // Setup the stepSize and min/max edges so that the lines look right.
        double stepSize = getBestStepSize(maxSide);
        bottomLeft.x = getDistFromZeroForStepSize(stepSize, bottomLeft.x, true);
        bottomLeft.y = getDistFromZeroForStepSize(stepSize, bottomLeft.y, true);
        topRight.x = getDistFromZeroForStepSize(stepSize, topRight.x, false);
        topRight.y = getDistFromZeroForStepSize(stepSize, topRight.y, false);

        GL2 gl = drawable.getGL().getGL2();
        gl.glPushMatrix();
            double offset = 0.001;

            gl.glLineWidth(1.5f);
            // grid
            gl.glBegin(GL_LINES);
            for(double x=bottomLeft.x;x<=topRight.x;x+=stepSize) {
                for (double y=bottomLeft.y; y<=topRight.y; y+=stepSize) {
                    if (x==0) continue; 
                    gl.glColor4fv(gridLineColor, 0);

                    gl.glVertex3d(x, bottomLeft.y, offset);
                    gl.glVertex3d(x, topRight.y  , offset);

                    gl.glVertex3d(x, bottomLeft.y, -offset);
                    gl.glVertex3d(x, topRight.y  , -offset);
                    
                    if (y==0) continue;
                    gl.glColor4fv(gridLineColor, 0);
                    gl.glVertex3d(bottomLeft.x, y,  offset);
                    gl.glVertex3d(topRight.x  , y,  offset);

                    gl.glVertex3d(bottomLeft.x, y, -offset);
                    gl.glVertex3d(topRight.x  , y, -offset);
                }
            }
            gl.glEnd();

            gl.glLineWidth(5f);
            gl.glBegin(GL_LINES);
                // X Axis Line
                gl.glColor4fv(yAxisColor, 0);
                gl.glVertex3d(0, bottomLeft.y, offset);
                gl.glVertex3d(0, topRight.y  , offset);

                gl.glVertex3d(0, bottomLeft.y, -offset);
                gl.glVertex3d(0, topRight.y  , -offset);

                // Y Axis Line
                gl.glColor4fv(xAxisColor, 0);
                gl.glVertex3d(bottomLeft.x, 0,  offset);
                gl.glVertex3d(topRight.x  , 0,  offset);

                gl.glVertex3d(bottomLeft.x, 0, -offset);
                gl.glVertex3d(topRight.x  , 0, -offset);

                // Z Axis Line
                gl.glColor4fv(zAxisColor, 0);
                gl.glVertex3d(0, 0, bottomLeft.z);
                gl.glVertex3d(0, 0, Math.max(topRight.z, -bottomLeft.z));
            gl.glEnd();

            //gl.glColor4f(.3f,.3f,.3f, .09f);
            gl.glColor4fv(gridPlaneColor, 0);

            // floor - cover entire model and a little extra.
            gl.glBegin(GL2.GL_QUADS);
                gl.glVertex3d(bottomLeft.x, bottomLeft.y, 0);
                gl.glVertex3d(bottomLeft.x, topRight.y  , 0);
                gl.glVertex3d(topRight.x  , topRight.y  , 0);
                gl.glVertex3d(topRight.x  , bottomLeft.y, 0);
            gl.glEnd();
        gl.glPopMatrix();
    }
}
