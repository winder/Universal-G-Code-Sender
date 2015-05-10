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
        id = "com.willwinder.ugs.nbp.toolbar.Stop"
)
@ActionRegistration(
        iconBase = "resources/16x16-stop.png",
        displayName = "#CTL_Stop"
)
@ActionReference(path = "Toolbars/StartPauseStop", position = 2)
@Messages("CTL_Stop=Stop")
public final class Stop implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            CentralLookup.getDefault().lookup(BackendAPI.class).cancel();
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
