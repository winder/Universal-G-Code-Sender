/*
    Copyright 2017-2024 Will Winder

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
package com.willwinder.ugs.platform.surfacescanner.renderable;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Iterables;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.util.gl2.GLUT;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_AUTOLEVEL_PREVIEW;
import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import com.willwinder.universalgcodesender.model.CNCPoint;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;

import java.util.Arrays;
import java.util.stream.DoubleStream;

/**
 * @author wwinder
 */
public class AutoLevelPreview extends Renderable {

    private static final String TOUCH_PLATE_VISIBLE_OPTION = "platform.visualizer.autolevel.touch-plate";

    private final GLUT glut;

    private ImmutableCollection<Position> positions;
    private Position[][] grid = null;
    private double touchPlateThickness = 0;
    private double probeAreaMinZ = 0;
    private boolean touchPlateVisible = VisualizerOptions.getBooleanOption(TOUCH_PLATE_VISIBLE_OPTION, true);

    // The maximum distance of a probe used for coloring.
    private double maxZ;
    private double minZ;

    private float[] high = {0, 255, 0}; // green
    private float[] low = {255, 0, 0}; // red
    private static final float[] touchPlateColor = {0.4f, 0.5f, 0.8f, 0.35f};
    private static final float[] probeAreaColor = {0.3f, 0, 0, 0.2f};

