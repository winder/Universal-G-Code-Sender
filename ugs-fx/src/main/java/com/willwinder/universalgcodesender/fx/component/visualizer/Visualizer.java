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

import com.willwinder.universalgcodesender.fx.component.visualizer.machine.Machine;
import com.willwinder.universalgcodesender.fx.component.visualizer.models.Model;
import com.willwinder.universalgcodesender.fx.service.VisualizerService;
import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.DirectionalLight;
import javafx.scene.Group;
import javafx.scene.ParallelCamera;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SpotLight;
import javafx.scene.SubScene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

public class Visualizer extends Pane {
    private final Camera camera;
    private final Group worldGroup;
    private double mouseOldX;
    private double mouseOldY;

    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(180, Rotate.Y_AXIS);
    private final Rotate rotateZ = new Rotate(180, Rotate.Z_AXIS);

    private final Translate translate = new Translate(0, 0, 0);
    private final Translate cameraTranslate = new Translate(0, 0, -500); // initial zoom
    private final SubScene subScene;
    private final Group root3D;

    public Visualizer() {

        // Rotate group contains 3D objects
        Tool tool = new Tool();
        Machine machine = new Machine();
        worldGroup = new Group(new Axes(), new Grid(), new GcodeModel(), tool, machine);
        worldGroup.getTransforms().addAll(rotateX, rotateY, rotateZ);

        // Lighting
        DirectionalLight light = new DirectionalLight(Color.WHITE);
        light.setDirection(new Point3D(1, -1, -1));
        light.getScope().addAll(tool, machine);

        Point3D lightDirection = new Point3D(0.5, 0.7, 0);
        SpotLight spotLight = new SpotLight(Color.DARKGREY);
        spotLight.setTranslateX(-400);
        spotLight.setTranslateY(-1000);
        spotLight.setTranslateZ(-200);
        spotLight.setDirection(lightDirection);
        spotLight.getScope().addAll(machine);

        // Root group applies panning
        AmbientLight ambient = new AmbientLight(Color.rgb(255, 255, 255));
        root3D = new Group(worldGroup, ambient, light, spotLight);
        root3D.getTransforms().add(translate);

        subScene = new SubScene(root3D, 800, 600, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.LIGHTGRAY);

        camera = createCamera();
        subScene.setCamera(camera);

        setMouseInteraction();

        OrientationCube orientationCube = new OrientationCube(110);
        orientationCube.setOnFaceClicked(this::rotateTo);
        orientationCube.setRotations(rotateX, rotateY, rotateZ);
        orientationCube.layoutXProperty().bind(widthProperty().subtract(orientationCube.sizeProperty()).subtract(5));
        orientationCube.layoutYProperty().set(5);

        getChildren().addAll(subScene, orientationCube);

        // Add new models added through the visualizer service
        VisualizerService.getInstance().getModels().addListener((ListChangeListener<Model>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    worldGroup.getChildren().addAll(change.getAddedSubList());
                    spotLight.getScope().addAll(change.getAddedSubList());
                }
                if (change.wasRemoved()) {
                    worldGroup.getChildren().removeAll(change.getRemoved());
                    spotLight.getScope().removeAll(change.getRemoved());
                }
            }
        });
    }

    private void rotateTo(OrientationCubeFace face) {
        // Optional: animate instead of jumping
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(0),
                        new KeyValue(rotateX.angleProperty(), rotateX.getAngle()),
                        new KeyValue(rotateY.angleProperty(), rotateY.getAngle()),
                        new KeyValue(rotateZ.angleProperty(), rotateZ.getAngle())
                ),
                new KeyFrame(Duration.seconds(0.5),
                        new KeyValue(rotateX.angleProperty(), face.getRotation().getX()),
                        new KeyValue(rotateY.angleProperty(), face.getRotation().getY()),
                        new KeyValue(rotateZ.angleProperty(), face.getRotation().getZ())
                )
        );
        timeline.play();
    }

    private static MouseButton parseMouseButton(String value, MouseButton fallback) {
        if (value == null || value.isBlank()) return fallback;
        try {
            return MouseButton.valueOf(value.trim().toUpperCase());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private static boolean isModifierDown(MouseEvent event, VisualizerSettings.ModifierKey modifier) {
        if (modifier == null) return true;
        return switch (modifier) {
            case NONE -> true;
            case SHIFT -> event.isShiftDown();
            case CTRL -> event.isControlDown();
            case ALT -> event.isAltDown();
            case META -> event.isMetaDown();
        };
    }

    private static boolean isButtonDown(MouseEvent event, MouseButton button) {
        if (button == null) return false;
        return switch (button) {
            case PRIMARY -> event.isPrimaryButtonDown();
            case MIDDLE -> event.isMiddleButtonDown();
            case SECONDARY -> event.isSecondaryButtonDown();
            default -> false;
        };
    }

    private void setMouseInteraction() {
        // Handle mouse press event to store the initial position
        subScene.setOnMousePressed(event -> {
            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        });

        // Handle mouse dragged event to implement panning and rotating
        subScene.setOnMouseDragged((MouseEvent event) -> {
            double dx = event.getSceneX() - mouseOldX;
            double dy = event.getSceneY() - mouseOldY;

            VisualizerSettings settings = VisualizerSettings.getInstance();

            MouseButton panButton = parseMouseButton(settings.panMouseButtonProperty().getValue(), MouseButton.SECONDARY);
            VisualizerSettings.ModifierKey panModifier = VisualizerSettings.ModifierKey.fromString(
                    settings.panModifierKeyProperty().getValue(),
                    VisualizerSettings.ModifierKey.NONE
            );

            MouseButton rotateButton = parseMouseButton(settings.rotateMouseButtonProperty().getValue(), MouseButton.SECONDARY);
            VisualizerSettings.ModifierKey rotateModifier = VisualizerSettings.ModifierKey.fromString(
                    settings.rotateModifierKeyProperty().getValue(),
                    VisualizerSettings.ModifierKey.NONE
            );

            boolean doPan = isButtonDown(event, panButton) && isModifierDown(event, panModifier);
            boolean doRotate = isButtonDown(event, rotateButton) && isModifierDown(event, rotateModifier);

            // If both match (misconfiguration), prefer panning.
            if (doPan) {
                // Pan (translate) the 3D scene
                translate.setX(translate.getX() + dx * 0.5);
                translate.setY(translate.getY() + dy * 0.5);
            } else if (doRotate) {
                // Orbit rotation
                rotateX.setAngle(rotateX.getAngle() + dy * 0.5);
                rotateZ.setAngle(rotateZ.getAngle() + dx * 0.5);
            }

            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        });

        // Zoom with mouse scroll
        subScene.setOnScroll(event -> {
            boolean invert = VisualizerSettings.getInstance().invertZoomProperty().get();
            double delta = invert ? -event.getDeltaY() : event.getDeltaY();

            if (camera instanceof ParallelCamera) {
                double scale = root3D.getScaleY() + (delta / 200f);
                root3D.setScaleX(scale);
                root3D.setScaleY(scale);
            } else {
                cameraTranslate.setZ(cameraTranslate.getZ() + delta);
            }
        });
    }

    private Camera createCamera() {
        boolean useParallelCamera = false;
        if (useParallelCamera) {
            Camera camera = new ParallelCamera();

            // Set the camera position and scale
            Platform.runLater(() -> {
                camera.setTranslateX(-(subScene.getWidth() / 2d));
                camera.setTranslateY(-(subScene.getHeight() / 2d));
                root3D.setScaleX(3);
                root3D.setScaleY(3);
            });
            return camera;
        } else {
            // Camera
            PerspectiveCamera camera = new PerspectiveCamera(true);
            camera.setNearClip(0.1);
            camera.setFarClip(10000);
            camera.getTransforms().add(cameraTranslate);
            return camera;
        }
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        // Ensure SubScene resizes with the parent node
        subScene.setWidth(getWidth());
        subScene.setHeight(getHeight());
    }

    public void resetScene() {
        // Reset transformations
        rotateX.setAngle(0);
        rotateY.setAngle(0);
        translate.setX(0);
        translate.setY(0);
        cameraTranslate.setZ(-500); // reset zoom
    }

    public void setCameraPosition(double x, double y, double z) {
        cameraTranslate.setX(x);
        cameraTranslate.setY(y);
        cameraTranslate.setZ(z);
    }
}
