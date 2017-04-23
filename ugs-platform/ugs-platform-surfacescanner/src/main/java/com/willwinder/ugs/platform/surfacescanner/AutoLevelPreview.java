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
package com.willwinder.ugs.platform.surfacescanner;

import com.jogamp.opengl.GL2;
import static com.jogamp.opengl.GL2ES3.GL_QUADS;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.gl2.GLUT;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.ugs.nbm.visualizer.shared.IRendererNotifier;
import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class AutoLevelPreview extends Renderable {
    private Point3d lowerLeft = null;
    private Point3d upperRight = null;
    private double resolution = 1;

    private final IRendererNotifier notifier;
    private final GLUT glut;

    public AutoLevelPreview(int priority, IRendererNotifier notifier) {
        super(10);

        this.notifier = notifier;
        lowerLeft = new Point3d(-0, -0, -0);
        upperRight = new Point3d(0, 0, 0);
        resolution = 0.1;

        glut = new GLUT();
    }

    public void updateSettings(Point3d lowerLeft, Point3d upperRight, double resolution) {
        if (!this.lowerLeft.equals(lowerLeft)
                || !this.upperRight.equals(upperRight)
                || this.resolution != resolution) {
            this.lowerLeft = lowerLeft;
            this.upperRight = upperRight;
            this.resolution = resolution;
            if (this.notifier != null) {
                this.notifier.forceRedraw();
            }
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
    }

    @Override
    public void reloadPreferences(VisualizerOptions vo) {
    }

    @Override
    public void draw(GLAutoDrawable drawable, boolean idle, Point3d workCoord, Point3d objectMin, Point3d objectMax, double scaleFactor, Point3d mouseWorldCoordinates, Point3d rotation) {
        // Don't draw something invalid.
        if (this.lowerLeft == null || this.upperRight == null || resolution <= 0) {
            return;
        }

        double objectX = objectMax.x - objectMin.x;
        double objectY = objectMax.y - objectMin.y;
                
        GL2 gl = drawable.getGL().getGL2();
        
        gl.glPushMatrix();
            gl.glEnable(GL2.GL_LIGHTING); 

            //VisualizerOptions.colorToFloatArray(toolColor), 0);

            double minx = Math.min(lowerLeft.x, upperRight.x);
            double maxx = Math.max(lowerLeft.x, upperRight.x);
            double miny = Math.min(lowerLeft.y, upperRight.y);
            double maxy = Math.max(lowerLeft.y, upperRight.y);
            double minz = Math.min(lowerLeft.z, upperRight.z);
            double maxz = Math.max(lowerLeft.z, upperRight.z);

            gl.glColor4fv(new float[]{0.1f, 0.1f, 0.1f, 1.0f}, 0);
            double diameter = Math.max(objectX*0.005, objectY*0.005);
            for(double x = minx; x <= maxx; x = Math.min(maxx, x + resolution)) {
                for(double y = miny; y <= maxy; y = Math.min(maxy, y + resolution)) {
                    gl.glPushMatrix();
                        gl.glTranslated(x, y, maxz);
                        glut.glutSolidSphere(diameter, 7, 7);
                        gl.glTranslated(0, 0, -maxz+minz);
                    gl.glPopMatrix();
                    if (y == maxy) break;
                }
                if (x == maxx) break;
            }

            //drawCube(gl, this.lowerLeft, this.upperRight, this.resolution);
            gl.glPushMatrix();
                gl.glTranslated(
                        (lowerLeft.x+upperRight.x)/2,
                        (lowerLeft.y+upperRight.y)/2,
                        (lowerLeft.z+upperRight.z)/2);
                gl.glScaled(upperRight.x-lowerLeft.x, upperRight.y-lowerLeft.y, upperRight.z-lowerLeft.z);
                gl.glColor4fv(new float[]{0.3f, 0, 0, 0.1f}, 0);
                glut.glutSolidCube((float) 1.);
            gl.glPopMatrix();

            gl.glDisable(GL2.GL_LIGHTING); 
        gl.glPopMatrix();
    }
    
  private static void drawCube(GL2 gl, Point3d lowerLeft, Point3d upperRight, double resolution) {
    gl.glBegin(GL_QUADS);
        // Six faces of cube
        // Top face
        gl.glPushMatrix();
            gl.glRotatef(-90, 1, 0, 0);
            gl.glRotatef(180, 0, 0, 1);
            gl.glVertex3d(lowerLeft.x, lowerLeft.y, upperRight.z);
            gl.glVertex3d(lowerLeft.x, upperRight.y, upperRight.z);
            gl.glVertex3d(upperRight.x, upperRight.y, upperRight.z);
            gl.glVertex3d(upperRight.x, lowerLeft.y, upperRight.z);
        gl.glPopMatrix();

        // Bottom face
        gl.glPushMatrix();
            gl.glRotatef(-90, 1, 0, 0);
            gl.glRotatef(180, 0, 0, 1);
            gl.glVertex3d(upperRight.x, lowerLeft.y, lowerLeft.z);
            gl.glVertex3d(upperRight.x, upperRight.y, lowerLeft.z);
            gl.glVertex3d(lowerLeft.x, upperRight.y, lowerLeft.z);
            gl.glVertex3d(lowerLeft.x, lowerLeft.y, lowerLeft.z);
        gl.glPopMatrix();

        gl.glPushMatrix();
            gl.glRotatef(90, 0, 1, 0);

            // Right face
            gl.glPushMatrix();
                gl.glRotatef(90, 0, 0, 1);
                gl.glVertex3d(upperRight.x, upperRight.y, lowerLeft.z);
                gl.glVertex3d(upperRight.x, upperRight.y, upperRight.z);
                gl.glVertex3d(upperRight.x, lowerLeft.y, upperRight.z);
                gl.glVertex3d(upperRight.x, lowerLeft.y, lowerLeft.z);
            gl.glPopMatrix();

            // Back face    
            gl.glRotatef(90, 0, 1, 0);
            gl.glPushMatrix();
                gl.glRotatef(180, 0, 0, 1);
                //drawFace(gl, size, color, border, "Z-");
                gl.glVertex3d(upperRight.x, upperRight.y, lowerLeft.z);
                gl.glVertex3d(upperRight.x, upperRight.y, upperRight.z);
                gl.glVertex3d(lowerLeft.x, upperRight.y, upperRight.z);
                gl.glVertex3d(lowerLeft.x, upperRight.y, lowerLeft.z);
            gl.glPopMatrix();


            // Left face    
            gl.glRotatef(90, 0, 1, 0);
            gl.glRotatef(-90, 0, 0, 1);
            gl.glVertex3d(lowerLeft.x, lowerLeft.y, lowerLeft.z);
            gl.glVertex3d(lowerLeft.x, lowerLeft.y, upperRight.z);
            gl.glVertex3d(lowerLeft.x, upperRight.y, upperRight.z);
            gl.glVertex3d(lowerLeft.x, upperRight.y, lowerLeft.z);
        gl.glPopMatrix();
    gl.glPushMatrix();

        gl.glRotatef(90, 1, 0, 0);
        // Front face
        gl.glVertex3d(lowerLeft.x, lowerLeft.y, lowerLeft.z);
        gl.glVertex3d(lowerLeft.x, lowerLeft.y, upperRight.z);
        gl.glVertex3d(upperRight.x, lowerLeft.y, upperRight.z);
        gl.glVertex3d(upperRight.x, lowerLeft.y, lowerLeft.z);
    gl.glPopMatrix();
    gl.glEnd();
  }
}
