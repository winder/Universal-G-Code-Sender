/*
    Copyright 2017-2023 Will Winder

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
import com.willwinder.ugs.platform.probe.ProbeParameters;
import com.willwinder.ugs.platform.probe.ProbeSettings;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;

/**
 *
 * @author wwinder
 */
public class ZProbePathPreview extends AbstractProbePreview {
    private Double probeDepth = null;
    private Double probeOffset = null;
    private Position start = null;

    private final GLUT glut;

    public ZProbePathPreview() {
        super(10, Localization.getString("probe.visualizer.z-preview"));
        glut = new GLUT();
    }

    @Override
    public void setContext(ProbeParameters pc, Position startWork) {
        this.start = startWork;
    }

    @Override
    public void updateSettings() {
        UnitUtils.Units settingsUnits = ProbeSettings.getSettingsUnits();
        double scaleFactor = UnitUtils.scaleUnits(settingsUnits, UnitUtils.Units.MM);
        updateSpacing(ProbeSettings.getzDistance() * scaleFactor, ProbeSettings.getzOffset() * scaleFactor);
    }

    public void updateSpacing(double depth, double offset) {
        this.probeDepth = depth;
        this.probeOffset = offset;
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
    public void draw(GLAutoDrawable drawable, boolean idle, Position machineCoord, Position workCoord, Position objectMin, Position objectMax, double scaleFactor, Position mouseWorldCoordinates, Position rotation) {
        if (this.probeDepth == null || this.probeOffset == null) return;
        final int slices = 10;
        final int stacks = 10;

        int rot = (this.probeDepth > 0) ? 0 : 180;
        double zAbs = Math.abs(this.probeDepth);

        GL2 gl = drawable.getGL().getGL2();

        if (this.start != null && isProbeCycleActive()) {
            gl.glTranslated(start.x, start.y, start.z);
        } else {
            gl.glTranslated(workCoord.x, workCoord.y, workCoord.z);
        }
        gl.glRotated(rot, 1, 0, 0);

        // touch plate
        gl.glPushMatrix();
            gl.glTranslated(0, 0, zAbs);
            glut.glutSolidCylinder(5, this.probeOffset, slices*2, stacks);
        gl.glPopMatrix();

        // Everything is going to be red now!
        gl.glColor4d(8., 0., 0., 1);
        glut.glutSolidCylinder(.1, zAbs - 0.5, slices, stacks);
        gl.glTranslated(0, 0, zAbs - 1);
        glut.glutSolidCone(.2, 1, slices, stacks);
    }
}
