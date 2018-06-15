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

import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class Selection extends Renderable {
    private Point3d start = null;
    private Point3d end = null;

    public Selection(String title) {
        super(8, title);
    }

    public void clear() {
        this.start = null;
        this.end = null;
    }

    public void setStart(Point3d start) {
        this.start = start;
    }

    public void setEnd(Point3d end) {
        this.end = end;
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
    public void draw(GLAutoDrawable drawable, boolean idle, Point3d machineCoord, Point3d workCoord, Point3d objectMin, Point3d objectMax, double scaleFactor, Point3d mouseWorldCoordinates, Point3d rotation) {
        if (start == null || end == null) return;

        GL2 gl = drawable.getGL().getGL2();

        gl.glColor4f(.3f,.3f,.3f, .09f);
        //gl.glColor4fv(gridPlaneColor, 0);

        // floor - cover entire model and a little extra.
        gl.glBegin(GL2.GL_QUADS);
            gl.glVertex3d(start.x, start.y, 0);
            gl.glVertex3d(start.x, end.y  , 0);
            gl.glVertex3d(end.x  , end.y  , 0);
            gl.glVertex3d(end.x  , start.y, 0);
        gl.glEnd();
    }
    
}
