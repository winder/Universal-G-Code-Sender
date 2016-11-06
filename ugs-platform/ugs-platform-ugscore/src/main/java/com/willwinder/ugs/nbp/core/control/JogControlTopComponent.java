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
import com.willwinder.universalgcodesender.services.JogService;
import com.willwinder.universalgcodesender.uielements.jog.JogPanel;
import java.awt.BorderLayout;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@TopComponent.Description(
        preferredID = "JogControlTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "middle_left", openAtStartup = true)
@ActionID(category = LocalizingService.JogControlCategory, id = LocalizingService.JogControlActionId)
@ActionReference(path = LocalizingService.JogControlWindowPath)
@TopComponent.OpenActionRegistration(
        displayName = "<Not localized:JogControlTopComponent>",
        preferredID = "JogControlTopComponent"
)
public final class JogControlTopComponent extends TopComponent {
    public JogControlTopComponent() {
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        JogService jogService = CentralLookup.getDefault().lookup(JogService.class);
        super.setLayout(new BorderLayout());
        super.add(new JogPanel(backend, jogService, false), BorderLayout.CENTER);
    }
    
    @Override
    public void componentOpened() {
        setName(LocalizingService.JogControlTitle);
        setToolTipText(LocalizingService.JogControlTooltip);
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
