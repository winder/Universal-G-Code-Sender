/*
 * Simple dialog for configuring GRBL settings.
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

import com.willwinder.universalgcodesender.i18n.AvailableLanguages;
import com.willwinder.universalgcodesender.i18n.Language;
import com.willwinder.universalgcodesender.i18n.Localization;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 *
 * @author wwinder
 */
public class ConnectionSettingsDialog extends javax.swing.JDialog {
    private boolean saveChanges;
    
    /**
     * Creates new form GrblSettingsDialog
     */
    public ConnectionSettingsDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        languageBox.removeAllItems();
        Vector<Language> al = AvailableLanguages.getAvailableLanguages();
        for (Language language : al) {
            languageBox.addItem(language);
        }
     
        initLocalization();
        setLocationRelativeTo(parent);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        saveChanges = false;
    }
    
    private void initLocalization() {
        titleLabel.setText(Localization.getString("sender.header"));

        overrideSpeedCheckBox.setText(Localization.getString("sender.speed.override"));
        overrideSpeedPercentLabel.setText(Localization.getString("sender.speed.percent"));
        maxCommandLengthLabel.setText(Localization.getString("sender.command.length"));
        truncateDecimalDigitsLabel.setText(Localization.getString("sender.truncate"));
        singleStepModeCheckBox.setText(Localization.getString("sender.singlestep"));
        removeAllWhitespaceCheckBox.setText(Localization.getString("sender.whitespace"));
        sendStatusPolls.setText(Localization.getString("sender.status"));
        statusPollingRate.setText(Localization.getString("sender.status.rate"));
        displayStateColor.setText(Localization.getString("sender.state"));
        this.convertArcsToLinesCheckBox.setText(Localization.getString("sender.arcs"));
        smallArcSegmentLengthLabel.setText(Localization.getString("sender.arcs.length"));
        smallArcThresholdLabel.setText(Localization.getString("sender.arcs.threshold"));
        
        closeWithSave.setText(Localization.getString("save.close"));
        closeWithoutSave.setText(Localization.getString("close"));
        helpButton.setText(Localization.getString("help"));
    }
    
    /**
     * Return status.
     */
    public boolean saveChanges() {
        return saveChanges;
    }

    /**
     * Getters for all the values.
     */
    public boolean getSpeedOverrideEnabled() {
        return this.overrideSpeedCheckBox.isSelected();
    }
    
    public boolean getSingleStepModeEnabled() {
        return this.singleStepModeCheckBox.isSelected();
    }
    
    public int getSpeedOverridePercent() {
        return Integer.parseInt(this.overrideSpeedPercentSpinner.getValue().toString());
    }
    
    public int getMaxCommandLength() {
        return Integer.parseInt(this.maxCommandLengthSpinner.getValue().toString());
    }
    
    public int getTruncateDecimalLength() {
        return Integer.parseInt(this.truncateDecimalDigitsSpinner.getValue().toString());
    }

    public boolean getRemoveAllWhitespace() {
        return this.removeAllWhitespaceCheckBox.isSelected();
    }

    public boolean getStatusUpdatesEnabled() {
        return this.sendStatusPolls.isSelected();
    }
    
    public int getStatusUpdatesRate() {
        return Integer.parseInt(this.statusPollRateSpinner.getValue().toString());
    }
    
    public boolean getDisplayStateColor() {
        return this.displayStateColor.isSelected();
    }
    
    public boolean getConvertArcsToLines() {
        return this.convertArcsToLinesCheckBox.isSelected();
    }
    
    public double getSmallArcThreshold() {
        return (Double)this.smallArcThresholdSpinner.getValue();
    }

    public double getSmallArcSegmentLength() {
        return (Double)this.smallArcSegmentLengthSpinner.getValue();
    }
    
    public String getLanguage() {
        return ((Language)languageBox.getSelectedItem()).getLanguage() + "_" + ((Language)languageBox.getSelectedItem()).getRegion();
    }
    
    /**
     * Setters for all the values.
     */
    public void setSpeedOverrideEnabled(boolean enabled) {
        this.overrideSpeedCheckBox.setSelected(enabled);
    }
    
    public void setSpeedOverridePercent(int overridePercent) {
        this.overrideSpeedPercentSpinner.setValue(overridePercent);
    }
    
    public void setSingleStepModeEnabled(boolean enabled) {
        this.singleStepModeCheckBox.setSelected(enabled);
    }
    
    public void setMaxCommandLength(int commandLength) {
        this.maxCommandLengthSpinner.setValue(commandLength);
    }
    
    public void setTruncateDecimalLength(int truncateLength) {
        this.truncateDecimalDigitsSpinner.setValue(truncateLength);
    }
    
    public void setRemoveAllWhitespace(boolean enabled) {
        this.removeAllWhitespaceCheckBox.setSelected(enabled);
    }
    
    public void setStatusUpdatesEnabled(boolean enabled) {
        this.sendStatusPolls.setSelected(enabled);
    }
    
    public void setStatusUpdatesRate(int milliseconds) {
        this.statusPollRateSpinner.setValue(milliseconds);
    }
    
    public void setStateColorDisplayEnabled(boolean enabled) {
        this.displayStateColor.setSelected(enabled);
    }
    
    public void setConvertArcsToLines(boolean enabled) {
        this.convertArcsToLinesCheckBox.setSelected(enabled);
    }
    
    public void setSmallArcThreshold(double threshold) {
        this.smallArcThresholdSpinner.setValue(threshold);
    }
    
    public void setSmallArcSegmentLengthSpinner(double threshold) {
        this.smallArcSegmentLengthSpinner.setValue(threshold);
    }
    
    public void setselectedLanguage(String language){
        Language l = AvailableLanguages.getLanguageByString(language);
        this.languageBox.setSelectedItem(l);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        overrideSpeedCheckBox = new javax.swing.JCheckBox();
        overrideSpeedPercentSpinner = new javax.swing.JSpinner();
        overrideSpeedPercentLabel = new javax.swing.JLabel();
        maxCommandLengthSpinner = new javax.swing.JSpinner();
        maxCommandLengthLabel = new javax.swing.JLabel();
        truncateDecimalDigitsLabel = new javax.swing.JLabel();
        truncateDecimalDigitsSpinner = new javax.swing.JSpinner();
        singleStepModeCheckBox = new javax.swing.JCheckBox();
        titleLabel = new javax.swing.JLabel();
        closeWithSave = new javax.swing.JButton();
        closeWithoutSave = new javax.swing.JButton();
        helpButton = new javax.swing.JButton();
        removeAllWhitespaceCheckBox = new javax.swing.JCheckBox();
        sendStatusPolls = new javax.swing.JCheckBox();
        statusPollingRate = new javax.swing.JLabel();
        statusPollRateSpinner = new javax.swing.JSpinner();
        displayStateColor = new javax.swing.JCheckBox();
        convertArcsToLinesCheckBox = new javax.swing.JCheckBox();
        smallArcThresholdLabel = new javax.swing.JLabel();
        smallArcThresholdSpinner = new javax.swing.JSpinner();
        smallArcSegmentLengthLabel = new javax.swing.JLabel();
        smallArcSegmentLengthSpinner = new javax.swing.JSpinner();
        languageBox = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        overrideSpeedCheckBox.setText("Enable speed override");

        overrideSpeedPercentSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(100), Integer.valueOf(1), null, Integer.valueOf(1)));

        overrideSpeedPercentLabel.setText("Speed override percent");

        maxCommandLengthSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(50), Integer.valueOf(1), null, Integer.valueOf(1)));

        maxCommandLengthLabel.setText("Max command length");

        truncateDecimalDigitsLabel.setText("Truncate decimal digits");

        truncateDecimalDigitsSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(4), Integer.valueOf(1), null, Integer.valueOf(1)));

        singleStepModeCheckBox.setText("Enable single step mode");

        titleLabel.setFont(new java.awt.Font("Dialog", 0, 18)); // NOI18N
        titleLabel.setText("Connection Settings");

        closeWithSave.setText("Save and close");
        closeWithSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeWithSaveActionPerformed(evt);
            }
        });

        closeWithoutSave.setText("Close");
        closeWithoutSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeWithoutSaveActionPerformed(evt);
            }
        });

        helpButton.setText("Help");
        helpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpButtonActionPerformed(evt);
            }
        });

        removeAllWhitespaceCheckBox.setText("Remove all whitespace in commands");

        sendStatusPolls.setText("Enable status polling");

        statusPollingRate.setText("Status poll rate (ms)");

        statusPollRateSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(100), Integer.valueOf(1), null, Integer.valueOf(1)));

        displayStateColor.setText("Enable state color display");

        convertArcsToLinesCheckBox.setText("Convert arcs to lines");

        smallArcThresholdLabel.setText("Small arc threshold (mm)");

        smallArcThresholdSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(2.0d), Double.valueOf(0.0d), null, Double.valueOf(0.1d)));

        smallArcSegmentLengthLabel.setText("Small arc segment length (mm)");

        smallArcSegmentLengthSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(1.3d), Double.valueOf(0.0d), null, Double.valueOf(0.1d)));

        languageBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(helpButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeWithoutSave)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(closeWithSave)
                        .addGap(24, 24, 24))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(38, 38, 38)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(removeAllWhitespaceCheckBox)
                                    .addComponent(singleStepModeCheckBox)
                                    .addComponent(overrideSpeedCheckBox)))
                            .addComponent(titleLabel))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(overrideSpeedPercentSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(overrideSpeedPercentLabel))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(maxCommandLengthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(maxCommandLengthLabel))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(truncateDecimalDigitsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(truncateDecimalDigitsLabel)))
                        .addGap(0, 0, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(statusPollRateSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(statusPollingRate))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(38, 38, 38)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sendStatusPolls)
                            .addComponent(displayStateColor)
                            .addComponent(convertArcsToLinesCheckBox)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(smallArcThresholdSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(smallArcThresholdLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(smallArcSegmentLengthSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 59, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(smallArcSegmentLengthLabel))
                    .addComponent(languageBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(titleLabel)
                .addGap(18, 18, 18)
                .addComponent(overrideSpeedCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(overrideSpeedPercentLabel)
                    .addComponent(overrideSpeedPercentSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(maxCommandLengthSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxCommandLengthLabel))
                .addGap(10, 10, 10)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(truncateDecimalDigitsSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(truncateDecimalDigitsLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(singleStepModeCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(removeAllWhitespaceCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sendStatusPolls)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(statusPollingRate)
                    .addComponent(statusPollRateSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(displayStateColor)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(convertArcsToLinesCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(smallArcThresholdLabel)
                    .addComponent(smallArcThresholdSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(smallArcSegmentLengthLabel)
                    .addComponent(smallArcSegmentLengthSpinner, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(languageBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeWithSave)
                    .addComponent(closeWithoutSave)
                    .addComponent(helpButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void closeWithoutSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeWithoutSaveActionPerformed
        this.saveChanges = false;
        setVisible(false);
    }//GEN-LAST:event_closeWithoutSaveActionPerformed

    private void closeWithSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeWithSaveActionPerformed
        this.saveChanges = true;
        setVisible(false);
    }//GEN-LAST:event_closeWithSaveActionPerformed

    private void helpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_helpButtonActionPerformed
        StringBuilder message = new StringBuilder()
                .append(Localization.getString("sender.help.speed.override")).append("\n\n")
                .append(Localization.getString("sender.help.speed.percent")).append("\n\n")
                .append(Localization.getString("sender.help.command.length")).append("\n\n")
                .append(Localization.getString("sender.help.truncate")).append("\n\n")
                .append(Localization.getString("sender.help.singlestep")).append("\n\n")
                .append(Localization.getString("sender.help.whitespace")).append("\n\n")
                .append(Localization.getString("sender.help.status")).append("\n\n")
                .append(Localization.getString("sender.help.status.rate")).append("\n\n")
                .append(Localization.getString("sender.help.state")).append("\n\n")
                .append(Localization.getString("sender.help.arcs")).append("\n\n")
                .append(Localization.getString("sender.help.arcs.threshold")).append("\n\n")
                .append(Localization.getString("sender.help.arcs.length"));
                
        
        JOptionPane.showMessageDialog(new JFrame(), 
                message, 
                Localization.getString("sender.help.dialog.title"),
                JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_helpButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton closeWithSave;
    private javax.swing.JButton closeWithoutSave;
    private javax.swing.JCheckBox convertArcsToLinesCheckBox;
    private javax.swing.JCheckBox displayStateColor;
    private javax.swing.JButton helpButton;
    private javax.swing.JComboBox languageBox;
    private javax.swing.JLabel maxCommandLengthLabel;
    private javax.swing.JSpinner maxCommandLengthSpinner;
    private javax.swing.JCheckBox overrideSpeedCheckBox;
    private javax.swing.JLabel overrideSpeedPercentLabel;
    private javax.swing.JSpinner overrideSpeedPercentSpinner;
    private javax.swing.JCheckBox removeAllWhitespaceCheckBox;
    private javax.swing.JCheckBox sendStatusPolls;
    private javax.swing.JCheckBox singleStepModeCheckBox;
    private javax.swing.JLabel smallArcSegmentLengthLabel;
    private javax.swing.JSpinner smallArcSegmentLengthSpinner;
    private javax.swing.JLabel smallArcThresholdLabel;
    private javax.swing.JSpinner smallArcThresholdSpinner;
    private javax.swing.JSpinner statusPollRateSpinner;
    private javax.swing.JLabel statusPollingRate;
    private javax.swing.JLabel titleLabel;
    private javax.swing.JLabel truncateDecimalDigitsLabel;
    private javax.swing.JSpinner truncateDecimalDigitsSpinner;
    // End of variables declaration//GEN-END:variables
}
