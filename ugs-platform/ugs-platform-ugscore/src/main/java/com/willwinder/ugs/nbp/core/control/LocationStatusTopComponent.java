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

import com.willwinder.ugs.nbp.lib.helper.LocalizableTopComponent;
import com.willwinder.ugs.nbp.lib.services.ActionRegistrationService;
import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.machinestatus.MachineStatusPanel;
import java.awt.BorderLayout;
import java.io.IOException;
import javax.swing.Action;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Top component which displays something.
 */
@TopComponent.Description(
        preferredID = "StatusTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "bottom_left", openAtStartup = true)
@ActionID(category = LocationStatusTopComponent.category, id = LocationStatusTopComponent.actionId)
@ActionReference(path = LocationStatusTopComponent.windowPath)
@TopComponent.OpenActionRegistration(
        displayName = "<Not localized:LocationStatusTopComponent>",
        preferredID = "StatusTopComponent"
)
public final class LocationStatusTopComponent extends LocalizableTopComponent {
    public final static String windowPath = "Menu/Window";
    public final static String actionId = "com.willwinder.ugs.nbp.control.StatusTopComponent";
    public final static String category = "Window";

    public LocationStatusTopComponent() {
        super(
                Localization.getString("platform.window.dro"),
                Localization.getString("platform.window.dro.tooltip"),
                category,
                actionId,
                null,
                null
                );
        
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
}