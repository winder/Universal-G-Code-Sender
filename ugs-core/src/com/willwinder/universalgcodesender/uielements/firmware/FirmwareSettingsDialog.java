/*
    Copyright 2013-2018 Will Winder

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
package com.willwinder.universalgcodesender.uielements.firmware;

import com.willwinder.universalgcodesender.firmware.FirmwareSetting;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettingsListener;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.StringNumberComparator;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Firmware Settings. Dynamically load and save all settings.
 *
 * @author wwinder
 */
public class FirmwareSettingsDialog extends JDialog implements IFirmwareSettingsListener, WindowListener {

    private static final int COL_INDEX_SETTING = 0;
    private static final Logger logger = Logger.getLogger(FirmwareSettingsDialog.class.getName());

    private final IFirmwareSettings firmwareSettingsManager;
    private final FirmwareSettingsTableModel firmwareSettingsTableModel;

    private JButton closeButton;
    private JButton saveButton;
    private JTable settingsTable;

    /**
     * Creates new FirmwareSettingsDialog
     *
     * @param parent  the parent that opened the dialog
     * @param modal   if the dialog should be shown as a modal window
     * @param backend the backend api
     */
    public FirmwareSettingsDialog(Frame parent, boolean modal, BackendAPI backend) throws Exception {
        super(parent, modal);

        if (backend == null) {
            throw new Exception("There is no controller. Are you connected?");
        }

        firmwareSettingsManager = backend.getController().getFirmwareSettings();
        firmwareSettingsTableModel = new FirmwareSettingsTableModel(firmwareSettingsManager.getAllSettings());
        firmwareSettingsManager.addListener(this);

        initComponents();
        initLocalization();
        setLocationRelativeTo(parent);
    }

    private void initLocalization() {
        saveButton.setText(Localization.getString("save"));
        closeButton.setText(Localization.getString("close"));
        TableColumnModel tcm = settingsTable.getTableHeader().getColumnModel();
        tcm.getColumn(0).setHeaderValue(Localization.getString("setting"));
        tcm.getColumn(1).setHeaderValue(Localization.getString("value"));
        tcm.getColumn(2).setHeaderValue(Localization.getString("description"));
        settingsTable.getTableHeader().repaint();
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(this);

        saveButton = new JButton();
        saveButton.setText("Save");
        saveButton.addActionListener(event -> saveButtonActionPerformed());

        closeButton = new JButton();
        closeButton.setText("Close");
        closeButton.addActionListener(event -> closeButtonActionPerformed());

        settingsTable = new JTable();
        settingsTable.setModel(firmwareSettingsTableModel);

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(settingsTable.getModel());
        sorter.setComparator(COL_INDEX_SETTING, new StringNumberComparator());
        sorter.toggleSortOrder(COL_INDEX_SETTING);
        settingsTable.setRowSorter(sorter);

        JScrollPane settingsTableScrollPane = new JScrollPane();
        settingsTableScrollPane.setViewportView(settingsTable);
        settingsTable.getTableHeader().setReorderingAllowed(false);
        settingsTable.getColumnModel().getColumn(0).setMinWidth(60);
        settingsTable.getColumnModel().getColumn(0).setMaxWidth(80);
        settingsTable.getColumnModel().getColumn(2).setPreferredWidth(85);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(saveButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(closeButton)
                                .addContainerGap())
                        .addComponent(settingsTableScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 510, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(settingsTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(saveButton)
                                        .addComponent(closeButton))
                                .addContainerGap())
        );

        pack();
    }

    private void closeButtonActionPerformed() {
        setVisible(false);
        dispose();
    }

    private void saveButtonActionPerformed() {
        // Make a copy of all settings
        java.util.List<FirmwareSetting> settingsToUpdate = new ArrayList<>(firmwareSettingsTableModel.getSettings());

        // Loop through them and try to set them in the settings manager
        settingsToUpdate.forEach(s -> {
            FirmwareSetting updatedSetting = s;
            try {
                updatedSetting = firmwareSettingsManager.setValue(s.getKey(), s.getValue());
            } catch (FirmwareSettingsException ignored) {
                logger.log(Level.SEVERE, "Couldn't save setting: " + s.getKey() + "=" + s.getValue());
            } finally {
                firmwareSettingsTableModel.updateSetting(updatedSetting);
            }
        });
    }

    @Override
    public void onUpdatedFirmwareSetting(FirmwareSetting setting) {
        firmwareSettingsTableModel.updateSetting(setting);
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {

    }

    @Override
    public void windowClosed(WindowEvent e) {
        firmwareSettingsManager.removeListener(this);
    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
