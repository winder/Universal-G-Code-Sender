/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.component.designer.render;

import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.universalgcodesender.fx.component.designer.editor.DesignEditor;
import com.willwinder.universalgcodesender.fx.component.designer.editor.EditorState;
import com.willwinder.universalgcodesender.fx.component.designer.editor.HandleSet;
import com.willwinder.universalgcodesender.fx.component.designer.editor.VertexTool;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.LineMeshBuilder;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.MeshHandle;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.RenderContext;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Renderable;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.SceneLayer;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.SceneMeshes;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.VertexLayout;
import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;
import javafx.geometry.Point3D;
import javafx.scene.paint.Color;

import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Draws what the editor tools need on top of the design: the selection frame, the resize and
 * rotate handles, the vertex handles, the rubber band and the creation preview. Everything is
 * sized in pixels and rebuilt every frame, batched into one line mesh and one triangle mesh per
 * colour so a frame costs a handful of uploads regardless of how many handles there are.
 */
public final class HandlesRenderable implements Renderable {
    private static final Color FRAME_COLOR = Color.rgb(122, 161, 228);
    private static final Color RUBBER_BAND_FILL = Color.rgb(122, 161, 228, 0.15);
    private static final Color VERTEX_COLOR = Color.GRAY;
    private static final int ROTATE_HANDLE_SIDES = 12;

    private final DesignEditor editor;
    private final Map<Color, List<Float>> trianglesByColor = new LinkedHashMap<>();

    public HandlesRenderable(DesignEditor editor) {
        this.editor = editor;
    }

    @Override
    public SceneLayer layer() {
        return SceneLayer.HANDLES;
    }

    @Override
    public boolean isVisible() {
        return editor.isActive();
    }

    @Override
    public void render(RenderContext context) {
        EditorState state = editor.state();
        trianglesByColor.clear();
        LineMeshBuilder frameLines = new LineMeshBuilder(64);

        editor.controller().ifPresent(controller -> {
            if (editor.currentToolType() == Tool.SELECT || editor.currentToolType() == Tool.VERTEX) {
                addFrame(frameLines, HandleSet.frame(controller.getSelectionManager()));
            }
        });
        for (HandleSet.Handle handle : editor.handles()) {
            addHandle(frameLines, handle, handle == state.hoveredHandle());
        }
        addVertices(state, context);
        if (state.rubberBand() != null) {
            addTriangles(RUBBER_BAND_FILL, rectangleTriangles(state.rubberBand()));
            addOutline(frameLines, state.rubberBand(), FRAME_COLOR);
        }

        List<MeshHandle> meshes = new ArrayList<>();
        for (Map.Entry<Color, List<Float>> batch : trianglesByColor.entrySet()) {
            MeshHandle mesh = context.upload(toArray(batch.getValue()), VertexLayout.MESH);
            meshes.add(mesh);
            context.drawTriangles(mesh, null, toRgba(batch.getKey()), false);
        }
        if (!frameLines.isEmpty()) {
            MeshHandle mesh = context.upload(frameLines.build(), VertexLayout.LINE);
            meshes.add(mesh);
            context.drawColoredLines(mesh, null, 1);
        }
        if (state.preview() != null) {
            float[] outline = DesignTessellator.outline(state.preview(), Color.DODGERBLUE);
            if (outline.length > 0) {
                MeshHandle mesh = context.upload(outline, VertexLayout.LINE);
                meshes.add(mesh);
                context.drawColoredLines(mesh, null, 1.5f);
            }
        }
        meshes.forEach(context::release);
    }

    private static void addFrame(LineMeshBuilder builder, List<Point2D> corners) {
        for (int i = 0; i < corners.size(); i++) {
            Point2D a = corners.get(i);
            Point2D b = corners.get((i + 1) % corners.size());
            builder.add(a.getX(), a.getY(), 0, b.getX(), b.getY(), 0, FRAME_COLOR);
        }
    }

    private void addHandle(LineMeshBuilder builder, HandleSet.Handle handle, boolean hovered) {
        VisualizerSettings settings = VisualizerSettings.getInstance();
        Color color = Color.web(handle.kind() == HandleSet.Kind.ROTATE
                ? settings.colorDesignRotationProperty().get()
                : settings.colorDesignResizeProperty().get());
        if (hovered) {
            color = color.interpolate(Color.WHITE, 0.4);
        }
        int sides = handle.kind() == HandleSet.Kind.ROTATE ? ROTATE_HANDLE_SIDES : 4;
        double radius = handle.kind() == HandleSet.Kind.ROTATE ? handle.size() / 2 : handle.size() / 2 * Math.sqrt(2);
        addTriangles(color, polygonTriangles(handle.center(), radius, sides));
        addPolygonOutline(builder, handle.center(), radius, sides, FRAME_COLOR);
    }

