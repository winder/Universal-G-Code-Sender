package com.willwinder.universalgcodesender.fx.component;

import com.willwinder.ugs.nbp.dro.MachineStatusTopComponent;
import javafx.embed.swing.SwingNode;

public class MachineStatusPane extends SwingNode {
    public MachineStatusPane() {
        MachineStatusTopComponent machineStatusTopComponent = new MachineStatusTopComponent();
        setContent(machineStatusTopComponent);
    }
}
