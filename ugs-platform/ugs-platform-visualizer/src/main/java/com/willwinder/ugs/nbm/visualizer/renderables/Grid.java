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

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.ugs.nbm.visualizer.shader.PlainShader;
import com.willwinder.ugs.nbm.visualizer.shared.VertexObjectRenderable;
import com.willwinder.universalgcodesender.model.Position;

import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_GRID;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_X;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_XY_GRID;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_Y;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_Z;

/**
 * @author wwinder
 */
public class Grid extends VertexObjectRenderable {
    private float[] gridLineColor;
    private float[] xAxisColor;
    private float[] yAxisColor;
    private float[] zAxisColor;

    public Grid(String title) {
        super(10, title, VISUALIZER_OPTION_GRID, new PlainShader());
        reloadPreferences(new VisualizerOptions());
    }

    @Override
    public final void reloadPreferences(VisualizerOptions vo) {
        super.reloadPreferences(vo);
        gridLineColor = VisualizerOptions.colorToFloatArray(vo.getOptionForKey(VISUALIZER_OPTION_XY_GRID).value);
        xAxisColor = VisualizerOptions.colorToFloatArray(vo.getOptionForKey(VISUALIZER_OPTION_X).value);
        yAxisColor = VisualizerOptions.colorToFloatArray(vo.getOptionForKey(VISUALIZER_OPTION_Y).value);
        zAxisColor = VisualizerOptions.colorToFloatArray(vo.getOptionForKey(VISUALIZER_OPTION_Z).value);
    }

    @Override
    public void render(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glLineWidth(1.2f);
        gl.glDrawArrays(GL.GL_LINES, 0, getVertexCount());
    }

    @Override
    public void reloadModel(GL2 gl, Position bottomLeft, Position topRight, double scaleFactor) {
        for (double x = bottomLeft.x; x <= topRight.x; x += getStepSize()) {
            for (double y = bottomLeft.y; y <= topRight.y; y += getStepSize()) {
                if (x == 0) continue;
                addVertex(x, bottomLeft.y, 0);
                addColor(gridLineColor);

                addVertex(x, topRight.y, 0);
                addColor(gridLineColor);

                if (y == 0) continue;
                addVertex(bottomLeft.x, y, 0);
                addColor(gridLineColor);

                addVertex(topRight.x, y, 0);
                addColor(gridLineColor);
            }
        }

        addVertex(0, bottomLeft.y, 0);
        addColor(yAxisColor);
        addVertex(0, topRight.y, 0);
        addColor(yAxisColor);

        addVertex(bottomLeft.x, 0, 0);
        addColor(xAxisColor);
        addVertex(topRight.x, 0, 0);
        addColor(xAxisColor);

        addVertex(0, 0, bottomLeft.z);
        addColor(zAxisColor);
        addVertex(0, 0, Math.max(topRight.z, -bottomLeft.z));
        addColor(zAxisColor);
    }
}
