/*
    Copyright 2016-2023 Will Winder

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

import com.willwinder.ugs.nbp.lib.Mode;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.panels.OverridesPanel;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

import javax.swing.JScrollPane;
import java.awt.BorderLayout;

/**
 * Top component which displays something.
 */
@TopComponent.Description(
        preferredID = "OverridesTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = Mode.LEFT_TOP, openAtStartup = false)
@ActionID(category = LocalizingService.OverridesCategory, id = LocalizingService.OverridesActionId)
@ActionReference(path = LocalizingService.OverridesWindowPath)
@TopComponent.OpenActionRegistration(
        displayName = "<Not localized:OverridesTopComponent>",
        preferredID = "OverridesTopComponent"
)
public final class OverridesTopComponent extends TopComponent {

    public OverridesTopComponent() {
        this.setLayout(new BorderLayout());
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        this.add(new JScrollPane(new OverridesPanel(backend), VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
    }

    @Override
    public void componentOpened() {
        setName(LocalizingService.OverridesTitle);
        setToolTipText(LocalizingService.OverridesTooltip);
    }
}
