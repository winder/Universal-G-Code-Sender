package com.willwinder.universalgcodesender.uielements.toolbars;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.helpers.AbstractConnectionPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class ConnectionBaudRatePanel extends AbstractConnectionPanel {

    public ConnectionBaudRatePanel(BackendAPI backend) {
        super(backend);
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
    }

    @Override
    protected void layoutComponents() {
        // Layout components
        this.add(baudLabel);
        this.add(baudCombo);
    }
}
