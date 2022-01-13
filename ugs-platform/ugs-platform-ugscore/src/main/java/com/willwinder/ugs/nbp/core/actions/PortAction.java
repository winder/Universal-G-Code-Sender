/*
Copyright 2017-2018 Will Winder

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
import com.willwinder.universalgcodesender.connection.ConnectionFactory;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;

import static com.willwinder.universalgcodesender.utils.GUIHelpers.displayErrorDialog;

import java.awt.Component;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.CallableSystemAction;

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
    private static final String REFRESH_ICON = "resources/icons/refresh.svg";

    private final BackendAPI backend;
    private JComboBox<String> portCombo;
    private Component c;

    public PortAction() {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        this.backend.addUGSEventListener(this);

        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue(NAME, LocalizingService.ConnectionSerialPortToolbarTitle);
    }

    private void updatePort() {
        portCombo.setSelectedItem(this.backend.getSettings().getPort());
    }

    @Override
    public void performAction() {
        backend.getSettings().setPort(portCombo.getSelectedItem() + "");
    }

    @Override
    public String getName() {
        return LocalizingService.ConnectionSerialPortToolbarTitle;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null; // new HelpCtx("...ID") if you have a help set
    }


    @Override
    public void UGSEvent(UGSEvent evt) {
        if(c == null) {
            return;
        }

        // If a setting has changed elsewhere, update the combo boxes.
        if (evt instanceof SettingChangedEvent) {
            updatePort();
        }

        // if the state has changed, check if the baud box should be displayed.
        else if (evt instanceof ControllerStateEvent) {
            c.setVisible(backend.getControllerState() == ControllerState.DISCONNECTED);
        }
    }

    @Override
    public Component getToolbarPresenter() {
        if (c == null) {
            ImageIcon refreshIcon = null;
            try {
                refreshIcon = ImageUtilities.loadImageIcon(REFRESH_ICON, false);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }

            portCombo = new JComboBox<>();
            portCombo.setEditable(true);
            portCombo.addActionListener(e -> this.performAction());

            JButton refreshButton = new JButton();
            refreshButton.setIcon(refreshIcon);
            refreshButton.addActionListener(e -> loadPortSelector());

            JPanel panel = new JPanel(new FlowLayout());
            panel.add(new JLabel(Localization.getString("mainWindow.swing.portLabel")));
            panel.add(refreshButton);
            panel.add(portCombo);
            c = panel;

            updatePort();
        }

        return c;
    }

    private void loadPortSelector() {
        portCombo.removeAllItems();
        List<String> portList = ConnectionFactory.getPortNames(backend.getSettings().getConnectionDriver());
        if (portList.size() < 1) {
            if (backend.getSettings().isShowSerialPortWarning()) {
                displayErrorDialog(Localization.getString("mainWindow.error.noSerialPort"));
            }
        } else {
            for (String port : portList) {
                if (StringUtils.isNotEmpty(port)) {
                    portCombo.addItem(port);
                }
            }

            portCombo.setSelectedIndex(0);
            portCombo.repaint();
        }
    }
}
