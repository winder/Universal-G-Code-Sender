/*
    Copyright 2017-2018 Will Winder

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
package com.willwinder.ugs.platform.probe.renderable;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.gl2.GLUT;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import com.willwinder.ugs.platform.probe.ProbeService.ProbeParameters;
import com.willwinder.ugs.platform.probe.renderable.ProbeRenderableHelpers.Side;
import com.willwinder.universalgcodesender.model.Position;

import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_PROBE_PREVIEW;
import static com.willwinder.ugs.platform.probe.renderable.ProbeRenderableHelpers.Side.NEGATIVE;
import static com.willwinder.ugs.platform.probe.renderable.ProbeRenderableHelpers.Side.POSITIVE;
import static com.willwinder.ugs.platform.probe.renderable.ProbeRenderableHelpers.drawArrow;
import static com.willwinder.ugs.platform.probe.renderable.ProbeRenderableHelpers.drawTouchPlate;

/**
 *
 * @author risototh
 */
public class HoleCenterPathPreview extends Renderable
{
    private final Position spacing = new Position(0, 0, 0);
    private final Position thickness = new Position(0, 0, 0);
    private Position startWork = null;
    private Position startMachine = null;
    private ProbeParameters pc = null;
    private double hcDiameter = 0;

    private final GLUT glut;

    public HoleCenterPathPreview(String title) {
        super(10, title);
        glut = new GLUT();
    }

    public void setContext(ProbeParameters pc, Position startWork, Position startMachine) {
        this.pc = pc;
        this.startWork = startWork;
        this.startMachine = startMachine;
    }

    @Override
    public boolean isEnabled() {
        return VisualizerOptions.getBooleanOption(VISUALIZER_OPTION_PROBE_PREVIEW, true);
    }

    @Override
    public void setEnabled(boolean enabled) {
        VisualizerOptions.setBooleanOption(VISUALIZER_OPTION_PROBE_PREVIEW, enabled);
    }

    public void updateSpacing(double hcDiameter) {
        this.hcDiameter = hcDiameter;
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

    private boolean invalidSettings() {
        return this.hcDiameter <= 0;
    }

    @Override
    public void draw(GLAutoDrawable drawable, boolean idle, Position machineCoord, Position workCoord, Position objectMin, Position objectMax, double scaleFactor, Position mouseWorldCoordinates, Position rotation)
    {
        double inset = 0.5;

        if (invalidSettings()) return;

        GL2 gl = drawable.getGL().getGL2();

        if (startWork != null && pc.endPosition == null) {
            // The WCS is reset at the start of these operations.
            if (pc.startPosition != null) {
            }
            // Follow tool.
            else {
                gl.glTranslated(startWork.x, startWork.y, startWork.z);
            }
        }
        // Follow tool.
        else {
            gl.glTranslated(workCoord.x, workCoord.y, workCoord.z);
        }

        // TODO scale and draw hole
        drawHole(gl, glut, hcDiameter);

        // draw arrows
        gl.glColor4d(8., 0., 0., 1);

        // x probe arrows
        drawArrow(gl, glut,
                new Position(0, 0, 0),
                new Position(-0.5 * hcDiameter, 0, 0));
        drawArrow(gl, glut,
                new Position(-0.5 * hcDiameter, -inset, 0),
                new Position(+0.5 * hcDiameter, -inset, 0));
        drawArrow(gl, glut,
                new Position(+0.5 * hcDiameter, 0, 0),
                new Position(0, 0, 0));

        // y probe arrows
        drawArrow(gl, glut,
                new Position(0, 0, 0),
                new Position(0, -0.5 * hcDiameter, 0));
        drawArrow(gl, glut,
                new Position(inset, -0.5 * hcDiameter, 0),
                new Position(inset, +0.5 * hcDiameter, 0));
        drawArrow(gl, glut,
                new Position(0, +0.5 * hcDiameter, 0),
                new Position(0, 0, 0));
    }

    private static void drawHole(GL2 gl, GLUT glut, double holeDiameter) {
        // TODO a lot of work... draw the cilinder 5mm over and 5mm under the Z0, scale the model by holeDiameter
    }
}
