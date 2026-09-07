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
package com.willwinder.universalgcodesender.fx.component.visualizer.scene;

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * An orbit camera looking at a target point in a Z up world. The view is described by yaw and
 * pitch around the target and the distance to it, all exposed as properties so they can be
 * animated. The projection is perspective or orthographic; the orthographic half height is
 * derived from the distance so that switching projection keeps the target at the same apparent
 * size and the same orbit, pan and zoom code drives both.
 *
 * <p>Screen coordinates in {@link #project}, {@link #unproject} and the pixel based helpers are
 * logical JavaFX pixels of the current {@link Viewport}, the same space mouse events arrive in.
 */
public final class Camera {

    public enum Projection {
        PERSPECTIVE, ORTHOGRAPHIC
    }

    public static final double FIELD_OF_VIEW_DEGREES = 45;
    private static final double NEAR_CLIP = 1;
    private static final double FAR_CLIP = 20_000;
    private static final double MIN_PITCH_DEGREES = -89;
    private static final double MAX_PITCH_DEGREES = 89;
    private static final double MIN_DISTANCE = 5;
    private static final double MAX_DISTANCE = 10_000;
    private static final double ZOOM_PER_SCROLL_UNIT = 0.0015;
    private static final double FRAME_MARGIN = 1.6;

    private final DoubleProperty yaw = new SimpleDoubleProperty(-30);
    private final DoubleProperty pitch = new SimpleDoubleProperty(35);
    private final DoubleProperty distance = new SimpleDoubleProperty(420);
    private final DoubleProperty targetX = new SimpleDoubleProperty(100);
    private final DoubleProperty targetY = new SimpleDoubleProperty(100);
    private final DoubleProperty targetZ = new SimpleDoubleProperty(0);
    private final ObjectProperty<Projection> projection = new SimpleObjectProperty<>(Projection.PERSPECTIVE);
    private final List<Runnable> changeListeners = new CopyOnWriteArrayList<>();
    private Viewport viewport = Viewport.EMPTY;

    public Camera() {
        InvalidationListener notify = observable -> changeListeners.forEach(Runnable::run);
        for (DoubleProperty property : List.of(yaw, pitch, distance, targetX, targetY, targetZ)) {
            property.addListener(notify);
        }
        projection.addListener(notify);
    }

    public DoubleProperty yawProperty() {
        return yaw;
    }

    public DoubleProperty pitchProperty() {
        return pitch;
    }

    public DoubleProperty distanceProperty() {
        return distance;
    }

    public DoubleProperty targetXProperty() {
        return targetX;
    }

    public DoubleProperty targetYProperty() {
        return targetY;
    }

    public DoubleProperty targetZProperty() {
        return targetZ;
    }

    public ObjectProperty<Projection> projectionProperty() {
        return projection;
    }

    public Point3D target() {
        return new Point3D(targetX.get(), targetY.get(), targetZ.get());
    }

    public Viewport viewport() {
        return viewport;
    }

    /**
     * The size of the image the camera renders into. Set by the frame renderer before each frame
     * and by the visualizer when it is resized, so that picking works between frames too.
     */
    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }

    /**
     * Called whenever the view changes, so the owner can request a frame.
     */
    public void addChangeListener(Runnable listener) {
        changeListeners.add(listener);
    }

    public void removeChangeListener(Runnable listener) {
        changeListeners.remove(listener);
    }

    public void orbit(double deltaYawDegrees, double deltaPitchDegrees) {
        yaw.set(yaw.get() + deltaYawDegrees);
        pitch.set(Math.clamp(pitch.get() + deltaPitchDegrees, MIN_PITCH_DEGREES, MAX_PITCH_DEGREES));
    }

    public void zoom(double scrollDelta) {
        setDistance(distance.get() * Math.exp(-scrollDelta * ZOOM_PER_SCROLL_UNIT));
    }

    /**
     * Zooms while keeping the world point under the given pixel fixed on screen, so the view
     * zooms towards the cursor rather than the centre. The point is taken on the plane facing
     * the camera through the target, which exists in every orientation, unlike the work plane
     * that a side view looks along.
     */
    public void zoomAt(double scrollDelta, double pixelX, double pixelY) {
        Optional<Point3D> before = intersectTargetPlane(pixelX, pixelY);
        zoom(scrollDelta);
        if (before.isEmpty()) {
            return;
        }
        intersectTargetPlane(pixelX, pixelY).ifPresent(after -> {
            Point3D shift = before.get().subtract(after);
            targetX.set(targetX.get() + shift.getX());
            targetY.set(targetY.get() + shift.getY());
            targetZ.set(targetZ.get() + shift.getZ());
        });
    }

    /**
     * Where the ray through a screen position crosses the plane perpendicular to the view
     * direction through the target.
     */
    public Optional<Point3D> intersectTargetPlane(double pixelX, double pixelY) {
        Ray ray = unproject(pixelX, pixelY);
        Point3D normal = viewDirection();
        double along = ray.direction().dotProduct(normal);
        if (Math.abs(along) < 1e-12) {
            return Optional.empty();
        }
        double t = target().subtract(ray.origin()).dotProduct(normal) / along;
        return Optional.of(ray.pointAt(t));
    }

    /**
     * The direction the camera looks in, as a unit vector in world coordinates.
     */
    public Point3D viewDirection() {
        float[] rotation = rotation();
        // The third row of the world to camera rotation is the camera's backward axis.
        return new Point3D(-rotation[2], -rotation[6], -rotation[10]);
    }

    /**
     * Drags the target across the plane facing the camera, so the scene follows the cursor
     * one-to-one regardless of the current zoom level.
     */
    public void pan(double deltaPixelsX, double deltaPixelsY) {
        double worldPerPixel = worldUnitsPerPixel();
        float[] rotation = rotation();
        double rightX = rotation[0];
        double rightY = rotation[4];
        double rightZ = rotation[8];
        double upX = rotation[1];
        double upY = rotation[5];
        double upZ = rotation[9];
        targetX.set(targetX.get() + (-rightX * deltaPixelsX + upX * deltaPixelsY) * worldPerPixel);
        targetY.set(targetY.get() + (-rightY * deltaPixelsX + upY * deltaPixelsY) * worldPerPixel);
        targetZ.set(targetZ.get() + (-rightZ * deltaPixelsX + upZ * deltaPixelsY) * worldPerPixel);
    }

    /**
     * Points the camera at a bounding box and backs off far enough to fit it in view, so a
     * loaded program lands on screen wherever it happens to sit in machine coordinates.
     */
    public void frame(Bounds3 bounds) {
        targetX.set(bounds.centerX());
        targetY.set(bounds.centerY());
        targetZ.set(bounds.centerZ());
        setDistance(distanceToFit(bounds));
    }

    /**
     * The distance at which a box of the given size fills the view with some margin.
     */
    public double distanceToFit(double size) {
        double halfSize = Math.max(size, 1) / 2;
        double fitted = halfSize / Math.tan(Math.toRadians(FIELD_OF_VIEW_DEGREES) / 2);
        return Math.clamp(fitted * FRAME_MARGIN, MIN_DISTANCE, MAX_DISTANCE);
    }

    /**
     * The distance at which the box fits the viewport seen from above, taking the viewport's
     * aspect ratio into account so a wide box is fitted by its width.
     */
    public double distanceToFit(Bounds3 bounds) {
        double neededHeight = Math.max(Math.max(bounds.height(), bounds.width() / viewport.aspect()), bounds.depth());
        return distanceToFit(neededHeight);
    }

    public void setDistance(double value) {
        distance.set(Math.clamp(value, MIN_DISTANCE, MAX_DISTANCE));
    }

    public float[] view() {
        return Mat4.multiply(
                Mat4.translation(0, 0, -distance.get()),
                Mat4.multiply(rotation(), Mat4.translation(-targetX.get(), -targetY.get(), -targetZ.get())));
    }

    public float[] projectionMatrix() {
        double aspect = viewport.aspect();
        if (projection.get() == Projection.ORTHOGRAPHIC) {
            double halfHeight = orthographicHalfHeight();
            return Mat4.orthographic(halfHeight * aspect, halfHeight, -FAR_CLIP, FAR_CLIP);
        }
        return Mat4.perspective(Math.toRadians(FIELD_OF_VIEW_DEGREES), aspect, NEAR_CLIP, FAR_CLIP);
    }

    public float[] viewProjection() {
        return Mat4.multiply(projectionMatrix(), view());
    }

    /**
     * The screen position of a world point in logical pixels, or empty when the point is behind
     * the camera.
     */
    public Optional<Point2D> project(Point3D world) {
        double[] clip = Mat4.transform(viewProjection(), world.getX(), world.getY(), world.getZ());
        if (clip[3] <= 1e-9) {
            return Optional.empty();
        }
        double ndcX = clip[0] / clip[3];
        double ndcY = clip[1] / clip[3];
        return Optional.of(new Point2D(
                (ndcX + 1) / 2 * logicalWidth(),
                (ndcY + 1) / 2 * logicalHeight()));
    }

    /**
     * The ray through a screen position, from the near plane into the scene.
     */
    public Ray unproject(double pixelX, double pixelY) {
        double ndcX = pixelX / logicalWidth() * 2 - 1;
        double ndcY = pixelY / logicalHeight() * 2 - 1;
        float[] inverse = Mat4.invert(viewProjection());
        Point3D near = toWorld(inverse, ndcX, ndcY, 0);
        Point3D far = toWorld(inverse, ndcX, ndcY, 1);
        return new Ray(near, far.subtract(near).normalize());
    }

    /**
     * Where the ray through a screen position hits the work plane (Z = 0).
     */
    public Optional<Point3D> intersectWorkPlane(double pixelX, double pixelY) {
        return unproject(pixelX, pixelY).intersectPlaneZ(0);
    }

    /**
     * How many world units one logical pixel covers at the target distance.
     */
    public double worldUnitsPerPixel() {
        return 2 * orthographicHalfHeight() / logicalHeight();
    }

    /**
     * How many world units one logical pixel covers at the depth of a world point. Constant
     * in orthographic projection; shrinks with distance to the camera in perspective.
     */
    public double worldUnitsPerPixelAt(Point3D world) {
        if (projection.get() == Projection.ORTHOGRAPHIC) {
            return worldUnitsPerPixel();
        }
        double[] viewSpace = Mat4.transform(view(), world.getX(), world.getY(), world.getZ());
        double depth = Math.max(-viewSpace[2], NEAR_CLIP);
        double visibleHeight = 2 * depth * Math.tan(Math.toRadians(FIELD_OF_VIEW_DEGREES) / 2);
        return visibleHeight / logicalHeight();
    }

    private double orthographicHalfHeight() {
        return distance.get() * Math.tan(Math.toRadians(FIELD_OF_VIEW_DEGREES) / 2);
    }

    private double logicalWidth() {
        return viewport.toLogical(viewport.width());
    }

    private double logicalHeight() {
        return viewport.toLogical(viewport.height());
    }

    private static Point3D toWorld(float[] inverseViewProjection, double ndcX, double ndcY, double ndcZ) {
        double[] world = Mat4.transform(inverseViewProjection, ndcX, ndcY, ndcZ);
        return new Point3D(world[0] / world[3], world[1] / world[3], world[2] / world[3]);
    }

    /**
     * World to camera rotation. Pitch is offset by 90 degrees so that a pitch of 90 leaves the
     * camera looking down the world Z axis.
     */
    private float[] rotation() {
        return Mat4.multiply(
                Mat4.rotationX(Math.toRadians(pitch.get()) - Math.PI / 2),
                Mat4.rotationZ(Math.toRadians(-yaw.get())));
    }
}
