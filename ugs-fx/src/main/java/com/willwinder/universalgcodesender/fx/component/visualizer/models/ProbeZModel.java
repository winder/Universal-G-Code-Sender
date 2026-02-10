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
package com.willwinder.universalgcodesender.fx.component.visualizer.models;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.fx.component.visualizer.PositionAnimatorTimer;
import static com.willwinder.universalgcodesender.fx.component.visualizer.VisualizerUtils.createCone;
import static com.willwinder.universalgcodesender.fx.component.visualizer.machine.Colors.COLOR_ALUMINIUM;
import static com.willwinder.universalgcodesender.fx.component.visualizer.machine.Colors.COLOR_DARK_GREY;
import com.willwinder.universalgcodesender.fx.settings.ProbeSettings;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableValue;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.transform.Rotate;

public class ProbeZModel extends Model {
    private final PositionAnimatorTimer positionAnimator = new PositionAnimatorTimer();

    public ProbeZModel() {
        BackendAPI backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
        backendAPI.addUGSEventListener(this::onEvent);

        ObservableValue<Double> zPlateThickness = ProbeSettings.getInstance().zPlateThicknessProperty()
                .map(s -> s.convertTo(Unit.MM).doubleValue());

        ObservableNumberValue probeZDistanceMm = Bindings.createDoubleBinding(
                () -> ProbeSettings.getInstance()
                        .probeZDistanceProperty()
                        .get()
                        .convertTo(Unit.MM)
                        .doubleValue(),
                ProbeSettings.getInstance().probeZDistanceProperty()
        );

        getChildren().addAll(createProbePuck(zPlateThickness));
        getChildren().add(createArrow(probeZDistanceMm));


        translateXProperty().bind(positionAnimator.posXProperty());
        translateYProperty().bind(positionAnimator.posYProperty());
        translateZProperty().bind(positionAnimator.posZProperty().subtract(probeZDistanceMm));
    }

    private Node createArrow(ObservableNumberValue probeZDistanceMm) {
        PhongMaterial material = new PhongMaterial(COLOR_DARK_GREY.deriveColor(0, 1, 1, 0.6));
        material.setSpecularColor(Color.BLACK);
        material.setSpecularPower(96);
        double arrowHeight = 4;

        Cylinder arrowShaft = new Cylinder(0.3, 1.0, 10);
        arrowShaft.setRotationAxis(Rotate.X_AXIS);
        arrowShaft.setRotate(-90);
        arrowShaft.translateZProperty().bind(probeZDistanceMm.map(value -> (value.doubleValue() + arrowHeight) * .5));
        arrowShaft.heightProperty().bind(probeZDistanceMm.map(value -> value.doubleValue() - arrowHeight));
        arrowShaft.setMaterial(material);

        MeshView arrowHead = createCone(2, (float) arrowHeight, 16);
        arrowHead.setRotationAxis(Rotate.X_AXIS);
        arrowHead.setRotate(90);
        arrowHead.translateZProperty().set(2);
        arrowHead.setMaterial(material);

        return new Group(arrowShaft, arrowHead);
    }

    private Group createProbePuck(ObservableValue<Double> zPlateThickness) {
        Cylinder cylinder = new Cylinder(20d, zPlateThickness.getValue(), 32);
        cylinder.setRotationAxis(Rotate.X_AXIS);
        cylinder.setRotate(-90);
        cylinder.translateZProperty().bind(zPlateThickness.map(unitValue -> -unitValue / 2 - 1));
        cylinder.heightProperty().bind(zPlateThickness);

        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(COLOR_DARK_GREY);
        material.setSpecularColor(Color.WHITE);
        material.setSpecularPower(96);
        cylinder.setMaterial(material);

        Cylinder cylinder2 = new Cylinder(15d, zPlateThickness.getValue(), 32);
        cylinder2.setRotationAxis(Rotate.X_AXIS);
        cylinder2.setRotate(-90);
        cylinder2.translateZProperty().bind(zPlateThickness.map(unitValue -> -unitValue / 2));
        cylinder2.heightProperty().bind(zPlateThickness);

        PhongMaterial material2 = new PhongMaterial();
        material2.setDiffuseColor(COLOR_ALUMINIUM);
        material2.setSpecularColor(Color.WHITE);
        material2.setSpecularPower(1);
        cylinder2.setMaterial(material2);

        return new Group(cylinder, cylinder2);
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof ControllerStatusEvent controllerStatusEvent) {
            positionAnimator.setTarget(controllerStatusEvent.getStatus().getWorkCoord().getPositionIn(UnitUtils.Units.MM));
            positionAnimator.start();
        }
    }
}
