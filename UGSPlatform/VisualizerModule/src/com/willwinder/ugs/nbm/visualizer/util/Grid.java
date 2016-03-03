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

import static com.jogamp.opengl.GL.GL_LINES;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.willwinder.universalgcodesender.visualizer.VisualizerUtils;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class Grid extends Renderable {
    public Grid() {
        super(5);
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
    public void draw(GLAutoDrawable drawable, Point3d workCoord, Point3d focusMin, Point3d focusMax, double scaleFactor) {
        double maxSide = VisualizerUtils.findMaxSide(focusMin, focusMax);
        double buffer = maxSide * 0.05;
        Point3d bottomLeft = new Point3d(focusMin);
        Point3d topRight = new Point3d(focusMax);

        bottomLeft.x -= buffer;
        bottomLeft.y -= buffer;
        topRight.x += buffer;
        topRight.y += buffer;

        GL2 gl = drawable.getGL().getGL2();
        gl.glPushMatrix();
            gl.glRotated(90, 1.0, 0.0, 0.0);
            gl.glColor4f(.3f,.3f,.3f, .05f);

            // floor - cover entire model and a little extra.
            gl.glPushMatrix();
                gl.glBegin(gl.GL_QUADS);
                    gl.glVertex3d(bottomLeft.x, 0, -bottomLeft.y);
                    gl.glVertex3d(bottomLeft.x, 0, -topRight.y);
                    gl.glVertex3d(topRight.x  , 0, -topRight.y);
                    gl.glVertex3d(topRight.x  , 0, -bottomLeft.y);
                gl.glEnd();
            gl.glPopMatrix();
            
            double stepSize = maxSide / 20;
            double offset = 0.01;

            gl.glLineWidth(1.5f);
            // grid
            gl.glBegin(GL_LINES);
            for(double x=bottomLeft.x;x<=topRight.x;x+=stepSize) {
                for (double y=bottomLeft.y; y<=topRight.y; y+=stepSize) {
                    if (x==0) { gl.glColor3d(.6f,.3f,.3f); } else { gl.glColor3d(.25,.25,.25); };
                    gl.glVertex3d(x,  offset, -bottomLeft.y);
                    gl.glVertex3d(x,  offset, -topRight.y);

                    gl.glVertex3d(x, -offset, -bottomLeft.y);
                    gl.glVertex3d(x, -offset, -topRight.y);
                    
                    if (y==0) { gl.glColor3d(.3,.3,.6); } else { gl.glColor3d(.25,.25,.25); };
                    gl.glVertex3d(bottomLeft.x,  offset, -y);
                    gl.glVertex3d(topRight.x  ,  offset, -y);

                    gl.glVertex3d(bottomLeft.x, -offset, -y);
                    gl.glVertex3d(topRight.x  , -offset, -y);
                }
            };
            gl.glEnd();
        gl.glPopMatrix();
    }
}
