package com.willwinder.universalgcodesender.fx.component.visualizer.machine;

import com.willwinder.universalgcodesender.fx.component.visualizer.VisualizerSettings;
import com.willwinder.universalgcodesender.fx.component.visualizer.machine.genmitsu3020.Genmitsu3020;
import javafx.scene.Group;

public class Machine extends Group {
    public Machine() {
        getChildren().add(new Genmitsu3020());

        visibleProperty().bind(VisualizerSettings.getInstance().showMachineProperty());
    }
}
