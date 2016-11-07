/*
    Copywrite 2016 Will Winder

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
package com.willwinder.universalgcodesender.uielements.panels;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.uielements.components.CommandTextArea;
import com.willwinder.universalgcodesender.uielements.components.LengthLimitedDocument;
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
    public void statusStringListener(ControllerStatus status) {

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
