package com.willwinder.universalgcodesender.fx.component;

import com.willwinder.ugs.nbp.core.control.MacrosTopComponent;
import javafx.embed.swing.SwingNode;
import javafx.scene.layout.BorderPane;

public class MacrosPane extends BorderPane {
    public MacrosPane() {
        SwingNode content = new SwingNode();
        content.setContent(new MacrosTopComponent());
        setCenter(content);
    }
}
