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
package com.willwinder.universalgcodesender.fx.component.visualizer.machine;

import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;
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
