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

import com.willwinder.ugs.designer.constraint.ConstraintManager;
import com.willwinder.ugs.designer.constraint.ConstraintPlacement;
import com.willwinder.ugs.designer.constraint.DimensionLine;
import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.universalgcodesender.fx.component.designer.editor.DesignEditor;
import com.willwinder.universalgcodesender.fx.component.designer.editor.DimensionStyle;
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
import com.willwinder.universalgcodesender.fx.helper.Colors;
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
    private static final Color VERTEX_FILL = Color.WHITE;
    private static final Color ENDPOINT_COLOR = Colors.ORANGE;
    private static final double VERTEX_HOVER_TINT = 0.35;
    private static final float VERTEX_OUTLINE_WIDTH_PX = 1.5f;
    private static final int VERTEX_SIDES = 16;
    private static final int ROTATE_HANDLE_SIDES = 12;
    private static final double HOVER_TINT = 0.25;
    private static final double HOVER_BRIGHTNESS = 1.15;
    private static final double SNAP_MARKER_PX = 7;

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
        LineMeshBuilder curveHandleLines = new LineMeshBuilder(8);
        LineMeshBuilder vertexOutlines = new LineMeshBuilder(64);
        LineMeshBuilder dimensionLines = new LineMeshBuilder(32);

        editor.controller().ifPresent(controller -> {
            ConstraintManager constraintManager = controller.getModel().getConstraintManager();
            DimensionLine.allOf(constraintManager.getActiveConstraints(), constraint -> state.isHighlighted(constraint, constraintManager))
                    .forEach(drawn -> addDimension(dimensionLines, context, drawn.dimension(),
                            DimensionStyle.color(drawn.highlighted())));
            if (state.previewDimension() != null) {
                addDimension(dimensionLines, context, state.previewDimension(), DimensionStyle.color(true));
            }
            if (editor.currentToolType() == Tool.SELECT || editor.currentToolType() == Tool.VERTEX) {
                addFrame(frameLines, HandleSet.frame(controller.getSelectionManager()));
            }
        });
        for (HandleSet.Handle handle : editor.handles()) {
            addHandle(frameLines, handle, handle.isSameHandle(state.hoveredHandle()));
        }
        addVertices(state, context, curveHandleLines, vertexOutlines);
        addSnapMarkers(state, context, vertexOutlines);
        if (state.rubberBand() != null) {
            addTriangles(RUBBER_BAND_FILL, rectangleTriangles(state.rubberBand()));
            addOutline(frameLines, state.rubberBand(), FRAME_COLOR);
        }

        List<MeshHandle> meshes = new ArrayList<>();
        // Lines to the curve handles go below the vertices so they do not cross their fill.
        drawLines(context, meshes, curveHandleLines, 1);
        drawLines(context, meshes, dimensionLines, 1);
        for (Map.Entry<Color, List<Float>> batch : trianglesByColor.entrySet()) {
            MeshHandle mesh = context.upload(toArray(batch.getValue()), VertexLayout.MESH);
            meshes.add(mesh);
            context.drawTriangles(mesh, null, toRgba(batch.getKey()), false);
        }
        drawLines(context, meshes, frameLines, 1);
        drawLines(context, meshes, vertexOutlines, VERTEX_OUTLINE_WIDTH_PX);
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

    private void addDimension(LineMeshBuilder lines, RenderContext context, DimensionLine dimension, Color color) {
        Point2D start = dimension.anchor();
        double worldPerPixel = context.camera().worldUnitsPerPixelAt(new Point3D(start.getX(), start.getY(), 0));
        dimension.lines(worldPerPixel).forEach(line -> addLine(lines, line[0], line[1], color));
        dimension.arrowheads(worldPerPixel).forEach(triangle -> {
            float[] vertices = new float[3 * SceneMeshes.FLOATS_PER_VERTEX];
            int offset = 0;
            for (Point2D corner : triangle) {
                offset = writeVertex(vertices, offset, corner.getX(), corner.getY());
            }
            addTriangles(color, vertices);
        });
    }

    private static void addLine(LineMeshBuilder lines, Point2D from, Point2D to, Color color) {
        lines.add(from.getX(), from.getY(), 0, to.getX(), to.getY(), 0, color);
    }

    private void addSnapMarkers(EditorState state, RenderContext context, LineMeshBuilder outlines) {
        for (ConstraintPlacement.Marker marker : state.snapMarkers()) {
            Point2D position = marker.position();
            double worldPerPixel = context.camera().worldUnitsPerPixelAt(new Point3D(position.getX(), position.getY(), 0));
            double radius = SNAP_MARKER_PX * worldPerPixel / 2 * Math.sqrt(2);
            addTriangles(marker.highlighted() ? FRAME_COLOR : VERTEX_FILL, polygonTriangles(position, radius, 4));
            addPolygonOutline(outlines, position, radius, 4, FRAME_COLOR);
        }
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
            color = color.interpolate(Color.WHITE, HOVER_TINT).deriveColor(0, 1, HOVER_BRIGHTNESS, 1);
        }
        int sides = handle.kind() == HandleSet.Kind.ROTATE ? ROTATE_HANDLE_SIDES : 4;
        double radius = handle.kind() == HandleSet.Kind.ROTATE ? handle.size() / 2 : handle.size() / 2 * Math.sqrt(2);
        addTriangles(color, polygonTriangles(handle.center(), radius, sides));
        addPolygonOutline(builder, handle.center(), radius, sides, FRAME_COLOR);
    }

    private static void drawLines(RenderContext context, List<MeshHandle> meshes, LineMeshBuilder lines, float widthPx) {
        if (lines.isEmpty()) {
            return;
        }
        MeshHandle mesh = context.upload(lines.build(), VertexLayout.LINE);
        meshes.add(mesh);
        context.drawColoredLines(mesh, null, widthPx);
    }

    /**
     * Draws the points of the path being edited as white circles with a blue outline, the
     * selected node filled blue. The loose ends of open paths are orange squares. Curve handles
     * are smaller and joined to their node by a line.
     */
    private void addVertices(EditorState state, RenderContext context, LineMeshBuilder curveHandleLines,
                             LineMeshBuilder outlines) {
        for (EditorState.VertexHandle vertex : state.vertices()) {
            Point2D position = vertex.position();
            if (vertex.isCurveHandle()) {
                curveHandleLines.add(vertex.node().getX(), vertex.node().getY(), 0,
                        position.getX(), position.getY(), 0, FRAME_COLOR);
            }
            double worldPerPixel = context.camera().worldUnitsPerPixelAt(new Point3D(position.getX(), position.getY(), 0));
            double size = vertex.isCurveHandle() ? VertexTool.CURVE_HANDLE_SIZE_PX : VertexTool.NODE_SIZE_PX;
            // Loose ends of open paths are squares as wide as the circles, reaching to their corners
            int sides = vertex.endpoint() ? 4 : VERTEX_SIDES;
            double radius = size * worldPerPixel / 2 * (vertex.endpoint() ? Math.sqrt(2) : 1);
            addTriangles(vertexFill(vertex), polygonTriangles(position, radius, sides));
            addPolygonOutline(outlines, position, radius, sides, vertexColor(vertex));
        }
    }

    /**
     * The colour of a vertex's outline, and its fill when selected: orange for the loose ends
     * of open paths so they stand out, blue for every other point.
     */
    private static Color vertexColor(EditorState.VertexHandle vertex) {
        return vertex.endpoint() ? ENDPOINT_COLOR : FRAME_COLOR;
    }

    private static Color vertexFill(EditorState.VertexHandle vertex) {
        Color color = vertexColor(vertex);
        if (vertex.selected()) {
            return vertex.hovered()
                    ? color.interpolate(Color.WHITE, HOVER_TINT).deriveColor(0, 1, HOVER_BRIGHTNESS, 1)
                    : color;
        }
        return vertex.hovered() ? Color.WHITE.interpolate(color, VERTEX_HOVER_TINT) : VERTEX_FILL;
    }

    private void addTriangles(Color color, float[] vertices) {
        List<Float> batch = trianglesByColor.computeIfAbsent(color, _ -> new ArrayList<>());
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
