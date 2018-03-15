/*
    Copyright 2018 Will Winder

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
package com.willwinder.ugs.nbp.jog;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.services.JogService;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

import java.awt.*;

/**
 * The jog control panel in NetBeans
 *
 * @author Joacim Breiler
 */
@TopComponent.Description(
        preferredID = "JogTopComponent",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED
)
@TopComponent.Registration(
        mode = "top_left",
        openAtStartup = false)
@ActionID(
        category = JogTopComponent.CATEGORY,
        id = JogTopComponent.ACTION_ID)
@ActionReference(
        path = JogTopComponent.WINOW_PATH)
@TopComponent.OpenActionRegistration(
        displayName = "Jog Controller",
        preferredID = "JogTopComponent"
)
public final class JogTopComponent extends TopComponent implements UGSEventListener {

    public static final String WINOW_PATH = LocalizingService.MENU_WINDOW_PLUGIN;
    public static final String CATEGORY = LocalizingService.CATEGORY_WINDOW;
    public static final String ACTION_ID = "com.willwinder.ugs.nbp.jog.JogTopComponent";

    private final BackendAPI backend;
    private final JogPanel jogPanel;
    private final JogService jogService;

    public JogTopComponent() {
        jogService = CentralLookup.getDefault().lookup(JogService.class);
        jogPanel = new JogPanel(jogService);
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this);

        setLayout(new BorderLayout());
        setName(LocalizingService.JogControlTitle);
        setToolTipText(LocalizingService.JogControlTooltip);

        setPreferredSize(new Dimension(250, 270));

        add(jogPanel, BorderLayout.CENTER);
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        backend.removeUGSEventListener(this);
    }

    @Override
    public void UGSEvent(UGSEvent event) {
        boolean canJog = jogService.canJog();
        if (canJog != jogPanel.isEnabled()) {
            jogPanel.setEnabled(canJog);
        }

        if (event.isSettingChangeEvent()) {
            jogPanel.setFeedRate(Double.valueOf(backend.getSettings().getJogFeedRate()).intValue());
            jogPanel.setStepSizeXY(backend.getSettings().getManualModeStepSize());
            jogPanel.setStepSizeZ(backend.getSettings().getzJogStepSize());
            jogPanel.setUnit(backend.getSettings().getPreferredUnits());
            jogPanel.setUseStepSizeZ(backend.getSettings().useZStepSize());
        }
    }
}
