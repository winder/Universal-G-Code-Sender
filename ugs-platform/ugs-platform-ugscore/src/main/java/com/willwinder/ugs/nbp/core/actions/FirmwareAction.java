/*
    Copyright 2017 Will Winder

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
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import com.willwinder.universalgcodesender.utils.FirmwareUtils;
import static com.willwinder.universalgcodesender.utils.GUIHelpers.displayErrorDialog;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

import java.awt.*;
import static javax.swing.Action.NAME;
import static javax.swing.Action.SMALL_ICON;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

@ActionID(
        category = LocalizingService.ConnectionFirmwareToolbarCategory,
        id = LocalizingService.ConnectionFirmwareToolbarActionId
)
@ActionRegistration(
        iconBase = FirmwareAction.ICON_BASE,
        displayName = "resources.MessagesBundle#" + LocalizingService.ConnectionFirmwareToolbarTitleKey,
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = "Toolbars/Connection",
                position = 980)})
public class FirmwareAction extends CallableSystemAction implements UGSEventListener {
    public static final String ICON_BASE = "resources/icons/firmware.svg";

    private final BackendAPI backend;
    private Component c;
    private JComboBox<String> firmwareCombo;

    public FirmwareAction() {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        this.backend.addUGSEventListener(this);

        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue(NAME, LocalizingService.ConnectionFirmwareToolbarTitle);
    }

    private void setFirmware() {
        String firmware = firmwareCombo.getSelectedItem().toString();
        backend.getSettings().setFirmwareVersion(firmware);
    }

    private void firmwareUpdated() {
        firmwareCombo.setSelectedItem( backend.getSettings().getFirmwareVersion());
    }

    @Override
    public void performAction() {
        backend.getSettings().setFirmwareVersion(firmwareCombo.getSelectedItem() + "");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public Component getToolbarPresenter() {
        if (c == null) {
            firmwareCombo = new JComboBox<>();
            JPanel panel = new JPanel(new FlowLayout());
            panel.add(new JLabel(Localization.getString("mainWindow.swing.firmwareLabel")));
            panel.add(firmwareCombo);
            c = panel;

            // Baud rate options.
            loadFirmwareSelector();

            firmwareCombo.addActionListener(a -> setFirmware());
        }
        return c;
    }

    @Override
    public String getName() {
        return LocalizingService.ConnectionFirmwareToolbarTitle;
    }

    @Override
    public void UGSEvent(com.willwinder.universalgcodesender.model.UGSEvent evt) {
        if (c == null) {
            return;
        }

        // If a setting has changed elsewhere, update the combo boxes.
        if (evt instanceof SettingChangedEvent) {
            firmwareUpdated();
        }

        // if the state has changed, check if the baud box should be displayed.
        else if (evt instanceof ControllerStateEvent) {
            c.setVisible(backend.getControllerState() == ControllerState.DISCONNECTED);
        }

    }

    private void loadFirmwareSelector() {
        firmwareCombo.removeAllItems();
        java.util.List<String> firmwareList = FirmwareUtils.getFirmwareList();

        if (firmwareList.size() < 1) {
            displayErrorDialog(Localization.getString("mainWindow.error.noFirmware"));
        } else {
            firmwareList.forEach(firmwareCombo::addItem);
        }

        firmwareUpdated();
    }
}