    private void addVertices(EditorState state, RenderContext context) {
        List<Point2D> vertices = state.vertices();
        for (int i = 0; i < vertices.size(); i++) {
            Point2D vertex = vertices.get(i);
            double worldPerPixel = context.camera().worldUnitsPerPixelAt(new Point3D(vertex.getX(), vertex.getY(), 0));
            double radius = VertexTool.HANDLE_SIZE_PX * worldPerPixel / 2 * Math.sqrt(2);
            Color color = i == state.hoveredVertex() ? FRAME_COLOR : VERTEX_COLOR;
            addTriangles(color, polygonTriangles(vertex, radius, 4));
        }
    }

    private void addTriangles(Color color, float[] vertices) {
        List<Float> batch = trianglesByColor.computeIfAbsent(color, key -> new ArrayList<>());
        for (float value : vertices) {
            batch.add(value);
        }
    }

    private static void addOutline(LineMeshBuilder builder, Shape shape, Color color) {
        float[] segments = DesignTessellator.outline(shape, color);
        int floats = VertexLayout.LINE.floatsPerVertex();
        for (int i = 0; i + 2 * floats <= segments.length; i += 2 * floats) {
            builder.add(segments[i], segments[i + 1], 0, segments[i + floats], segments[i + floats + 1], 0, color);
        }
    }

    /**
     * A regular polygon as a triangle fan, rotated so a four sided one is an axis aligned
     * square.
     */
    private static float[] polygonTriangles(Point2D center, double radius, int sides) {
        float[] vertices = new float[sides * 3 * SceneMeshes.FLOATS_PER_VERTEX];
        int offset = 0;
        double phase = sides == 4 ? Math.PI / 4 : 0;
        for (int i = 0; i < sides; i++) {
            double a = phase + 2 * Math.PI * i / sides;
            double b = phase + 2 * Math.PI * (i + 1) / sides;
            offset = writeVertex(vertices, offset, center.getX(), center.getY());
            offset = writeVertex(vertices, offset, center.getX() + Math.cos(a) * radius, center.getY() + Math.sin(a) * radius);
            offset = writeVertex(vertices, offset, center.getX() + Math.cos(b) * radius, center.getY() + Math.sin(b) * radius);
        }
        return vertices;
    }

    private static void addPolygonOutline(LineMeshBuilder builder, Point2D center, double radius, int sides, Color color) {
        double phase = sides == 4 ? Math.PI / 4 : 0;
        for (int i = 0; i < sides; i++) {
            double a = phase + 2 * Math.PI * i / sides;
            double b = phase + 2 * Math.PI * (i + 1) / sides;
            builder.add(center.getX() + Math.cos(a) * radius, center.getY() + Math.sin(a) * radius, 0,
                    center.getX() + Math.cos(b) * radius, center.getY() + Math.sin(b) * radius, 0, color);
        }
    }

    private static float[] rectangleTriangles(Rectangle2D rectangle) {
        float[] vertices = new float[6 * SceneMeshes.FLOATS_PER_VERTEX];
        int offset = 0;
        double[][] corners = {
                {rectangle.getMinX(), rectangle.getMinY()}, {rectangle.getMaxX(), rectangle.getMinY()},
                {rectangle.getMaxX(), rectangle.getMaxY()}, {rectangle.getMinX(), rectangle.getMaxY()}
        };
        for (int corner : new int[]{0, 1, 2, 0, 2, 3}) {
            offset = writeVertex(vertices, offset, corners[corner][0], corners[corner][1]);
        }
        return vertices;
    }

    private static int writeVertex(float[] vertices, int offset, double x, double y) {
        vertices[offset] = (float) x;
        vertices[offset + 1] = (float) y;
        vertices[offset + 2] = 0;
        vertices[offset + 3] = 0;
        vertices[offset + 4] = 0;
        vertices[offset + 5] = 1;
        return offset + SceneMeshes.FLOATS_PER_VERTEX;
    }

    private static float[] toArray(List<Float> values) {
        float[] array = new float[values.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = values.get(i);
        }
        return array;
    }

    private static float[] toRgba(Color color) {
        return new float[]{(float) color.getRed(), (float) color.getGreen(), (float) color.getBlue(), (float) color.getOpacity()};
    }
}
