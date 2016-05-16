/*
    Copywrite 2015-2016 Will Winder

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

package com.willwinder.ugs.nbp.core.control;

import com.willwinder.ugs.nbp.core.control.Bundle;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UGSEvent.ControlState;
import com.willwinder.universalgcodesender.model.Utils.Units;
import com.willwinder.universalgcodesender.uielements.StepSizeSpinnerModel;
import com.willwinder.universalgcodesender.utils.Settings;
import java.awt.Component;
import java.awt.Container;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//com.willwinder.universalgcodesender.nbp.control//JogControl//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "JogControlTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "middle_left", openAtStartup = true)
@ActionID(category = LocalizingService.JogControlCategory, id = LocalizingService.JogControlActionId)
@ActionReference(path = LocalizingService.JogControlWindowPath)
@TopComponent.OpenActionRegistration(
        displayName = "<Not localized:JogControlTopComponent>",
        preferredID = "JogControlTopComponent"
)

@Messages({
})
public final class JogControlTopComponent extends TopComponent implements UGSEventListener, PreferenceChangeListener {

    BackendAPI backend;
    Settings settings;
    JogService jogService;
    Preferences pref;

    public JogControlTopComponent() {
        setName(LocalizingService.JogControlTitle);
        setToolTipText(LocalizingService.JogControlTooltip);

        initComponents();
        pref = NbPreferences.forModule(JogService.class);
        pref.addPreferenceChangeListener(this);
    }

    private void updateValues() {
        this.stepSizeSpinner.setValue(jogService.getStepSize());
        this.mmRadioButton.setSelected(jogService.getUnits() == Units.MM);
        this.inchRadioButton.setSelected(jogService.getUnits() == Units.INCH);
    }

    /**
     * TODO: Move this to the backend with a UGSEvent?
     */
    @Override
    public void preferenceChange(PreferenceChangeEvent evt) {
        updateValues();
    }

    public void enableComponents(Container container, boolean enable) {
        Component[] components = container.getComponents();
        for (Component component : components) {
            component.setEnabled(enable);
            if (component instanceof Container) {
                enableComponents((Container)component, enable);
            }
        }
    }
    
    private void updateComponents() {
        enableComponents(this, backend.getControlState() == ControlState.COMM_IDLE);
    }
    
    private double getStepSize() {
        String value = this.stepSizeSpinner.getValue().toString();
        BigDecimal bd = new BigDecimal(value);
        bd.setScale(3, RoundingMode.HALF_EVEN);
        return bd.doubleValue();
        //return Double.parseDouble( this.stepSizeSpinner.getValue().toString() );
    }
    
    @Override
    public void UGSEvent(UGSEvent cse) {
        updateComponents();
    }
    
    @Override
    public void componentOpened() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        settings = CentralLookup.getDefault().lookup(Settings.class);
        jogService = Lookup.getDefault().lookup(JogService.class);

        jogService.setStepSize(getStepSize());
        switch (jogService.getUnits()) {
            case MM:
                this.mmRadioButton.setSelected(true);
                break;
            case INCH:
                this.inchRadioButton.setSelected(true);
                break;
            default:
                break;
        }

        backend.addUGSEventListener(this);
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        stepSizeLabel = new javax.swing.JLabel();
        inchRadioButton = new javax.swing.JRadioButton();
        mmRadioButton = new javax.swing.JRadioButton();
        stepSizeSpinner = new javax.swing.JSpinner();
        movementButtonPanel = new javax.swing.JPanel();
        zMinusButton = new javax.swing.JButton();
        yMinusButton = new javax.swing.JButton();
        xPlusButton = new javax.swing.JButton();
        xMinusButton = new javax.swing.JButton();
        zPlusButton = new javax.swing.JButton();
        yPlusButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(stepSizeLabel, org.openide.util.NbBundle.getMessage(JogControlTopComponent.class, "JogControlTopComponent.stepSizeLabel.text")); // NOI18N
        stepSizeLabel.setEnabled(false);

        buttonGroup1.add(inchRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(inchRadioButton, org.openide.util.NbBundle.getMessage(JogControlTopComponent.class, "JogControlTopComponent.inchRadioButton.text")); // NOI18N
        inchRadioButton.setEnabled(false);
        inchRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inchRadioButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(mmRadioButton);
        org.openide.awt.Mnemonics.setLocalizedText(mmRadioButton, org.openide.util.NbBundle.getMessage(JogControlTopComponent.class, "JogControlTopComponent.mmRadioButton.text")); // NOI18N
        mmRadioButton.setEnabled(false);
        mmRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mmRadioButtonActionPerformed(evt);
            }
        });

        stepSizeSpinner.setModel(new StepSizeSpinnerModel(1.0, 0.0, null, 1.0));
        stepSizeSpinner.setEnabled(false);
        stepSizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                stepSizeSpinnerStateChanged(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(zMinusButton, org.openide.util.NbBundle.getMessage(JogControlTopComponent.class, "JogControlTopComponent.zMinusButton.text")); // NOI18N
        zMinusButton.setEnabled(false);
        zMinusButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zMinusButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(yMinusButton, org.openide.util.NbBundle.getMessage(JogControlTopComponent.class, "JogControlTopComponent.yMinusButton.text")); // NOI18N
        yMinusButton.setEnabled(false);
        yMinusButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yMinusButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(xPlusButton, org.openide.util.NbBundle.getMessage(JogControlTopComponent.class, "JogControlTopComponent.xPlusButton.text")); // NOI18N
        xPlusButton.setEnabled(false);
        xPlusButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xPlusButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(xMinusButton, org.openide.util.NbBundle.getMessage(JogControlTopComponent.class, "JogControlTopComponent.xMinusButton.text")); // NOI18N
        xMinusButton.setEnabled(false);
        xMinusButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xMinusButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(zPlusButton, org.openide.util.NbBundle.getMessage(JogControlTopComponent.class, "JogControlTopComponent.zPlusButton.text")); // NOI18N
        zPlusButton.setEnabled(false);
        zPlusButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zPlusButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(yPlusButton, org.openide.util.NbBundle.getMessage(JogControlTopComponent.class, "JogControlTopComponent.yPlusButton.text")); // NOI18N
        yPlusButton.setEnabled(false);
        yPlusButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yPlusButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout movementButtonPanelLayout = new javax.swing.GroupLayout(movementButtonPanel);
        movementButtonPanel.setLayout(movementButtonPanelLayout);
        movementButtonPanelLayout.setHorizontalGroup(
            movementButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(movementButtonPanelLayout.createSequentialGroup()
                .addComponent(xMinusButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(movementButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(yPlusButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(yMinusButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(xPlusButton, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(movementButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(zMinusButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(zPlusButton, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        movementButtonPanelLayout.setVerticalGroup(
            movementButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(movementButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                .addComponent(xMinusButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(xPlusButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(movementButtonPanelLayout.createSequentialGroup()
                    .addComponent(yPlusButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(yMinusButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(movementButtonPanelLayout.createSequentialGroup()
                    .addComponent(zPlusButton, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(zMinusButton, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(movementButtonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(inchRadioButton)
                            .addComponent(stepSizeLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stepSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(mmRadioButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(stepSizeLabel)
                    .addComponent(stepSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(inchRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(mmRadioButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(movementButtonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void inchRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inchRadioButtonActionPerformed
        jogService.setUnits(Units.INCH);
    }//GEN-LAST:event_inchRadioButtonActionPerformed

    private void mmRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mmRadioButtonActionPerformed
        jogService.setUnits(Units.MM);
    }//GEN-LAST:event_mmRadioButtonActionPerformed

    private void stepSizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_stepSizeSpinnerStateChanged
        jogService.setStepSize(getStepSize());
    }//GEN-LAST:event_stepSizeSpinnerStateChanged

    private void zMinusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zMinusButtonActionPerformed
        jogService.adjustManualLocation(0, 0, -1);
    }//GEN-LAST:event_zMinusButtonActionPerformed

    private void yMinusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yMinusButtonActionPerformed
        jogService.adjustManualLocation(0, -1, 0);
    }//GEN-LAST:event_yMinusButtonActionPerformed

    private void xPlusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xPlusButtonActionPerformed
        jogService.adjustManualLocation(1, 0, 0);
    }//GEN-LAST:event_xPlusButtonActionPerformed

    private void xMinusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xMinusButtonActionPerformed
        jogService.adjustManualLocation(-1, 0, 0);
    }//GEN-LAST:event_xMinusButtonActionPerformed

    private void zPlusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zPlusButtonActionPerformed
        jogService.adjustManualLocation(0, 0, 1);
    }//GEN-LAST:event_zPlusButtonActionPerformed

    private void yPlusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yPlusButtonActionPerformed
        jogService.adjustManualLocation(0, 1, 0);
    }//GEN-LAST:event_yPlusButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JRadioButton inchRadioButton;
    private javax.swing.JRadioButton mmRadioButton;
    private javax.swing.JPanel movementButtonPanel;
    private javax.swing.JLabel stepSizeLabel;
    private javax.swing.JSpinner stepSizeSpinner;
    private javax.swing.JButton xMinusButton;
    private javax.swing.JButton xPlusButton;
    private javax.swing.JButton yMinusButton;
    private javax.swing.JButton yPlusButton;
    private javax.swing.JButton zMinusButton;
    private javax.swing.JButton zPlusButton;
    // End of variables declaration//GEN-END:variables
}
