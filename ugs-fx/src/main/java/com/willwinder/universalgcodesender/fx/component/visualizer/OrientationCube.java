package com.willwinder.universalgcodesender.fx.component.visualizer;

import com.willwinder.universalgcodesender.fx.helper.Colors;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.DirectionalLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

import java.util.function.Consumer;

public class OrientationCube extends Group {

    private Consumer<OrientationCubeFace> onFaceClicked = (face) -> {};
    private SubScene subScene;
    private Group cubeRoot = new Group();

    public OrientationCube(double size) {
        getChildren().add(createOrientationSubScene(size));
    }

    public void setOnFaceClicked(Consumer<OrientationCubeFace> handler) {
        this.onFaceClicked = handler;
    }

    private Group createCube(double size) {
        Group cube = new Group();
        cube.getChildren().add(createFace(size, OrientationCubeFace.BOTTOM, -size / 2, 0, 0, 0));
        cube.getChildren().add(createFace(size, OrientationCubeFace.TOP, size / 2, 0, 0, 180));
        cube.getChildren().add(createFace(size, OrientationCubeFace.RIGHT, size / 2, 0, 90, 270));
        cube.getChildren().add(createFace(size, OrientationCubeFace.LEFT, -size / 2, 0, 90, 270));
        cube.getChildren().add(createFace(size, OrientationCubeFace.FRONT, size / 2, 90, 0, 180));
        cube.getChildren().add(createFace(size, OrientationCubeFace.BACK, -size / 2, 90, 0, 180));
        return cube;
    }

    private Box createFace(double size, OrientationCubeFace faceEnum, double translateZ, double rotateX, double rotateY, double rotateZ) {
        Box face = new Box(size, size, 0.1);
        face.getTransforms().addAll(new Rotate(rotateX, Rotate.X_AXIS), new Rotate(rotateY, Rotate.Y_AXIS), new Rotate(rotateZ, Rotate.Z_AXIS), new Translate(0, 0, translateZ));
        face.setOnMouseClicked(event -> onFaceClicked.accept(faceEnum));
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseMap(createLabeledFace(faceEnum.name(), Colors.BLACKISH));
        material.setSpecularColor(Color.BLACK);
        material.setSpecularPower(1);
        face.setMaterial(material);
        return face;
    }

    private SubScene createOrientationSubScene(double size) {
        cubeRoot = createCube(size * 0.55);

        AmbientLight ambient = new AmbientLight(Color.rgb(200, 200, 200));
        DirectionalLight light = new DirectionalLight(Color.WHITE);
        light.setDirection(new Point3D(1, -1, 1));

        Group root3D = new Group();
        root3D.getChildren().addAll(cubeRoot, ambient, light);

        subScene = new SubScene(
                root3D,
                size, size,
                true,
                SceneAntialiasing.BALANCED
        );

        /*ParallelCamera cam = new ParallelCamera();
        cam.translateXProperty().bind(subScene.widthProperty().divide(2).negate());
        cam.translateYProperty().bind(subScene.heightProperty().divide(2).negate());
        subScene.setCamera(cam);*/

        PerspectiveCamera cam = new PerspectiveCamera();
        cam.setNearClip(0.1);
        cam.setFarClip(100);
        cam.translateXProperty().bind(subScene.widthProperty().divide(2).negate());
        cam.translateYProperty().bind(subScene.heightProperty().divide(2).negate());
        subScene.setCamera(cam);

        subScene.heightProperty().bind(subScene.widthProperty());
        return subScene;
    }

    DoubleProperty sizeProperty() {
        return subScene.widthProperty();
    }

    private WritableImage createLabeledFace(String label, Color background) {
        Canvas canvas = new Canvas(100, 100);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        Font font = Font.font(20);
        Text text = new Text(label);
        text.setFont(font);

        gc.setFill(background);
        gc.fillRect(0, 0, 100, 100);
        gc.setFill(Color.WHITE);
        gc.setFont(font);

        gc.fillText(label, (100 - text.getLayoutBounds().getWidth()) / 2, (100 / 2.0) + 8); // center-ish

        WritableImage image = new WritableImage(100, 100);
        canvas.snapshot(null, image);
        return image;
    }

    public void setRotations(Rotate rotateX, Rotate rotateY, Rotate rotateZ) {
        cubeRoot.getTransforms().addAll(rotateX, rotateY, rotateZ);
    }
}
