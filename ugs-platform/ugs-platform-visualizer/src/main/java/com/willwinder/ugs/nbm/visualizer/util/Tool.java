/*
    Copywrite 2016 Will Winder

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
package com.willwinder.ugs.nbm.visualizer.util;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.glu.GLUquadric;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import java.awt.Color;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public final class Tool extends Renderable {
    private GLU glu;
    GLUquadric gq;
    Color toolColor;

    public Tool() {
        super(9);
        reloadPreferences(new VisualizerOptions());
    }

    @Override
    final public void reloadPreferences(VisualizerOptions vo) {
        toolColor = (Color)vo.getOptionForKey("platform.visualizer.color.tool").value;
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
    public void draw(GLAutoDrawable drawable, boolean idle, Point3d workCoord, Point3d focusMin, Point3d focusMax, double scaleFactor) {
        GL2 gl = drawable.getGL().getGL2();

        gl.glEnable(GL2.GL_LIGHTING); 
        
        gl.glPushMatrix();
            gl.glTranslated(workCoord.x, workCoord.y, workCoord.z);
            gl.glScaled(1./scaleFactor, 1./scaleFactor, 1./scaleFactor);

            gl.glColor4fv(VisualizerOptions.colorToFloatArray(toolColor), 0);
            glu.gluQuadricNormals(gq, GLU.GLU_SMOOTH);
            glu.gluCylinder(gq, 0f, .03f, .2, 16, 1);
            gl.glTranslated(0, 0, 0.2);
            glu.gluCylinder(gq, 0.03f, .0f, .01, 16, 1);
        gl.glPopMatrix();
        gl.glDisable(GL2.GL_LIGHTING); 

        /*
        // The ugly yellow line. RIP.
        gl.glBegin(GL_LINES);
        
            gl.glLineWidth(8.0f);
            gl.glColor3ub(color[0], color[1], color[2]);
            gl.glVertex3d(this.workCoord.x, this.workCoord.y, this.workCoord.z);
            gl.glColor3ub(color[0], color[1], color[2]);
            gl.glVertex3d(this.workCoord.x, this.workCoord.y, this.workCoord.z+(1.0/this.scaleFactor));
            
        gl.glEnd();
        */
    }
}
