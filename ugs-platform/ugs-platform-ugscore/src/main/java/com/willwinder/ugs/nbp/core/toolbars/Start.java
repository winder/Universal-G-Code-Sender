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
package com.willwinder.ugs.nbp.core.toolbars;

import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import static javax.swing.Action.SMALL_ICON;
import javax.swing.Icon;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;

@ActionID(
        category = "Edit",
        id = "com.willwinder.ugs.nbp.connectiontoolbar.Start"
)
@ActionRegistration(
        displayName = "#CTL_Start",
        lazy = false
)
@ActionReference(path = "Toolbars/StartPauseStop", position = 10)
@Messages("CTL_Start=Start")
public final class Start extends AbstractAction implements UGSEventListener {
    BackendAPI backend;

    public Start() {
        this (Utilities.actionsGlobalContext());
    }
    
    public Start(Lookup lookup) {
        Icon icon = ImageUtilities.image2Icon(
                ImageUtilities.loadImage("resources/play.png"));
        putValue(SMALL_ICON, icon);
 
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this);

        setEnabled(isEnabled());
    }
    
    @Override
    public void UGSEvent(UGSEvent cse) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                setEnabled(isEnabled());
            }
        });

    }
    
    @Override
    public boolean isEnabled() {
        return backend.canSend() || backend.isPaused(); 
    }
   
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (backend.isPaused()) {
                backend.pauseResume();
            } else {
                backend.send();
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
