package com.willwinder.universalgcodesender.fx.component;

import com.willwinder.ugs.nbp.jog.JogTopComponent;
import javafx.embed.swing.SwingNode;

public class JogPane  extends SwingNode {
    public JogPane() {
        JogTopComponent jogTopComponent = new JogTopComponent();
        setContent(jogTopComponent);
    }
}
