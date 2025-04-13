package com.willwinder.universalgcodesender.fx.visualizer;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;

public class Tool extends Group {
    DoubleProperty posX = new SimpleDoubleProperty(0);
    DoubleProperty posY = new SimpleDoubleProperty(0);
    DoubleProperty posZ = new SimpleDoubleProperty(0);

    public Tool() {
        BackendAPI backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
        backendAPI.addUGSEventListener(this::onEvent);

        MeshView cone = createCone(5, 10, 10, Color.YELLOW);
        cone.setRotationAxis(Rotate.X_AXIS);
        cone.setRotate(90);
        setTranslateZ(5);

        getChildren().add(cone);

        cone.translateXProperty().bind(posX);
        cone.translateYProperty().bind(posY);
        cone.translateZProperty().bind(posZ);

    }

    private void onEvent(UGSEvent ugsEvent) {
        if (ugsEvent instanceof ControllerStatusEvent) {
            Position workCoord = ((ControllerStatusEvent) ugsEvent).getStatus().getWorkCoord();
            posX.set(workCoord.getX());
            posY.set(workCoord.getY());
            posZ.set(workCoord.getZ());
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
        cone.setDrawMode(DrawMode.FILL); // or DrawMode.LINE for wireframe
        cone.setCullFace(CullFace.BACK);

        PhongMaterial material = new PhongMaterial(color);
        cone.setMaterial(material);

        return cone;
    }
}
