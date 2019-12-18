/*
    Copyright 2016-2019 Will Winder

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
package com.willwinder.universalgcodesender.uielements.components;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.pendantui.PendantUI;
import com.willwinder.universalgcodesender.pendantui.PendantURLBean;

import javax.swing.*;
import java.util.Collection;

public class PendantMenu extends JMenu {

    private final JMenuItem startServer = new javax.swing.JMenuItem(Localization.getString("PendantMenu.item.StartServer"));
    private final JMenuItem stopServer = new javax.swing.JMenuItem(Localization.getString("PendantMenu.item.StopServer"));

    private final PendantUI pendantUI;
    private final BackendAPI backend;

    public PendantMenu() {
        this(null);
    }
    
    public PendantMenu(BackendAPI backend) {
        this.backend = backend;
        if (backend != null) {
            pendantUI = new PendantUI(backend);
        } else {
            pendantUI = null;
        }

        initComponents();
    }

    private void initComponents() {
        startServer.addActionListener(evt -> startPendantServerButtonActionPerformed());
        stopServer.addActionListener(evt -> stopPendantServerButtonActionPerformed());
        stopServer.setEnabled(false);

        add(startServer);
        add(stopServer);
    }

    private void startPendantServerButtonActionPerformed() {
        Collection<PendantURLBean> results = this.pendantUI.start();
        for (PendantURLBean result : results) {
            backend.dispatchMessage(MessageType.INFO, "Pendant URL: " + result.getUrlString());
        }
        startServer.setEnabled(false);
        stopServer.setEnabled(true);
    }

    private void stopPendantServerButtonActionPerformed() {
        this.pendantUI.stop();
        this.backend.dispatchMessage(MessageType.INFO, "Pendant stopped");
        this.startServer.setEnabled(true);
        this.stopServer.setEnabled(false);
    }


}
