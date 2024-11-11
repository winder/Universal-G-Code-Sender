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
package com.willwinder.ugs.nbm.visualizer.utils;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.FloatBuffer;
import java.util.List;

public class RenderableUtils {
    /**
     * Generates and binds a color buffer
     *
     * @param gl                the GL context to use
     * @param colorList         a list of float values containing the color RGBA values
     * @param vertexBufferIndex the parameter index of the shader to bind to
     * @return the id of the color buffer to be reference to when rendering
     */
    public static int bindColorBuffer(GL2 gl, List<Float> colorList, int vertexBufferIndex) {
        // Create and upload the Color Buffer Object (CBO) for colors
        int[] cbo = new int[1];
        gl.glGenBuffers(1, cbo, 0);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, cbo[0]);

        // Upload vertex colors to the GPU
        FloatBuffer colorBuffer = Buffers.newDirectFloatBuffer(ArrayUtils.toPrimitive(colorList.toArray(new Float[0])));
        gl.glBufferData(GL.GL_ARRAY_BUFFER, colorBuffer.capacity() * Float.BYTES, colorBuffer, GL.GL_STATIC_DRAW);

        // Specify vertex attribute layout (index 1 for color)
        gl.glEnableVertexAttribArray(vertexBufferIndex);
        gl.glVertexAttribPointer(vertexBufferIndex, 4, GL.GL_FLOAT, false, 4 * Float.BYTES, 0);

        return cbo[0];
    }

    /**
     * Generates and binds a color buffer
     *
     * @param gl                the GL context to use
     * @param normalList        a list of float values containing the vertex normal
     * @param normalBufferIndex the parameter index of the shader to bind to
     * @return the id of the color buffer to be reference to when rendering
     */
    public static int bindNormalBuffer(GL2 gl, List<Float> normalList, int normalBufferIndex) {
        // Create and upload the Normal Buffer Object (NBO) for colors
        int[] nbo = new int[1];
        gl.glGenBuffers(1, nbo, 0);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, nbo[0]);

        // Upload vertex colors to the GPU
        FloatBuffer normalBuffer = Buffers.newDirectFloatBuffer(ArrayUtils.toPrimitive(normalList.toArray(new Float[0])));
        gl.glBufferData(GL.GL_ARRAY_BUFFER, normalBuffer.capacity() * Float.BYTES, normalBuffer, GL.GL_STATIC_DRAW);

        // Specify vertex attribute layout (index 1 for color)
        gl.glEnableVertexAttribArray(normalBufferIndex);
        gl.glVertexAttribPointer(normalBufferIndex, 4, GL.GL_FLOAT, false, 4 * Float.BYTES, 0);

        return nbo[0];
    }


    public static int bindVertexBuffer(GL2 gl, List<Float> vertexList, int positionIndex) {
        // Create a Vertex Buffer Object (VBO)
        int[] vbo = new int[1];
        gl.glGenBuffers(1, vbo, 0);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vbo[0]);

        // Send vertex data to the GPU
        FloatBuffer vertexBuffer = Buffers.newDirectFloatBuffer(ArrayUtils.toPrimitive(vertexList.toArray(new Float[0])));
        gl.glBufferData(GL.GL_ARRAY_BUFFER, vertexBuffer.capacity() * Float.BYTES, vertexBuffer, GL.GL_STATIC_DRAW);

        // Specify the layout of the vertex data
        gl.glEnableVertexAttribArray(positionIndex);
        gl.glVertexAttribPointer(positionIndex, 3, GL.GL_FLOAT, false, 3 * Float.BYTES, 0);

        return vbo[0];
    }

    public static int bindVertexObject(GL2 gl) {
        int[] vao = new int[1];
        gl.glGenVertexArrays(1, vao, 0);
        gl.glBindVertexArray(vao[0]);
        return vao[0];
    }

    public static void addColor(List<Float> colorList, float[] color) {
        colorList.add(color[0]);
        colorList.add(color[1]);
        colorList.add(color[2]);

        if (color.length > 3) {
            colorList.add(color[3]);
        } else {
            colorList.add(1f);
        }
    }

    public static void addVertex(List<Float> vertexList, double x, double y, double z) {
        vertexList.add((float) x);
        vertexList.add((float) y);
        vertexList.add((float) z);
    }


    public static double getStepSize(double scaleFactor) {
        if (scaleFactor < 0.001) {
            return 50;
        } else if (scaleFactor < 0.01) {
            return 10;
        } else if (scaleFactor < 0.04) {
            return 5;
        } else if (scaleFactor < 0.1) {
            return 1;
        }
        return 0.5;
    }
}
