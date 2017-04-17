package com.willwinder.universalgcodesender.uielements.toolbars;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.helpers.AbstractConnectionPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;

public class ConnectionSerialPortPanel extends AbstractConnectionPanel {

    public ConnectionSerialPortPanel(BackendAPI backend) {
        super(backend);
        this.setLayout(new FlowLayout(FlowLayout.LEFT));
    }

    @Override
    protected void layoutComponents() {
        // Layout components
        this.add(portLabel);
        this.add(refreshButton);
        this.add(portCombo);
    }
}
