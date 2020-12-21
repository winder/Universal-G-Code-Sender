/**
 * Display some lines and measurements for the current objects size.
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

import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_SIZE;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.visualizer.VisualizerUtils;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;

/**
 *
 * @author wwinder
 */
public class SizeDisplay extends Renderable {

    private static final DecimalFormat FORMATTER = new DecimalFormat("#.##");
    private Units units = Units.MM;

    private TextRenderer renderer;
    private float[] color;
    private boolean textRendererDirty = true;

    public SizeDisplay(String title) {
        super(3, title);
        reloadPreferences(new VisualizerOptions());
    }

    public void setUnits(Units units) {
        this.units = units;
    }

    @Override
    final public void reloadPreferences(VisualizerOptions vo) {
        color = VisualizerOptions.colorToFloatArray(vo.getOptionForKey(VISUALIZER_OPTION_SIZE).value);
        textRendererDirty = true;
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
        renderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 72));
        renderer.setColor(color[0], color[1], color[2], color[3]);
        textRendererDirty = false;
    }

    private static String getTextForMM(double mm, Units goal) {
        Position p = new Position(mm, 0, 0, Units.MM);
        double converted = p.getPositionIn(goal).x;
        return FORMATTER.format(converted) + " " + goal.abbreviation;
    }

    @Override
    public void draw(GLAutoDrawable drawable, boolean idle, Position machineCoord, Position workCoord, Position focusMin, Position focusMax, double scaleFactor, Position mouseCoordinates, Position rotation) {
        if (idle) return;

        if (textRendererDirty) init(drawable);

        double maxSide = VisualizerUtils.findMaxSide(focusMin, focusMax);
        double buffer = maxSide * 0.03;
        double offset = buffer*2;

        GL2 gl = drawable.getGL().getGL2();

            // X
            gl.glPushMatrix();
                gl.glTranslated(0, -offset, 0);
                gl.glColor4fv(color, 0);
                gl.glLineWidth(2f);
                gl.glBegin(GL2.GL_LINES);
                    gl.glVertex3d(focusMin.x, focusMin.y, 0);
                    gl.glVertex3d(focusMin.x, focusMin.y-offset, 0);
                    gl.glVertex3d(focusMin.x, focusMin.y-buffer, 0);
                    gl.glVertex3d(focusMax.x, focusMin.y-buffer, 0);
                    gl.glVertex3d(focusMax.x, focusMin.y-offset, 0);
                    gl.glVertex3d(focusMax.x, focusMin.y, 0);
                gl.glEnd();
                
                {
                renderer.begin3DRendering();
                double xSize = focusMax.x-focusMin.x;
                String text = this.getTextForMM(xSize, units);
                Rectangle2D bounds = renderer.getBounds(text);
                float w = (float) bounds.getWidth();
                float h = (float) bounds.getHeight();

                float textScaleFactor = (float)(buffer/h);
                // Center text and move to line.
                gl.glTranslated((focusMin.x+focusMax.x)/2-(w*textScaleFactor/2),
                        focusMin.y-offset, 0);
                //gl.glRotated(-rotation.y, 1.0, 0.0, 0.0);
                //gl.glRotated(-rotation.x, 0.0, 1.0, 0.0);
                renderer.draw3D(text,
                        0f, 0f,
                        0f, textScaleFactor);
                renderer.end3DRendering();
                }
            gl.glPopMatrix();

            // Y
            gl.glPushMatrix();
                gl.glTranslated(-offset, 0, 0);
                gl.glColor4fv(color, 0);
                gl.glLineWidth(2f);
                gl.glBegin(GL2.GL_LINES);
                    gl.glVertex3d(focusMin.x       , focusMin.y, 0);
                    gl.glVertex3d(focusMin.x-offset, focusMin.y, 0);
                    gl.glVertex3d(focusMin.x-buffer, focusMin.y, 0);
                    gl.glVertex3d(focusMin.x-buffer, focusMax.y, 0);
                    gl.glVertex3d(focusMin.x-offset, focusMax.y, 0);
                    gl.glVertex3d(focusMin.x       , focusMax.y, 0);
                gl.glEnd();

                {
                renderer.begin3DRendering();
                double ySize = focusMax.y-focusMin.y;
                String text = this.getTextForMM(ySize, units);
                Rectangle2D bounds = renderer.getBounds(text);
                float w = (float) bounds.getWidth();
                float h = (float) bounds.getHeight();

                float textScaleFactor = (float)(buffer/h);
                // Center text and move to line.
                gl.glRotated(90,0,0,1);
                gl.glTranslated((focusMin.y+focusMax.y)/2-(w*textScaleFactor/2),
                        -focusMin.x+buffer*1.1, 0);
                //gl.glRotated(rotation.y, 0.0, 1.0, 0.0);
                //gl.glRotated(-rotation.x, 1.0, 0.0, 0.0);
                renderer.draw3D(text,
                        0f, 0f,
                        0f, textScaleFactor);
                renderer.end3DRendering();
                }
            gl.glPopMatrix();

            // Z
            gl.glPushMatrix();
                gl.glTranslated(offset, 0, 0);
                gl.glColor4fv(color, 0);
                gl.glLineWidth(2f);
                gl.glBegin(GL2.GL_LINES);
                    gl.glVertex3d(focusMax.x       , focusMin.y, focusMin.z);
                    gl.glVertex3d(focusMax.x+offset, focusMin.y, focusMin.z);
                    gl.glVertex3d(focusMax.x+buffer, focusMin.y, focusMin.z);
                    gl.glVertex3d(focusMax.x+buffer, focusMin.y, focusMax.z);
                    gl.glVertex3d(focusMax.x+offset, focusMin.y, focusMax.z);
                    gl.glVertex3d(focusMax.x       , focusMin.y, focusMax.z);
                gl.glEnd();

                {
                renderer.begin3DRendering();
                double zSize = focusMax.z-focusMin.z;
                String text = this.getTextForMM(zSize, units);
                Rectangle2D bounds = renderer.getBounds(text);
                float w = (float) bounds.getWidth();
                float h = (float) bounds.getHeight();

                float textScaleFactor = (float)(buffer/h);
                // Center text and move to line.
                gl.glRotated(90,1,0,0);
                gl.glTranslated(focusMax.x + buffer*1.1,
                        (focusMin.z+focusMax.z)/2-(h*textScaleFactor/2),
                        //focusMin.y-offset,
                        -focusMin.y);
                gl.glRotated(-rotation.y-90, 1.0, 0.0, 0.0);
                gl.glRotated(-rotation.x, 0.0, 1.0, 0.0);
                renderer.draw3D(text,
                        0f, 0f,
                        0f, textScaleFactor);
                renderer.end3DRendering();
                }
            gl.glPopMatrix();
    }
    
}
