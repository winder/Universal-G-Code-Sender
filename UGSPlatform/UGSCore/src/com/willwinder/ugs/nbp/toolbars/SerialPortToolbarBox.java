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
package com.willwinder.ugs.nbp.toolbars;

import com.willwinder.ugs.nbp.connection.BaudComboBox;
import com.willwinder.ugs.nbp.connection.FirmwareComboBox;
import com.willwinder.ugs.nbp.connection.PortComboBox;
import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
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

        JLabel fwLabel;
        JLabel portLabel;
        JLabel baudLabel;
        FirmwareComboBox fwComboBox;
        PortComboBox portComboBox;
        BaudComboBox baudComboBox;
        
        private boolean initializing = true;
        
        MyToolbarPresenter() {
            initComponents();

            setInitializing(true);

            backend = CentralLookup.getDefault().lookup(BackendAPI.class);

            setInitializing(false);
        }
        
        private void setInitializing(Boolean initializing) {
            this.fwComboBox.setInitializing(initializing);
            this.portComboBox.setInitializing(initializing);
            this.baudComboBox.setInitializing(initializing);
        }
        
        private void initComponents() {
            fwLabel = new javax.swing.JLabel();
            portLabel = new javax.swing.JLabel();
            baudLabel = new javax.swing.JLabel();
            fwComboBox = new FirmwareComboBox();
            portComboBox = new PortComboBox();
            baudComboBox = new BaudComboBox();

            fwComboBox.setEditable(false);
            portComboBox.setEditable(true);
            baudComboBox.setEditable(true);

            org.openide.awt.Mnemonics.setLocalizedText(fwLabel, org.openide.util.NbBundle.getMessage(MyToolbarPresenter.class, "SerialPortToolbarBox.Firmware.text")); // NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(portLabel, org.openide.util.NbBundle.getMessage(MyToolbarPresenter.class, "SerialPortToolbarBox.Port.text")); // NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(baudLabel, org.openide.util.NbBundle.getMessage(MyToolbarPresenter.class, "SerialPortToolbarBox.Baud.text")); // NOI18N
            fwLabel.setText(org.openide.util.NbBundle.getMessage(MyToolbarPresenter.class, "SerialPortToolbarBox.Firmware.text"));
            portLabel.setText(org.openide.util.NbBundle.getMessage(MyToolbarPresenter.class, "SerialPortToolbarBox.Port.text"));
            baudLabel.setText(org.openide.util.NbBundle.getMessage(MyToolbarPresenter.class, "SerialPortToolbarBox.Baud.text"));
            
            portComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "" }));

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(fwLabel)
                    .addGap(2)
                    .addComponent(fwComboBox)
                    .addGap(20)
                    .addComponent(portLabel)
                    .addGap(2)
                    .addComponent(portComboBox, 100, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                        .addComponent(fwLabel)
                        .addComponent(fwComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(portLabel)
                        .addComponent(portComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(baudLabel)
                        .addComponent(baudComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
        }

    }
}
