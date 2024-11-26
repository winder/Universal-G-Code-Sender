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

import com.jogamp.opengl.GL;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.FloatBuffer;
import java.util.List;

public class RenderableUtils {

    public static void bindVertexBuffer(GL gl, int vertexBufferId, List<Float> vertexList) {
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBufferId);

        // Send vertex data to the GPU
        float[] floatArray = ArrayUtils.toPrimitive(vertexList.toArray(new Float[0]));
        gl.glBufferData(GL.GL_ARRAY_BUFFER, (long) floatArray.length * Float.BYTES, FloatBuffer.wrap(floatArray), GL.GL_DYNAMIC_DRAW);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
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
