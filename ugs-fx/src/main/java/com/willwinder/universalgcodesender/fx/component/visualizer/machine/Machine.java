package com.willwinder.universalgcodesender.fx.component.visualizer.machine;

import com.willwinder.universalgcodesender.fx.component.visualizer.VisualizerSettings;
import com.willwinder.universalgcodesender.fx.component.visualizer.machine.genmitsupm.GenmitsuProMaxModel;
import com.willwinder.universalgcodesender.fx.component.visualizer.machine.longmill.LongMillModel;
import com.willwinder.universalgcodesender.fx.component.visualizer.machine.unkown.UnknownMachine;
import javafx.scene.Group;

public class Machine extends Group {

    public Machine() {
        visibleProperty().bind(VisualizerSettings.getInstance().showMachineProperty());
        VisualizerSettings.getInstance().machineModelProperty().addListener((o, oldValue, newValue) -> updateMachineModel(newValue));
        updateMachineModel(VisualizerSettings.getInstance().machineModelProperty().get());
    }

    private void updateMachineModel(String newValue) {
        getChildren().clear();
        MachineType machineType = toMachineType(newValue);
        MachineModel machineModel = switch (machineType) {
            case GENMITSU_PRO_MAX -> new GenmitsuProMaxModel();
            case LONGMILL -> new LongMillModel();
            default -> new UnknownMachine();
        };
        getChildren().add(machineModel);
    }

    private MachineType toMachineType(String machineTypeString) {
        try {
            return MachineType.valueOf(machineTypeString);
        } catch (IllegalArgumentException e) {
            return MachineType.UNKNOWN;
        }
    }
}
