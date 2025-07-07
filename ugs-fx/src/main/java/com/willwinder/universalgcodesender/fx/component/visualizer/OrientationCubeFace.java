package com.willwinder.universalgcodesender.fx.component.visualizer;

import javafx.geometry.Point3D;

public enum OrientationCubeFace {
    FRONT(new Point3D(-90, 180, 180)),
    BACK(new Point3D(-90, 180, 0)),
    LEFT(new Point3D(-90, 180, 270)),
    RIGHT(new Point3D(-90, 180, 90)),
    TOP(new Point3D(0, 180, 180)),
    BOTTOM(new Point3D(-180, 180, 180));

    private final Point3D rotation;

    OrientationCubeFace(Point3D rotation) {
        this.rotation = rotation;
    }

    public Point3D getRotation() {
        return rotation;
    }
}
