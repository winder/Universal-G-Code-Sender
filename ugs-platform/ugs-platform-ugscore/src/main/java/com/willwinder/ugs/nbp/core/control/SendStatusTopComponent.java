/*
    Copyright 2016 Will Winder

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
package com.willwinder.ugs.nbp.core.control;

import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.uielements.panels.SendStatusPanel;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import java.awt.BorderLayout;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@TopComponent.Description(
        preferredID = "SendStatusTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "bottom_left", openAtStartup = false)
@ActionID(category = LocalizingService.SendStatusCategory, id = LocalizingService.SendStatusActionId)
@ActionReference(path = LocalizingService.SendStatusWindowPath)
@TopComponent.OpenActionRegistration(
        displayName = "<Not localized:SendStatusTopComponent>",
        preferredID = "SendStatusTopComponent"
)
public final class SendStatusTopComponent extends TopComponent {
    BackendAPI backend;

    public SendStatusTopComponent() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        super.setLayout(new BorderLayout());
        super.add(new SendStatusPanel(backend));
    }

    @Override
    public void componentOpened() {
        setName(LocalizingService.SendStatusTitle);
        setToolTipText(LocalizingService.SendStatusTooltip);
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }
}