    public AutoLevelPreview(String title) {
        super(10, title, VISUALIZER_OPTION_AUTOLEVEL_PREVIEW);

        glut = new GLUT();

        reloadPreferences(new VisualizerOptions());
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
    public final void reloadPreferences(VisualizerOptions vo) {
        super.reloadPreferences(vo);
        high = VisualizerOptions.colorToFloatArray(vo.getOptionForKey(VisualizerOptions.VISUALIZER_OPTION_HIGH).value);
        low = VisualizerOptions.colorToFloatArray(vo.getOptionForKey(VisualizerOptions.VISUALIZER_OPTION_LOW).value);
    }

    public boolean isTouchPlateVisible() {
        return touchPlateVisible;
    }

    public void setTouchPlateVisible(boolean touchPlateVisible) {
        this.touchPlateVisible = touchPlateVisible;
        VisualizerOptions.setBooleanOption(TOUCH_PLATE_VISIBLE_OPTION, touchPlateVisible);
    }

    public void updateSettings(
            ImmutableCollection<Position> positions,
            final Position[][] grid,
            double probeAreaMinZ,
            double touchPlateThickness) {
        this.probeAreaMinZ = probeAreaMinZ;
        this.touchPlateThickness = touchPlateThickness;
        if (positions != null && !positions.isEmpty()) {
            this.positions = positions;
            this.grid = grid;
            updateMinMaxZ();
        }
    }

    @Override
    public void draw(GLAutoDrawable drawable, boolean idle, Position machineCoord, Position workCoord, Position objectMin, Position objectMax, double scaleFactor, Position mouseWorldCoordinates, Position rotation) {

        // Don't draw something invalid.
        if (positions == null || positions.isEmpty()) {
            return;
        }

        Position first = Iterables.getFirst(positions, null);
        Units unit = first.getUnits();

        double objectX = objectMax.x - objectMin.x;
        double objectY = objectMax.y - objectMin.y;
        double diameter = Math.max(objectX * 0.005, objectY * 0.005);

        double minx, miny;
        double maxx, maxy;
        double maxz;

        minx = maxx = first.x;
        miny = maxy = first.y;
        maxz = first.z;

        GL2 gl = drawable.getGL().getGL2();
        gl.glPushMatrix();
        // Scale inch to mm if needed
        double scale = UnitUtils.scaleUnits(unit, Units.MM);
        if (unit != Units.MM) {
            gl.glScaled(scale, scale, scale);
        }

        // Balls indicating the probe start locations.
        gl.glColor4fv(new float[]{0.1f, 0.1f, 0.1f, 1.0f}, 0);
        for (Position p : positions) {
            gl.glPushMatrix();
            gl.glTranslated(p.x, p.y, p.z);
            glut.glutSolidSphere(diameter / scale, 7, 7);

            // update min/max
            minx = Math.min(minx, p.x);
            maxx = Math.max(maxx, p.x);
            miny = Math.min(miny, p.y);
            maxz = Math.max(maxz, p.z);
            maxy = Math.max(maxy, p.y);
            gl.glPopMatrix();
        }

        drawProbedSurface(gl);

        // With lighting enabled the alpha channel is taken from the diffuse material instead of the
        // current color, so translucent geometry would be drawn opaque
        gl.glDisable(GL2.GL_LIGHTING);
        drawProbeAreaOutline(gl, minx, maxx, miny, maxy, maxz);
        drawProbeAreaOutline(gl, minx, maxx, miny, maxy, probeAreaMinZ);
        drawTouchPlate(gl, minx, maxx, miny, maxy);
        gl.glEnable(GL2.GL_LIGHTING);

        gl.glPopMatrix();
    }

    private void drawProbeAreaOutline(GL2 gl, double minx, double maxx, double miny, double maxy, double z) {
        gl.glPushMatrix();
        gl.glTranslated(
                (minx + maxx) / 2,
                (miny + maxy) / 2,
                z);
        gl.glScaled(maxx - minx, maxy - miny, 0);
        gl.glColor4fv(probeAreaColor, 0);
        glut.glutWireCube((float) 1.);
        gl.glPopMatrix();
    }

    private void drawTouchPlate(GL2 gl, double minx, double maxx, double miny, double maxy) {
        if (!touchPlateVisible || touchPlateThickness <= 0) {
            return;
        }

        // Translucent geometry must not hide anything drawn after it
        gl.glDepthMask(false);
        gl.glPushMatrix();
        gl.glTranslated(
                (minx + maxx) / 2,
                (miny + maxy) / 2,
                touchPlateThickness / 2);
        gl.glScaled(maxx - minx, maxy - miny, touchPlateThickness);
        gl.glColor4fv(touchPlateColor, 0);
        glut.glutSolidCube((float) 1.);
        gl.glPopMatrix();
        gl.glDepthMask(true);
    }

    private void setColorForZ(GL2 gl, double zPos) {
        float ratio = (float) ((zPos - minZ) / (maxZ - minZ));
        float r = ratio * high[0] + (1 - ratio) * low[0];
        float g = ratio * high[1] + (1 - ratio) * low[1];
        float b = ratio * high[2] + (1 - ratio) * low[2];
        float a = ratio * high[3] + (1 - ratio) * low[3];

        gl.glColor4f(r, g, b, a);
    }

    private void drawProbedSurface(GL2 gl) {
        if (this.grid == null) {
            return;
        }

        /*
    0,5 ?   ?   ?   ?   ? 5,5

        *   ?   ?   ?   ?
          \
        *   *   ?   ?   ?
          x   \
        *   *   *   ?   ?
          x   x   \
    0,0 *   *   *   *   ? 5,0

        */

        gl.glBegin(GL2.GL_TRIANGLES);
        int xLen = this.grid.length;
        for (int x = 0; x < xLen - 1; x++) {
            if (this.grid[x] == null || this.grid[x + 1] == null) {
                continue;
            }

            for (int y = 0; y < this.grid[x].length - 1; y++) {
                Position pos1 = this.grid[x][y];
                Position pos2 = this.grid[x + 1][y];
                Position pos3 = this.grid[x][y + 1];
                Position pos4 = this.grid[x + 1][y + 1];

                // Bottom left of quad
                if (pos1 != null && pos2 != null && pos3 != null) {
                    setColorForZ(gl, pos1.z);
                    gl.glVertex3d(pos1.x, pos1.y, pos1.z); // Left Of Triangle (Front)

                    setColorForZ(gl, pos3.z);
                    gl.glVertex3d(pos3.x, pos3.y, pos3.z); // Top Of Triangle (Front)

                    setColorForZ(gl, pos2.z);
                    gl.glVertex3d(pos2.x, pos2.y, pos2.z); // Right Of Triangle (Front)
                }
                // Top right of quad
                if (pos2 != null && pos3 != null && pos4 != null) {
                    setColorForZ(gl, pos4.z);
                    gl.glVertex3d(pos4.x, pos4.y, pos4.z); // Right Of Triangle (Front)

                    setColorForZ(gl, pos3.z);
                    gl.glVertex3d(pos3.x, pos3.y, pos3.z); // Top Of Triangle (Front)

                    setColorForZ(gl, pos2.z);
                    gl.glVertex3d(pos2.x, pos2.y, pos2.z); // Left Of Triangle (Front)
                }
            }
        }

        gl.glEnd();
    }

    private void updateMinMaxZ() {
        // Figure out the min/max scanned points
        minZ = getGridZStream()
                .min()
                .orElse(0);

        maxZ = getGridZStream()
                .max()
                .orElse(0);
    }

    private DoubleStream getGridZStream() {
        return Arrays.stream(grid).flatMap(Arrays::stream)
                .mapToDouble(CNCPoint::getZ)
                .filter(z -> !Double.isNaN(z));
    }
}
