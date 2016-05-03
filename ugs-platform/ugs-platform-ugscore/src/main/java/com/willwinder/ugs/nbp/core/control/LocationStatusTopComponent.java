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

import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.machinestatus.MachineStatusPanel;
import java.awt.BorderLayout;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//com.willwinder.ugs.nbp.control//Status//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "StatusTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "bottom_left", openAtStartup = true)
@ActionID(category = "Window", id = "com.willwinder.ugs.nbp.control.StatusTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_StatusAction",
        preferredID = "StatusTopComponent"
)
@Messages({
    "CTL_StatusAction=Controller State (DRO)",
    "CTL_StatusTopComponent=Controller State (DRO)",
    "HINT_StatusTopComponent=Current controller state."
})
public final class LocationStatusTopComponent extends TopComponent {
    public LocationStatusTopComponent() {
        setName(Bundle.CTL_StatusTopComponent());
        setToolTipText(Bundle.HINT_StatusTopComponent());
        
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        setLayout(new BorderLayout());
        add(new MachineStatusPanel(backend), BorderLayout.CENTER);
    }

    @Override
    public void componentOpened() {
        // TODO add custom code on component opening
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    public LocationStatusTopComponent(Lookup lookup) {
        super(lookup);
    }
}