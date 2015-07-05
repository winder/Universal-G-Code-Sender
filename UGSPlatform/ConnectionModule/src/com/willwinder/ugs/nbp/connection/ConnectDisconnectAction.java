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
import com.willwinder.universalgcodesender.listeners.ControlStateListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.ControlStateEvent.event;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import static javax.swing.Action.SMALL_ICON;
import javax.swing.Icon;
import javax.swing.JComboBox;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
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

//@NbBundle.Messages("CTL_Disconnect=Disconnect")

public final class ConnectDisconnectAction extends AbstractAction implements ContextAwareAction, ControlStateListener {
    BackendAPI backend;
    private String CONNECT_ICON_PATH = "resources/disconnect.gif";
    private String DISCONNECT_ICON_PATH = "resources/connect.png";

    public ConnectDisconnectAction() {
        this (Utilities.actionsGlobalContext());
    }
    
    public ConnectDisconnectAction(Lookup lookup) {
        Icon icon = ImageUtilities.image2Icon(ImageUtilities.loadImage(CONNECT_ICON_PATH));
        putValue(SMALL_ICON, icon);
 
        this.setEnabled(true);
        
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addControlStateListener(this);
    }
    
    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new ConnectDisconnectAction(actionContext);
    }

    @Override
    public void ControlStateEvent(com.willwinder.universalgcodesender.model.ControlStateEvent cse) {
        if (cse.getEventType() == event.STATE_CHANGED) {
            Icon icon = null;
            switch (cse.getState()) {
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
            ConnectionGUITopComponent.connect();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
