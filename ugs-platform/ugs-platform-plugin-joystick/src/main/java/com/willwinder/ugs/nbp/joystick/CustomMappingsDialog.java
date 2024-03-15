/*
    Copyright 2024 Will Winder

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
package com.willwinder.ugs.nbp.joystick;

import com.willwinder.ugs.nbp.joystick.service.JoystickService;
import com.willwinder.universalgcodesender.i18n.Localization;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

/**
 * @author Joacim Breiler
 */
public class CustomMappingsDialog extends JDialog {

    private final transient JoystickService joystickService;
    private JButton cancelButton;
    private JButton okButton;
    private JTextArea textArea;

    public CustomMappingsDialog(Component parent, JoystickService joystickService) {
        super(SwingUtilities.getWindowAncestor(parent), ModalityType.APPLICATION_MODAL);
        this.joystickService = joystickService;
        setTitle(Localization.getString("platform.plugin.joystick.customMappings.title"));
        setPreferredSize(new Dimension(600, 300));
        setLayout(new MigLayout("fill, insets 5", "", ""));
        setResizable(true);

        createComponents();
        addEventListeners();

        pack();
        setLocationRelativeTo(SwingUtilities.getWindowAncestor(parent));
    }

    private void addEventListeners() {
        cancelButton.addActionListener(event -> onCancel());
        okButton.addActionListener(event -> onOk());
    }

    private void createComponents() {
        setLayout(new BorderLayout());

        textArea = new JTextArea(Settings.getCustomMapping());
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new MigLayout("insets 5", "[center, grow]"));
        cancelButton = new JButton(Localization.getString("mainWindow.swing.cancelButton"));
        buttonPanel.add(cancelButton);

        okButton = new JButton(Localization.getString("mainWindow.swing.okButton"));
        buttonPanel.add(okButton);
        add(buttonPanel, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(okButton);
    }

    private void onCancel() {
        setVisible(false);
        dispose();
    }

    private void onOk() {
        Settings.setCustomMapping(textArea.getText());
        joystickService.destroy();
        joystickService.initialize();
        setVisible(false);
        dispose();
    }
}
