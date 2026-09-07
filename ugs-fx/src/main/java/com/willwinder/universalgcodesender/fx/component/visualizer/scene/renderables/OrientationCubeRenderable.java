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
package com.willwinder.universalgcodesender.fx.component.visualizer.scene.renderables;

import com.willwinder.universalgcodesender.fx.component.visualizer.overlay.OverlayPainter;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Camera;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.LineMeshBuilder;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.MeshHandle;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.OrientationCube;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Ray;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.RenderContext;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Renderable;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Scene;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.SceneLayer;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.SceneMeshes;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.VertexLayout;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.ViewOrientation;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Viewport;
import com.willwinder.universalgcodesender.fx.helper.Colors;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Affine;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * The orientation cube in the top-left corner: a cube turning with the main camera whose faces
 * can be clicked to snap the view. It is drawn in a sub viewport of the frame with its own
 * camera, with a small axis triad at its corner. The face labels are drawn on the overlay
 * canvas through the affine map of each face onto the screen, so they lie on the faces and
 * foreshorten with them, fading out as a face turns edge on.
 */
public final class OrientationCubeRenderable implements Renderable, OverlayPainter {
    private static final float[] EDGE_COLOR = {0.82f, 0.82f, 0.82f, 1.0f};
    private static final float EDGE_WIDTH_PX = 1.5f;
    private static final float AXIS_WIDTH_PX = 10f;
    private static final double AXIS_LENGTH = OrientationCube.HALF_SIZE * 1.1;
    private static final Color LABEL_COLOR = Color.WHITE;
    /** The side of the square the labels are laid out in before it is mapped onto a face. */
    private static final double FACE_UNITS = 100;
    private static final Font LABEL_FONT = Font.font("System", FontWeight.BOLD, 26);
    /** Brightness of each face relative to the base colour, so adjoining faces read apart. */
    private static final Map<ViewOrientation, Double> FACE_BRIGHTNESS = Map.of(
            ViewOrientation.TOP, 1.9,
            ViewOrientation.FRONT, 1.4,
            ViewOrientation.RIGHT, 1.2,
            ViewOrientation.LEFT, 1.1,
            ViewOrientation.BACK, 1.0,
            ViewOrientation.BOTTOM, 0.8);
    /** The triad sits this far outside the cube's origin corner so it is not lost in the edges. */
    private static final double AXIS_OFFSET = OrientationCube.HALF_SIZE * 0.3;

    /**
     * How a label reads on each face: {@code right} runs along the text and {@code up} is the
     * top of the letters, chosen so the label is upright when that face is looked at straight
     * on with the view the face snaps to.
     */
    private record FaceFrame(Point3D right, Point3D up) {
    }

    private static final Map<ViewOrientation, FaceFrame> FACE_FRAMES = Map.of(
            ViewOrientation.TOP, new FaceFrame(new Point3D(1, 0, 0), new Point3D(0, 1, 0)),
            ViewOrientation.BOTTOM, new FaceFrame(new Point3D(1, 0, 0), new Point3D(0, -1, 0)),
            ViewOrientation.FRONT, new FaceFrame(new Point3D(1, 0, 0), new Point3D(0, 0, 1)),
            ViewOrientation.BACK, new FaceFrame(new Point3D(-1, 0, 0), new Point3D(0, 0, 1)),
            ViewOrientation.RIGHT, new FaceFrame(new Point3D(0, 1, 0), new Point3D(0, 0, 1)),
            ViewOrientation.LEFT, new FaceFrame(new Point3D(0, -1, 0), new Point3D(0, 0, 1)));

    private final Camera cubeCamera = OrientationCube.createCamera();
    private final Map<ViewOrientation, MeshHandle> faces = new EnumMap<>(ViewOrientation.class);
    private final double left;
    private final double top;
    private final double size;
    private MeshHandle edges;
    private MeshHandle axes;
    private Scene scene;
    private ViewOrientation hoveredFace;

