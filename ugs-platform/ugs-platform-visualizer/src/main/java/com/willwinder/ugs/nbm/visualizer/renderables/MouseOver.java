/**
 * Draws a vertical line along the Z axis at the (X,Y) coordinate where the
 * mouse is considered to be.
 * 
 * Ray - Plane intersection: http://stackoverflow.com/a/21114992/204023
 */
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

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import com.willwinder.universalgcodesender.model.Position;
import java.awt.Color;
import java.util.logging.Logger;

/**
 *
 * @author wwinder
 */
public class MouseOver extends Renderable {
    private static final Logger logger = Logger.getLogger(MouseOver.class.getName());

    private static final GLU GLU = new GLU();
    private static GLUquadric GQ;

    public MouseOver(String title) {
        super(8, title);
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

    static private boolean inBounds(Position point, Position bottomLeft, Position topRight) {
        if (point.x > topRight.x || point.x < bottomLeft.x) return false;
        if (point.y > topRight.y || point.y < bottomLeft.y) return false;
        return true;
    }

    @Override
    public void draw(GLAutoDrawable drawable, boolean idle, Position machineCoord, Position workCoord, Position objectMin, Position objectMax, double scaleFactor, Position mouseWorldCoordinates, Position rotation) {
        if (mouseWorldCoordinates == null) return;

        if (inBounds(mouseWorldCoordinates, objectMin, objectMax)) {
            GL2 gl = drawable.getGL().getGL2();

            double scale = 1. / (scaleFactor * 2);

            gl.glPushMatrix();
                gl.glTranslated(mouseWorldCoordinates.x, mouseWorldCoordinates.y, 0.);
                gl.glScaled(scale, scale, scale);

                gl.glColor4fv(VisualizerOptions.colorToFloatArray(Color.WHITE), 0);
                GLU.gluQuadricNormals(GQ, GLU.GLU_SMOOTH);
                GLU.gluCylinder(GQ, 0f, .03f, .2, 16, 1);
                gl.glTranslated(0, 0, 0.2);
                GLU.gluCylinder(GQ, 0.03f, .0f, .01, 16, 1);
            gl.glPopMatrix();
        }
    }
}
