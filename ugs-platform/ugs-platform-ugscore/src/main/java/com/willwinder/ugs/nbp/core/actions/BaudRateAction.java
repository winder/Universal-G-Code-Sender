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

import com.willwinder.ugs.nbp.core.ui.BaudComboBox;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.connection.ConnectionDriver;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.CallableSystemAction;

import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.FlowLayout;

/**
 * @author wwinder
 */
@ActionID(
        category = LocalizingService.ConnectionBaudRateToolbarCategory,
        id = LocalizingService.ConnectionBaudRateToolbarActionId)
@ActionRegistration(
        iconBase = BaudRateAction.ICON_BASE,
        displayName = "resources/MessagesBundle#" + LocalizingService.ConnectionBaudRateToolbarTitleKey,
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = "Toolbars/Connection",
                position = 995)
})
public class BaudRateAction extends CallableSystemAction implements UGSEventListener {
    public static final String ICON_BASE = "resources/icons/baudrate.svg";

    private final transient BackendAPI backend;
    private Component toolbarComponent;
    private BaudComboBox baudCombo;
    private JLabel baudLabel = new JLabel();

    public BaudRateAction() {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        this.backend.addUGSEventListener(this);

        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue(NAME, LocalizingService.ConnectionBaudRateToolbarTitle);
    }

    private void updateBaudRate() {
        if (backend.getSettings().getConnectionDriver() == ConnectionDriver.TCP || backend.getSettings().getConnectionDriver() == ConnectionDriver.WS) {
            baudLabel.setText(Localization.getString("mainWindow.swing.portLabel"));
        } else {
            baudLabel.setText(Localization.getString("mainWindow.swing.baudLabel"));
        }
    }

    @Override
    public void performAction() {
        backend.getSettings().setPortRate(baudCombo.getSelectedItem() + "");
    }

    @Override
    public String getName() {
        return LocalizingService.ConnectionBaudRateToolbarTitle;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        // if the state has changed, check if the baud box should be displayed.
        if (toolbarComponent != null && evt instanceof ControllerStateEvent) {
            toolbarComponent.setVisible(backend.getControllerState() == ControllerState.DISCONNECTED);
        }
    }

    @Override
    public Component getToolbarPresenter() {
        if (toolbarComponent == null) {
            // Baud rate options.
            baudCombo = new BaudComboBox(backend);
            baudLabel = new JLabel();

            JPanel panel = new JPanel(new FlowLayout());
            panel.add(baudLabel);
            panel.add(baudCombo);
            toolbarComponent = panel;

            updateBaudRate();
        }

        return toolbarComponent;
    }
}
