package com.willwinder.universalgcodesender.fx.component.visualizer.machine;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.fx.component.visualizer.PositionAnimatorTimer;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import com.willwinder.universalgcodesender.model.events.FirmwareSettingEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;

public abstract class MachineModel extends Group {

    private final PositionAnimatorTimer machinePositionAnimator = new PositionAnimatorTimer();
    private final PositionAnimatorTimer workPositionAnimator = new PositionAnimatorTimer();
    private final DoubleProperty machineSizeX = new SimpleDoubleProperty(300);
    private final DoubleProperty machineSizeY = new SimpleDoubleProperty(300);
    private final DoubleProperty machineSizeZ = new SimpleDoubleProperty(100);

    private final BooleanProperty homingInvertX = new SimpleBooleanProperty(false);
    private final BooleanProperty homingInvertY = new SimpleBooleanProperty(false);
    private final BooleanProperty homingInvertZ = new SimpleBooleanProperty(false);

    private final BackendAPI backend;

    public MachineModel() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this::onEvent);

        updateControllerSettings();
        updateMachinePosition(backend.getMachinePosition().getPositionIn(UnitUtils.Units.MM));
        updateWorkPosition(backend.getWorkPosition().getPositionIn(UnitUtils.Units.MM));
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof ControllerStatusEvent controllerStatusEvent) {
            updateMachinePosition(controllerStatusEvent.getStatus()
                    .getMachineCoord()
                    .getPositionIn(UnitUtils.Units.MM));

            updateWorkPosition(controllerStatusEvent.getStatus()
                    .getWorkCoord()
                    .getPositionIn(UnitUtils.Units.MM));
        } else if (event instanceof FirmwareSettingEvent) {
            updateControllerSettings();
        }
    }

    private void updateWorkPosition(Position targetWorkPostion) {
        workPositionAnimator.setTarget(targetWorkPostion);
        workPositionAnimator.start();
    }

    private void updateMachinePosition(Position targetMachinePosition) {
        if (!homingInvertX.get()) {
            targetMachinePosition.set(Axis.X, targetMachinePosition.get(Axis.X) + machineSizeX.get());
        }

        if (!homingInvertY.get()) {
            targetMachinePosition.set(Axis.Y, targetMachinePosition.get(Axis.Y) + machineSizeY.get());
        }

        if (!homingInvertZ.get()) {
            targetMachinePosition.set(Axis.Z, targetMachinePosition.get(Axis.Z) + machineSizeZ.get());
        }

        machinePositionAnimator.setTarget(targetMachinePosition);
        machinePositionAnimator.start();
    }

    private void updateControllerSettings() {
        if (backend.getController() == null) {
            return;
        }

        try {
            homingInvertX.set(backend.getController().getFirmwareSettings().isHomingDirectionInverted(Axis.X));
            homingInvertY.set(backend.getController().getFirmwareSettings().isHomingDirectionInverted(Axis.Y));
            homingInvertZ.set(backend.getController().getFirmwareSettings().isHomingDirectionInverted(Axis.Z));

            machineSizeX.set(backend.getController().getFirmwareSettings().getSoftLimit(Axis.X));
            machineSizeY.set(backend.getController().getFirmwareSettings().getSoftLimit(Axis.Y));
            machineSizeZ.set(backend.getController().getFirmwareSettings().getSoftLimit(Axis.Z));
        } catch (Exception e) {
            // Never mind
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

    public DoubleProperty machineSizeXProperty() {
        return machineSizeX;
    }

    public DoubleProperty machineSizeYProperty() {
        return machineSizeY;
    }

    public DoubleProperty machineSizeZProperty() {
        return machineSizeZ;
    }
}