    /**
     * @param left the left edge of the inset in logical pixels
     * @param top  the top edge of the inset in logical pixels
     * @param size the width and height of the inset in logical pixels
     */
    public OrientationCubeRenderable(double left, double top, double size) {
        this.left = left;
        this.top = top;
        this.size = size;
    }

    @Override
    public SceneLayer layer() {
        return SceneLayer.OVERLAY;
    }

    @Override
    public void onAttached(Scene scene) {
        this.scene = scene;
        for (ViewOrientation face : ViewOrientation.values()) {
            Point3D normal = face.normal();
            float[] vertices = SceneMeshes.cubeFace(
                    new double[]{normal.getX(), normal.getY(), normal.getZ()}, OrientationCube.HALF_SIZE);
            faces.put(face, scene.context().upload(vertices, VertexLayout.MESH));
        }
        edges = scene.context().upload(cubeEdges(), VertexLayout.LINE);
        axes = scene.context().upload(axisTriad(), VertexLayout.LINE);
    }

    @Override
    public void onDetached(Scene scene) {
        faces.values().forEach(scene.context()::release);
        faces.clear();
        for (MeshHandle mesh : new MeshHandle[]{edges, axes}) {
            if (mesh != null) {
                scene.context().release(mesh);
            }
        }
        edges = null;
        axes = null;
        this.scene = null;
    }

    @Override
    public void render(RenderContext context) {
        Viewport viewport = context.viewport();
        follow(context.camera(), viewport);
        int x = (int) Math.round(viewport.toPhysical(left));
        int y = (int) Math.round(viewport.toPhysical(top));
        int extent = (int) Math.round(viewport.toPhysical(size));
        if (x + extent > viewport.width() || y + extent > viewport.height()) {
            return;
        }
        context.beginSubViewport(x, y, extent, extent, cubeCamera.viewProjection());
        try {
            context.setDepthTest(true);
            for (Map.Entry<ViewOrientation, MeshHandle> face : faces.entrySet()) {
                context.drawTriangles(face.getValue(), null, faceColor(face.getKey()), false);
            }
            context.drawLines(edges, null, EDGE_COLOR, EDGE_WIDTH_PX);
            context.drawColoredLines(axes, null, AXIS_WIDTH_PX);
        } finally {
            context.endSubViewport();
        }
    }

    @Override
    public void paint(GraphicsContext graphics, Camera camera, double width, double height) {
        follow(camera, camera.viewport());
        Point3D viewDirection = cubeCamera.unproject(size / 2, size / 2).direction();
        graphics.setFont(LABEL_FONT);
        graphics.setTextAlign(TextAlignment.CENTER);
        graphics.setTextBaseline(VPos.CENTER);
        for (ViewOrientation face : ViewOrientation.values()) {
            double facing = -face.normal().dotProduct(viewDirection);
            if (facing <= 0.05) {
                continue;
            }
            faceToScreen(face).ifPresent(affine -> {
                graphics.save();
                graphics.setTransform(affine);
                graphics.setGlobalAlpha(Math.min(1, (facing - 0.05) / 0.45));
                graphics.setFill(LABEL_COLOR);
                graphics.fillText(face.label(), FACE_UNITS / 2, FACE_UNITS / 2);
                graphics.restore();
            });
        }
    }

    /**
     * The face under a position of the visualizer, in logical pixels, or empty when the
     * position is outside the inset or misses the cube.
     */
    public Optional<ViewOrientation> faceAt(double pixelX, double pixelY) {
        double localX = pixelX - left;
        double localY = pixelY - top;
        if (localX < 0 || localY < 0 || localX > size || localY > size) {
            return Optional.empty();
        }
        Ray ray = cubeCamera.unproject(localX, localY);
        return OrientationCube.faceAt(ray);
    }

    public boolean contains(double pixelX, double pixelY) {
        return pixelX >= left && pixelY >= top && pixelX <= left + size && pixelY <= top + size;
    }

