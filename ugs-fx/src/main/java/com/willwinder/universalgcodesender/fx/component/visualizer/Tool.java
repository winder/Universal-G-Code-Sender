package com.willwinder.universalgcodesender.fx.component.visualizer;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;

public class Tool extends Group {
    private final PositionAnimatorTimer positionAnimator = new PositionAnimatorTimer();

    public Tool() {
        BackendAPI backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
        backendAPI.addUGSEventListener(this::onEvent);

        MeshView cone = createCone(4, 10, 16, Color.ORANGE);
        cone.setRotationAxis(Rotate.X_AXIS);
        cone.setRotate(90);
        cone.setTranslateZ(5);

        MeshView coneTop = createCone(4, 2, 16, Color.ORANGE);
        coneTop.setRotationAxis(Rotate.X_AXIS);
        coneTop.setRotate(-90);
        coneTop.setTranslateZ(11);

        getChildren().addAll(cone, coneTop);

        translateXProperty().bind(positionAnimator.posXProperty());
        translateYProperty().bind(positionAnimator.posYProperty());
        translateZProperty().bind(positionAnimator.posZProperty());
    }

    private void onEvent(UGSEvent ugsEvent) {
        if (ugsEvent instanceof ControllerStatusEvent controllerStatusEvent) {
            positionAnimator.setTarget(controllerStatusEvent.getStatus().getWorkCoord().getPositionIn(UnitUtils.Units.MM));
            positionAnimator.start();
        }
    }


    public MeshView createCone(float radius, float height, int divisions, Color color) {
        TriangleMesh mesh = new TriangleMesh();

        // 1. Add the tip point (apex of the cone)
        mesh.getPoints().addAll(0f, -height / 2f, 0f); // tip

        // 2. Add base circle points
        for (int i = 0; i < divisions; i++) {
            double angle = 2 * Math.PI * i / divisions;
            float x = (float) (radius * Math.cos(angle));
            float z = (float) (radius * Math.sin(angle));
            mesh.getPoints().addAll(x, height / 2f, z);
        }

        // 3. Texture coordinates (needed, but not used here)
        mesh.getTexCoords().addAll(0, 0);

        // 4. Create triangle faces from tip to each base segment
        for (int i = 0; i < divisions; i++) {
            int p0 = 0; // tip
            int p1 = i + 1;
            int p2 = (i + 1) % divisions + 1;

            mesh.getFaces().addAll(p0, 0, p1, 0, p2, 0); // side face
        }

        // 5. Optionally add base (disk) using triangles to center
        int baseCenterIndex = mesh.getPoints().size() / 3;
        mesh.getPoints().addAll(0f, height / 2f, 0f); // center of base

        for (int i = 0; i < divisions; i++) {
            int p0 = baseCenterIndex;
            int p1 = (i + 1) % divisions + 1;
            int p2 = i + 1;

            mesh.getFaces().addAll(p0, 0, p1, 0, p2, 0); // base face
        }

        // Create MeshView and material
        MeshView cone = new MeshView(mesh);
        cone.setDrawMode(DrawMode.FILL);
        cone.setCullFace(CullFace.BACK);

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(color);
        material.setSpecularColor(Color.WHITE);
        material.setSpecularPower(32);
        cone.setMaterial(material);

        return cone;
    }
}
