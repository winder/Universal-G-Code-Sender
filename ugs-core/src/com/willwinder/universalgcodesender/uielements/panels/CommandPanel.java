/*
    Copyright 2016-2018 Will Winder

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
import com.willwinder.universalgcodesender.listeners.MessageListener;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import com.willwinder.universalgcodesender.uielements.components.CommandTextArea;
import com.willwinder.universalgcodesender.uielements.components.LengthLimitedDocument;
import com.willwinder.universalgcodesender.utils.SwingHelpers;
import net.miginfocom.swing.MigLayout;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.text.DefaultCaret;

/**
 * A panel for displaying console messages and a command line for manually entering commands.
 * It will automatically register it self as a {@link MessageListener} and {@link UGSEventListener}.
 *
 * @author wwinder
 */
public class CommandPanel extends JPanel implements UGSEventListener, MessageListener {
    private static final int CONSOLE_SIZE = 1024 * 1024;

    private final BackendAPI backend;

    private final JScrollPane scrollPane = new JScrollPane();
    private final JTextArea consoleTextArea = new JTextArea();
    private final CommandTextArea commandTextField;
    private final JLabel commandLabel = new JLabel(Localization.getString("mainWindow.swing.commandLabel"));

    private final JPopupMenu menu = new JPopupMenu();
    private final JCheckBoxMenuItem showVerboseMenuItem = new JCheckBoxMenuItem(
            Localization.getString("mainWindow.swing.showVerboseOutputCheckBox"));
    private final JCheckBoxMenuItem scrollWindowMenuItem = new JCheckBoxMenuItem(
            Localization.getString("mainWindow.swing.scrollWindowCheckBox"));


    /**
     * No-Arg constructor to make this control work in the UI builder tools
     *
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
            backend.addMessageListener(this);
        }
        commandTextField = new CommandTextArea(backend);
        initComponents();
        loadSettings();
    }


    private void initComponents() {
        consoleTextArea.setEditable(false);
        consoleTextArea.setColumns(20);
        consoleTextArea.setDocument(new LengthLimitedDocument(CONSOLE_SIZE));
        consoleTextArea.setRows(5);
        consoleTextArea.setMaximumSize(new java.awt.Dimension(32767, 32767));
        consoleTextArea.setMinimumSize(new java.awt.Dimension(0, 0));
        scrollPane.setViewportView(consoleTextArea);
        commandLabel.setEnabled(backend.isIdle());

        scrollWindowMenuItem.addActionListener(e -> checkScrollWindow());

        setLayout(new MigLayout("inset 0 0 5 0, fill, wrap 1", "", "[][min!]"));
        add(scrollPane, "grow, growy");
        add(commandLabel, "gapleft 5, al left, split 2");
        add(commandTextField, "gapright 5, r, grow");

        showVerboseMenuItem.addChangeListener((e) ->backend.getSettings().setVerboseOutputEnabled(showVerboseMenuItem.isSelected()));
        menu.add(showVerboseMenuItem);

        scrollWindowMenuItem.addChangeListener((e) -> backend.getSettings().setScrollWindowEnabled(scrollWindowMenuItem.isSelected()));
        menu.add(scrollWindowMenuItem);
        SwingHelpers.traverse(this, (comp) -> comp.setComponentPopupMenu(menu));
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof SettingChangedEvent) {
            loadSettings();
        } else if (evt instanceof ControllerStateEvent) {
            commandLabel.setEnabled(backend.isIdle());
        }
    }

    /**
     * When new messages are created this method will be called.
     * It will decide using the {@code messageType} if the message should be written to the console.
     *
     * @param messageType the type of message to be written
     * @param message     the message to be written to the console
     */
    @Override
    public void onMessage(MessageType messageType, String message) {
        java.awt.EventQueue.invokeLater(() -> {
            boolean verbose = MessageType.VERBOSE.equals(messageType);
            if (messageType.equals(MessageType.ERROR)) {
                consoleTextArea.append("[" +  messageType.getLocalizedString() + "] " + message);
            } else if (!verbose || showVerboseMenuItem.isSelected()) {
                if (verbose) {
                    consoleTextArea.append("[" + messageType.getLocalizedString() + "] ");
                }
                consoleTextArea.append(message);

                if (consoleTextArea.isVisible() && scrollWindowMenuItem.isSelected()) {
                    consoleTextArea.setCaretPosition(consoleTextArea.getDocument().getLength());
                }
            }
        });
    }

    private void checkScrollWindow() {
        // Console output.
        DefaultCaret caret = (DefaultCaret) consoleTextArea.getCaret();
        if (scrollWindowMenuItem.isSelected()) {
            caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
            consoleTextArea.setCaretPosition(consoleTextArea.getDocument().getLength());
        } else {
            caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
        }
    }

    public void loadSettings() {
        scrollWindowMenuItem.setSelected(backend.getSettings().isScrollWindowEnabled());
        showVerboseMenuItem.setSelected(backend.getSettings().isVerboseOutputEnabled());
        checkScrollWindow();
    }

    public void saveSettings() {
        backend.getSettings().setScrollWindowEnabled(scrollWindowMenuItem.isSelected());
        backend.getSettings().setVerboseOutputEnabled(showVerboseMenuItem.isSelected());
    }

    @Override
    public void requestFocus() {
        commandTextField.requestFocus();
    }
}
