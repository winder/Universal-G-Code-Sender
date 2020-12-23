/*
    Copyright 2020 Will Winder

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
package com.willwinder.ugs.nbp.toolbox;

import com.willwinder.ugs.nbp.lib.services.ActionRegistrationService;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.ugs.nbp.toolbox.settings.ISettingsListener;
import com.willwinder.ugs.nbp.toolbox.settings.Settings;
import com.willwinder.universalgcodesender.uielements.panels.ButtonGridPanel;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

import javax.swing.*;
import java.awt.*;

import static javax.swing.BorderFactory.createEmptyBorder;

/**
 * A component that displays a toolbox menu
 *
 * @author Joacim breiler
 */
@TopComponent.Description(
        preferredID = "ugs-platform-plugin-toolboxTopComponent")
@TopComponent.Registration(
        mode = "bottom_left",
        openAtStartup = true,
        position = 1000)
@ActionReference(path = ToolboxTopComponent.WINOW_PATH)
@ActionID(
        category = ToolboxTopComponent.CATEGORY,
        id = ToolboxTopComponent.ACTION_ID)
@TopComponent.OpenActionRegistration(
        displayName = "Toolbox",
        preferredID = "ugs-platform-plugin-toolboxTopComponent")
public final class ToolboxTopComponent extends TopComponent implements ISettingsListener {

    public static final String WINOW_PATH = LocalizingService.MENU_WINDOW_PLUGIN;
    public static final String CATEGORY = LocalizingService.CATEGORY_WINDOW;
    public static final String ACTION_ID = "com.willwinder.ugs.nbp.toolbox.ToolboxTopComponent";
    private final ActionRegistrationService actionRegistrationService;
    private final ButtonGridPanel buttonGridPanel;

    public ToolboxTopComponent() {
        setLayout(new BorderLayout());
        setName(LocalizingService.ToolboxTitle);
        setToolTipText(LocalizingService.ToolboxTooltip);

        actionRegistrationService = Lookup.getDefault().lookup(ActionRegistrationService.class);
        buttonGridPanel = new ButtonGridPanel();

        add(buttonGridPanel, BorderLayout.CENTER);
        settingsChanged();
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        revalidate();
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        Settings.addSettingsListener(this);
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        Settings.removeSettingsListener(this);
    }

    @Override
    public void settingsChanged() {
        buttonGridPanel.removeAll();

        Settings.getActions().forEach(actionId ->
                actionRegistrationService.getActionById(actionId)
                        .ifPresent(actionReference -> {
                            JButton button = new JButton(actionReference.getAction());
                            button.setMinimumSize(new Dimension(100, 16));
                            buttonGridPanel.add(button);
                        }));

        revalidate();
    }
}