    public void setHoveredFace(ViewOrientation face) {
        if (hoveredFace != face) {
            hoveredFace = face;
            if (scene != null) {
                scene.requestRender();
            }
        }
    }

    /**
     * The affine transform that maps the label square {@code [0, FACE_UNITS]²}, with its origin
     * in the face's top-left corner as read, onto the face's parallelogram on the overlay canvas.
     * The cube camera is orthographic, so the mapping is exact.
     */
    private Optional<Affine> faceToScreen(ViewOrientation face) {
        FaceFrame frame = FACE_FRAMES.get(face);
        Point3D center = OrientationCube.faceCenter(face);
        double half = OrientationCube.HALF_SIZE;
        Point3D topLeft = center.subtract(frame.right().multiply(half)).add(frame.up().multiply(half));
        Point3D topRight = center.add(frame.right().multiply(half)).add(frame.up().multiply(half));
        Point3D bottomLeft = center.subtract(frame.right().multiply(half)).subtract(frame.up().multiply(half));
        Optional<Point2D> origin = cubeCamera.project(topLeft);
        Optional<Point2D> alongText = cubeCamera.project(topRight);
        Optional<Point2D> downText = cubeCamera.project(bottomLeft);
        if (origin.isEmpty() || alongText.isEmpty() || downText.isEmpty()) {
            return Optional.empty();
        }
        Point2D o = origin.get();
        Point2D x = alongText.get().subtract(o).multiply(1 / FACE_UNITS);
        Point2D y = downText.get().subtract(o).multiply(1 / FACE_UNITS);
        return Optional.of(new Affine(x.getX(), y.getX(), left + o.getX(), x.getY(), y.getY(), top + o.getY()));
    }

    private float[] faceColor(ViewOrientation face) {
        Color color = Colors.BLACKISH.deriveColor(0, 1, FACE_BRIGHTNESS.get(face), 1);
        if (face == hoveredFace) {
            color = color.interpolate(Color.WHITE, 0.4);
        }
        return new float[]{(float) color.getRed(), (float) color.getGreen(), (float) color.getBlue(), 1};
    }

    /**
     * Points the cube camera the way the main camera looks, at the inset's size. Called before
     * every use so the cube follows the main camera without listening to it.
     */
    private void follow(Camera mainCamera, Viewport mainViewport) {
        OrientationCube.follow(cubeCamera, mainCamera);
        int extent = (int) Math.round(mainViewport.toPhysical(size));
        cubeCamera.setViewport(new Viewport(extent, extent, mainViewport.outputScale()));
    }

    private static float[] cubeEdges() {
        double h = OrientationCube.HALF_SIZE;
        Color color = Color.WHITE;
        LineMeshBuilder builder = new LineMeshBuilder(12);
        for (double z : new double[]{-h, h}) {
            builder.add(-h, -h, z, h, -h, z, color)
                    .add(h, -h, z, h, h, z, color)
                    .add(h, h, z, -h, h, z, color)
                    .add(-h, h, z, -h, -h, z, color);
        }
        for (double x : new double[]{-h, h}) {
            for (double y : new double[]{-h, h}) {
                builder.add(x, y, -h, x, y, h, color);
            }
        }
        return builder.build();
    }

    /**
     * Short X, Y and Z axes in the usual red, green and blue, running parallel to the edges just
     * outside the cube's origin corner. They are depth tested like the cube, so they show when
     * that corner faces the camera and hide behind the cube otherwise.
     */
    private static float[] axisTriad() {
        double c = -OrientationCube.HALF_SIZE - AXIS_OFFSET;
        return new LineMeshBuilder(3)
                .add(c, c, c, c + AXIS_LENGTH, c, c, Color.color(0.85, 0.20, 0.20))
                .add(c, c, c, c, c + AXIS_LENGTH, c, Color.color(0.20, 0.70, 0.25))
                .add(c, c, c, c, c, c + AXIS_LENGTH, Color.color(0.25, 0.45, 0.90))
                .build();
    }
}
