/*
    Copyright 2016-2024 Will Winder

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
import com.willwinder.ugs.nbp.lib.services.TopComponentLocalizer;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.ugs.nbp.core.panels.FileBrowserPanel;
import static com.willwinder.ugs.nbp.lib.services.LocalizingService.FileBrowserPanelCategory;
import static com.willwinder.ugs.nbp.lib.services.LocalizingService.FileBrowserPanelActionId;
import static com.willwinder.ugs.nbp.lib.services.LocalizingService.FileBrowserPanelWindowPath;
import static com.willwinder.ugs.nbp.lib.services.LocalizingService.FileBrowserPanelTitle;
import static com.willwinder.ugs.nbp.lib.services.LocalizingService.FileBrowserPanelTooltip;


import java.awt.BorderLayout;

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.modules.OnStart;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@TopComponent.Description(
        preferredID = "FileBrowserTopComponent"
)
@TopComponent.Registration(mode = Mode.LEFT_TOP, openAtStartup = false, position = 2200)
@ActionID(category = FileBrowserPanelCategory, id = FileBrowserPanelActionId)
@ActionReference(path = FileBrowserPanelWindowPath)
@TopComponent.OpenActionRegistration(
        displayName = "<Not localized:FileBrowserTopComponent>",
        preferredID = "FileBrowserTopComponent"
)
public final class FileBrowserTopComponent extends TopComponent {

    public FileBrowserTopComponent() {
        this.setLayout(new BorderLayout());
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        FileBrowserPanel panel = new FileBrowserPanel(backend);
        this.add(panel, BorderLayout.CENTER);
    }

    @Override
    public void componentOpened() {
        setName(FileBrowserPanelTitle);
        setToolTipText(FileBrowserPanelTooltip);
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

    @OnStart
    public static class Localizer extends TopComponentLocalizer {
        public Localizer() {
            super(FileBrowserPanelCategory, FileBrowserPanelActionId, FileBrowserPanelTitle);
        }
    }
}
