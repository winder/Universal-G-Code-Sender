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
package com.willwinder.universalgcodesender.uielements.actions;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.Settings;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;

/**
 *
 * @author wwinder
 */
public class ConnectDisconnectAction extends AbstractAction {
    private static final Logger logger = Logger.getLogger(ConnectDisconnectAction.class.getName());
    private final BackendAPI backend;

    public ConnectDisconnectAction(BackendAPI backend) {
        this.backend = backend;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            connect();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private void connect() {
        logger.log(Level.INFO, "openclose button, connection open: {0}", backend.isConnected());
        if( !backend.isConnected() ) {
            Settings s = backend.getSettings();
            
            String firmware = s.getFirmwareVersion();
            String port = s.getPort();
            int baudRate = Integer.parseInt(s.getPortRate());
            
            try {
                backend.connect(firmware, port, baudRate);
            } catch (Exception e) {
                GUIHelpers.displayErrorDialog(e.getMessage());
            }
        } else {
            try {
                backend.disconnect();
            } catch (Exception e) {
                GUIHelpers.displayErrorDialog(e.getMessage());
            }
        }
    }
}
