/*
    Copyright 2015-2016 Will Winder

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
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.panels.ActionButtonPanel;
import java.awt.BorderLayout;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 *
 * @deprecated This actions component is deprecated and should be replaced by the toolbox.
 */
@TopComponent.Description(
        preferredID = "ActionsTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "top_left", openAtStartup = false)
@TopComponent.OpenActionRegistration(
        displayName = "<Not localized:ActionsTopComponent>",
        preferredID = "ActionsTopComponent"
)
public final class ActionsTopComponent extends TopComponent {

    public ActionsTopComponent() {
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        this.setLayout(new BorderLayout());
        this.add(new ActionButtonPanel(backend), BorderLayout.CENTER);
    }

    @Override
    public void componentOpened() {
        setName(LocalizingService.ActionsTitle);
        setToolTipText(LocalizingService.ActionsTooltip);
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    public void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    public void readProperties(java.util.Properties p) {
        //String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
