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

import static com.willwinder.ugs.nbp.connection.ConnectionGUITopComponent.setCombo;
import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.CommUtils;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.actions.Presenter;
import org.openide.windows.TopComponent;


@ActionID(
        category = "Edit",
        id = "com.willwinder.ugs.nbp.connectiontoolbar.SerialPortToolbarBox"
)
@ActionRegistration(
        displayName = "#CTL_Port",
        lazy = false
)
@ActionReference(path = "Toolbars/Connection", position = 0)

@NbBundle.Messages("CTL_Port=Port")

/**
 *
 * @author wwinder
 */
public final class SerialPortToolbarBox extends AbstractAction implements ActionListener, Presenter.Toolbar {
    @Override
    public void actionPerformed(ActionEvent e) {
        //Nothing needs to happen here.
    }

    //Define what will be displayed in the toolbar:
    @Override
    public Component getToolbarPresenter() {
        return new MyToolbarPresenter();
    }

    private class MyToolbarPresenter extends TopComponent {
        BackendAPI backend;

        JLabel portLabel;
        JLabel baudLabel;
        JComboBox<String> portComboBox;
        JComboBox<String> baudComboBox;
        
        private boolean initializing = true;
        
        MyToolbarPresenter() {
            initializing = true;
            
            initComponents();
            backend = CentralLookup.getDefault().lookup(BackendAPI.class);

            portComboBox.removeAllItems();
            baudComboBox.removeAllItems();
            
            final JComboBox baudCombo = baudComboBox;
            final JComboBox portCombo = portComboBox;

            // Add serial ports
            String[] portList = CommUtils.getSerialPortList();
            if (portList.length < 1) {
                //MainWindow.displayErrorDialog(Localization.getString("mainWindow.error.noSerialPort"));
            } else {
                for (String port : portList) {
                    portComboBox.addItem(port);
                }

                portComboBox.setSelectedIndex(0);
            }

            // Add baud rates
            baudComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "2400", "4800", "9600", "19200", "38400", "57600", "115200" }));
            baudComboBox.setSelectedIndex(6);
            
            final Preferences pref = NbPreferences.forModule(ConnectionProperty.class);
        
            String baud = pref.get("baud", "115200");
            System.out.println("Initial baud = " + baud);
            setCombo(baudCombo, baud);
            setCombo(portCombo, pref.get("address", ""));
            
            if (! pref.get("address", "").equals(portComboBox.getSelectedItem())) {
                NbPreferences.forModule(ConnectionProperty.class).put("address", portComboBox.getSelectedItem().toString());
            }

            portComboBox.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    portChangeEvent(evt);
                }
            });
            
            baudComboBox.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    baudChangeEvent(evt);
                }
            });
            
            pref.addPreferenceChangeListener(new PreferenceChangeListener() {
                @Override
                public void preferenceChange(PreferenceChangeEvent evt) {
                    JComboBox combo = null;
                    if (evt.getKey().equals("address")) {
                        combo = portCombo;
                    } else if (evt.getKey().equals("baud")) {
                        combo = baudCombo;
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

            initializing = false;
        }
        
        private void portChangeEvent(ActionEvent evt) {
            if (initializing) return;
            Object port = this.portComboBox.getSelectedItem();
            if (port != null)
                NbPreferences.forModule(ConnectionProperty.class).put("address", port.toString());

        }
        private void baudChangeEvent(ActionEvent evt) {
            if (initializing) return;
            Object baud = this.baudComboBox.getSelectedItem();
            if (baud != null)
                NbPreferences.forModule(ConnectionProperty.class).put("baud", baud.toString());
        }
        
        private void initComponents() {
            portLabel = new javax.swing.JLabel();
            baudLabel = new javax.swing.JLabel();
            portComboBox = new javax.swing.JComboBox<>();
            baudComboBox = new javax.swing.JComboBox<>();

            portComboBox.setEditable(true);
            baudComboBox.setEditable(true);

            org.openide.awt.Mnemonics.setLocalizedText(portLabel, org.openide.util.NbBundle.getMessage(MyToolbarPresenter.class, "SerialPortToolbarBox.Port.text")); // NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(baudLabel, org.openide.util.NbBundle.getMessage(MyToolbarPresenter.class, "SerialPortToolbarBox.Baud.text")); // NOI18N
            portLabel.setText(org.openide.util.NbBundle.getMessage(MyToolbarPresenter.class, "SerialPortToolbarBox.Port.text"));
            baudLabel.setText(org.openide.util.NbBundle.getMessage(MyToolbarPresenter.class, "SerialPortToolbarBox.Baud.text"));
            
            portComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "" }));

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(portLabel)
                    .addGap(2)
                    .addComponent(portComboBox)
                    .addGap(20)
                    .addComponent(baudLabel)
                    .addGap(2)
                    .addComponent(baudComboBox)
                    .addContainerGap())
            );
            
            layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(portLabel)
                        .addComponent(portComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(baudLabel)
                        .addComponent(baudComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
        }

    }
    
}
