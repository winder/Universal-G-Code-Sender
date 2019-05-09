/*
    Copyright 2019 Will Winder

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
package com.willwinder.universalgcodesender.uielements;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.macros.MacroSettingsPanel;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle;
import javax.swing.WindowConstants;
import java.awt.Frame;

/**
 * A macro settings dialog for managing macros
 *
 * @author Joacim Breiler
 */
public class MacroSettingsDialog extends JDialog {

    private final BackendAPI backend;
    private MacroSettingsPanel macroSettingsPanel;

    public MacroSettingsDialog(Frame parent, boolean modal, BackendAPI backend) {
        super(parent, modal);
        this.backend = backend;

        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        JButton saveButton = new JButton();
        saveButton.setText(Localization.getString("save.close"));
        saveButton.addActionListener(event -> saveButtonActionPerformed());

        JButton closeButton = new JButton();
        closeButton.setText(Localization.getString("close"));
        closeButton.addActionListener(event -> closeButtonActionPerformed());

        JScrollPane scrollPane = new JScrollPane();
        macroSettingsPanel = new MacroSettingsPanel(backend);
        scrollPane.setViewportView(macroSettingsPanel);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(closeButton)
                                .addContainerGap()
                                .addComponent(saveButton)
                                .addContainerGap())
                        .addComponent(scrollPane, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(closeButton)
                                        .addComponent(saveButton))
                                .addContainerGap())
        );

        pack();
        setSize(620, 400);
        setLocationRelativeTo(null);
    }

    private void closeButtonActionPerformed() {
        setVisible(false);
        dispose();
    }

    private void saveButtonActionPerformed() {
        macroSettingsPanel.save();
        closeButtonActionPerformed();
    }
}
