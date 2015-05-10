/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.ugs.nbp.toolbar;

import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Edit",
        id = "com.willwinder.ugs.nbp.toolbar.Start"
)
@ActionRegistration(
        iconBase = "resources/play.png",
        displayName = "#CTL_Start"
)
@ActionReference(path = "Toolbars/StartPauseStop", position = 0)
@Messages("CTL_Start=Start")
public final class Start implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
            if (backend.isSending()) {
                backend.pauseResume();
            } else {
                backend.send();
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
