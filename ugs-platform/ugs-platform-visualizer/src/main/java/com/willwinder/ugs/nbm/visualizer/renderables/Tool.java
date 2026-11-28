/*
    Copyright 2016-2024 Will Winder

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
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_TOOL;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_TOOL_COLOR;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_TOOL_SIZE;
import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import com.willwinder.universalgcodesender.model.Position;

import java.awt.Color;

/**
 *
 * @author wwinder
 */
public final class Tool extends Renderable {
    private GLU glu;
    GLUquadric gq;
    Color toolColor;
    double toolSize = 0.4;

    public Tool(String title) {
        super(11, title, VISUALIZER_OPTION_TOOL);
        reloadPreferences(new VisualizerOptions());
    }

    @Override
    public void reloadPreferences(VisualizerOptions vo) {
        super.reloadPreferences(vo);
        toolColor = vo.getOptionForKey(VISUALIZER_OPTION_TOOL_COLOR).value;
        toolSize = VisualizerOptions.getDoubleOption(VISUALIZER_OPTION_TOOL_SIZE, 0.4);
        if (toolSize <= 0.05) {
            toolSize = 0.4;
        }
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
    public void draw(GLAutoDrawable drawable, boolean idle, Position machineCoord, Position workCoord, Position focusMin, Position focusMax, double scaleFactor, Position mouseCoordinates, Position rotation) {
        GL2 gl = drawable.getGL().getGL2();

        Position position = workCoord.getCartesian();

        double scale = 1. / scaleFactor;
        gl.glPushMatrix();
            gl.glTranslated(position.x, position.y, position.z);
            gl.glScaled(scale, scale, scale);

            if (!Double.isNaN(workCoord.a)) {
                gl.glRotated(workCoord.a, 1.0d, 0.0d, 0.0d);   //X
            }
            if (!Double.isNaN(workCoord.b)) {
                gl.glRotated(workCoord.b, 0.0d, 1.0d, 0.0d);   //Y
            }
            if (!Double.isNaN(workCoord.c)) {
                gl.glRotated(workCoord.c, 0.0d, 0.0d, 1.0d);   //Z
            }

            gl.glColor4fv(VisualizerOptions.colorToFloatArray(toolColor), 0);
            glu.gluQuadricNormals(gq, GLU.GLU_SMOOTH);
            float radius = (float) (0.03f * toolSize);
            double height = 0.2 * toolSize;
            double capHeight = 0.01 * toolSize;
            glu.gluCylinder(gq, 0f, radius, height, 16, 1);
            gl.glTranslated(0, 0, height);
            glu.gluCylinder(gq, radius, 0f, capHeight, 16, 1);
        gl.glPopMatrix();
    }
}
