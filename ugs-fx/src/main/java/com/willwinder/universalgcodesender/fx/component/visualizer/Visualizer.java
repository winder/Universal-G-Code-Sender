package com.willwinder.universalgcodesender.fx.component.visualizer;

import com.willwinder.universalgcodesender.fx.component.visualizer.machine.Machine;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
        Group rotateGroup = new Group(new Axes(), new Grid(), new GcodeModel(), tool, machine);
        rotateGroup.getTransforms().addAll(rotateX, rotateY, rotateZ);

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
        root3D = new Group(rotateGroup, ambient, light, spotLight);
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

            if (event.getButton() == MouseButton.SECONDARY) {
                if (event.isShiftDown()) {
                    // Pan (translate) the 3D scene
                    translate.setX(translate.getX() + dx * 0.5);
                    translate.setY(translate.getY() + dy * 0.5);
                } else {
                    // Regular orbit rotation
                    rotateX.setAngle(rotateX.getAngle() + dy * 0.5);
                    rotateZ.setAngle(rotateZ.getAngle() + dx * 0.5);
                }
            }

            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        });

        // Zoom with mouse scroll
        subScene.setOnScroll(event -> {
            if (camera instanceof ParallelCamera) {
                double scale = root3D.getScaleY() + (event.getDeltaY() / 200f);
                root3D.setScaleX(scale);
                root3D.setScaleY(scale);
            } else {
                double zoomFactor = event.getDeltaY();
                cameraTranslate.setZ(cameraTranslate.getZ() + zoomFactor);
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
