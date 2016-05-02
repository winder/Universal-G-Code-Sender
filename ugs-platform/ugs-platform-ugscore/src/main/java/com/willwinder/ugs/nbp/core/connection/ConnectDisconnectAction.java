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
package com.willwinder.ugs.nbp.core.connection;

import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.utils.Settings;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.AbstractAction;
import static javax.swing.Action.SMALL_ICON;
import javax.swing.Icon;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.Utilities;

@ActionID(
        category = "Edit",
        id = "com.willwinder.ugs.nbp.connectiontoolbar.ConnectDisconnect"
)
@ActionRegistration(
        displayName = "#CTL_Connect",
        lazy = false
)
@ActionReference(path = "Toolbars/Connection", position = 1)
@NbBundle.Messages("CTL_Connect=Connect")
public final class ConnectDisconnectAction extends AbstractAction implements UGSEventListener {
    private static final Logger logger = Logger.getLogger(ConnectDisconnectAction.class.getName());

    BackendAPI backend;
    final private String CONNECT_ICON_PATH = "resources/disconnect.gif";
    final private String DISCONNECT_ICON_PATH = "resources/connect.png";

    public ConnectDisconnectAction() {
        this (Utilities.actionsGlobalContext());
    }
    
    public ConnectDisconnectAction(Lookup lookup) {
        Icon icon = ImageUtilities.image2Icon(ImageUtilities.loadImage(CONNECT_ICON_PATH));
        super.putValue(SMALL_ICON, icon);
 
        super.setEnabled(true);
        
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this);
    }
    
    /**
     * Update icon when the connection state changes.
     */
    @Override
    public void UGSEvent(UGSEvent cse) {
        if (cse.isStateChangeEvent()) {
            Icon icon = null;
            switch (cse.getControlState()) {
                case COMM_IDLE:
                    icon = ImageUtilities.image2Icon(ImageUtilities.loadImage(DISCONNECT_ICON_PATH));
                    break;
                case COMM_DISCONNECTED:
                    icon = ImageUtilities.image2Icon(ImageUtilities.loadImage(CONNECT_ICON_PATH));
                    break;
            }
            
            if (icon != null) {
                putValue(SMALL_ICON, icon);
            }
        }
    }
   
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            connect();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void connect() {
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        Settings settings = CentralLookup.getDefault().lookup(Settings.class);

        logger.log(Level.INFO, "openclose button, connection open: {0}", backend.isConnected());
        if( !backend.isConnected() ) {
            
            final Preferences pref = NbPreferences.forModule(ConnectionProperty.class);
            
            String firmware = pref.get("firmware", "GRBL");
            String port = pref.get("address", "");
            int baudRate = Integer.parseInt(pref.get("baud", "115200"));
            
            try {
                backend.applySettings(settings);
                backend.connect(firmware, port, baudRate);
                settings.setFirmwareVersion(firmware);
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

}
