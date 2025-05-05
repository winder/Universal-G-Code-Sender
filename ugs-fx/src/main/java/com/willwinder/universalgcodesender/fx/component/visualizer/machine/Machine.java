package com.willwinder.universalgcodesender.fx.component.visualizer.machine;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.fx.component.visualizer.PositionAnimatorTimer;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Group;

public abstract class Machine extends Group {

    private final PositionAnimatorTimer machinePositionAnimator = new PositionAnimatorTimer();
    private final PositionAnimatorTimer workPositionAnimator = new PositionAnimatorTimer();

    public Machine() {
        BackendAPI backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
        backendAPI.addUGSEventListener(this::onEvent);

    }

    private void onEvent(UGSEvent event) {
        if (event instanceof ControllerStatusEvent controllerStatusEvent) {
            machinePositionAnimator.setTarget(controllerStatusEvent.getStatus().getMachineCoord());
            machinePositionAnimator.start();

            workPositionAnimator.setTarget(controllerStatusEvent.getStatus().getWorkCoord());
            workPositionAnimator.start();
        }
    }

    public DoubleProperty machinePositionXProperty() {
        return machinePositionAnimator.posXProperty();
    }

    public DoubleProperty machinePositionYProperty() {
        return machinePositionAnimator.posYProperty();
    }

    public DoubleProperty machinePositionZProperty() {
        return machinePositionAnimator.posZProperty();
    }

    public DoubleProperty workPositionXProperty() {
        return workPositionAnimator.posXProperty();
    }

    public DoubleProperty workPositionYProperty() {
        return workPositionAnimator.posYProperty();
    }

    public DoubleProperty workPositionZProperty() {
        return workPositionAnimator.posZProperty();
    }
}
