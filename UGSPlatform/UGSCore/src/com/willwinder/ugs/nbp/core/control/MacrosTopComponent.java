/*
    Copywrite 2016 Will Winder

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
import com.willwinder.universalgcodesender.uielements.MacroActionPanel;
import com.willwinder.universalgcodesender.utils.Settings;
import java.awt.BorderLayout;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//com.willwinder.ugs.nbp.core.control//Macros//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "MacrosTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "middle_left", openAtStartup = true)
@ActionID(category = "Window", id = "com.willwinder.ugs.nbp.core.control.MacrosTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_MacrosAction",
        preferredID = "MacrosTopComponent"
)
@Messages({
    "CTL_MacrosAction=Macros",
    "CTL_MacrosTopComponent=Macros Window",
    "HINT_MacrosTopComponent=This is a Macros window"
})
public final class MacrosTopComponent extends TopComponent {
    Settings settings;
    BackendAPI backend;

    public MacrosTopComponent() {
        settings = CentralLookup.getDefault().lookup(Settings.class);
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        super.setLayout(new BorderLayout());
        super.add(new MacroActionPanel(settings, backend), BorderLayout.CENTER);

        setName(Bundle.CTL_MacrosTopComponent());
        setToolTipText(Bundle.HINT_MacrosTopComponent());
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
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
}
