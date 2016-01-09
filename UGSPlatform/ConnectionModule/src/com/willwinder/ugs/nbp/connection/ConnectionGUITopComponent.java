/*
    Copywrite 2015 Will Winder

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

package com.willwinder.ugs.nbp.connection;

import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.utils.FirmwareUtils;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.ControlStateEvent;
import com.willwinder.universalgcodesender.listeners.ControlStateListener;
import com.willwinder.universalgcodesender.utils.Settings;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.modules.OnStart;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//com.willwinder.universalgcodesender.nbp.connection//ConnectionGUI//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "ConnectionGUITopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "connection", openAtStartup = true)
@ActionID(category = "Window", id = "com.willwinder.universalgcodesender.nbp.connection.ConnectionGUITopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ConnectionGUIAction",
        preferredID = "ConnectionGUITopComponent"
)
@Messages({
    "CTL_ConnectionGUIAction=ConnectionGUI",
    "CTL_ConnectionGUITopComponent=ConnectionGUI",
    "HINT_ConnectionGUITopComponent=This is a ConnectionGUI"
})

@OnStart
public final class ConnectionGUITopComponent extends TopComponent implements ControlStateListener, Runnable {
    private static final Logger logger = Logger.getLogger(ConnectionGUITopComponent.class.getName());
    private boolean initializing = true;
    
    // OnStart
    @Override
    public void run() {
        CentralLookup.getDefault().add(new ConnectionProperty());
    }
    
    BackendAPI backend;
    Settings settings;
    ConnectionProperty cp;
    
    public ConnectionGUITopComponent() throws Exception {
        initComponents();

        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        settings = CentralLookup.getDefault().lookup(Settings.class);
            
        backend.addControlStateListener(this);
        loadFirmwareSelector();
    }
    
    public static void connect() {
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        Settings settings = CentralLookup.getDefault().lookup(Settings.class);

        logger.log(Level.INFO, "openclose button, connection open: " + backend.isConnected());
        if( !backend.isConnected() ) {
            
            final Preferences pref = NbPreferences.forModule(ConnectionProperty.class);
            
            String firmware = pref.get("firmware", "GRBL");
            String port = pref.get("address", "");
            int baudRate = Integer.parseInt(pref.get("baud", "115200"));
            
            try {
                backend.applySettings(settings);
                backend.connect(firmware, port, baudRate);
            } catch (Exception e) {
                e.printStackTrace();
                NotifyDescriptor nd = new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
        } else {
            try {
                backend.disconnect();
            } catch (Exception e) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(e.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
        }
    }
    
    private void loadFirmwareSelector() {
        firmwareComboBox.removeAllItems();
        List<String> firmwareList = FirmwareUtils.getFirmwareList();
        
        if (firmwareList.size() < 1) {
            //MainWindow.displayErrorDialog(Localization.getString("mainWindow.error.noFirmware"));
        } else {
            java.util.Iterator<String> iter = firmwareList.iterator();
            while ( iter.hasNext() ) {
                firmwareComboBox.addItem(iter.next());
            }
        }
    }
    
    private void initializePorts() {
        commPortComboBox.removeAllItems();
        
        String[] portList = jssc.SerialPortList.getPortNames();

        if (portList.length < 1) {
            //MainWindow.displayErrorDialog(Localization.getString("mainWindow.error.noSerialPort"));
            System.out.println("NO SERIAL PORTS!!!");
            NotifyDescriptor nd = new NotifyDescriptor.Message(Localization.getString("mainWindow.error.noSerialPort"), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);

        } else {
            // Sort?
            //java.util.Collections.sort(portList);

            for (String port : portList) {
                commPortComboBox.addItem(port);
            }

            commPortComboBox.setSelectedIndex(0);
        }    
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        connectionPanel = new javax.swing.JPanel();
        commPortComboBox = new PortComboBox();
        baudrateSelectionComboBox = new BaudComboBox();
        opencloseButton = new javax.swing.JButton();
        refreshButton = new javax.swing.JButton();
        baudLabel = new javax.swing.JLabel();
        portLabel = new javax.swing.JLabel();
        firmwareLabel = new javax.swing.JLabel();
        firmwareComboBox = new javax.swing.JComboBox<String>();

        connectionPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(ConnectionGUITopComponent.class, "ConnectionGUITopComponent.connectionPanel.border.title"))); // NOI18N
        connectionPanel.setMaximumSize(new java.awt.Dimension(247, 100));
        connectionPanel.setMinimumSize(new java.awt.Dimension(247, 100));
        connectionPanel.setName("Connection"); // NOI18N

        commPortComboBox.setEditable(true);
        commPortComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                commPortComboBoxActionPerformed(evt);
            }
        });

        baudrateSelectionComboBox.setToolTipText(org.openide.util.NbBundle.getMessage(ConnectionGUITopComponent.class, "ConnectionGUITopComponent.baudrateSelectionComboBox.toolTipText")); // NOI18N
        baudrateSelectionComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                baudrateSelectionComboBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(opencloseButton, org.openide.util.NbBundle.getMessage(ConnectionGUITopComponent.class, "ConnectionGUITopComponent.opencloseButton.text")); // NOI18N
        opencloseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                opencloseButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(refreshButton, org.openide.util.NbBundle.getMessage(ConnectionGUITopComponent.class, "ConnectionGUITopComponent.refreshButton.text")); // NOI18N
        refreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(baudLabel, org.openide.util.NbBundle.getMessage(ConnectionGUITopComponent.class, "ConnectionGUITopComponent.baudLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(portLabel, org.openide.util.NbBundle.getMessage(ConnectionGUITopComponent.class, "ConnectionGUITopComponent.portLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(firmwareLabel, org.openide.util.NbBundle.getMessage(ConnectionGUITopComponent.class, "ConnectionGUITopComponent.firmwareLabel.text")); // NOI18N

        firmwareComboBox.setEditable(true);
        firmwareComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                firmwareComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout connectionPanelLayout = new javax.swing.GroupLayout(connectionPanel);
        connectionPanel.setLayout(connectionPanelLayout);
        connectionPanelLayout.setHorizontalGroup(
            connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(connectionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(baudLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(firmwareLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 92, Short.MAX_VALUE)
                    .addComponent(portLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(refreshButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(firmwareComboBox, 0, 229, Short.MAX_VALUE)
                    .addComponent(baudrateSelectionComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(commPortComboBox, javax.swing.GroupLayout.Alignment.TRAILING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, connectionPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(opencloseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        connectionPanelLayout.setVerticalGroup(
            connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(connectionPanelLayout.createSequentialGroup()
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(commPortComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(portLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(baudLabel)
                    .addComponent(baudrateSelectionComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(10, 10, 10)
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(firmwareLabel)
                    .addComponent(firmwareComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(connectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(refreshButton)
                    .addComponent(opencloseButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(connectionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(connectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void baudrateSelectionComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_baudrateSelectionComboBoxActionPerformed
        if (initializing) return;
        
        Object baud = this.baudrateSelectionComboBox.getSelectedItem();
        if (baud != null)
            NbPreferences.forModule(ConnectionProperty.class).put("baud", baud.toString());
    }//GEN-LAST:event_baudrateSelectionComboBoxActionPerformed

    private void opencloseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_opencloseButtonActionPerformed
        connect();
    }//GEN-LAST:event_opencloseButtonActionPerformed

    private void refreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButtonActionPerformed
        initializePorts();
    }//GEN-LAST:event_refreshButtonActionPerformed

    private void commPortComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_commPortComboBoxActionPerformed
        if (initializing) return;
        Object port = this.commPortComboBox.getSelectedItem();
        if (port != null)
            NbPreferences.forModule(ConnectionProperty.class).put("address", port.toString());
    }//GEN-LAST:event_commPortComboBoxActionPerformed

    private void firmwareComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_firmwareComboBoxActionPerformed
        if (initializing) return;
        Object firmware = this.firmwareComboBox.getSelectedItem();
        if (firmware != null)
            NbPreferences.forModule(ConnectionProperty.class).put("firmware", firmware.toString());

    }//GEN-LAST:event_firmwareComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel baudLabel;
    private javax.swing.JComboBox<String> baudrateSelectionComboBox;
    private javax.swing.JComboBox<String> commPortComboBox;
    private javax.swing.JPanel connectionPanel;
    private javax.swing.JComboBox<String> firmwareComboBox;
    private javax.swing.JLabel firmwareLabel;
    private javax.swing.JButton opencloseButton;
    private javax.swing.JLabel portLabel;
    private javax.swing.JButton refreshButton;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        initializing = true;
        setName(Bundle.CTL_ConnectionGUITopComponent());
        setToolTipText(Bundle.HINT_ConnectionGUITopComponent());
        
        //cp = CentralLookup.getDefault().lookup(ConnectionProperty.class);
        //cp.addPropertyChangeListener(this);

        /*
        commPortComboBox.removeAllItems();
        initializePorts();
        
        final JComboBox baudCombo = this.baudrateSelectionComboBox;
        final JComboBox portCombo = this.commPortComboBox;
        final JComboBox firmCombo = this.firmwareComboBox;
        
        final Preferences pref = NbPreferences.forModule(ConnectionProperty.class);
        
        setCombo(baudCombo, pref.get("baud", "115200"));
        setCombo(portCombo, pref.get("address", ""));
        setCombo(firmCombo, pref.get("firmware", "GRBL"));
        
        pref.addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                JComboBox combo = null;
                if (evt.getKey().equals("address")) {
                    combo = portCombo;
                } else if (evt.getKey().equals("baud")) {
                    combo = baudCombo;
                } else if (evt.getKey().equals("firmware")) {
                    combo = firmCombo;
                }

                if (combo == null) return;

                initializing = true;
                try {
                    setCombo(combo, evt.getNewValue());
                } finally {
                    initializing = false;
                }
            }
        });
                */
        initializing = false;
    }

    @Override
    public void componentClosed() {
        //CentralLookup.getDefault().lookup(ConnectionProperty.class).removePropertyChangeListener(this);
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

    @Override
    public void ControlStateEvent(ControlStateEvent cse) {
        if (backend.isConnected()) {
            this.opencloseButton.setText(Localization.getString("close"));
        } else {
            this.opencloseButton.setText(Localization.getString("open"));
        }
    }
}
