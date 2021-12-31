/*
Copyright 2015-2018 Will Winder

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
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An action for connecting or disconnecting to a controller
 *
 * @author wwinder
 */
@ActionID(
        category = LocalizingService.ConnectDisconnectCategory,
        id = LocalizingService.ConnectDisconnectActionId)
@ActionRegistration(
        iconBase = ConnectDisconnectAction.ICON_BASE_DISCONNECT,
        displayName = "resources.MessagesBundle#" + LocalizingService.ConnectDisconnectActionTitleKey,
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.ConnectWindowPath,
                position = 900),
        @ActionReference(
                path = "Toolbars/Connection",
                position = 975)
})
public class ConnectDisconnectAction extends AbstractAction implements UGSEventListener {
    public static final String ICON_BASE = "resources/icons/connect.svg";
    public static final String ICON_BASE_DISCONNECT = "resources/icons/disconnect.svg";

    private static final Logger logger = Logger.getLogger(ConnectDisconnectAction.class.getName());
    private BackendAPI backend;

    public ConnectDisconnectAction() {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        if (this.backend != null) {
            this.backend.addUGSEventListener(this);
        }

        updateIconAndText();
    }

    @Override
    public void UGSEvent(UGSEvent cse) {
        if (cse instanceof ControllerStateEvent) {
            EventQueue.invokeLater(this::updateIconAndText);
        }
    }

    private void updateIconAndText() {
        if (backend.isConnected()) {
            putValue(NAME, LocalizingService.ConnectDisconnectTitleDisconnect);
            putValue("menuText", LocalizingService.ConnectDisconnectTitleDisconnect);
            putValue("iconBase", ICON_BASE);
            putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        } else {
            putValue(NAME, LocalizingService.ConnectDisconnectTitleConnect);
            putValue("menuText", LocalizingService.ConnectDisconnectTitleConnect);
            putValue("iconBase", ICON_BASE_DISCONNECT);
            putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE_DISCONNECT, false));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            connect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isEnabled() {
        // The action should always be enabled
        return true;
    }

    private void connect() {
        logger.log(Level.INFO, "openclose button, connection open: {0}", backend.isConnected());
        if (!backend.isConnected()) {
            Settings s = backend.getSettings();

            String firmware = s.getFirmwareVersion();
            String port = s.getPort();
            int baudRate = Integer.parseInt(s.getPortRate());

            ThreadHelper.invokeLater(() -> {
                try {
                    backend.connect(firmware, port, baudRate);
                } catch (Exception e) {
                    GUIHelpers.displayErrorDialog(e.getMessage());
                }
            });
        } else {
            try {
                backend.disconnect();
            } catch (Exception e) {
                String message = e.getMessage();
                if(StringUtils.isEmpty(message)) {
                    message = "Got an unknown error while disconnecting, see log for more details";
                    logger.log(Level.SEVERE, "Got an unknown error while disconnecting", e);
                }
                GUIHelpers.displayErrorDialog(message);
            }
        }
    }
}
