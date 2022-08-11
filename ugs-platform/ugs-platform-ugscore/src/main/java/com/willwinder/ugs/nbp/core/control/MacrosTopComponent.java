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

import com.willwinder.ugs.nbp.core.actions.EditMacrosAction;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;
import com.willwinder.universalgcodesender.uielements.panels.ButtonGridPanel;
import com.willwinder.universalgcodesender.utils.SwingHelpers;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

import javax.swing.JButton;
import javax.swing.JPopupMenu;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.concurrent.Future;

/**
 * Top component which displays something.
 */
@TopComponent.Description(
        preferredID = "MacrosTopComponent"
)
@TopComponent.Registration(mode = "bottom_left", openAtStartup = true)
@ActionID(category = LocalizingService.MacrosCategory, id = LocalizingService.MacrosActionId)
@ActionReference(path = LocalizingService.MacrosWindowPath)
@TopComponent.OpenActionRegistration(
        displayName = "<Not localized:MacrosTopComponent>",
        preferredID = "MacrosTopComponent"
)
public final class MacrosTopComponent extends TopComponent implements UGSEventListener {
    private final ButtonGridPanel buttonGridPanel;
    private Future<?> settingsChangedFuture;

    public MacrosTopComponent() {
        super.setLayout(new BorderLayout());
        buttonGridPanel = new ButtonGridPanel();
        add(buttonGridPanel, BorderLayout.CENTER);
        settingsChanged();
    }

    private void settingsChanged() {
        buttonGridPanel.removeAll();

        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.getSettings().getMacros().forEach(macro -> {
            JButton button = new JButton(new MacroAction(macro));
            button.setMinimumSize(new Dimension(100, 16));
            buttonGridPanel.add(button);
        });

        JPopupMenu popupMenu = new JPopupMenu();
        popupMenu.add(new EditMacrosAction());
        SwingHelpers.traverse(this, comp -> comp.setComponentPopupMenu(popupMenu));

        revalidate();
        settingsChangedFuture = null;
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        revalidate();
    }

    @Override
    protected void componentOpened() {
        setName(LocalizingService.MacrosTitle);
        setToolTipText(LocalizingService.MacrosTooltip);
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this);
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.removeUGSEventListener(this);
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof SettingChangedEvent && settingsChangedFuture == null) {
            settingsChangedFuture = ThreadHelper.invokeLater(this::settingsChanged, 2000);
        }
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
