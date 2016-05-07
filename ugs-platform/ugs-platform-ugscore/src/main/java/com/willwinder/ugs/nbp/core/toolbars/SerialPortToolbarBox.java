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
package com.willwinder.ugs.nbp.core.toolbars;

import com.willwinder.ugs.nbp.core.connection.BaudComboBox;
import com.willwinder.ugs.nbp.core.connection.FirmwareComboBox;
import com.willwinder.ugs.nbp.core.connection.PortComboBox;
import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
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
        private BackendAPI backend;

        private JLabel fwLabel;
        private JLabel portLabel;
        private JLabel baudLabel;
        private JButton refreshButton;
        private FirmwareComboBox fwComboBox;
        private PortComboBox portComboBox;
        private BaudComboBox baudComboBox;
        
        private final boolean initializing = true;
        
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
            refreshButton = new JButton();

            fwComboBox.setEditable(false);
            portComboBox.setEditable(true);
            baudComboBox.setEditable(true);

            org.openide.awt.Mnemonics.setLocalizedText(fwLabel, org.openide.util.NbBundle.getMessage(MyToolbarPresenter.class, "SerialPortToolbarBox.Firmware.text")); // NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(portLabel, org.openide.util.NbBundle.getMessage(MyToolbarPresenter.class, "SerialPortToolbarBox.Port.text")); // NOI18N
            org.openide.awt.Mnemonics.setLocalizedText(baudLabel, org.openide.util.NbBundle.getMessage(MyToolbarPresenter.class, "SerialPortToolbarBox.Baud.text")); // NOI18N
            fwLabel.setText(org.openide.util.NbBundle.getMessage(MyToolbarPresenter.class, "SerialPortToolbarBox.Firmware.text"));
            portLabel.setText(org.openide.util.NbBundle.getMessage(MyToolbarPresenter.class, "SerialPortToolbarBox.Port.text"));
            baudLabel.setText(org.openide.util.NbBundle.getMessage(MyToolbarPresenter.class, "SerialPortToolbarBox.Baud.text"));

            refreshButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/refresh.gif")));
            refreshButton.addActionListener((java.awt.event.ActionEvent evt) -> {
                portComboBox.initComboBox();
            });

            JPanel p = new JPanel();
            p.setLayout(new MigLayout("insets 0 0 0 0"));
            p.add(fwLabel, "gapleft 5");
            p.add(fwComboBox, "gapright 20");
            p.add(portLabel);
            p.add(refreshButton);
            p.add(portComboBox, "gapright 20");
            p.add(baudLabel);
            p.add(baudComboBox, "gapright 10");

            setLayout(new BorderLayout());
            add(p);
        }
    }
}
