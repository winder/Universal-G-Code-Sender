/*
Copyright 2017-2023 Will Winder

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
package com.willwinder.ugs.nbp.core.actions;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.connection.ConnectionDriver;
import com.willwinder.universalgcodesender.connection.ConnectionFactory;
import com.willwinder.universalgcodesender.connection.IConnectionDevice;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import com.willwinder.universalgcodesender.utils.RefreshThread;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.CallableSystemAction;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

/**
 * @author wwinder
 */
@ActionID(
        category = LocalizingService.ConnectionSerialPortToolbarCategory,
        id = LocalizingService.ConnectionSerialPortToolbarActionId)
@ActionRegistration(
        iconBase = PortAction.ICON_BASE,
        displayName = "resources.MessagesBundle#" + LocalizingService.ConnectionSerialPortToolbarTitleKey,
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = "Toolbars/Connection",
                position = 990)
})
public class PortAction extends CallableSystemAction implements UGSEventListener {
    protected static final String ICON_BASE = "resources/icons/serialport.svg";

    private final transient BackendAPI backend;
    private final transient RefreshThread refreshThread;
    private JComboBox<IConnectionDevice> portCombo;
    private JPanel panel;
    private JLabel portLabel;

    public PortAction() {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        this.backend.addUGSEventListener(this);

        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue(NAME, LocalizingService.ConnectionSerialPortToolbarTitle);

        initializeComponents();

        refreshThread = new RefreshThread(this::refreshPorts, 3000);
        refreshThread.start();
    }

    @Override
    public void performAction() {
        Object selectedItem = portCombo.getSelectedItem();
        if (selectedItem == null) {
            return;
        }

        if (selectedItem instanceof IConnectionDevice) {
            IConnectionDevice selectedDevice = (IConnectionDevice) selectedItem;
            backend.getSettings().setPort(selectedDevice.getAddress());
            selectedDevice.getPort().ifPresent(port -> backend.getSettings().setPortRate(port.toString()));
        } else {
            backend.getSettings().setPort(selectedItem.toString());
        }
    }

    @Override
    public String getName() {
        return LocalizingService.ConnectionSerialPortToolbarTitle;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }


    @Override
    public void UGSEvent(UGSEvent evt) {
        // If a setting has changed elsewhere, update the combo box.
        if (evt instanceof SettingChangedEvent) {
            updatePortSettings();
        }

        // if the state has changed, check if the baud box should be displayed.
        else if (evt instanceof ControllerStateEvent) {
            ControllerState currentState = backend.getControllerState();
            panel.setVisible(currentState == ControllerState.DISCONNECTED);

            // Start stop the refresh thread
            if (currentState == ControllerState.DISCONNECTED) {
                refreshThread.start();
            } else {
                refreshThread.interrupt();
            }
        }
    }

    private void updatePortSettings() {
        portCombo.setSelectedItem(backend.getSettings().getPort());
        if (backend.getSettings().getConnectionDriver() == ConnectionDriver.TCP || backend.getSettings().getConnectionDriver() == ConnectionDriver.WS) {
            portLabel.setText(Localization.getString("mainWindow.swing.hostLabel"));
        } else {
            portLabel.setText(Localization.getString("mainWindow.swing.portLabel"));
        }
    }

    @Override
    public Component getToolbarPresenter() {
        initializeComponents();
        return panel;
    }

    private void initializeComponents() {
        portCombo = new JComboBox<>(new PortComboBoxModel());
        portCombo.setRenderer(new PortCellRenderer());
        portCombo.setEditable(true);
        portCombo.addActionListener(e -> performAction());
        portCombo.setSelectedItem(backend.getSettings().getPort());
        setMaximumWidth(portCombo, 150);

        portLabel = new JLabel(Localization.getString("mainWindow.swing.portLabel"));

        panel = new JPanel(new FlowLayout());
        panel.setOpaque(false);
        panel.add(portLabel);
        panel.add(portCombo);
    }

    private void setMaximumWidth(Component component, int width) {
        Dimension maximumSize = component.getPreferredSize();
        maximumSize.setSize(width, maximumSize.getHeight());
        component.setPreferredSize(maximumSize);
    }

    private void refreshPorts() {
        List<IConnectionDevice> availablePorts = ConnectionFactory.getDevices(backend.getSettings().getConnectionDriver());
        PortComboBoxModel model = (PortComboBoxModel) portCombo.getModel();
        model.setElements(availablePorts);
    }
}
