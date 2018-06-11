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
package com.willwinder.ugs.nbp.core.control;

import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.panels.MachineStatusPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

import javax.swing.JScrollPane;

/**
 * Top component which displays something.
 */
@TopComponent.Description(
        preferredID = "StatusTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "bottom_left", openAtStartup = true)
@ActionID(category = LocalizingService.LocationStatusCategory, id = LocalizingService.LocationStatusActionId)
@ActionReference(path = LocalizingService.LocationStatusWindowPath)
@TopComponent.OpenActionRegistration(
        displayName = "<Not localized:LocationStatusTopComponent>",
        preferredID = "StatusTopComponent"
)
public final class LocationStatusTopComponent extends TopComponent {

    public LocationStatusTopComponent() {
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        setLayout(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(new MachineStatusPanel(backend));
        add(scrollPane, BorderLayout.CENTER);
        setMinimumSize(new Dimension(100, 100));
    }

    @Override
    public void componentOpened() {
        setName(LocalizingService.LocationStatusTitle);
        setToolTipText(LocalizingService.LocationStatusTooltip);
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    public void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
    }

    public void readProperties(java.util.Properties p) {
    }
}