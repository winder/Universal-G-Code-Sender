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

import com.willwinder.ugs.nbp.core.control.Bundle;
import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.action.ActionButtonPanel;
import com.willwinder.universalgcodesender.utils.Settings;
import java.awt.BorderLayout;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//com.willwinder.universalgcodesender.nbp.control//Actions//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "ActionsTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "top_left", openAtStartup = true)
@ActionID(category = "Window", id = "com.willwinder.universalgcodesender.nbp.control.ActionsTopComponent")
@ActionReference(path = "Menu/Window/Classic" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ActionsAction",
        preferredID = "ActionsTopComponent"
)
@Messages({
    "CTL_ActionsAction=Common Actions",
    "CTL_ActionsTopComponent=Common Actions",
    "HINT_ActionsTopComponent=Common action shortcuts."
})
public final class ActionsTopComponent extends TopComponent {
    protected Settings settings = CentralLookup.getDefault().lookup(Settings.class);
    protected BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);

    public ActionsTopComponent() {
        this.setLayout(new BorderLayout());
        this.add(new ActionButtonPanel(backend), BorderLayout.CENTER);
        setName(Bundle.CTL_ActionsTopComponent());
        setToolTipText(Bundle.HINT_ActionsTopComponent());
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
