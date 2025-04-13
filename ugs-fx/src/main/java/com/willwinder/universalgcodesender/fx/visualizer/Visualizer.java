package com.willwinder.universalgcodesender.fx.visualizer;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.ParallelCamera;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class Visualizer extends Pane {

    private double anchorAngleY;
    private double anchorAngleX;
    private double anchorX = 0;
    private double anchorY = 0;
    private final double currentAngleX = 0, currentAngleY = 0; // Current rotation angles

    private long lastTime = System.nanoTime(); // Time of the last frame
    private int frameCount = 0; // Count the number of frames

    private final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);

    public Visualizer() {
        // Root of the 3D scene graph
        Group root3D = new Group();

        // Create a cube
        Box cube = new Box(10, 10, 100);
        cube.setMaterial(new PhongMaterial(Color.CORNFLOWERBLUE));

        // Rotate for better viewing
        cube.getTransforms().addAll(
                new Rotate(30, Rotate.Y_AXIS),
                new Rotate(20, Rotate.X_AXIS)
        );

        // Lighting
        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateX(200);
        light.setTranslateY(-100);
        light.setTranslateZ(-100);

        AmbientLight ambient = new AmbientLight(Color.rgb(80, 80, 80));

        root3D.getChildren().addAll(light, ambient, new GcodeModel(), new Tool());

        root3D.getTransforms().addAll(rotateX, rotateY);

        Camera camera = createCamera();


        // SubScene with 3D content
        SubScene subScene = new SubScene(root3D, 400, 400, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.GRAY);
        subScene.setCamera(camera);


        subScene.widthProperty().bind(widthProperty());
        subScene.heightProperty().bind(heightProperty());

        getChildren().add(subScene);

        subScene.setOnScroll((final ScrollEvent e) -> {
            //camera.setTranslateZ(camera.getTranslateZ() + e.getDeltaY());
            if (camera instanceof ParallelCamera) {
                double scale = root3D.getScaleY() + (e.getDeltaY() / 200f);
                System.out.println(root3D.getScaleX() + " " + (root3D.getScaleX() + scale));
                root3D.setScaleX(scale);
                root3D.setScaleY(scale);
            } else {
                System.out.println(camera.getTranslateZ());
                camera.setTranslateZ(camera.getTranslateZ() + e.getDeltaY());
            }
        });

        // Mouse interaction
        subScene.setOnMousePressed((MouseEvent event) -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = rotateX.getAngle();
            anchorAngleY = rotateY.getAngle();
        });

        // Use Timeline to update rotations smoothly
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(16), e -> {
                    root3D.setRotationAxis(Rotate.Y_AXIS);
                    root3D.setRotate(currentAngleY); // Apply Y rotation
                    root3D.setRotationAxis(Rotate.X_AXIS);
                    root3D.setRotate(currentAngleX); // Apply X rotation


                    // Calculate and print the frame rate
                    long currentTime = System.nanoTime();
                    long elapsedTime = currentTime - lastTime;
                    frameCount++;

                    // Every second, print the frame rate
                    if (elapsedTime >= 1_000_000_000) { // 1 second
                        double frameRate = frameCount / (elapsedTime / 1_000_000_000.0);
                        System.out.println("Frame Rate: " + frameRate + " FPS");
                        frameCount = 0; // Reset frame count
                        lastTime = currentTime; // Reset time
                    }
                })
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        subScene.setOnMouseDragged((MouseEvent event) -> {
            rotateX.setAngle(anchorAngleX + (event.getSceneY() - anchorY));
            rotateY.setAngle(anchorAngleY - (event.getSceneX() - anchorX));
        });
    }

    private Camera createCamera() {
        boolean useParallelCamera = false;
        if (useParallelCamera) {
            Camera camera = new ParallelCamera();
            camera.setTranslateX(-300);
            camera.setTranslateY(-300);
            return camera;
        } else {
            // Camera
            PerspectiveCamera camera = new PerspectiveCamera(true);
            camera.setTranslateZ(-500);
            camera.setNearClip(0.1);
            camera.setFarClip(1000);
            return camera;
        }
    }
}