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
import com.jogamp.opengl.fixedfunc.GLMatrixFunc;
import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.ugs.nbm.visualizer.shader.PlainShader;
import com.willwinder.ugs.nbm.visualizer.shared.VertexObjectRenderable;
import com.willwinder.universalgcodesender.model.Position;
import eu.mihosoft.vrl.v3d.CSG;
import eu.mihosoft.vrl.v3d.Color;
import eu.mihosoft.vrl.v3d.Cube;
import eu.mihosoft.vrl.v3d.Polygon;
import eu.mihosoft.vrl.v3d.RoundedCube;
import eu.mihosoft.vrl.v3d.Transform;
import eu.mihosoft.vrl.v3d.Vector3d;

import java.util.List;
import java.util.Optional;

import static com.jogamp.opengl.GL.GL_DEPTH_TEST;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_ORIENTATION_CUBE;

/**
 * Draw a cube with the orientation labeled on the sides.
 *
 * @author wwinder
 */
public class OrientationCube extends VertexObjectRenderable {
    private final CSG model;

    protected void clear() {
        super.clear();
    }

    public OrientationCube(String title) {
        super(Integer.MIN_VALUE, title, VISUALIZER_OPTION_ORIENTATION_CUBE, new PlainShader());
        reloadPreferences(new VisualizerOptions());
        model = generateModel();
    }

    @Override
    public boolean center() {
        return false;
    }

    private CSG generateModel() {
        double size = 0.5;

        Color faceColor = Color.LIGHTGRAY;
        Color cornerColor = Color.GRAY;

        CSG cube = new Cube(size, size * 0.8, size * 0.8).toCSG()
                .union(new Cube(size * 0.8, size * 0.8, size).toCSG())
                .union(new Cube(size * 0.8, size, size * 0.8).toCSG())
                .setColor(faceColor);

        CSG corners = new RoundedCube(size).cornerRadius(0.03).toCSG().setColor(cornerColor);

        CSG result = cube.union(corners);

        CSG text = CSG.text("Z-", 1)
                .setColor(Color.BLUE)
                .transformed(new Transform().scale(0.01).rot(180, 0, 0).translate(-15, -10, 25));
        result = result.dumbUnion(text);

        text = CSG.text("Z+", 1)
                .setColor(Color.BLUE)
                .transformed(new Transform().scale(0.01).translate(-16, -10, 25));
        result = result.dumbUnion(text);

        text = CSG.text("Y+", 1)
                .setColor(Color.GREEN)
                .transformed(new Transform().scale(0.01).rot(90, 0, 180).translate(-16, -10, 25));
        result = result.dumbUnion(text);

        text = CSG.text("Y-", 1)
                .setColor(Color.GREEN)
                .transformed(new Transform().scale(0.01).rot(270, 0, 0).translate(-14, -10, 25));
        result = result.dumbUnion(text);

        text = CSG.text("X-", 1)
                .setColor(Color.RED)
                .transformed(new Transform().scale(0.01).rot(270, 90, 0).translate(-14, -10, 25));
        result = result.dumbUnion(text);

        text = CSG.text("X+", 1)
                .setColor(Color.RED)
                .transformed(new Transform().scale(0.01).rot(270, 270, 0).translate(-16, -10, 25));
        result = result.dumbUnion(text);

        return result.scale(0.8);
    }

    private void generateBuffers(CSG csg) {
        clear();
        List<Polygon> polygons = csg.triangulate().getPolygons();

        for (Polygon polygon : polygons) {
            Color color = Optional.ofNullable(polygon.getColor()).orElse(Color.GRAY);
            float[] colorArray = new float[]{(float) color.getRed(), (float) color.getGreen(), (float) color.getBlue(), 1f};

            Vector3d a = polygon.getPoints().get(0);
            Vector3d b = polygon.getPoints().get(1);
            Vector3d c = polygon.getPoints().get(2);
            Vector3d normal = b.minus(a).cross(c.minus(a)).normalized();

            polygon.getPoints().forEach(point -> {
                addPoint(point, normal, colorArray);
            });
        }
    }

    private void addPoint(Vector3d point, Vector3d normal, float[] colorArray) {
        addVertex(point.getX(), point.getY(), point.getZ());
        addNormal(normal.getX(), normal.getY(), normal.getZ());
        addColor(colorArray);
    }

    @Override
    public void render(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        int ySize = drawable.getDelegatedDrawable().getSurfaceHeight();
        int xSize = drawable.getDelegatedDrawable().getSurfaceWidth();

        // Set viewport to the corner.
        float fromEdge = 0.8f;
        int squareSize = ySize - (int) (ySize * fromEdge);


        gl.glViewport(0, (int) (ySize * fromEdge), squareSize, squareSize);

        gl.glMatrixMode(GLMatrixFunc.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(-0.5, 0.5, -0.5, 0.5, -1, 2);
        gl.glMatrixMode(GLMatrixFunc.GL_MODELVIEW);

        gl.glEnable(GL_DEPTH_TEST);
        int count = getVertexCount();
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, count);

        gl.glViewport(0, 0, xSize, ySize);
    }

    @Override
    public void reloadModel(GL2 gl, Position bottomLeft, Position topRight, double scaleFactor) {
        generateBuffers(model);
    }
}
