package com.willwinder.universalgcodesender.uielements.command;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.uielements.CommandTextArea;
import com.willwinder.universalgcodesender.uielements.LengthLimitedDocument;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class CommandPanel extends JPanel implements UGSEventListener, ControllerListener {
    private final int consoleSize = 1024 * 1024;

    private final BackendAPI backend;

    private final JCheckBox scrollWindowCheckBox = new JCheckBox(Localization.getString("mainWindow.swing.scrollWindowCheckBox"));
    private final JCheckBox showVerboseOutputCheckBox = new JCheckBox(Localization.getString("mainWindow.swing.showVerboseOutputCheckBox"));

    private final JScrollPane scrollPane = new JScrollPane();
    private final JTextArea consoleTextArea = new JTextArea();
    private final CommandTextArea commandTextField;
    private final JLabel commandLabel = new JLabel(Localization.getString("mainWindow.swing.commandLabel"));

    /**
     * No-Arg constructor to make this control work in the UI builder tools
     * @deprecated Use constructor with BackendAPI.
     */
    @Deprecated
    public CommandPanel() {
        this(null);
    }

    public CommandPanel(BackendAPI backend) {
        this.backend = backend;
        if (backend != null) {
            backend.addUGSEventListener(this);
            backend.addControllerListener(this);
        }
        commandTextField = new CommandTextArea(backend);
        initComponents();
    }


    private void initComponents() {
        consoleTextArea.setEditable(false);
        consoleTextArea.setColumns(20);
        consoleTextArea.setDocument(new LengthLimitedDocument(consoleSize));
        consoleTextArea.setRows(5);
        consoleTextArea.setMaximumSize(new java.awt.Dimension(32767, 32767));
        consoleTextArea.setMinimumSize(new java.awt.Dimension(0, 0));
        scrollPane.setViewportView(consoleTextArea);

        scrollWindowCheckBox.addActionListener(e -> checkScrollWindow());

        MigLayout layout = new MigLayout("fill, wrap 1", "", "[min!][][min!]");
        setLayout(layout);
        add(scrollWindowCheckBox, "al left, split 2");
        add(showVerboseOutputCheckBox, "al left");
        add(scrollPane, "grow, growy");
        add(commandLabel, "al left, split 2");
        add(commandTextField, "r, grow");
    }

    @Override
    public void UGSEvent(UGSEvent evt) {

    }

    @Override
    public void controlStateChange(UGSEvent.ControlState state) {
    }

    @Override
    public void fileStreamComplete(String filename, boolean success) {

    }

    @Override
    public void commandSkipped(GcodeCommand command) {

    }

    @Override
    public void commandSent(GcodeCommand command) {

    }

    @Override
    public void commandComplete(GcodeCommand command) {

    }

    @Override
    public void commandComment(String comment) {

    }

    @Override
    public void statusStringListener(String state, Position machineCoord, Position workCoord) {

    }

    @Override
    public void postProcessData(int numRows) {

    }

    @Override
    public void messageForConsole(MessageType type, String msg) {
        java.awt.EventQueue.invokeLater(() -> {
            boolean verbose = MessageType.VERBOSE.equals(type);
            if (!verbose || showVerboseOutputCheckBox.isSelected()) {
                if (verbose) {
                    consoleTextArea.append("[" + type.getLocalizedString() + "] ");
                }
                consoleTextArea.append(msg);

                if (consoleTextArea.isVisible() && scrollWindowCheckBox.isSelected()) {
                    consoleTextArea.setCaretPosition(consoleTextArea.getDocument().getLength());
                }
            }
        });
    }


    private void checkScrollWindow() {
        // Console output.
        DefaultCaret caret = (DefaultCaret)consoleTextArea.getCaret();
        if (scrollWindowCheckBox.isSelected()) {
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
            consoleTextArea.setCaretPosition(consoleTextArea.getDocument().getLength());
        } else {
            caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }
    }

    public void loadSettings() {
        scrollWindowCheckBox.setSelected(backend.getSettings().isScrollWindowEnabled());
        showVerboseOutputCheckBox.setSelected(backend.getSettings().isVerboseOutputEnabled());
        checkScrollWindow();
    }

    public void saveSettings() {
        backend.getSettings().setScrollWindowEnabled(scrollWindowCheckBox.isSelected());
        backend.getSettings().setVerboseOutputEnabled(showVerboseOutputCheckBox.isSelected());
    }

}
