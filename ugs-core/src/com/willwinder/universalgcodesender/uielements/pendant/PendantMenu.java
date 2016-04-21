package com.willwinder.universalgcodesender.uielements.pendant;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.pendantui.PendantUI;
import com.willwinder.universalgcodesender.pendantui.PendantURLBean;

import javax.swing.*;
import java.util.Collection;

public class PendantMenu extends JMenu {

    private final JMenuItem startServer = new javax.swing.JMenuItem(Localization.getString("PendantMenu.item.StartServer"));
    private final PendantUI pendantUI;

    public PendantMenu(BackendAPI backend) {
        if (backend != null) {
            pendantUI = new PendantUI(backend);
        } else {
            pendantUI = null;
        }

        initComponents();
    }

    private void initComponents() {
        startServer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startPendantServerButtonActionPerformed(evt);
            }
        });


        add(startServer);
    }

    private void startPendantServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startPendantServerButtonActionPerformed
//        this.pendantUI = new PendantUI(backend);
        Collection<PendantURLBean> results = this.pendantUI.start();
        for (PendantURLBean result : results) {
            this.messageForConsole(ControllerListener.MessageType.INFO, "Pendant URL: " + result.getUrlString());
        }
        this.startPendantServerButton.setEnabled(false);
        this.stopPendantServerButton.setEnabled(true);
        this.backend.addControllerListener(pendantUI);
    }//GEN-LAST:event_startPendantServerButtonActionPerformed

    private void stopPendantServerButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopPendantServerButtonActionPerformed
        this.pendantUI.stop();
        this.startPendantServerButton.setEnabled(true);
        this.stopPendantServerButton.setEnabled(false);
    }//GEN-LAST:event_stopPendantServerButtonActionPerformed


}
