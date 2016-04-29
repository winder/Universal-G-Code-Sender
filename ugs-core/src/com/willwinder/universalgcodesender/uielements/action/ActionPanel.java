package com.willwinder.universalgcodesender.uielements.action;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.MacroActionPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ActionPanel extends JPanel {

    private final ActionButtonPanel actionButtonPanel;
    private final MacroActionPanel macroActionPanel;

    /**
     * No-Arg constructor to make this control work in the UI builder tools
     * @deprecated Use constructor with BackendAPI.
     */
    @Deprecated
    public ActionPanel() {
        this(null);
    }

    public ActionPanel(BackendAPI backend) {
        actionButtonPanel = new ActionButtonPanel(backend);
        macroActionPanel = new MacroActionPanel(backend);

        initComponents();
    }

    private void initComponents() {
        MigLayout layout = new MigLayout("fill");
        setLayout(layout);

        add(actionButtonPanel);
        add(macroActionPanel, "grow");
    }
}
