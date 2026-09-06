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
package com.willwinder.universalgcodesender.fx.component.visualizer;

import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Bounds3;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Camera;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.ViewOrientation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Point3D;
import javafx.util.Duration;

/**
 * Moves the camera smoothly to a new view. Every camera property is a JavaFX property, so a
 * {@link Timeline} drives them and the camera's change listeners request the frames.
 */
public final class CameraAnimator {
    public static final Duration ROTATE_DURATION = Duration.millis(500);
    public static final Duration FRAME_DURATION = Duration.seconds(1);

    private final Camera camera;
    private Timeline timeline;

    public CameraAnimator(Camera camera) {
        this.camera = camera;
    }

    /**
     * Turns the view to look at the work area from the given side, keeping the target and the
     * distance.
     */
    public void rotateTo(ViewOrientation orientation) {
        animate(ROTATE_DURATION, orientation.yawDegrees(), orientation.pitchDegrees(),
                camera.distanceProperty().get(), camera.target());
    }

    /**
     * Looks straight down at the box and backs off until it fits the viewport.
     */
    public void frame(Bounds3 bounds) {
        animate(FRAME_DURATION, ViewOrientation.TOP.yawDegrees(), ViewOrientation.TOP.pitchDegrees(),
                camera.distanceToFit(bounds), new Point3D(bounds.centerX(), bounds.centerY(), bounds.centerZ()));
    }

    public void stop() {
        if (timeline != null) {
            timeline.stop();
            timeline = null;
        }
    }

    private void animate(Duration duration, double yaw, double pitch, double distance, Point3D target) {
        stop();
        timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(camera.yawProperty(), camera.yawProperty().get()),
                        new KeyValue(camera.pitchProperty(), camera.pitchProperty().get()),
                        new KeyValue(camera.distanceProperty(), camera.distanceProperty().get()),
                        new KeyValue(camera.targetXProperty(), camera.targetXProperty().get()),
                        new KeyValue(camera.targetYProperty(), camera.targetYProperty().get()),
                        new KeyValue(camera.targetZProperty(), camera.targetZProperty().get())),
                new KeyFrame(duration,
                        new KeyValue(camera.yawProperty(), shortestYaw(yaw), Interpolator.EASE_BOTH),
                        new KeyValue(camera.pitchProperty(), pitch, Interpolator.EASE_BOTH),
                        new KeyValue(camera.distanceProperty(), distance, Interpolator.EASE_BOTH),
                        new KeyValue(camera.targetXProperty(), target.getX(), Interpolator.EASE_BOTH),
                        new KeyValue(camera.targetYProperty(), target.getY(), Interpolator.EASE_BOTH),
                        new KeyValue(camera.targetZProperty(), target.getZ(), Interpolator.EASE_BOTH)));
        timeline.play();
    }

    /**
     * The target yaw expressed so the turn goes the short way round from the current yaw.
     */
    private double shortestYaw(double targetYaw) {
        double current = camera.yawProperty().get();
        double delta = ((targetYaw - current) % 360 + 540) % 360 - 180;
        return current + delta;
    }
}
