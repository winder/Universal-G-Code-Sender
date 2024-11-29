/*
    Copyright 2024 Will Winder

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

import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_PLANE_COLOR;

public class Plane extends VertexObjectRenderable {

    private float[] gridPlaneColor;

    public Plane(String title) {
        super(7, title, VisualizerOptions.VISUALIZER_OPTION_PLANE, new PlainShader());
    }

    @Override
    public void reloadPreferences(VisualizerOptions vo) {
        super.reloadPreferences(vo);
        gridPlaneColor = VisualizerOptions.colorToFloatArray(vo.getOptionForKey(VISUALIZER_OPTION_PLANE_COLOR).value);
    }

    @Override
    public void render(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, getVertexCount());
    }

    @Override
    public void reloadModel(GL2 gl, Position bottomLeft, Position topRight, double scaleFactor) {
        addVertex(bottomLeft.x, bottomLeft.y, -0.01);
        addColor(gridPlaneColor);

        addVertex(bottomLeft.x, topRight.y, -0.01);
        addColor(gridPlaneColor);

        addVertex(topRight.x, topRight.y, -0.01);
        addColor(gridPlaneColor);

        addVertex(topRight.x, topRight.y, -0.01);
        addColor(gridPlaneColor);

        addVertex(topRight.x, bottomLeft.y, -0.01);
        addColor(gridPlaneColor);

        addVertex(bottomLeft.x, bottomLeft.y, -0.01);
        addColor(gridPlaneColor);
    }
}
