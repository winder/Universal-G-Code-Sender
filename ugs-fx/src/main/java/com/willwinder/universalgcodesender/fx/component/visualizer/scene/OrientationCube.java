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

import javafx.geometry.Point3D;

import java.util.Optional;

/**
 * The geometry of the orientation cube: a cube centred on the origin whose faces are the
 * {@link ViewOrientation}s, seen through its own orthographic camera that copies the yaw and
 * pitch of the main camera.
 */
public final class OrientationCube {
    public static final double HALF_SIZE = 1;
    /** Leaves room around the cube for it to rotate, and for its axis triad, inside the inset. */
    private static final double VISIBLE_HALF_HEIGHT = 2.5;

    private OrientationCube() {
    }

    /**
     * A camera that shows the cube in an inset of the given size, in logical pixels.
     */
    public static Camera createCamera() {
        Camera camera = new Camera();
        camera.projectionProperty().set(Camera.Projection.ORTHOGRAPHIC);
        camera.targetXProperty().set(0);
        camera.targetYProperty().set(0);
        camera.targetZProperty().set(0);
        camera.setDistance(VISIBLE_HALF_HEIGHT / Math.tan(Math.toRadians(Camera.FIELD_OF_VIEW_DEGREES) / 2));
        return camera;
    }

    /**
     * Makes the cube camera look the same way as the main camera.
     */
    public static void follow(Camera cubeCamera, Camera mainCamera) {
        cubeCamera.yawProperty().set(mainCamera.yawProperty().get());
        cubeCamera.pitchProperty().set(mainCamera.pitchProperty().get());
    }

    /**
     * The face a ray hits first, or empty when it misses the cube.
     */
    public static Optional<ViewOrientation> faceAt(Ray ray) {
        ViewOrientation nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (ViewOrientation face : ViewOrientation.values()) {
            Point3D normal = face.normal();
            double facing = normal.dotProduct(ray.direction());
            if (facing >= 0) {
                continue;
            }
            double t = (HALF_SIZE - normal.dotProduct(ray.origin())) / facing;
            if (t < 0 || t >= nearestDistance) {
                continue;
            }
            Point3D hit = ray.pointAt(t);
            if (isOnFace(hit, normal)) {
                nearest = face;
                nearestDistance = t;
            }
        }
        return Optional.ofNullable(nearest);
    }

    /**
     * Whether a face is turned towards the camera, so its label should be drawn.
     */
    public static boolean isFacing(ViewOrientation face, Point3D viewDirection) {
        return face.normal().dotProduct(viewDirection) < -0.05;
    }

    public static Point3D faceCenter(ViewOrientation face) {
        return face.normal().multiply(HALF_SIZE);
    }

    private static boolean isOnFace(Point3D hit, Point3D normal) {
        double tolerance = HALF_SIZE + 1e-9;
        boolean withinX = Math.abs(normal.getX()) > 0.5 || Math.abs(hit.getX()) <= tolerance;
        boolean withinY = Math.abs(normal.getY()) > 0.5 || Math.abs(hit.getY()) <= tolerance;
        boolean withinZ = Math.abs(normal.getZ()) > 0.5 || Math.abs(hit.getZ()) <= tolerance;
        return withinX && withinY && withinZ;
    }
}
