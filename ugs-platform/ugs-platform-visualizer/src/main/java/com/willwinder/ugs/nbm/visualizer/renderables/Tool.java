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
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_TOOL;
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

    public Tool(String title) {
        super(9, title);
        reloadPreferences(new VisualizerOptions());
    }

    @Override
    final public void reloadPreferences(VisualizerOptions vo) {
        toolColor = vo.getOptionForKey(VISUALIZER_OPTION_TOOL).value;
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
        
        double scale = 1. / scaleFactor;
        gl.glPushMatrix();
            gl.glTranslated(workCoord.x, workCoord.y, workCoord.z);
            gl.glScaled(scale, scale, scale);

            gl.glColor4fv(VisualizerOptions.colorToFloatArray(toolColor), 0);
            glu.gluQuadricNormals(gq, GLU.GLU_SMOOTH);
            glu.gluCylinder(gq, 0f, .03f, .2, 16, 1);
            gl.glTranslated(0, 0, 0.2);
            glu.gluCylinder(gq, 0.03f, .0f, .01, 16, 1);
        gl.glPopMatrix();
    }
}
