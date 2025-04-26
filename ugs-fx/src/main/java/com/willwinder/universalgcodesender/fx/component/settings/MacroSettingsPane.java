package com.willwinder.universalgcodesender.fx.component.settings;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.macros.MacroSettingsPanel;
import javafx.embed.swing.SwingNode;
import javafx.scene.layout.BorderPane;

public class MacroSettingsPane  extends BorderPane {
    public MacroSettingsPane() {
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        SwingNode swingNode = new SwingNode();
        swingNode.setContent(new MacroSettingsPanel(backend));
        setCenter(swingNode);
    }
}
