/*
 * GRBL Firmware Settings. Dynamically load and save all GRBL settings.
 */
/*
    Copywrite 2013 Will Winder

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

import com.willwinder.universalgcodesender.GrblController;
import com.willwinder.universalgcodesender.GrblUtils;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.Utils;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import java.awt.event.ActionEvent;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class GrblFirmwareSettingsDialog extends javax.swing.JDialog implements ControllerListener {
    private static final String ERROR = "Error";
    // Controller object to fetch settings from.
    private final BackendAPI grblController;
    private final TableCellListener tcl;
    private int numberOfSettings = 0;
    //private List<String> commandList;
    private boolean loadingSettings;
    
    private String[] commands = null;
    
    private static final int COL_INDEX_SETTING     = 0;
    private static final int COL_INDEX_VALUE       = 1;
    private static final int COL_INDEX_DESCRIPTION = 2;
    
    
    private static final String settingNumRegex = "\\$(\\d*)";
    private static final String settingValueRegex = "\\=(\\d*\\.?\\d*)";
    private static final String commentRegex = "\\(.*\\)";
    
    private final Pattern settingNumPattern;
    private final Pattern settingValuePattern;
    private final Pattern commentPattern;

    // These guys are used to save initial settings and determine when they can
    // be restored.
    private boolean initialSingleStepMode;
    private boolean statusUpdatesEnabled;
    private boolean savingSettings;
    
    /**
     * Creates new form GrblFirmwareSettingsDialog
     */
    public GrblFirmwareSettingsDialog(java.awt.Frame parent, boolean modal, BackendAPI backend) throws Exception {
        super(parent, modal);
        initComponents();
        initLocalization();
        setLocationRelativeTo(parent);

        if (backend == null) {
            throw new Exception("There is no controller. Are you connected?");
        }
        
        this.grblController = backend;
        this.grblController.addControllerListener(this);
        
        this.loadingSettings = false;
        
        // Compile regular expressions.
        this.settingNumPattern   = Pattern.compile(settingNumRegex);
        this.settingValuePattern = Pattern.compile(settingValueRegex);
        this.commentPattern      = Pattern.compile(commentRegex);
        
        initSettings();
        
        Action action = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                TableCellListener tcl = (TableCellListener)e.getSource();
                handleSettingChange(tcl.getRow(), tcl.getColumn(), tcl.getOldValue(), tcl.getNewValue());
            }
        };

        tcl = new TableCellListener(this.settingsTable, action);
    }
    
    private void initSettings() throws Exception {
        this.loadingSettings = true;
        this.grblController.sendGcodeCommand(GrblUtils.GRBL_VIEW_SETTINGS_COMMAND);
    }
    
    private void checkDoneSavingSettings() {
                
        if (this.savingSettings) {
            // If the controller is done sending (we just received the final OK)
            // then reset the original user settings.
            if (!this.grblController.isSending()) {
            //if (this.grblController.rowsRemaining() == 0) {
                // Reset controller to previous settings.
                //These should not be re-enabled until all ok arrive
                this.grblController.getController().setSingleStepMode(initialSingleStepMode);
                this.grblController.getController().setStatusUpdatesEnabled(statusUpdatesEnabled);
                this.savingSettings = false;
            }
        }

    }
    
    /**
     * We are interested in settings.
     */
    @Override
    public void messageForConsole(MessageType type, String msg) {
        // Initially we are in load-settings mode, looking for results to "$$".
        if (this.loadingSettings) {
            if ("ok".equals(msg)) {
                this.loadingSettings = false;
            } else if (this.isSettingString(msg)) {
                this.addSetting(msg);
                // Increment for each setting.
                this.numberOfSettings++;
            }
        }
        
        // Later we can be in save-settings mode.
        if (this.savingSettings) {
            this.checkDoneSavingSettings();
        }
    }
    
    private boolean isSettingString(String msg) {
        if (msg.startsWith("$$")) {
            return false;
        }
        else if (msg.startsWith("$")) {
            return true;
        }
        return false;
    }
    
    private void addSetting(String msg) {
        String setting;
        String value;
        String comment;
        
        Matcher matcher = this.settingNumPattern.matcher(msg);
        if (matcher.find()) {
            setting = matcher.group();
        } else { return; }
        
        matcher = this.settingValuePattern.matcher(msg);
        if (matcher.find()) {
            value = matcher.group(1);
        } else { return; }
        
        matcher = this.commentPattern.matcher(msg);
        if (matcher.find()) {
            comment = matcher.group();
        } else { return; }
        
        addSetting(setting, value, comment);
    }
    
    private void addSetting(String setting, String value, String description) {
        ((DefaultTableModel)this.settingsTable.getModel()).addRow(new String[]{
            setting,
            value,
            description});
    }
    
    private void updateSetting(int row, String setting, String value, String description) {
        this.settingsTable.getModel().setValueAt(setting, row, COL_INDEX_SETTING);
        this.settingsTable.getModel().setValueAt(value, row, COL_INDEX_VALUE);
        this.settingsTable.getModel().setValueAt(description, row, COL_INDEX_DESCRIPTION);
    }

    private void handleSettingChange(int row, int column, Object oldValue, Object newValue) {
                
        // TODO: Initialize the command array the first time a column is edited.
        if ((this.commands == null) && (this.numberOfSettings > 0)) {
            this.commands = new String[numberOfSettings];
        }
        
        // If the user somehow edited a command (thus creating the array) before
        // all the commands were loaded this situation arises.
        if (row > this.commands.length) {
            String before[] = this.commands;
            this.commands = new String[numberOfSettings];
            System.arraycopy(before, 0, this.commands, 0, before.length);
        }
        
        String setting = this.settingsTable.getModel().getValueAt(row, COL_INDEX_SETTING).toString();
        String command = setting + "=" + newValue;
        
        this.commands[row] = command;
    }
      
    private void initLocalization() {
        closeButton.setText(Localization.getString("close"));
        saveButton.setText(Localization.getString("save"));
        TableColumnModel tcm = settingsTable.getTableHeader().getColumnModel();
        tcm.getColumn(0).setHeaderValue(Localization.getString("setting"));
        tcm.getColumn(1).setHeaderValue(Localization.getString("value"));
        tcm.getColumn(2).setHeaderValue(Localization.getString("description"));
        settingsTable.getTableHeader().repaint();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        saveButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        settingsTableScrollPane = new javax.swing.JScrollPane();
        settingsTable = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        saveButton.setText("Save");
        saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveButtonActionPerformed(evt);
            }
        });

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        settingsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Setting", "Value", "Description"
            }
        ) {
            private Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            private boolean[] canEdit = new boolean [] {
                false, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        settingsTable.getTableHeader().setReorderingAllowed(false);
        settingsTableScrollPane.setViewportView(settingsTable);
        settingsTable.getColumnModel().getColumn(0).setMinWidth(50);
        settingsTable.getColumnModel().getColumn(0).setMaxWidth(50);
        settingsTable.getColumnModel().getColumn(1).setPreferredWidth(85);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
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
    }// </editor-fold>//GEN-END:initComponents

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        if (this.savingSettings) {
            JOptionPane.showMessageDialog(new JFrame(),
                "Cannot close dialog until settings have finished being saved."
                + "\nIf we got here by mistake open a bug report on github.",
                ERROR, JOptionPane.ERROR_MESSAGE);
        } else {
            this.setVisible(false);
        }
    }//GEN-LAST:event_closeButtonActionPerformed

    private void saveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveButtonActionPerformed
        this.settingsTable.editCellAt(-1, -1);
        // Cannot update firmware if the controller is busy.
        if (this.grblController.isSending()) {
            JOptionPane.showMessageDialog(new JFrame(),
                "Cannot update firmware while it is busy.",
                ERROR, JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        initialSingleStepMode = this.grblController.getController().getSingleStepMode();
        statusUpdatesEnabled = this.grblController.getController().getStatusUpdatesEnabled();
        
        // Single step mode is required for commands which modify GRBL's EEPROM.
        this.grblController.getController().setSingleStepMode(true);
        this.grblController.getController().setStatusUpdatesEnabled(false);
        this.savingSettings = true;

        // Search command array for commands and send them.
        try {            
            String command;
            for (int i=0; i < this.commands.length; i++) {
                command = this.commands[i];
                if (command != null) {
                    // If GRBL is feeling especially quick, we may need to keep
                    // setting these guys.
                    this.grblController.getController().setSingleStepMode(true);
                    this.grblController.getController().setStatusUpdatesEnabled(false);

                    this.grblController.sendGcodeCommand(command);
                    this.commands[i] = null;
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(new JFrame(),
                "Error from firmware while saving settings: " + ex.getMessage(),
                ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_saveButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeButton;
    private javax.swing.JButton saveButton;
    private javax.swing.JTable settingsTable;
    private javax.swing.JScrollPane settingsTableScrollPane;
    // End of variables declaration//GEN-END:variables
    
    @Override
    public void controlStateChange(UGSEvent.ControlState state) {
    }

    @Override
    public void fileStreamComplete(String filename, boolean success) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void commandComplete(GcodeCommand command) {
    }
    
    @Override
    public void commandSkipped(GcodeCommand command) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void commandSent(GcodeCommand command) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void commandComment(String comment) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void statusStringListener(String state, Position machineCoord, Position workCoord) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void postProcessData(int numRows) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}
