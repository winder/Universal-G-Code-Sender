package com.willwinder.universalgcodesender.fx.component.visualizer.machine.longmill;

import com.willwinder.universalgcodesender.fx.component.visualizer.machine.longmill.parts.YAxisFoot;
import com.willwinder.universalgcodesender.fx.component.visualizer.machine.longmill.parts.YRail;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class Base extends Group {
    public Base(double width, double depth) {

        YRail rail1 = new YRail(depth);
        rail1.setTranslateX(-45);
        rail1.setTranslateZ(5);
        getChildren().add(rail1);

        YRail rail2 = new YRail(depth);
        rail2.getTransforms().add(new Rotate(180, Rotate.Y_AXIS));
        rail2.setTranslateX(width + 44 + 26);
        rail2.setTranslateZ(-5);
        getChildren().add(rail2);


        YAxisFoot foot1 = new YAxisFoot();
        foot1.getTransforms().add(new Rotate(180, Rotate.Z_AXIS));
        foot1.getTransforms().add(new Translate(0, 4, 0));
        getChildren().add(foot1);

        YAxisFoot foot2 = new YAxisFoot();
        foot2.getTransforms().add(new Rotate(180, Rotate.Z_AXIS));
        foot2.getTransforms().add(new Translate(-width - 26, 4, 0));
        getChildren().add(foot2);

        YAxisFoot foot3 = new YAxisFoot();
        foot3.getTransforms().add(new Translate(-1, depth + 4, 0));
        getChildren().add(foot3);

        YAxisFoot foot4 = new YAxisFoot();
        foot4.getTransforms().add(new Translate(width + 25, depth + 4, 0));
        getChildren().add(foot4);

        double bedWidth = width + 144;
        double bedDepth = depth + 100;
        Box basePlate = new Box(bedWidth, bedDepth, 16);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.BURLYWOOD);
        material.setSpecularColor(Color.LIGHTGRAY);
        material.setSpecularPower(50);

        basePlate.setMaterial(material);
        basePlate.getTransforms().add(new Translate(bedWidth / 2 - 60 , bedDepth / 2 - 50, -88));
        getChildren().add(basePlate);
    }
}
