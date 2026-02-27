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

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
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
        MeshView cone = VisualizerUtils.createCone(radius, height, divisions);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(color);
        material.setSpecularColor(Color.WHITE);
        material.setSpecularPower(32);
        cone.setMaterial(material);

        return cone;
    }
}
