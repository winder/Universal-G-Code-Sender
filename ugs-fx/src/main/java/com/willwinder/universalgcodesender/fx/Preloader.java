package com.willwinder.universalgcodesender.fx;

import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import eu.mihosoft.vrl.v3d.Cylinder;
import eu.mihosoft.vrl.v3d.Vector3d;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.geometry.Point3D;
import javafx.scene.AmbientLight;
import javafx.scene.DirectionalLight;
import javafx.scene.Group;
import javafx.scene.ParallelCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Preloader extends javafx.application.Preloader {

    private Stage preloaderStage;

    @Override
    public void start(Stage primaryStage) {
        this.preloaderStage = primaryStage;

        SubScene tool = createTool();
        StackPane stackPane = new StackPane(tool, new ImageView(SvgLoader.loadIcon("icons/preloader.svg", 190).orElse(null)));

        Scene scene = new Scene(stackPane, 300, 200, true);
        scene.setFill(Color.TRANSPARENT);
        scene.setCamera(new ParallelCamera());

        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private static SubScene createTool() {
        MeshView cone = new Cylinder(new Vector3d(0d, 0d, 0d), new Vector3d(0d, 0d, 150d), 0, 38, 16)
                .toCSG()
                .rotx(-90)
                .newMesh();

        MeshView top = new Cylinder(new Vector3d(0d, 0d, 150d), new Vector3d(0d, 0d, 165d), 38, 0, 16)
                .toCSG()
                .rotx(-90)
                .newMesh();
        PhongMaterial material = new PhongMaterial(Color.ORANGE);
        material.setSpecularColor(Color.WHITE);
        top.setMaterial(material);
        cone.setMaterial(material);
        Group coneGroup = new Group(cone, top);

        // Rotate animation
        RotateTransition rotate = new RotateTransition(Duration.seconds(6), coneGroup);
        rotate.setByAngle(360);
        rotate.setAxis(new Point3D(0, 1, 0));
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setAutoReverse(false);
        rotate.play();


        AmbientLight ambientLight = new AmbientLight(Color.rgb(80, 80, 80));
        DirectionalLight directionalLight = new DirectionalLight(Color.WHITE);
        directionalLight.setTranslateX(200);
        directionalLight.setTranslateY(-100);
        directionalLight.setTranslateZ(-300);

        Group root = new Group();
        root.getChildren().addAll(coneGroup, ambientLight, directionalLight);
        root.setTranslateX(150);
        root.setTranslateY(180);

        SubScene subScene3D = new SubScene(root, 300, 200, true, SceneAntialiasing.BALANCED);
        subScene3D.setCamera(new ParallelCamera());
        subScene3D.setFill(Color.TRANSPARENT);
        return subScene3D;
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification info) {
        if (info.getType() == StateChangeNotification.Type.BEFORE_START) {
            preloaderStage.hide();
        }
    }
}