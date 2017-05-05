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

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Iterables;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.gl2.GLUT;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.ugs.nbm.visualizer.shared.IRendererNotifier;
import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class AutoLevelPreview extends Renderable {
    private final IRendererNotifier notifier;
    private final GLUT glut;

    public AutoLevelPreview(int priority, IRendererNotifier notifier) {
        super(10);

        this.notifier = notifier;

        glut = new GLUT();
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

    private ImmutableCollection<Position> positions;
    public void updateSettings(ImmutableCollection<Position> positions) {
        if (positions != null && !positions.isEmpty() && this.notifier != null) {
            this.positions = positions;
            this.notifier.forceRedraw();
        }
    }

    @Override
    public void draw(GLAutoDrawable drawable, boolean idle, Point3d workCoord, Point3d objectMin, Point3d objectMax, double scaleFactor, Point3d mouseWorldCoordinates, Point3d rotation) {
        // Don't draw something invalid.
        if (positions == null || positions.isEmpty()) {
            return;
        }

        Position first = Iterables.getFirst(positions, null);
        Units unit = first.getUnits();

        double objectX = objectMax.x - objectMin.x;
        double objectY = objectMax.y - objectMin.y;
        double diameter = Math.max(objectX*0.005, objectY*0.005);
                
        double minx, miny, minz;
        double maxx, maxy, maxz;

        minx = maxx = first.x;
        miny = maxy = first.y;
        minz = maxz = first.z;
        
        GL2 gl = drawable.getGL().getGL2();
        gl.glPushMatrix();
            gl.glEnable(GL2.GL_LIGHTING); 

            // Scale inch to mm if needed
            if (unit != Units.MM) {
                double scale = UnitUtils.scaleUnits(unit, Units.MM);
                gl.glScaled(scale, scale, scale);
            }

            gl.glColor4fv(new float[]{0.1f, 0.1f, 0.1f, 1.0f}, 0);
            for (Position p : positions) {
                gl.glPushMatrix();
                    gl.glTranslated(p.x, p.y, p.z);
                    glut.glutSolidSphere(diameter, 7, 7);

                    // update min/max
                    minx = Math.min(minx, p.x);
                    maxx = Math.max(maxx, p.x);
                    miny = Math.min(miny, p.y);
                    maxz = Math.max(maxz, p.z);
                    minz = Math.min(minz, p.z);
                    maxy = Math.max(maxy, p.y);
                gl.glPopMatrix();
            }

            gl.glPushMatrix();
                gl.glTranslated(
                        (minx+maxx)/2,
                        (miny+maxy)/2,
                        (minz+maxz)/2);
                gl.glScaled(maxx-minx, maxy-miny, maxz-minz);
                gl.glColor4fv(new float[]{0.3f, 0, 0, 0.1f}, 0);
                glut.glutSolidCube((float) 1.);
            gl.glPopMatrix();

            gl.glDisable(GL2.GL_LIGHTING); 
        gl.glPopMatrix();
    }
}
