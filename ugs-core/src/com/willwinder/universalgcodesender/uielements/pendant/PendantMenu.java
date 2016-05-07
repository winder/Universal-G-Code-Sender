package com.willwinder.universalgcodesender.uielements.pendant;

import com.willwinder.universalgcodesender.i18n.Localization;
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
            backend.sendMessageForConsole("Pendant URL: " + result.getUrlString());
        }
        startServer.setEnabled(false);
        stopServer.setEnabled(true);
        this.backend.addControllerListener(pendantUI);
    }

    private void stopPendantServerButtonActionPerformed() {
        this.pendantUI.stop();
        backend.sendMessageForConsole("Pendant stopped");
        this.startServer.setEnabled(true);
        this.stopServer.setEnabled(false);
    }


}
